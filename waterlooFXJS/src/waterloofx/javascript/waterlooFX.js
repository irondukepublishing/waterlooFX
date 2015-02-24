/*

 Copyright (c) 2015, Malcolm Lidierth
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 and the following disclaimer in the documentation and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

/**
 * JavaScript support for using the waterlooFX charting library.
 *
 * Requirements:
 * <ul>
 * <li>JavaFX 8+ browser plugin support (presently available for IE, Firefox and Safari).</li>
 * <li>dtjava.js - </li>
 * <li>A compatible jnlp file that supports the methods used herein. waterlooFXJS.jnlp is provided as standard.</li>
 * </ul>
 */
var wfxjs;
wfxjs = function () {

    //var jnlp_content = 'dPD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPGpubHAgc3BlYz0iMS4wIiB4bWxuczpqZng9Imh0dHA6Ly9qYXZhZnguY29tIiBocmVmPSJ3YXRlcmxvb0ZYSlMuam5scCI+CiAgPGluZm9ybWF0aW9uPgogICAgPHRpdGxlPndhdGVybG9vRlhKUzwvdGl0bGU+CiAgICA8dmVuZG9yPk1MPC92ZW5kb3I+CiAgICA8ZGVzY3JpcHRpb24+bnVsbDwvZGVzY3JpcHRpb24+CiAgICA8b2ZmbGluZS1hbGxvd2VkLz4KICA8L2luZm9ybWF0aW9uPgogIDxyZXNvdXJjZXM+CiAgICA8amZ4OmphdmFmeC1ydW50aW1lIHZlcnNpb249IjguMCsiIGhyZWY9Imh0dHA6Ly9qYXZhZGwuc3VuLmNvbS93ZWJhcHBzL2Rvd25sb2FkL0dldEZpbGUvamF2YWZ4LWxhdGVzdC93aW5kb3dzLWk1ODYvamF2YWZ4Mi5qbmxwIi8+CiAgPC9yZXNvdXJjZXM+CiAgPHJlc291cmNlcz4KICAgIDxqMnNlIHZlcnNpb249IjEuNisiIGhyZWY9Imh0dHA6Ly9qYXZhLnN1bi5jb20vcHJvZHVjdHMvYXV0b2RsL2oyc2UiLz4KICAgIDxqYXIgaHJlZj0id2F0ZXJsb29GWEpTLmphciIgc2l6ZT0iNzkyNSIgZG93bmxvYWQ9ImVhZ2VyIiAvPgogICAgPGphciBocmVmPSJsaWIvd2F0ZXJsb29GWC0wLjgtU05BUFNIT1QuamFyIiBzaXplPSIyODM2MTAiIGRvd25sb2FkPSJsYXp5IiAvPgogIDwvcmVzb3VyY2VzPgo8c2VjdXJpdHk+CiAgPGFsbC1wZXJtaXNzaW9ucy8+Cjwvc2VjdXJpdHk+CiAgPGFwcGxldC1kZXNjICB3aWR0aD0iODAwIiBoZWlnaHQ9IjYwMCIgbWFpbi1jbGFzcz0iY29tLmphdmFmeC5tYWluLk5vSmF2YUZYRmFsbGJhY2siICBuYW1lPSJ3YXRlcmxvb0ZYSlMiID4KICAgIDxwYXJhbSBuYW1lPSJyZXF1aXJlZEZYVmVyc2lvbiIgdmFsdWU9IjguMCsiLz4KICA8L2FwcGxldC1kZXNjPgogIDxqZng6amF2YWZ4LWRlc2MgIHdpZHRoPSI4MDAiIGhlaWdodD0iNjAwIiBtYWluLWNsYXNzPSJ3YXRlcmxvb2Z4LldhdGVybG9vRlhKUyIgIG5hbWU9IndhdGVybG9vRlhKUyIgLz4KICA8dXBkYXRlIGNoZWNrPSJhbHdheXMiLz4KPC9qbmxwPgo='


    return {

        /**
         * Location of the Java jnlp file.
         * Set this to the appropriate location for the host web-site.
         * The value can be changed from a web-page using
         *       wfxjs.jnlp = file_specifier_string
         */
        jnlp: './waterlooFXJS/dist/waterlooFXJS.jnlp',



        findCharts: function () {
            var apps = [{app:null, object:null, fxml:''}];
            var nodes = document.getElementsByClassName("chart");
            for (var k = 0; k < nodes.length; k++) {
                var node = document.getElementById(nodes[k].id);
                var w = node.style.width;
                if (w<=0) w=400;
                var h = node.style.height;
                if (h<=0) h=300;
                var fxml = node.textContent;
                // Create an app with the same id as the HTML node
                this.embedFXML(node, '', node, w, h);
                // Retrieve the app
                var app = document.getElementById(node);
                // Parse the FXML
                var object=app.parseFXML(fxml);
                // Add the node
                //app.add(object);
                apps[k].app=app;
                apps[k].object=object;
                apps[k].fxml=fxml;
            }
            return apps;
        },


        // SUPPORT FOR FXML

        /**
         * This parses the content of an FXML file on the server specified as a URL
         * and returns the specified {@code JavaFX Node} tree.
         *
         * The returned root {@code Node} can be added to the tree of an existing app
         * created using the {@code embedFXML} method.
         *
         * @param appID
         * @param url
         * @returns {*}
         */
        parseFXMLFile: function (appID, url) {
            var app = document.getElementById(appID);
            var client = new XMLHttpRequest();
            client.open('GET', url, false);
            client.overrideMimeType("text/xml; charset=utf-8");
            client.send();
            var text = client.responseText;
            var object = app.parseFXML(text);
            if (object instanceof String) {
                // If a Java Exception has occurred, ......
                alert(object);
                return null;
            } else {
                //... otherwise return the node
                return object;
            }
        },


        /**
         * Embeds the contents of an fxml file contained in the specified jnlp file
         * in the document at the specified location and launched the app in the page.
         *
         * The app can then be located in the page with document.getElementById(appID)
         *
         * This includes in-built error handling to replace charts with an HTML text message
         * when Java/JavaFX is not supported.
         *
         * @param appID Element id to assign to the app
         * @param fxmlFile the FXML file to use from the app
         * @param placeholder id for element in the document where content should be added
         * @param w width of the content
         * @param h height of the content
         */
        embedFXML: function (appID, fxmlFile, placeholder, w, h) {
            dtjava.embed(
                {
                    id: appID,
                    url: this.jnlp,
                    placeholder: placeholder,
                    width: w,
                    height: h,
                    params: {
                        fxml: fxmlFile
                    }
                },
                {
                    javafx: '8.0+'
                },
                {
                    onDeployError: reportError
                }
            );

            function reportError(app, r) {
                //ovewrite behavior for unsupported browser
                var a = app.placeholder;
                if (a != null && r.isUnsupportedBrowser()) {
                    var p = document.createElement('div');
                    p.id = "splash";
                    p.style.width = app.width;
                    p.style.height = app.height;
                    p.style.background = "black";
                    p.style.color = "white";
                    p.appendChild(
                        document.createTextNode("Charts not available: this browser is not supported."));
                    //clear embedded application placeholder
                    while (a.hasChildNodes()) a.removeChild(a.firstChild);
                    //show custom message
                    a.appendChild(p);
                } else {
                    //use default handlers otherwise
                    var def = new dtjava.Callbacks();
                    return def.onDeployError(app, r);
                }
            }

        },


        /**
         *
         * @param appID
         * @param selector
         * @returns {*}
         */
        lookup: function (appID, selector) {
            var app = document.getElementById(appID);
            return app.lookup(selector);
        },

        /**
         *
         * @param appID
         * @param selector
         * @returns {Array}
         */
        lookupAll: function (appID, selector) {
            var app = document.getElementById(appID);
            var list = app.lookupAll(selector);
            var array = [];
            for (var k = 0; k < list.size(); k++) {
                array[k] = list.get(k);
            }
            return array;
        }
    };


}();