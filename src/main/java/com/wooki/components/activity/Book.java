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

package com.wooki.components.activity;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;

import com.wooki.base.AbstractActivity;
import com.wooki.domain.model.activity.BookActivity;

/**
 * Display activities.
 * 
 * @author ccordenier
 */
public class Book extends AbstractActivity
{

    @Property
    @Parameter(allowNull = false, required = true)
    private BookActivity activity;

    public boolean isBookAvailable()
    {
        return activity.getBook().getDeletionDate() == null;
    }

    @SetupRender
    public boolean checkByPass()
    {
        return !this.activity.isResourceUnavailable();
    }

}
