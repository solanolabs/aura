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

import java.util.ArrayList;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.util.type.Converter;

import aQute.bnd.annotation.component.Component;

/**
 * Used by aura.util.type.TypeUtil
 */
@SuppressWarnings("rawtypes")
@Component (provide=AuraServiceProvider.class)
public class ArrayListToBooleanArrayConverter implements Converter<ArrayList, Boolean[]> {

    @SuppressWarnings("unchecked")
    @Override
    public Boolean[] convert(ArrayList value) {
        return ((ArrayList<Boolean>) value).toArray(new Boolean[0]);
    }

    @Override
    public Class<ArrayList> getFrom() {
        return ArrayList.class;
    }

    @Override
    public Class<Boolean[]> getTo() {
        return Boolean[].class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return null;
    }

}
