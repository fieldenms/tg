import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

const template = html`
    <style>
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
        .grid-toolbar {
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .grid-toolbar-content {
            min-width:fit-content;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .grid-toolbar-content ::slotted(*) {
            margin-top: 8px;
        }
        .grid-toolbar-content ::slotted(.first-group) {
            @apply --layout-horizontal;
        }
        .grid-toolbar-content ::slotted(.group) {
            margin-left: 30px;
            @apply --layout-horizontal;
        }
        #expandToolbarButton.invisible {
            display: none;
        }
    </style>
    <div id="toolbar" class="grid-toolbar">
        <slot></slot>
        <div id="left-toolbar-container" class="grid-toolbar-content">
            <slot id="top_action_selctor" name="entity-specific-action"></slot>
        </div>
        <div id="right-toolbar-container" class="grid-toolbar-content" style="margin-left:auto">
            <slot id="standard_action_selector" name="standart-action"></slot>
            <paper-icon-button id="expandToolbarButton" icon="more-vert" on-tap="_showMoreActions" class="invisible"></paper-icon-button>
        </div>
    </div>
    <iron-dropdown id="dropdown" horizontal-align="right" open-animation-config="[[openAnimationConfig]]" close-animation-config="[[closeAnimationconfig]]">
        <div id="hiddenToolbar" class="dropdown-content" slot="dropdown-content">
            <div id="specificActionContainer"></div>
            <div id="standartActionContainer"></div>
        </div>
    </iron-dropdown>`;

class ToolbarElement {

    constructor(element, standartAction, groupIndex) {
        this.element = element;
        this.hidden = false;
        this.standartAction = standartAction;
        this.groupIndex = groupIndex;
    }

    addAfter(element) {
        this.after = new ToolbarElement(element);
        this.after.previous = this;
        return this.after;
    }

    addPrevious(element) {
        this.previous = new ToolbarElement(element);
        this.previous.after = this;
        return this.previous;
    }
}

export class TgResponsiveToolbar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            toolbarElement: Object
        };
    }

    ready () {
        super.ready();
        if (!this.toolbarElement) {
            let groupIndex = 0;
            this.$.top_action_selctor.assignedNodes({ flatten: true }).forEach(node => {
                const specificActionSelector = ".entity-specific-action:not(.group):not(.first-group)";
                if (node.matches(specificActionSelector)) {
                    this._addToolbarAction(node, false);
                } else {
                    this._addToolbarActions([...node.querySelectorAll(specificActionSelector)], groupIndex);
                    groupIndex += 1;
                }
            });
            this.$.standard_action_selector.assignedNodes({ flatten: true }).forEach(node => {
                const standartActionSelector = "[slot=standart-action]";
                if (node.matches(standartActionSelector)) {
                    this._addToolbarStandratAction(node)
                }
            });
        }

        //Initialising resize event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    }

    _addToolbarAction (node, standartAction, groupIndex) {
        if (!this.toolbarElement) {
            this.toolbarElement = new ToolbarElement(node, standartAction, groupIndex);
        } else {
            this.toolbarElement = this.toolbarElement.addAfter(node, standartAction, groupIndex)
        }
    }

    _addToolbarActions (nodes, groupIndex) {
        nodes.forEach(node => this._addToolbarAction(node, false, groupIndex));
    }

    _addToolbarStandratAction (standratAction) {
        this._addToolbarAction(standratAction, true);
    }

    _resizeEventListener () {
        const thisComponentWidth = this.getBoundingClientRect().width;
        const widthOfToolbar = this.$.toolbar.getBoundingClientRect().width;
        if (thisComponentWidth < widthOfToolbar) {
            this._hideButtons(widthOfToolbar - thisComponentWidth);
        } else {
            this._showButtons(thisComponentWidth - widthOfToolbar);
        }
    }

    _hideButtons (widthOfButtonsToHide) {
        let expandButtonWidth = 0;
        if (this.$.expandToolbarButton.offsetParent === null) {
            this.$.expandToolbarButton.classList.toggle("invisible", false);
            expandButtonWidth = this.$.expandToolbarButton.getBoundingClientRect().width;
        }
        let totalWidth = 0 - expandButtonWidth;
        const elementsToHide = [];
        while (totalWidth < widthOfButtonsToHide && this.toolbarElement) {
            const nextButtonWidth = this.toolbarElement.element.getBoundingClientRect().width;
            elementsToHide.push(this.toolbarElement);
            totalWidth += nextButtonWidth;
            this.toolbarElement = this.toolbarElement.previous;
        }
        elementsToHide.forEach(element => {
            if (element.standartAction) {
                this.$.standartActionContainer.prepend(element.element);                
            } else if (!element.groupIndex){
                this.$.specificActionContainer.prepend(element.element);
            } else {
                const groupContainer = this.$.specificActionContainer.querySelector("[group-index=" + element.groupIndex + "]");
                if (groupContainer) {
                    groupContainer.prepend(element.element);
                } else {
                    const newGroup = document.createElement("div");
                    newGroup.setAttribute("group-index", element.groupIndex);
                    newGroup.appendChild(element.element);
                    this.$.specificActionContainer.prepend(newGroup);
                }
            }
        })
    }

    _showButtons (thisComponentWidth, widthOfToolbar) {

    }
}

customElements.define('tg-responsive-toolbar', TgResponsiveToolbar);

