import { TgMasterWithChartBehavior} from '/resources/master/tg-master-with-chart-behavior.js';
import { TgAppConfig } from '/app/tg-app-config.js';

const getTooltipValueForEntity = function (entity) {
    const keyValue = entity.toString();
    let desc = null;
    try {
        desc = entity.get("desc");
    } catch (ex) {
        desc = "";
    }
    return `<b>${keyValue}</b>${desc ? "<br>" + desc : ""}`;
}

const getActionTooltip = function (action) {
    const shortDesc = "<b>" + action.shortDesc + "</b>";
    let longDesc = "";
    if (action.shortDesc) {
        longDesc = action.longDesc ? "<br>" + action.longDesc : "";
    } else {
        longDesc = action.longDesc ? "<b>" + action.longDesc + "</b>" : "";
    }
    return shortDesc + longDesc;
}

const getDataRange = function (data, prop) {
    if (data && prop) {
        const values = data.map(e => e.get(prop));
        return [Math.min(...values), Math.max(...values)];
    }
    return [];
}

const getMasterEntityRange = function (entity, prop) {
    const newEntity = entity ? entity['@@origin'] : null;
    if (newEntity && prop) {
        return newEntity.get(prop);
    }
    return [];
}

const TgScatterPlotMasterBehaviorImpl = {

    properties: {
        categoryRangeSource : {
            type: String,
            value: ""
        },
        
        valueRangeSource: {
            type: String,
            value: ""
        }
    },

    ready: function () {
        this._appConfig = new TgAppConfig();
        this._click = this._click.bind(this);
        this._tooltip = this._tooltip.bind(this);
        this._getCategoryRange = this._getCategoryRange.bind(this);
        this._getValueRange = this._getValueRange.bind(this);
    },

    _getCategoryRange: function () {
        return this._getRange(this.categoryRangeSource);
    },

    _getValueRange: function () {
        return this._getRange(this.valueRangeSource);
    },

    _getRange: function (sourceProperty) {
        if (sourceProperty) {
            const source = sourceProperty.split(":");
            if (source[0] && source[0] == "data") {
                return getDataRange(this.retrievedEntities, source[1]);
            } else if (source[0] && source[0] == "masterEntity") {
                return getMasterEntityRange(this._currBindingEntity, source[1]);
            }
        }
        return [];
    },

    _click: function (entity, idx) {
        const action = this.actions[0];
        if (action) {
            action.currentEntity = () => entity;
            action._run();
        }
    },

    _tooltip: function (entity, tooltipProps) {
        let res = ""; 
        tooltipProps.forEach(prop => {
            const title = entity.type().prop(prop).title();
            let value = entity.get(prop);
            if (this._reflector().isEntity(value)) {
                value = getTooltipValueForEntity(value);
            } else {
                value = this._reflector().tg_toString(value, entity.constructor.prototype.type.call(entity), prop, { display: true, locale: this._appConfig.locale });
            }
            if (title && value) {
                res += `<tr><td valign="top" style="padding-right:10px;">${title}:</td><td>${value}</td></tr>`;
            }
        });
        if (this.actions && this.actions[0]) {
            res += `<tr><td valign="top" style="padding-right:10px;">With action:</td><td>${getActionTooltip(this.actions[0])}</td></tr>`
        }
        return res ? `<table>${res}</table>` : "";
    }

};

export const TgScatterPlotMasterBehavior = [
    TgMasterWithChartBehavior,
    TgScatterPlotMasterBehaviorImpl
];