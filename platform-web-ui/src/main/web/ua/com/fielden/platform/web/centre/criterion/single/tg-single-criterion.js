import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgAbstractCriterionBehavior } from '/resources/centre/criterion/tg-abstract-criterion-behavior.js';
import '/resources/centre/criterion/tg-abstract-criterion.js';

const template = html`
    <tg-abstract-criterion id="dom"
        mnemonics-visible="[[mnemonicsVisible]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]">
        <slot name="criterion-editors" slot="criterion-editors"></slot>
    </tg-abstract-criterion>
`;

Polymer({
    _template: template,

    is: 'tg-single-criterion',

    behaviors: [ TgAbstractCriterionBehavior ],

    ready: function () {
        this._updateIconButtonStyle();
    },

    _dom: function () {
        return this.$.dom;
    }
});