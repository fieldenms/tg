import { TgAbstractCriterionBehavior } from '/resources/centre/criterion/tg-abstract-criterion-behavior.js';
import '/resources/centre/criterion/tg-abstract-criterion.js';

import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';


const template = html`
    <tg-abstract-criterion id="dom"
        mnemonics-visible="[[mnemonicsVisible]]"
        exclude-missing="[[excludeMissing]]"
        exclude-not="[[excludeNot]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]"
        _or-null="{{_orNull}}"
        _not="{{_not}}">
        <slot name="criterion-editors" slot="criterion-editors"></slot>
    </tg-abstract-criterion>`; 

class TgCriterion extends mixinBehaviors([TgAbstractCriterionBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get observers() {
        return [
            '_updateIconButtonStyle(orNull, not, orGroup)'
        ]
    }

    _dom () {
        return this.$.dom;
    }
   
}

customElements.define('tg-criterion', TgCriterion);