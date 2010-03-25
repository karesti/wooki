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

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;

/**
 * Submit the form on change event
 * 
 * @author ccordenier
 */
@MixinAfter
public class SubmitFormOnChange
{

    @Inject
    private RenderSupport renderSupport;

    @Inject
    private FormSupport formSupport;

    @InjectContainer
    private ClientElement container;

    /**
     * Simply bind the input element to submit the enclosing form on change event.
     */
    @AfterRender
    public void addSubmitOnChange()
    {
        renderSupport.addInit("submitFormOnChange", container.getClientId(), formSupport
                .getClientId());
    }

}
