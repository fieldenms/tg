import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

import '/resources/components/moment-lib.js';

moment.locale('custom-locale', {
    longDateFormat: {
        LTS: @timeWithMillisFormat,
        LT: @timeFormat,
        L: @dateFormat
    }
});

export const TgAppConfig = Polymer({
    
    is: "tg-app-config",
    
    properties: {
        minDesktopWidth: {
            type: Number,
            readOnly: true,
            value: @minDesktopWidth
        },
        minTabletWidth: {
            type: Number,
            readOnly: true,
            value: @minTabletWidth
        },
        locale: {
            type: String,
            readOnly: true,
            value: @locale
        },
        dateFormat: {
            type: String,
            readOnly: true,
            value: @dateFormat
        },
        timeFormat: {
            type: String,
            readOnly: true,
            value: @timeFormat
        }
    },
    
    attached: function() {
        this.style.display = "none";
    }
    
});