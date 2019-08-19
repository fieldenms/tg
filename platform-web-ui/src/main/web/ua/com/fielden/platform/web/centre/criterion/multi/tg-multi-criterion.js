import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/centre/criterion/tg-abstract-criterion.js';
import { TgAbstractMultiCriterionBehavior } from '/resources/centre/criterion/multi/tg-abstract-multi-criterion-behavior.js';

const template = html`
    <tg-abstract-criterion id="dom"
        mnemonics-visible="[[mnemonicsVisible]]"
        exclude-missing="[[excludeMissing]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]"
        _or-null="{{_orNull}}"
        _not="{{_not}}">
        <slot name="criterion-editors" slot="criterion-editors"></slot>
    </tg-abstract-criterion>
`;

Polymer({
    _template: template,

    is: 'tg-multi-criterion',

    observers: [
        '_updateIconButtonStyle(orNull, not)'
    ],

    behaviors: [ TgAbstractMultiCriterionBehavior ],

    _dom: function () {
        return this.$.dom;
    }
});