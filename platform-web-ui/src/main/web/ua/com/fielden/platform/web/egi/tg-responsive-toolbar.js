import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
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
    </style>
    <div class="grid-toolbar">
            <div class="grid-toolbar-content">
                <slot id="top_action_selctor" name="entity-specific-action"></slot>
            </div>
            <div class="grid-toolbar-content" style="margin-left:auto">
                <slot name="standart-action"></slot>
            </div>
    </div>
    <paper-icon-button icon="more-vert" on-tap="_showMoreActions"></paper-icon-button>
    <iron-dropdown id="dropdown" horizontal-align="right" open-animation-config="[[openAnimationConfig]]" close-animation-config="[[closeAnimationconfig]]">
        <span class="dropdown-content" slot="dropdown-content">This is overflow menu</span>
    </iron-dropdown>`;

export class TgResponsiveToolbar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            
        };
    }
}

