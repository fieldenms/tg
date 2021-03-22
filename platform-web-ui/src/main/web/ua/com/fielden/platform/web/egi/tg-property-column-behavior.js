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
        vertical: {
            type: Boolean,
            value: false
        }
    },

    hostAttributes: {
        hidden: true
    }
} 