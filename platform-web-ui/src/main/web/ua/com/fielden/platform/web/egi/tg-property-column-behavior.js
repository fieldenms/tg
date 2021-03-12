export const TgPropertyColumnBehavior = {

    properties: {
        property: String,
        tooltipProperty: String,
        type: String,
        width: Number,
        minWidth: Number,
        growFactor: Number,
        columnTitle: String,
        columnDesc: String,
        visible: {
            type: Boolean,
            value: true
        }
    },

    hostAttributes: {
        hidden: true
    }
} 