import { TgAppConfig } from '/app/tg-app-config.js';

import { TgEntityMasterTemplateBehavior} from '/resources/master/tg-entity-master-template-behavior.js';
import {_momentTz, _millisDateRepresentation} from '/resources/reflection/tg-date-utils.js';

const TgMasterWithChartBehaviorImpl = {

    properties: {
        actions: Array,
        charts: Array
    },

    ready: function () {
        this.appConfig = new TgAppConfig();
        this.actions = [...this._masterDom().querySelectorAll(".chart-action")];
        this.charts = this._masterDom().querySelectorAll(".chart-deck");
    },
    
    _datePropAccessor: function (propertyName, dateType) {
        return function (entity, value) {
            if (!value) {
                const splitedType = dateType.split(':');
                return _momentTz(entity.get(propertyName), splitedType[1] || null, splitedType[2] || null).toDate();
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
    
    _formatValue: function (value, entity, property, type) {
        if (typeof entity.get(property) === 'number') {
            if (type === 'BigDecimal') {
                const metaProp = this._reflector().getEntityTypeProp(entity, property);
                return this._reflector().tg_formatDecimal(value, this.appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
            } else {
                return this._reflector().tg_formatInteger(value, this.appConfig.locale);
            }
        } else if (type === 'Money') {
            const metaProp = this._reflector().getEntityTypeProp(entity, property);
            return this._reflector().tg_formatMoney(value, this.appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
        }
    },

    _click: function (deckIndex) {
        return function (entity, idx) {
            const action = this.actions.find(a => a.getAttribute("deck-index") === deckIndex + "" && a.getAttribute("action-index") === idx + "");
            if (action) {
                action.currentEntity = () => entity;
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

export const TgMasterWithChartBehavior = [
    TgEntityMasterTemplateBehavior,
    TgMasterWithChartBehaviorImpl
];