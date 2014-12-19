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
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.auraframework.Aura;
import org.auraframework.def.ActionDef;
import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.instance.Action;
import org.auraframework.system.AuraContext;
import org.auraframework.system.Message;
import org.auraframework.throwable.quickfix.QuickFixException;
import org.auraframework.util.json.Json;
import org.auraframework.util.json.JsonReader;

import com.google.common.collect.Lists;

import aQute.bnd.annotation.component.Component;

/**
 */
@ThreadSafe
@Component (provide=AuraServiceProvider.class)
public class MessageJSONFormatAdapter extends JSONFormatAdapter<Message> {

    @Override
    public Class<Message> getType() {
        return Message.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Message read(Reader in) throws IOException, QuickFixException {
        Map<?, ?> message = (Map<?, ?>) new JsonReader().read(in);

        List<?> actions = (List<?>) message.get("actions");
        List<Action> actionList = Lists.newArrayList();
        if (actions != null) {
            for (Object action : actions) {
                Map<?, ?> map = (Map<?, ?>) action;

                // FIXME: ints are getting translated into BigDecimals here.
                Map<String, Object> params = (Map<String, Object>) map.get("params");

                Action instance = (Action) Aura.getInstanceService().getInstance((String) map.get("descriptor"),
                        ActionDef.class, params);
                instance.setId((String) map.get("id"));

                actionList.add(instance);
            }
        }

        return new Message(actionList);
    }

    @Override
    public void write(Object value, Map<String, Object> attributes, Appendable out) throws IOException {
        Message message = (Message) value;
        AuraContext c = Aura.getContextService().getCurrentContext();
        Map<String, Object> m = new HashMap<String, Object>();
        if (attributes != null) {
            m.putAll(attributes);
        }

        m.put("actions", message.getActions());
        m.put("context", c);
        Json.serialize(m, out, c.getJsonSerializationContext());
    }
}
