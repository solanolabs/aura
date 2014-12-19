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
public class ArrayListToIntegerArrayConverter implements Converter<ArrayList, Integer[]> {

    @SuppressWarnings("unchecked")
    @Override
    public Integer[] convert(ArrayList value) {

        if (value == null) {
            return new Integer[0];
        }

        ArrayList<Integer> convertedValues = new ArrayList<Integer>(value.size());

        for (String s : (ArrayList<String>) value) {
            convertedValues.add(Integer.valueOf(s));
        }
        return convertedValues.toArray(new Integer[value.size()]);
    }

    @Override
    public Class<ArrayList> getFrom() {
        return ArrayList.class;
    }

    @Override
    public Class<Integer[]> getTo() {
        return Integer[].class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return null;
    }
}
