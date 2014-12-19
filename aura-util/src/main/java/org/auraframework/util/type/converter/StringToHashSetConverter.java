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
package org.auraframework.util.type.converter;

import java.util.HashSet;
import java.util.List;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.util.AuraTextUtil;
import org.auraframework.util.type.Converter;

import com.google.common.collect.Sets;

import aQute.bnd.annotation.component.Component;

@SuppressWarnings("rawtypes")
@Component (provide=AuraServiceProvider.class)
public class StringToHashSetConverter implements Converter<String, HashSet> {

    @Override
    public HashSet<String> convert(String value) {
        List<String> splitList = AuraTextUtil.splitSimple(",", value);
        return Sets.newHashSet(splitList);
    }

    @Override
    public Class<String> getFrom() {
        return String.class;
    }

    @Override
    public Class<HashSet> getTo() {
        return HashSet.class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return null;
    }

}
