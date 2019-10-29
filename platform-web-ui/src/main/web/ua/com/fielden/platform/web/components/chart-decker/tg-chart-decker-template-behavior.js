import { TgAppConfig } from '/app/tg-app-config.js';

import { TgEntityMasterTemplateBehavior} from '/resources/master/tg-entity-master-template-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import {_momentTz, _millisDateRepresentation} from '/resources/reflection/tg-date-utils.js';

const generateActionTooltip = function (action) {
    const shortDesc = "<b>" + action.shortDesc + "</b>";
    let longDesc = "";
    if (action.shortDesc) {
        longDesc = action.longDesc ? "<br>" + action.longDesc : "";
    } else {
        longDesc = action.longDesc ? "<b>" + action.longDesc + "</b>" : "";
    }
    let tooltip = shortDesc + longDesc;
    return tooltip && "<div style='display:flex;'>" +
        "<div style='margin-right:10px;'>With action: </div>" +
        "<div style='flex-grow:1;'>" + tooltip + "</div>" +
        "</div>"
};

const TgChartDeckerTemplateBehaviorImpl = {

    properties: {
        actions: Array,
        charts: Array,
        centreSelection: {
            type: Object,
            observer: "_centreSelectionChanged"
        }
    },

    ready: function () {
        this.appConfig = new TgAppConfig();
        this.actions = [...this._masterDom().querySelectorAll(".chart-action")];
        this.charts = this._masterDom().querySelectorAll(".chart-deck");
        this._chartLeftMargins = [];
        this.charts.forEach((chart, index) => {
            chart.addEventListener("bar-entity-selected", this._entitySelectedListener.bind(this));
            chart.addEventListener("y-axis-label-positioned", this._alignCharts(index))
        });
        this._labelFormatter = this._labelFormatter.bind(this);
        this._tooltip = this._tooltip.bind(this);
    },

    _alignCharts: function (index) {
        return function (event) {
            if (!this._chartLeftMargins[index]) {
                this._chartLeftMargins[index] = event.detail;
                let allDefined = true;
                this.charts.forEach((chart, index) => {
                    if (!this._chartLeftMargins[index]) {
                        allDefined = false;
                    }
                });
                if (allDefined) {
                    const maxLeftMargin = Math.max.apply(null, this._chartLeftMargins);
                    if (!this._lastLeftMargin || maxLeftMargin !== this._lastLeftMargin) {
                        this._lastLeftMargin = maxLeftMargin;
                        this.charts.forEach(chart => chart.options = {
                            margin: {
                                left: this._lastLeftMargin
                            }
                        });
                    }
                    this._chartLeftMargins = [];
                }
            }
        }.bind(this);
    },

    _entitySelectedListener: function (event) {
        const target = this.customEventTarget || this;
        target.fire("tg-entity-selected", event.detail);
        tearDownEvent(event);
    },
    
    _datePropAccessor: function (propertyName, dateType) {
        return function (entity, value) {
            if (!value) {
                const splitedType = dateType.split(':');
                return _millisDateRepresentation(entity.get(propertyName), splitedType[1] || null, splitedType[2] || null);
            }
        }
    },

    _moneyPropAccessor: function (propertyName) {
        return function (entity, value) {
            if (!value) {
                return entity.get(propertyName).amount;
            }
        }.bind(this);
    },

    _labelFormatter: function (entity, idx, propertyNames, propertyTypes, mode) {
        if (mode == d3.barChart.BarMode.STACKED) {
            const res = propertyNames.map((prop, i) => propertyTypes[i] === "Money" ? this._moneyPropAccessor(prop)(entity, entity.get(prop)) : entity.get(prop));
            const resPos = res.filter(val => val > 0).reduce((a,b) => a + b, 0);
            const resNeg = res.filter(val => val < 0).reduce((a,b) => a + b, 0);
            if (idx > 0) {
                if (!resPos) {
                    return resNeg ? "" : this._formatValue(0, entity, propertyNames[0], propertyTypes[0]);
                }
                return this._formatValue(resPos, entity, propertyNames[0], propertyTypes[0]);
            } else {
                return resNeg ? this._formatValue(resNeg, entity, propertyNames[0], propertyTypes[0]) : "";
            }
        }
        return this._getValue(entity, propertyNames[idx], propertyTypes[idx]);
    },
    
    _formatValue: function (value, entity, property, type) {
        if (typeof entity.get(property) === 'number') {
            if (type === 'BigDecimal') {
                const metaProp = this._reflector().getEntityTypeProp(entity, property);
                return this._reflector().formatDecimal(value, this.appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
            } else {
                return this._reflector().formatNumber(value, this.appConfig.locale);
            }
        } else if (type === 'Money') {
            const metaProp = this._reflector().getEntityTypeProp(entity, property);
            return this._reflector().formatMoney(value, this.appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
        }
    },

    _tooltip: function (entity, groupProperty, valuePropertyName, propertyType, valuePropTitle, deckIndex, actionIndex) {
        const valueTooltip = this._getValue(entity, valuePropertyName, propertyType);
        const groupTooltip = typeof groupProperty === 'function' ? groupProperty(entity) : entity.get(groupProperty);
        const action = this.actions.find(a => a.getAttribute("deck-index") === deckIndex + "" && a.getAttribute("action-index") === actionIndex + "");
        const actionTooltip = action ? generateActionTooltip(action) : "";
        let tooltip = valueTooltip ? "<b>" + valueTooltip + "</b>" : "";
        tooltip += (groupTooltip && tooltip && "<br>") + groupTooltip;
        tooltip += (valuePropTitle && tooltip && "<br>") + valuePropTitle;
        tooltip += (actionTooltip && tooltip && "<br><br>") + actionTooltip;
        return tooltip;
    },

    _click: function (deckIndex) {
        return function (entity, idx) {
            const action = this.actions.find(a => a.getAttribute("deck-index") === deckIndex + "" && a.getAttribute("action-index") === idx + "");
            if (action) {
                action.currentEntity = entity;
                action._run();
            }
        }.bind(this);
    },

    _getValue: function (entity, property, type) {
        return this._formatValue(entity.get(property), entity, property, type);
    },

    _centreSelectionChanged: function (newSelection, oldSelection) {
        newSelection.entities.forEach(selectionEntity => this.selectChartEntity(selectionEntity.entity, selectionEntity.select));
    },

    selectChartEntity: function (entity, select) {
        this.charts.forEach(chart => chart.selectEntity(entity, select));
    }

};

export const TgChartDeckerTemplateBehavior = [
    TgEntityMasterTemplateBehavior,
    TgChartDeckerTemplateBehaviorImpl
];