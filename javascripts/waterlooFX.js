/**
 * Created by ML on 14/02/2015.
 */

var wfxjs;
wfxjs = function () {


    return {


        /**
         *
         * @param appID
         * @constructor
         */
        VisualModel: function (appID) {
            // Get a VisualModel with default settings from the app.
            var app = document.getElementById(appID);
            if (app == undefined || app == null) {
                window.alert('wfxjs.VisualModel: App is not installed or nor yet ready (id = ' + appID + ')');
                return;
            }
            var model = app.newVisualModel();
            /**
             * @property lineColor
             * @type string
             */
            this.lineColor = model.getLineColor();
            /**
             * @property lineWidth
             * @type double
             */
            this.lineWidth = model.getLineWidth();
            this.fill = model.getFill();
            this.markerType = model.getMarkerType();

        },

        findCharts: function(jnlp){
            var nodes = document.getElementsByClassName("chart");
            for (var k=0; k<nodes.length; k++){
                var node = document.getElementById(nodes[k].id);
                var fxml = node.innerHTML;
                alert(fxml);
                var appID="app".concat(k);
                this.embedFXML(appID, '', jnlp, node, 200, 200);
                if (fxml!=null && fxml!=undefined){
                    var object = this.parseFXML(appID, fxml);
                    print(object);
                    document.getElementById(appID).add(object);
                }
            }
        },


        // SUPPORT FOR FXML

        parseFXML: function (appID, url) {
            var app = document.getElementById(appID);
            var client = new XMLHttpRequest();
            client.open('GET', url, false);
            client.overrideMimeType("application/xml; charset=utf-8");
            client.send();
            var text = client.responseText;
            var object = app.parseFXML(text);
            try {
                // Will work if a Java Exception object has been returned...
                alert(object.getMessage());
                return null;
            } catch (ex) {
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
         * @param url for the jnlp file
         * @param placeholder id for element in the document where content should be added
         * @param w width of the content
         * @param h height of the content
         */
        embedFXML: function (appID, fxmlFile, url, placeholder, w, h) {
            dtjava.embed(
                {
                    id: appID,
                    url: url,
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

        }
    }


}();