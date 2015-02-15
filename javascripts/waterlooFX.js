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
            var app=document.getElementById(appID);
            if (app == undefined || app == null){
                window.alert('wfxjs.VisualModel: App is not installed or nor yet ready (id = ' + appID +')');
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

        /**
         * Embeds the contents of an fxml file in the page at the specified location.
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
                {}
            );
        }
    }
}
();
