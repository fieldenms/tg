import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgPropertyColumnBehavior } from '/resources/egi/tg-property-column-behavior.js';

export class TgHierarchyColumn extends mixinBehaviors([TgPropertyColumnBehavior], PolymerElement) {

    static get properties() {
        
        return {
            contentBuilder: Function,
        };
        
    }    
}

customElements.define('tg-hierarchy-column', TgHierarchyColumn);