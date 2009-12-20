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

package com.wooki.domain.biz;

import java.io.UnsupportedEncodingException;

public abstract class AbstractManager {
	protected static final <T> void protectionNotNull(T param) {
		if (param == null) {
			// wow , i'm sure this is totally ugly to cast a new object to the
			// type T, but is there any way to do it better?
			throw new IllegalArgumentException("Object of type: '" + ((T) new Object()).getClass().getName() + "' must not be null.");
		}
	}

	protected static final String toStringWithCharset(byte[] content, String charset) {
		try {
			return new String(content, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
