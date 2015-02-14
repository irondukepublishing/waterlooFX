/**
 * Created by ML on 14/02/2015.
 */

var wplot= function () {

    return {
        /**
         *
         * @param appID
         * @param fxmlFile
         * @param url
         * @param placeholder
         * @param w
         * @param h
         */
        javafxEmbed: function (appID, fxmlFile, url, placeholder, w, h) {
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
}();
