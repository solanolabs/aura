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
    assertText: function(component, id, text, index) {
        var children = component.find(id);
        var elem;
        for (var i = 0; i < children.length; i++) {
            elem = children[i].getElement();
            $A.test.assertEquals(text.charCodeAt(0), $A.test.getText(elem).charCodeAt(index), "Entity " + id + " not rendered as expected at index " + i);
        }
    },

    testHtmlEntities: {
        test: function(component){
            this.assertText(component, "gt", ">", 0);
            this.assertText(component, "lt", "<", 0);
            this.assertText(component, "amp", "&", 0);
            this.assertText(component, "apos", "'", 0);
            this.assertText(component, "quot", "\"", 0);
            this.assertText(component, "mdash", "\u2014", 0);
            this.assertText(component, "copy", "\u00A9", 0);
            this.assertText(component, "trade", "\u2122", 0);
            this.assertText(component, "reg", "\u00AE", 0);
            this.assertText(component, "laquo", "\u00AB", 0);
            this.assertText(component, "deg", "\u00B0", 0);
            this.assertText(component, "acute", "\u00B4", 0);
            this.assertText(component, "raquo", "\u00BB", 0);
            this.assertText(component, "cent", "\u00A2", 0);
            this.assertText(component, "euro", "\u20AC", 0);
            this.assertText(component, "yen", "\u00A5", 0);
            this.assertText(component, "pound", "\u00A3", 0);

            $A.test.assertEquals("in\u2002side", $A.test.getText(component.find("ensp")[0].getElement())); //text node with only whitespace get pruned
            $A.test.assertEquals("\u2002", $A.test.getText(component.find("ensp")[1].getElement()));
            $A.test.assertEquals("\u2002", $A.test.getText(component.find("ensp")[2].getElement()));
            $A.test.assertEquals("\u2002", $A.test.getText(component.find("ensp")[3].getElement()));
            $A.test.assertEquals("in\u2003side", $A.test.getText(component.find("emsp")[0].getElement())); //text node with only whitespace get pruned
            $A.test.assertEquals("\u2003", $A.test.getText(component.find("emsp")[1].getElement()));
            $A.test.assertEquals("\u2003", $A.test.getText(component.find("emsp")[2].getElement()));
            $A.test.assertEquals("\u2003", $A.test.getText(component.find("emsp")[3].getElement()));
        }
    },

    // IE outputs nbsp as 32, rather than 160
    testHtmlEntitiesInIE: {
        browsers: ["IE7", "IE8", "IE9", "IE10", "IE11"],
        test: function(component){
            var children = component.find("nbsp");
            $A.test.assertEquals(32, $A.test.getText(children[0].getElement()).charCodeAt(1), "Entity nbsp not rendered as expected at index 0");
            $A.test.assertEquals(32, $A.test.getText(children[1].getElement()).charCodeAt(1), "Entity nbsp not rendered as expected at index 1");
            $A.test.assertEquals(160, $A.test.getText(children[2].getElement()).charCodeAt(1), "Entity nbsp not rendered as expected at index 2");
            $A.test.assertEquals(32, $A.test.getText(children[3].getElement()).charCodeAt(1), "Entity nbsp not rendered as expected at index 3");
        }
    },

    testHtmlEntitiesOutsideOfIE: {
        browsers: ["-IE7","-IE8","-IE9", "-IE10", "-IE11"],
        test: function(component){
            this.assertText(component, "nbsp", "\u00A0", 1);
        }
    }
})
