package com.wooki.components.menu;

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import com.wooki.links.EventLink;
import com.wooki.links.Link;
import com.wooki.links.PageLink;

public class MenuItem
{

    @Parameter(name = "class")
    @Property
    private String rowClass;

    @Parameter
    @Property
    private Link link;

    @Inject
    private Messages messages;

    @Inject
    private JavaScriptSupport jsSupport;

    @InjectComponent
    private org.apache.tapestry5.corelib.components.EventLink elink;

    @InjectComponent
    private org.apache.tapestry5.corelib.components.PageLink plink;

    @AfterRender
    void setupConfirm()
    {
        if (link.getConfirmMessageKey() != null)
        {
            JSONObject params = new JSONObject();
            if (isAction())
            {
                params.put("lnkId", elink.getClientId());
            }
            else
            {
                params.put("lnkId", plink.getClientId());
            }
            params.put("message", messages.get(link.getConfirmMessageKey()));
            jsSupport.addInitializerCall("initConfirm", params);
        }
    }

    public boolean isAction()
    {
        return link instanceof EventLink;
    }

    public EventLink getCurrentEventLink()
    {
        return EventLink.class.cast(link);
    }

    public PageLink getCurrentPageLink()
    {
        return PageLink.class.cast(link);
    }

    public String getLabel()
    {
        MessageFormatter formatter = messages.getFormatter(link.getLabelMessageKey());
        return link.format(formatter);
    }

}
