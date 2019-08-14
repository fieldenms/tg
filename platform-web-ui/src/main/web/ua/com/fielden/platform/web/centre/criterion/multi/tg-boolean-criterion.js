import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/centre/criterion/tg-abstract-criterion.js';
import { TgAbstractMultiCriterionBehavior } from '/resources/centre/criterion/multi/tg-abstract-multi-criterion-behavior.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

const template = html`
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-abstract-criterion id="dom"
        mnemonics-visible="[[mnemonicsVisible]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]"
        _or-null="{{_orNull}}"
        _not="{{_not}}">
        <div slot="criterion-editors" class="layout horizontal flex criterion-editors" style="margin-right:20px;">
            <slot name="range-criterion-editor-1"></slot>
        </div>
        <div slot="criterion-editors" class="layout horizontal flex criterion-editors">
            <slot name="range-criterion-editor-2"></slot>
        </div>
    </tg-abstract-criterion>
`;

Polymer({
    _template: template,

    is: 'tg-boolean-criterion',

    observers: [
        '_updateIconButtonStyle(orNull, not)'
    ],

    behaviors: [ TgAbstractMultiCriterionBehavior ],

    _dom: function () {
        return this.$.dom;
    }
});