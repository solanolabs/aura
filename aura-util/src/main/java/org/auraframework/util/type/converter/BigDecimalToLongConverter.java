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

import java.math.BigDecimal;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.util.type.Converter;

import aQute.bnd.annotation.component.Component;

/**
 * Used by aura.util.type.TypeUtil
 * 
 * 
 * @since 0.0.142
 */
@Component (provide=AuraServiceProvider.class)
public class BigDecimalToLongConverter implements Converter<BigDecimal, Long> {

    @Override
    public Long convert(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.longValue();
    }

    @Override
    public Class<BigDecimal> getFrom() {
        return BigDecimal.class;
    }

    @Override
    public Class<Long> getTo() {
        return Long.class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return null;
    }

}
