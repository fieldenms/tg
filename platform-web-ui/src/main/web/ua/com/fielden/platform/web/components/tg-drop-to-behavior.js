import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

export const TgDropToBehavior = {

    canDropType: function (types, type) {
        for (var i = 0; i < types.length; ++i) {
            if (types[i] === type.toLowerCase()) return true;
        }
        return false;
    },

    canDropTo: function (e) {
        return false;
    },

    dropTo: function (e) {}
}