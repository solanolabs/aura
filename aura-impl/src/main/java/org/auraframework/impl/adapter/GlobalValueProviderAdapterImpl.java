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
package org.auraframework.impl.adapter;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.auraframework.adapter.GlobalValueProviderAdapter;
import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.instance.GlobalValueProvider;
import org.auraframework.instance.ValueProviderType;

import aQute.bnd.annotation.component.Component;

/**
 */
@Component (provide=AuraServiceProvider.class)
public class GlobalValueProviderAdapterImpl implements GlobalValueProviderAdapter {

    @Override
    public List<GlobalValueProvider> createValueProviders() {
        List<GlobalValueProvider> l = new LinkedList<GlobalValueProvider>();

        // $Label.Section.Key
        l.add(new LabelValueProvider());

        // $Locale
        l.add(new LocaleValueProvider());

        // $Browser
        l.add(new BrowserValueProvider());

        return l;
    }

    @Override
    public Set<ValueProviderType> getKeys() {
        return EnumSet.of(ValueProviderType.LABEL, ValueProviderType.LOCALE, ValueProviderType.BROWSER);
    }
}
