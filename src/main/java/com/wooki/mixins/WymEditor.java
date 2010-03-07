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

package com.wooki.mixins;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.corelib.components.TextArea;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;

/**
 * Integrate wymeditor as a mixin to be used with textarea.
 */
@IncludeJavaScriptLibrary( { "context:/static/js/wymeditor/jquery.wymeditor.pack.js",
		"context:/static/js/wymeditor/plugins/fullscreen/jquery.wymeditor.fullscreen.js",
		"context:/static/js/wymeditor/plugins/upload-image-dialog/jquery.wymeditor.upload-image-dialog.js", "context:/static/js/ajaxupload.js" })
public class WymEditor {

	@Inject
	@Path("context:/static/js/wymeditor/")
	private Asset basePath;

	@Inject
	@Path("context:/static/js/wymeditor/jquery.wymeditor.js")
	private Asset wymPath;

	@Inject
	@Path("context:/static/js/jquery-1.3.2.min.js")
	private Asset jQueryPath;

	@Inject
	private ComponentResources resources;

	@Parameter(defaultPrefix = BindingConstants.ASSET)
	private String wymStyle;

	@Parameter(defaultPrefix = BindingConstants.LITERAL, value = "wooki")
	private String wymSkin;

	@InjectContainer
	private TextArea container;

	@Inject
	private RenderSupport renderSupport;

	@AfterRender
	public void attachWymEditor() {

		JSONObject data = new JSONObject();
		data.put("elt", container.getClientId());

		JSONObject params = new JSONObject();
		params.put("logoHtml", "");

		if (wymStyle != null) {
			params.put("stylesheet", wymStyle);
		}

		params.put("skin", wymSkin);

		// Set parameter for production mode compatibility
		params.put("basePath", basePath.toClientURL() + "/");
		params.put("wymPath", wymPath.toClientURL());
		params.put("jQueryPath", jQueryPath.toClientURL());
		params.put("classesHtml", "");

		Link uploadActionLink = resources.getPage().getComponentResources().createEventLink("uploadImage");
		params.put("uploadAction", uploadActionLink.toAbsoluteURI());

		// Add activation context
		String activationContext = uploadActionLink.getParameterValue(InternalConstants.PAGE_CONTEXT_NAME);
		if (activationContext != null) {
			JSONObject uploadDatas = new JSONObject();
			uploadDatas.put(InternalConstants.PAGE_CONTEXT_NAME, activationContext);
			params.put("uploadDatas", uploadDatas);
		}

		data.put("params", params);

		// Use wymeditor
		renderSupport.addInit("initWymEdit", data);

	}

}
