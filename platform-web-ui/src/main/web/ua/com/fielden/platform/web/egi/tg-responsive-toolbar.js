import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

const template = html`
    <style>
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
        }
        .grid-toolbar {
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .grid-toolbar-content {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .grid-toolbar-content ::slotted(*) {
            margin-top: 8px;
        }
        .grid-toolbar-content ::slotted(.group) {
            margin-left: 30px;
        }
        #expandToolbarButton.invisible {
            position: absolute;
            right:0;
            visibility: hidden;
        }
    </style>
    <div id="toolbar" class="grid-toolbar">
        <slot></slot>
        <div class="grid-toolbar-content">
            <slot id="top_action_selctor" name="entity-specific-action"></slot>
        </div>
        <div class="grid-toolbar-content" style="margin-left:auto">
            <slot id="standard_action_selector" name="standart-action"></slot>
            <paper-icon-button id="expandToolbarButton" icon="more-vert" on-tap="_showMoreActions" class="invisible"></paper-icon-button>
        </div>
    </div>
    <iron-dropdown id="dropdown" horizontal-align="right" open-animation-config="[[openAnimationConfig]]" close-animation-config="[[closeAnimationconfig]]">
        <span class="dropdown-content" slot="dropdown-content">This is overflow menu</span>
    </iron-dropdown>`;

export class TgResponsiveToolbar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            specificActions: Array,
            standartActions: Array
        };
    }

    ready () {
        super.ready();
        this.specificActions = [];
        this.$.top_action_selctor.assignedNodes({ flatten: true }).array.forEach(node => {
            const specificActionSelector = ".entity-specific-action:not(.group):not(.first-group)";
            if (node.matches(specificActionSelector)) {
                specificActions.push([node]);
            } else {
                specificActions.push([...node.querySelectorAll(specificActionSelector)]);
            }
        });
        this.standartActions = [];
        this.$.standard_action_selector.assignedNodes({ flatten: true }).array.forEach(node => {
            const standartActionSelector = ".standart-action";
            if (node.matches(standartActionSelector)) {
                standartActions.push(node);
            }
        });

        //Initialising resize event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    }

    _resizeEventListener () {

    }
}

customElements.define('tg-responsive-toolbar', TgResponsiveToolbar);

