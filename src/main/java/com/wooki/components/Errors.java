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

package com.wooki.components;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;

@IncludeJavaScriptLibrary(
{ "context:/static/js/jquery.notifyBar.js", "context:/static/js/notifybar.js" })
@IncludeStylesheet("context:/static/css/jquery.notifyBar.css")
public class Errors
{

    @Parameter
    private String[] messages;

    @Inject
    private RenderSupport support;

    // Allow null so we can generate a better error message if missing
    @Environmental(false)
    private ValidationTracker tracker;

    private String errorListId;

    void beginRender(MarkupWriter writer)
    {

        List<String> errors = null;

        if (tracker == null && messages != null)
        {
            errors = Arrays.asList(messages);
        }
        else
        {
            errors = tracker.getErrors();
        }

        if (errors != null && !errors.isEmpty())
        {

            errorListId = support.allocateClientId("error-list");

            writer.element("div", "style", "display:none;", "id", errorListId);

            // Only write out the <UL> if it will contain <LI> elements. An
            // empty <UL> is not
            // valid XHTML.
            writer.element("div", "class", "error-list shadowed");
            writer.element("ul", "class", "wrapper");

            for (String message : errors)
            {
                writer.element("li");
                writer.write(message);
                writer.end();
            }

            writer.end(); // ul
            writer.end(); // ul
            writer.end(); // div
        }

    }

    // Add javascript
    void afterRender()
    {
        if (errorListId != null)
        {
            support.addInit("initErrorBox", this.errorListId);
        }
    }

}
