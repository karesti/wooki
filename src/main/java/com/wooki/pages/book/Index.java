//
// Copyright 2009 Robin Komiwes, Bruno Verachten, Christophe Cordenier
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.wooki.pages.book;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.wooki.base.BookBase;
import com.wooki.domain.biz.BookManager;
import com.wooki.domain.biz.ChapterManager;
import com.wooki.domain.model.Chapter;
import com.wooki.domain.model.Publication;
import com.wooki.domain.model.User;
import com.wooki.links.Link;
import com.wooki.links.PageLink;
import com.wooki.links.impl.EditLink;
import com.wooki.links.impl.ExportLink;
import com.wooki.links.impl.NavLink;
import com.wooki.links.impl.ViewLink;
import com.wooki.pages.chapter.Edit;
import com.wooki.services.BookStreamResponse;
import com.wooki.services.HttpError;
import com.wooki.services.activity.ActivitySourceType;
import com.wooki.services.export.ExportService;
import com.wooki.services.feeds.FeedSource;
import com.wooki.services.security.WookiSecurityContext;

/**
 * This page displays a book with its table of contents.
 */
@Import(library =
{ "context:static/js/jquery-ui-1.7.3.custom.min.js" })
public class Index extends BookBase
{

    @Inject
    private Messages messages;

    @Inject
    private BookManager bookManager;

    @Inject
    private ChapterManager chapterManager;

    @Inject
    private WookiSecurityContext securityCtx;

    @Inject
    private ExportService exportService;

    @Inject
    private FeedSource feedSource;

    @Inject
    private JavaScriptSupport jsSupport;

    @Inject
    private ComponentResources resources;

    @InjectPage
    private Edit editChapter;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private boolean printError;

    @Property
    private Long bookAbstractId;

    @Property
    private String bookAbstractTitle;

    @Property
    private User currentUser;

    @Property
    private int loopIdx;

    @Property
    private List<User> authors;

    @Property
    private List<Chapter> chaptersInfo;

    private Chapter currentChapter;

    /**
     * Will be set if the author tries to add a new chapter
     */
    @Property
    private String chapterName;

    @Property
    private List<Link> publicLinks;

    @Property
    private List<Link> adminLinks;

    @Property
    private PageLink right;

    private Long firstChapterId;

    private String firstChapterTitle;

    private boolean showWorkingCopyLink;

    private boolean bookAuthor;

    /**
     * Setup all the data to display in the book index page.
     * 
     * @param bookId
     * @throws IOException
     */
    @OnEvent(value = EventConstants.ACTIVATE)
    public Object setupBookIndex(Long bookId, String revision)
    {

        this.setRevision(revision);
        this.setViewingRevision(true);

        // Only authors have access to the last revision
        if (ChapterManager.LAST.equalsIgnoreCase(revision)
                && !(this.securityCtx.isLoggedIn() && this.securityCtx.canWrite(this.getBook()))) { return new HttpError(
                403, "Access denied"); }

        return true;
    }

    /**
     * Prepare book display.
     */
    @SetupRender
    public void setupBookDisplay()
    {

        this.authors = this.getBook().getAuthors();
        this.bookAuthor = this.securityCtx.canWrite(this.getBook());

        // List chapter infos
        List<Chapter> chapters = chapterManager.listChaptersInfo(this.getBookId());
        this.bookAbstractId = chapters.get(0).getId();
        this.bookAbstractTitle = chapters.get(0).getTitle();

        Object[] firstChapterData = chapterManager.findNext(getBookId(), this.bookAbstractId);
        if (firstChapterData != null)
        {
            this.firstChapterId = (Long) firstChapterData[0];
            this.firstChapterTitle = (String) firstChapterData[1];
        }

        if (chapters.size() > 0)
        {
            this.chaptersInfo = chapters.subList(1, chapters.size());
        }

        // Get abstract publication
        Publication abstractPublication = this.isViewingRevision() ? this.chapterManager
                .getRevision(this.bookAbstractId, this.getRevision()) : this.chapterManager
                .getLastPublishedPublication(this.bookAbstractId);
        this.setPublication(abstractPublication);

        // Setup abstract content
        this.setupContent();

    }

