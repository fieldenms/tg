
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import { TgLayoutBehavior, layoutMediaQueryTemplate } from '/resources/layout/tg-layout-behavior.js';

const template = html`${layoutMediaQueryTemplate}`;

class TgGridLayout extends mixinBehaviors([TgLayoutBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    _setLayout (layout) {
        
    }

    _filterLayout (filter) {
        
    }
}

customElements.define('tg-grid-layout', TgGridLayout);