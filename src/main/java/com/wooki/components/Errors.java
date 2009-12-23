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

import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.services.FormSupport;

public class Errors {

	// Allow null so we can generate a better error message if missing
	@Environmental(false)
	private ValidationTracker tracker;

	@Environmental
	private FormSupport formSupport;

	void beginRender(MarkupWriter writer) {

		List<String> errors = tracker.getErrors();

		if (!errors.isEmpty()) {

			writer.element("div", "class", "wooki-form-error radied-box");

			// Only write out the <UL> if it will contain <LI> elements. An
			// empty <UL> is not
			// valid XHTML.

			writer.element("ul");

			for (String message : errors) {
				writer.element("li");
				writer.write(message);
				writer.end();
			}

			writer.end(); // ul

			writer.end(); // div
		}

	}

}