    @SetupRender
    public void setupMenus()
    {
        publicLinks = new ArrayList<Link>();
        adminLinks = new ArrayList<Link>();

        if (isAbstractHasWorkingCopy())
        {
            publicLinks.add(new ViewLink(this.getBook(), "book", "working-copy", true, this
                    .getBookId(), ChapterManager.LAST));
        }

        publicLinks.add(new ViewLink("chapter/issues", "all-feedback", this.getBookId(), "all"));
        publicLinks.add(new ExportLink("print-pdf", "pdf", this.getBookId()));
        publicLinks.add(new ExportLink("rss-feed", "feed", this.getBookId()));

        adminLinks.add(new EditLink(getBook(), "chapter/edit", "edit-intro", getBookId(),
                bookAbstractId));
        adminLinks.add(new EditLink(getBook(), "book/settings", "settings", getBookId()));

    }

    @SetupRender
    public void setupNav()
    {
        if ((firstChapterId != null) && (firstChapterTitle != null))
        {
            right = new NavLink("chapter/index", "nav-right", firstChapterTitle, getBookId(),
                    firstChapterId);
        }
    }

    @AfterRender
    public void setupChapterSort()
    {
        JSONObject params = new JSONObject();
        params.put("url", resources.createEventLink("reorder").toAbsoluteURI());
        jsSupport.addInitializerCall("initSortChapters", params);
        jsSupport.addInitializerCall("initAddChapterFocus", new JSONObject());
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "addChapterForm")
    public Object addNewChapter()
    {
        Chapter chapter = bookManager.addChapter(this.getBook(), chapterName);
        editChapter.setBookId(this.getBookId());
        editChapter.setChapterId(chapter.getId());
        return editChapter;
    }

    /**
     * Simply export to PDF.
     * 
     * @return
     */
    @OnEvent(value = "pdf")
    public Object exportPdf()
    {
        try
        {
            InputStream bookStream = this.exportService.exportPdf(this.getBookId());
            return new BookStreamResponse(this.getBook().getSlugTitle(), bookStream);
        }
        catch (Exception ex)
        {
            this.printError = true;
            ex.printStackTrace();
            return this;
        }
    }

    @OnEvent(value = "reorder")
    public void reorder(@RequestParameter(value = "chapterId") Long chapterId,
            @RequestParameter(value = "newPos") int newPos)
    {
        // add +1 because introduction is not sortable
        bookManager.updateChapterIndex(getBookId(), chapterId, newPos + 1);
    }

    /**
     * Create the Atom feed of the book activity
     * 
     * @throws IOException
     * @throws FeedException
     * @throws IllegalArgumentException
     */
    @OnEvent(value = "feed")
    public Feed getFeed(Long bookId) throws IOException, IllegalArgumentException, FeedException
    {
        return feedSource.produceFeed(ActivitySourceType.BOOK, bookId);
    }

    public String[] getPrintErrors()
    {
        return new String[]
        { this.messages.get("print-error") };
    }

    @OnEvent(value = EventConstants.PASSIVATE)
    public Long retrieveBookId()
    {
        return this.getBookId();
    }

    public boolean isPublished()
    {
        long chapterId = currentChapter.getId();

        Publication publication = this.chapterManager.getLastPublishedPublication(chapterId);

        return (publication != null);
    }

    public boolean isAbstractHasWorkingCopy()
    {
        return hasWorkingCopy(this.bookAbstractId);
    }

    public boolean isShowWorkingCopyLink()
    {
        long chapterId = currentChapter.getId();
        return hasWorkingCopy(chapterId);
    }

    private final boolean hasWorkingCopy(long chapterId)
    {
        Publication publication = this.chapterManager.getRevision(chapterId, ChapterManager.LAST);
        if (publication != null)
        {
            boolean workingCopy = !publication.isPublished();
            return bookAuthor && workingCopy;
        }
        return false;
    }

    /**
     * Get edit context for chapter
     * 
     * @return
     */
    public Object[] getEditCtx()
    {
        return new Object[]
        { this.getBookId(), this.bookAbstractId };
    }

    public Object[] getAbstractWorkingCopyCtx()
    {
        return new Object[]
        { this.getBookId(), ChapterManager.LAST };
    }

    public Object[] getIssuesCtx()
    {
        return new Object[]
        { this.getBookId(), "all" };
    }

    /**
     * Get id to link to chapter display
     * 
     * @return
     */
    public Object[] getChapterCtx()
    {
        return new Object[]
        { this.getBookId(), this.currentChapter.getId() };
    }

    public Object[] getChapterWorkingCopyCtx()
    {
        return new Object[]
        { this.getBookId(), this.currentChapter.getId(), ChapterManager.LAST };
    }

    public Chapter getCurrentChapter()
    {
        return currentChapter;
    }

    public void setCurrentChapter(Chapter currentChapter)
    {
        this.currentChapter = currentChapter;
    }

}
