/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.adapter.format.svg;

import java.io.IOException;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.auraframework.def.SVGDef;
import org.auraframework.impl.adapter.format.BaseFormatAdapter;
import org.auraframework.throwable.quickfix.QuickFixException;

@ThreadSafe
public class SVGFormatAdapter extends BaseFormatAdapter<SVGDef> {
    protected static final String name = "SVG";

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return SVGDef.class;
    }

    @Override
    public void write(Object value, Map<String, Object> attributes, Appendable out) throws IOException,
            QuickFixException {
        SVGDef def = (SVGDef) value;
        if (def != null) {
            out.append(def.getContents());
        }
    }
}
