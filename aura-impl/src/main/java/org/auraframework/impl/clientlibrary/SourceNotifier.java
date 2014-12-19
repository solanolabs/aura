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
package org.auraframework.impl.clientlibrary;

import org.auraframework.Aura;
import org.auraframework.def.DefDescriptor;
import org.auraframework.system.SourceListener;

import aQute.bnd.annotation.component.Component;



/**
 * Invalidate caches on source changes
 */
@Component (immediate=true)
class SourceNotifier implements SourceListener {
	private static SourceNotifier sourceNotifier = new SourceNotifier();

	static {
	    Aura.getDefinitionService().subscribeToChangeNotification(sourceNotifier);
	}

	@Override
    public void onSourceChanged(DefDescriptor<?> source, SourceMonitorEvent event, String filePath) {
    	
        Aura.getCachingService().getClientLibraryOutputCache().invalidateAll();
        Aura.getCachingService().getClientLibraryUrlsCache().invalidateAll();
    }
}

