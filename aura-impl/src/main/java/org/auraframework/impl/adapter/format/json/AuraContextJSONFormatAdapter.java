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
package org.auraframework.impl.adapter.format.json;

import java.io.IOException;
import java.util.Map;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.system.AuraContext;
import org.auraframework.util.json.Json;
import org.auraframework.util.json.JsonSerializationContext;

import aQute.bnd.annotation.component.Component;

/**
 */
@Component (provide=AuraServiceProvider.class)
public class AuraContextJSONFormatAdapter extends JSONFormatAdapter<AuraContext> {

    @Override
    public Class<AuraContext> getType() {
        return AuraContext.class;
    }

    @Override
    public void write(Object value, Map<String, Object> attributes, Appendable out) throws IOException {
        AuraContext ctx = (AuraContext) value;
        JsonSerializationContext s = ctx.getJsonSerializationContext();
        Json.serialize(ctx, out, s);
    }

}
