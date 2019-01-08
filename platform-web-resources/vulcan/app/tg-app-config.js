import moment from '/resources/moment/src/moment.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

(function () {

    moment.locale('custom-locale', {
        longDateFormat: {
            LTS: "HH:mm:ss.SSS",
            LT: "HH:mm",
            L: "DD/MM/YYYY"
        }
    });

    Polymer({

        is: "tg-app-config",

        properties: {
            minDesktopWidth: {
                type: Number,
                readOnly: true,
                value: 980
            },
            minTabletWidth: {
                type: Number,
                readOnly: true,
                value: 768
            },
            locale: {
                type: String,
                readOnly: true,
                value: "en-AU"
            },
            dateFormat: {
                type: String,
                readOnly: true,
                value: "DD/MM/YYYY"
            },
            timeFormat: {
                type: String,
                readOnly: true,
                value: "HH:mm"
            },

            /**
             * Returns 'true' if this tg-app-config instance is loaded during mobile application loading (vulcanised or non-vulcanised loading), 'false' otherwise.
             * See AbstactWebResource for more details.
             * 
             * It is very important not to confuse this parameter with MOBILE / TABLET / DESKTOP layouts (tg-tile-layout, tg-flex-layout).
             * These three layout modes can be used in 'desktop' application when resizing application window.
             * Two of these modes can be used for 'mobile' application: MOBILE / TABLET.
             * TABLET is activated commonly when landscape orientation is used for mobile device.
             */
            mobile: {
                type: Boolean,
                readOnly: true,
                value: false,
                notify: true
            }
        },

        attached: function() {
            this.style.display = "none";
        },

        /**
         * Determines whether iPhone specific browser is used for rendering this client application. This could be Safari, Chrome for iOS, 
         * Opera Mini (iOS WebKit), Firefox for iOS. See https://deviceatlas.com/blog/mobile-browser-user-agent-strings for more details.
         */
        iPhoneOs: function () {
            return window.navigator.userAgent.indexOf('iPhone OS') > 0;
        }
    });
})();
