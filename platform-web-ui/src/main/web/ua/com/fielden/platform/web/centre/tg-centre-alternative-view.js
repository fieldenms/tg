import '/resources/egi/tg-responsive-toolbar.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        :host {
            width: 100%;
            height: 100%;
            @apply --layout-vertical;
        }
    </style>
    <tg-responsive-toolbar id="viewToolbar">
        <slot slot="entity-specific-action" name="entity-specific-action"></slot>
        <slot slot="standart-action" name="standart-action"></slot>
    </tg-responsive-toolbar>
    <slot name="alternative-view-insertion-point"></slot>`;
    
class TgCentreAlternativeView extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {};
    }

    ready () {
        super.ready();
       
    }
}

customElements.define('tg-centre-alternative-view', TgCentreAlternativeView);