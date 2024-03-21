import { TgMasterWithChartBehavior} from '/resources/master/tg-master-with-chart-behavior.js';

const getTooltipValueForEntity = function (entity) {
    const keyValue = value.toString();
    let desc = null;
    try {
        desc = value.get("desc");
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

const TgScatterPlotMasterBehaviorImpl = {

    ready: function () {
        TgMasterWithChartBehavior.ready();
        this._click = this._click.bind(this);
        this._tooltip = this._tooltip.bind(this);
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
            }
            if (title) {
                res += `<tr><td>${title}</td><td>${value}</td></tr>`;
            }
        });
        if (this.actions && this.actions[0]) {
            res += `<tr><td>With action</td><td>${getActionTooltip(this.actions[0])}</td></tr>`
        }
        return res ? `<table>${res}</table>` : "";
    }

};

export const TgScatterPlotMasterBehavior = [
    TgMasterWithChartBehavior,
    TgScatterPlotMasterBehaviorImpl
];