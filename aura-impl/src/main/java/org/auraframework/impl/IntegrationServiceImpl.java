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
package org.auraframework.impl;

import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.impl.integration.IntegrationImpl;
import org.auraframework.integration.Integration;
import org.auraframework.integration.IntegrationServiceObserver;
import org.auraframework.service.IntegrationService;
import org.auraframework.system.AuraContext.Mode;
import org.auraframework.throwable.quickfix.QuickFixException;

import aQute.bnd.annotation.component.Component;

@Component (provide=AuraServiceProvider.class)
public class IntegrationServiceImpl implements IntegrationService {
 
    @Override
    public Integration createIntegration(String contextPath, Mode mode, boolean initializeAura, String userAgent, 
            String application, IntegrationServiceObserver observer) throws QuickFixException {
        return new IntegrationImpl(contextPath, mode, initializeAura, userAgent, application, observer);
    }

    private static final long serialVersionUID = -2650728458106333787L;
}
