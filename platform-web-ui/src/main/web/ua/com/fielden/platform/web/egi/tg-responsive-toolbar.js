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
        #expandToolbarButton {
            margin-top: 8px;
        }
        #expandToolbarButton.invisible {
            display: none;
        }
        paper-icon-button.revers {
            transform: scale(-1, 1);
        }
    </style>
    <div id="toolbar" class="grid-toolbar">
        <slot></slot>
        <div id="leftToolbarContainer" class="grid-toolbar-content">
            <slot id="top_action_selctor" name="entity-specific-action"></slot>
        </div>
        <div id="rightToolbarContainer" class="grid-toolbar-content" style="margin-left:auto">
            <slot id="standard_action_selector" name="standart-action"></slot>
            <paper-icon-button id="expandToolbarButton" icon="more-vert" on-tap="_showMoreActions" class="invisible"></paper-icon-button>
        </div>
    </div>
    <iron-dropdown id="dropdown" horizontal-align="right" vertical-offset="8">
        <div id="hiddenToolbar" class="dropdown-content" slot="dropdown-content">
            <div id="specificActionContainer"></div>
            <div id="standartActionContainer"></div>
        </div>
    </iron-dropdown>`;

class ToolbarElement {

    constructor(element, standartAction, groupIndex) {
        this.element = element;
        this.standartAction = standartAction;
        this.groupIndex = groupIndex;
    }

    get width() {
        if (!this._width) {
            this._width = this.element && this.element.offsetWidth;
        }
        return this._width;
    }

    addAfter(element, standartAction, groupIndex) {
        this.after = new ToolbarElement(element, standartAction, groupIndex);
        this.after.previous = this;
        return this.after;
    }

    addPrevious(element, standartAction, groupIndex) {
        this.previous = new ToolbarElement(element, standartAction, groupIndex);
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
            toolbarElement: Object,
            _slottedElementParent: Object,
        };
    }

    ready () {
        super.ready();

        this.toolbarElement = new ToolbarElement(null, false);
        let groupIndex = 0;
        this.$.top_action_selctor.assignedNodes({ flatten: true }).forEach(node => {
            const specificActionSelector = ".entity-specific-action:not(.group):not(.first-group)";
            if (node.matches(specificActionSelector)) {
                this._addToolbarAction(node, false);
            } else {
                node.setAttribute("group-index", groupIndex);
                this._addToolbarActions([...node.querySelectorAll(specificActionSelector)], groupIndex);
                groupIndex += 1;
            }
        });
        this.$.standard_action_selector.assignedNodes({ flatten: true }).forEach(node => {
            const standartActionSelector = "[slot=standart-action]";
            if (!this._slottedElementParent) {
                this._slottedElementParent = node.parentElement;
            }
            if (node.matches(standartActionSelector)) {
                this._addToolbarStandratAction(node)
            }
        });
        this.toolbarElement.addAfter(null, false);

        //Initialising resize event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    }

    _showMoreActions () {
        this.$.dropdown.open();
    }

    _addToolbarAction (node, standartAction, groupIndex) {
        this.toolbarElement = this.toolbarElement.addAfter(node, standartAction, groupIndex);
    }

    _addToolbarActions (nodes, groupIndex) {
        nodes.forEach(node => this._addToolbarAction(node, false, groupIndex));
    }

    _addToolbarStandratAction (standratAction) {
        this._addToolbarAction(standratAction, true);
    }

    _resizeEventListener (e) {
        const thisComponentWidth = this.offsetWidth;
        const widthOfToolbar = this.$.leftToolbarContainer.offsetWidth + this.$.rightToolbarContainer.offsetWidth;
        if (e.composedPath()[0] !== this.$.dropdown && this.$.dropdown.opened) {
            this.$.dropdown.close();
        }
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
            expandButtonWidth = this.$.expandToolbarButton.offsetWidth;
        }
        let totalWidth = 0 - expandButtonWidth;
        const elementsToHide = [];
        while (totalWidth < widthOfButtonsToHide && this.toolbarElement.element) {
            elementsToHide.push(this.toolbarElement);
            totalWidth += this.toolbarElement.width;
            this.toolbarElement = this.toolbarElement.previous;
        }
        elementsToHide.forEach(element => {
            if (element.standartAction) {
                this.$.standartActionContainer.prepend(element.element);                
            } else if (typeof element.groupIndex !== 'undefined'){
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
                const removedFromGroup = this.querySelector("[group-index=" + element.groupIndex + "]");
                if (removedFromGroup && removedFromGroup.childElementCount === 0) {
                    removedFromGroup.parentElement.removeChild(removedFromGroup);
                }
            }
        });
    }

    _showButtons (widthOfButtonsToshow) {
        if (this.toolbarElement.after.element !== null && this.$.expandToolbarButton.offsetParent !== null) {
            let totalWidth =  widthOfButtonsToshow;
            const elementsToShow = [];
            while (this.toolbarElement.after.element !== null && totalWidth -  this.toolbarElement.after.width > 0) {
                this.toolbarElement = this.toolbarElement.after;
                elementsToShow.push(this.toolbarElement);
                totalWidth -= this.toolbarElement.width;
            }
            if (this.toolbarElement.after.element === null) {
                this.$.expandToolbarButton.classList.toggle("invisible", true);
            } else if (this.toolbarElement.after.after.element === null) {
                elementsToShow.push(this.toolbarElement.after);
                this.toolbarElement = this.toolbarElement.after;
                this.$.expandToolbarButton.classList.toggle("invisible", true);
            }
            elementsToShow.forEach(element => {
                if (element.standartAction || typeof element.groupIndex !== 'undefined') {
                    this._slottedElementParent.append(element.element);                
                } else {
                    const groupContainer = this.querySelector("[group-index=" + element.groupIndex + "]");
                    if (groupContainer) {
                        groupContainer.append(element.element);
                    } else {
                        const newGroup = document.createElement("div");
                        newGroup.setAttribute("group-index", element.groupIndex);
                        newGroup.setAttribute("slot", "entity-specific-action");
                        newGroup.classList.toggle("entity-specific-action", true);
                        newGroup.classList.toggle(element.groupIndex === 0 ? "first-group": "group", true);
                        newGroup.append(element.element);
                        this._slottedElementParent.append(newGroup);
                    }
                    const removedFromGroup = this.$.specificActionContainer.querySelector("[group-index=" + element.groupIndex + "]");
                    if (removedFromGroup && removedFromGroup.childElementCount === 0) {
                        removedFromGroup.parentElement.removeChild(removedFromGroup);
                    }
                }
            });
        }
    }
}

customElements.define('tg-responsive-toolbar', TgResponsiveToolbar);

