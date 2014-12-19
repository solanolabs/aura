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

({

	afterRender: function (cmp, helper) {
		helper.initEditor(cmp);
		helper.setContent(cmp, cmp.get("v.value"));
		this.superAfterRender();
	},

	rerender: function (cmp, helper) {
		if (cmp.getConcreteComponent()._updatingValue) {
			//don't rerender when updating value
			return;
		}
		var shouldRender = false;
		var attributes = cmp.getDef().getAttributeDefs();
		attributes.each(function (attributeDef) {
			var name = attributeDef.getDescriptor().getName();
			if (name !== "value" && cmp.isDirty("v." + name)) {
				shouldRender = true;
			} else if (name === "value" && cmp.isDirty("v.value")) {
				//Cannot listen to v.value change event to update content
				//since the parent component inputTextArea also listens v.value change event and update v.value again
				helper.setContent(cmp, cmp.get("v.value"));
			}
		});
		if (shouldRender) {
			this.superRerender();
		}
	},

	unrender: function (cmp, helper) {
		helper.unrender(cmp);
		this.superUnrender();
	}
});