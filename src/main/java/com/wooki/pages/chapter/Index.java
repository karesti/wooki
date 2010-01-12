//
// Copyright 2009 Robin Komiwes, Bruno Verachten, Christophe Cordenier
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// 	http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.wooki.pages.chapter;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.wooki.base.BookBase;
import com.wooki.domain.biz.ChapterManager;
import com.wooki.domain.model.Chapter;
import com.wooki.services.security.WookiSecurityContext;

/**
 * Display a chapter and provide link to version/revision if needed.
 * 
 * @author ccordenier
 * 
 */
public class Index extends BookBase {

	@Inject
	private ChapterManager chapterManager;

	@Inject
	private WookiSecurityContext securityCtx;

	private Long chapterId;

	@Property
	private Chapter chapter;

	@Property
	private Long previous;

	@Property
	private String previousTitle;

	@Property
	private Long next;

	@Property
	private String nextTitle;

	@OnEvent(value = EventConstants.ACTIVATE)
	public Object setupChapter(Long bookId, Long chapterId) {

		// Get book related information
		this.chapterId = chapterId;
		this.chapter = this.chapterManager.findById(chapterId);
		if (this.chapter == null) {
			return redirectToBookIndex();
		}

		return null;
	}

	@OnEvent(value = EventConstants.ACTIVATE)
	public void setupChapter(Long bookId, Long chapterId, String revision) {
		this.chapterId = chapterId;

		if (this.securityCtx.isLoggedIn() && this.securityCtx.isAuthorOfBook(this.getBookId()) && LAST.equals(revision)) {
			this.setViewingRevision(true);
			this.setRevision(revision);
		}
	}

	@SetupRender
	public void setupDisplay() {

		// Prepare previous and next links
		Object[] data = this.chapterManager.findPrevious(this.getBookId(), this.chapterId);
		if (data != null && data.length == 2) {
			this.previous = (Long) data[0];
			this.previousTitle = (String) data[1];
		}

		data = this.chapterManager.findNext(this.getBookId(), this.chapterId);
		if (data != null && data.length == 2) {
			this.next = (Long) data[0];
			this.nextTitle = (String) data[1];
		}

		this.setupContent(this.chapterId, this.isViewingRevision());

	}

	@OnEvent(value = "delete")
	public Object deleteChapter(Long boodId, Long chapterId) {
		this.chapterManager.remove(chapterId);
		return this.redirectToBookIndex();
	}

	public Object[] getEditCtx() {
		return new Object[] { this.getBookId(), this.chapterId };
	}

	/**
	 * Get context for previous link.
	 * 
	 * @return Book id, previous chapter id and revision
	 */
	public Object[] getPreviousCtx() {
		if (this.isViewingRevision()) {
			return new Object[] { this.getBookId(), this.previous, LAST };
		}
		return new Object[] { this.getBookId(), this.previous };
	}

	/**
	 * Get context for next link.
	 * 
	 * @return Book id, previous chapter id and revision
	 */
	public Object[] getNextCtx() {
		if (this.isViewingRevision()) {
			return new Object[] { this.getBookId(), this.next, LAST };
		}
		return new Object[] { this.getBookId(), this.next };
	}

	@OnEvent(value = EventConstants.PASSIVATE)
	public Object[] retrieveBookId() {
		return new Object[] { this.getBookId(), this.chapterId };
	}

}
