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
package org.auraframework.util.json;

import java.io.IOException;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.util.json.Json.Serialization.ReferenceScope;
import org.auraframework.util.json.Json.Serialization.ReferenceType;

/**
 * serialize some thing
 */
public interface JsonSerializer<T> extends AuraServiceProvider {

    /**
     * serialize the thing
     */
    void serialize(Json json, T value) throws IOException;

    /**
     * Get the reference type for the value's Class. Defaults to NONE
     */
    ReferenceType getReferenceType(T value);

    /**
     * Get the reference type for the value's Class. Defaults to NONE
     */
    ReferenceScope getReferenceScope(T value);

    public static abstract class IdentitySerializer<T> implements JsonSerializer<T> {
        @Override
        public final ReferenceType getReferenceType(T value) {
            return ReferenceType.IDENTITY;
        }

        @Override
        public final ReferenceScope getReferenceScope(T value) {
            return ReferenceScope.ACTION;
        }
    }

    public static abstract class NoneSerializer<T> implements JsonSerializer<T> {
        @Override
        public final ReferenceType getReferenceType(T value) {
            return ReferenceType.NONE;
        }

        @Override
        public final ReferenceScope getReferenceScope(T value) {
            return ReferenceScope.ACTION;
        }
    }

}
