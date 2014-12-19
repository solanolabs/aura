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
package org.auraframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auraframework.adapter.BeanAdapter;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.adapter.ExceptionAdapter;
import org.auraframework.adapter.LocalizationAdapter;
import org.auraframework.adapter.StyleAdapter;
import org.auraframework.clientlibrary.ClientLibraryService;
import org.auraframework.def.ApplicationDef;
import org.auraframework.def.Definition;
import org.auraframework.ds.serviceloader.AuraServiceProvider;
import org.auraframework.instance.Application;
import org.auraframework.instance.Component;
import org.auraframework.instance.Instance;
import org.auraframework.service.BuilderService;
import org.auraframework.service.CachingService;
import org.auraframework.service.ClientService;
import org.auraframework.service.ContextService;
import org.auraframework.service.DefinitionService;
import org.auraframework.service.InstanceService;
import org.auraframework.service.IntegrationService;
import org.auraframework.service.LocalizationService;
import org.auraframework.service.LoggingService;
import org.auraframework.service.RenderingService;
import org.auraframework.service.SerializationService;
import org.auraframework.service.ServerService;
import org.auraframework.system.AuraContext;
import org.auraframework.system.AuraContext.Authentication;
import org.auraframework.system.AuraContext.Format;
import org.auraframework.system.AuraContext.Mode;
import org.auraframework.util.ServiceLocator;
import org.auraframework.util.adapter.SourceControlAdapter;

/**
 * Entry point for accessing Aura services
 */
public class Aura {
    private static final Log log = LogFactory.getLog(Aura.class);

    /**
     * Get the Builder Service: for constructing your own {@link Definition}
     */
    public static BuilderService getBuilderService() {
        return Aura.get(BuilderService.class);
    }

    /**
     * Get the Client Service: for contacting other Aura servers
     */
    public static ClientService getClientService() {
        return Aura.get(ClientService.class);
    }

    /**
     * Get the Context Service: for creating or interacting with a {@link AuraContext} A AuraContext must be started
     * before working using any other service.
     */
    public static ContextService getContextService() {
        return Aura.get(ContextService.class);
    }

    /**
     * Get the Definition Service: for loading, finding or interacting with a {@link Definition}
     */
    public static DefinitionService getDefinitionService() {
        return Aura.get(DefinitionService.class);
    }

    /**
     * Get the Logging Service: Provides Aura with a top-level Logging handler from the host environments
     */
    public static LoggingService getLoggingService() {
        return Aura.get(LoggingService.class);
    }

    /**
     * Get the Instance Service: for constructing an {@link Instance} of a {@link Definition}
     */
    public static InstanceService getInstanceService() {
        return Aura.get(InstanceService.class);
    }

    /**
     * Get the Rendering Service: for rendering a {@link Component} or {@link Application}
     */
    public static RenderingService getRenderingService() {
        return Aura.get(RenderingService.class);
    }

    /**
     * Get the Serialization Service: for serializing things into format specified in the current {@link AuraContext}
     */
    public static SerializationService getSerializationService() {
        return Aura.get(SerializationService.class);
    }

    /**
     * Get the Server Service: for responding to requests from a Aura Client
     */
    public static ServerService getServerService() {
        return Aura.get(ServerService.class);
    }

    /**
     * Get the Config Adapter: Provides Aura with configuration from the host environment
     */
    public static ConfigAdapter getConfigAdapter() {
        return Aura.get(ConfigAdapter.class);
    }

    /**
     * Get the Localization Adapter: Provides Aura with Localization configuration from the host environments
     */
    public static LocalizationAdapter getLocalizationAdapter() {
        return Aura.get(LocalizationAdapter.class);
    }

    /**
     * Gets the Localization Service: Gets the localization configuration
     */
    public static LocalizationService getLocalizationService() {
        return Aura.get(LocalizationService.class);
    }

    /**
     * Get the Exception Adapter: Provides Aura with a top-level Exception handler from the host environments
     */
    public static ExceptionAdapter getExceptionAdapter() {
        return Aura.get(ExceptionAdapter.class);
    }

    /**
     * Get the Source Control Adapter : Allows interaction with the source control system.
     */
    public static SourceControlAdapter getSourceControlAdapter() {
        return Aura.get(SourceControlAdapter.class);
    }

    /**
     * Get the Style Adapter: Used to provide CSS/Style specific functionality.
     */
    public static StyleAdapter getStyleAdapter() {
        return Aura.get(StyleAdapter.class);
    }

    /**
     * Get the Definition Parser Adapter: hooks for host environment to interact with definition parsing
     */
    public static DefinitionParserAdapter getDefinitionParserAdapter() {
        return Aura.get(DefinitionParserAdapter.class);
    }

    /**
     * Gets the Integration Service: Service that makes integrating into other containers easy.
     */
    public static IntegrationService getIntegrationService() {
        return Aura.get(IntegrationService.class);
    }

    /**
     * Gets {@link ClientLibraryService}: service for including external client libraries (CSS or JS)
     */
    public static ClientLibraryService getClientLibraryService() {
        return Aura.get(ClientLibraryService.class);
    }

    
    /**
     * Gets the caching service: a general service for setting and getting arbitrary blobs based on a key
     * Encapsulates the access to aura's known caches.
     */
    public static CachingService getCachingService() {
        return Aura.get(CachingService.class);
    }

    /**
     * Get the bean adapter for aura to create controller/model beans.
     */
    public static BeanAdapter getBeanAdapter() {
        return Aura.get(BeanAdapter.class);
    }
    
    public static <T extends AuraServiceProvider> T get(Class<T> type) {
        return ServiceLocator.get().get(type);
    }

    /**
     * Pass in the name of a Aura ApplicationDef, and it will be rendered to Standard Out
     */
    public static void main(String[] args) {
        try {
            Aura.getContextService().startContext(Mode.PROD, Format.HTML, Authentication.UNAUTHENTICATED);
            String tag = args[0];
            Application app = Aura.getInstanceService().getInstance(tag, ApplicationDef.class);
            Aura.getRenderingService().render(app, System.out);
            System.out.println();
        } catch (Exception e) {
            log.fatal(e.getClass() + ": " + e.getMessage(), e);
            System.exit(1);
        } finally {
            Aura.getContextService().endContext();
        }
    }
}
