import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import { isInHierarchy } from '/resources/reflection/tg-polymer-utils.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

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
        }
        .grid-toolbar-content ::slotted(.first-group) {
            @apply --layout-horizontal;
        }
        .grid-toolbar-content ::slotted(.group) {
            margin-left: 16px;
            @apply --layout-horizontal;
        }
        :host {
            cursor: default;
        }
        :host(.custom-responsive-toolbar) #expandToolbarButton {
            @apply --tg-responsove-toolbar-expand-button;
        }
        :host(.custom-responsive-toolbar) .dropdown-content {
            @apply --tg-responsove-toolbar-dropdown-content;
        }
        #expandToolbarButton.invisible {
            display: none;
        }
        #specificActionContainer * {
            flex-shrink: 0;
        }
        #specificActionContainer {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        #standartActionContainer * {
            flex-shrink: 0;
        }
        #standartActionContainer {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        paper-icon-button.revers {
            transform: scale(-1, 1);
        }
        div[disabled] {
            pointer-events: none;
        }
        #expandToolbarButton {
            pointer-events:all;
        }
    </style>
    <div id="toolbar" class="grid-toolbar" disabled$="[[disabled]]">
        <slot></slot>
        <div id="leftToolbarContainer" class="grid-toolbar-content">
            <slot id="top_action_selctor" name="entity-specific-action"></slot>
        </div>
        <div id="rightToolbarContainer" class="grid-toolbar-content" style="margin-left:auto">
            <slot id="standard_action_selector" name="standart-action"></slot>
            <paper-icon-button id="expandToolbarButton" tooltip-text="Show other actions." icon="more-vert" on-tap="_showMoreActions" class="invisible"></paper-icon-button>
        </div>
    </div>
    <iron-dropdown id="dropdown" horizontal-align="right" vertical-offset="8" always-on-top>
        <div id="hiddenToolbar" class="dropdown-content" slot="dropdown-content" disabled$="[[disabled]]">
            <div id="specificActionContainer"></div>
            <div id="standartActionContainer"></div>
        </div>
    </iron-dropdown>`;

/**
 * Represents the element of linked list it has links to next and previous elements. 
 * The element has indicators for group index and whether element is standart action or not. 
 */
class ToolbarElement {

    constructor(element, standartAction, groupIndex) {
        this.element = element;
        this.standartAction = standartAction;
        this.groupIndex = groupIndex;
        this.parent = element && (element.parentElement || element.getRootNode().host); // the host element that contains action which were slotted into this responsive toolbar
    }

    get width() {
        if (!this._width && this.element) {
            const style = this.element.currentStyle || window.getComputedStyle(this.element),
                width = this.element.getBoundingClientRect().width,
                margin = parseFloat(style.marginLeft) + parseFloat(style.marginRight);
            this._width = width + margin;
        }
        return this._width;
    }

    addNext(element, standartAction, groupIndex) {
        this.next = new ToolbarElement(element, standartAction, groupIndex);
        this.next.previous = this;
        return this.next;
    }
}

/**
 * The elements added to this responsive toolbar might have different level of slotted nodes.
 * In order to make responsive toolbar work correctly one should make sure that only top most slot should be used to determine whether element belongs to this toolbar or not.
 * This function helps to find top most slot used to add element to this toolbar.
 */
const findTopMostSlot = function (node, parent) {
    let slot = node;
    while(slot.assignedSlot && slot.assignedSlot !== parent) {
        slot = slot.assignedSlot;
    }
    return slot;
};

export class TgResponsiveToolbar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            disabled:  {
                type: Boolean,
                value: false,
                reflectToAttribute: true

            },
            //Points to last visible action on toolbar.
            _lastVisibleToolbarElement: Object
        };
    }

    ready () {
        super.ready();
        //Need to create list of all buttons that can be hidden start  from the specific action, those are in the left side of toolbar
        //Keep in mind that actions might be grouped.
        //First element of the list should be empty it indicates the end of list
        this._lastVisibleToolbarElement = new ToolbarElement(null, false);
        let groupIndex = 0;
        this.$.top_action_selctor.assignedNodes({ flatten: true }).forEach(node => {
            const specificActionSelector = ".entity-specific-action:not(.group):not(.first-group)";
            if (findTopMostSlot(node, this.$.top_action_selctor).matches(specificActionSelector)) {
                //that is not a group just an action.
                this._addToolbarAction(node, false);
            } else {
                //That is a group, in this case get all children in the group and add them as a separate actions.
                node.setAttribute("group-index", groupIndex);
                this._addToolbarActions([...node.querySelectorAll(specificActionSelector)], groupIndex);
                groupIndex += 1;
            }
        });
        //Now add actions those are in the right side of the egi that is standart action (i.e. config, navigation and refresh actions)
        //This actions shouldn't be group so add them as separate items.
        this.$.standard_action_selector.assignedNodes({ flatten: true }).forEach(node => {
            const standartActionSelector = "[slot=standart-action]";
            if (findTopMostSlot(node, this.$.standard_action_selector).matches(standartActionSelector)) {
                this._addToolbarStandartAction(node);
            }
        });
        //Add last one empty toolbar element as an end list indicator.
        this._lastVisibleToolbarElement.addNext(null, false);

        //Initialising resize event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        //Initiate observer that will listen when data in responsive toolbar chages in order to properly resize.
        const observer = new MutationObserver(mutations => {
            if (this.$.dropdown.opened) {
                this.$.dropdown.notifyResize();
            }
        });
        const config = {
            characterData: true,
            subtree: true
        };
        observer.observe(this.$.dropdown, config);
        return observer;
    }

    _showMoreActions (e) {
        e.preventDefault();
        this.$.dropdown.open();
    }

    /**
     * Adds new action to the list of actions that can be hidden. 
     *
     */
    _addToolbarAction (node, standartAction, groupIndex) {
        this._lastVisibleToolbarElement = this._lastVisibleToolbarElement.addNext(node, standartAction, groupIndex);
    }

    /**
     * Adds new action to the list of actions that can be hidden that should be within group with specified index.
     * 
     */
    _addToolbarActions (nodes, groupIndex) {
        nodes.forEach(node => this._addToolbarAction(node, false, groupIndex));
    }

    /**
     * Adds the standart action to the list of actions those can be hidden.
     *  
     */
    _addToolbarStandartAction (standartAction) {
        this._addToolbarAction(standartAction, true);
    }

    _resizeEventListener (e) {
        const thisComponentWidth = this.offsetWidth;
        const widthOfToolbar = this.$.leftToolbarContainer.offsetWidth + this.$.rightToolbarContainer.offsetWidth;
        if (e.composedPath()[0] !== this.$.dropdown && !isInHierarchy(this.$.dropdown, e.composedPath()[0])) {
            if (this.$.dropdown.opened) {
                this.$.dropdown.close();
            }
            if (thisComponentWidth < widthOfToolbar) {
                this._hideButtons(widthOfToolbar - thisComponentWidth);
            } else {
                this._showButtons(thisComponentWidth - widthOfToolbar);
            }
        }
    }

    _hideButtons (widthOfButtonsToHide) {
        let expandButtonWidth = 0;
        //If button that shows dropdown list is invisible (invisible elements (i.e. display style is none) don't have offsetParent set.)
        //Then make this button visible.
        if (this.$.expandToolbarButton.offsetParent === null) {
            this.$.expandToolbarButton.classList.toggle("invisible", false);
            expandButtonWidth = this.$.expandToolbarButton.offsetWidth;
        }
        //Subtract expand button width in order to hide more buttons, because expand button has become visible
        let totalWidth = 0 - expandButtonWidth;
        const elementsToHide = [];
        while (totalWidth < widthOfButtonsToHide && this._lastVisibleToolbarElement.element) {
            elementsToHide.push(this._lastVisibleToolbarElement);
            totalWidth += this._lastVisibleToolbarElement.width;
            this._lastVisibleToolbarElement = this._lastVisibleToolbarElement.previous;
        }
        //Iterate over the the elements  to hide and hide them (i.e. move to the dropdown list) This elements should be moved as:
        //Standart action
        //specific action (the one that was added in the centre configurtion)
        //As action in group. The group are also defined by end application developer.
        elementsToHide.forEach(element => {
            if (element.standartAction) {
                this.$.standartActionContainer.prepend(element.element);
            } else {
                this.$.specificActionContainer.prepend(element.element);
            }
        });
    }

    _showButtons (widthOfButtonsToshow) {
        //Show buttons if there is buttons to show and expand dropdown list button is visible,
        if (this._lastVisibleToolbarElement.next.element !== null && this.$.expandToolbarButton.offsetParent !== null) {
            let totalWidth =  widthOfButtonsToshow;
            const elementsToShow = [];
            //Create the list of elements to show.
            while (this._lastVisibleToolbarElement.next.element !== null && totalWidth -  this._lastVisibleToolbarElement.next.width > 0) {
                this._lastVisibleToolbarElement = this._lastVisibleToolbarElement.next;
                elementsToShow.push(this._lastVisibleToolbarElement);
                totalWidth -= this._lastVisibleToolbarElement.width;
            }
            //If there are no more elements to show then hide expand dropdown list button. 
            if (this._lastVisibleToolbarElement.next.element === null) {
                this.$.expandToolbarButton.classList.toggle("invisible", true);
            } //If there is left only one hidden button then make it visible and hide expand dropdown list button.  
            else if (this._lastVisibleToolbarElement.next.next.element === null) {
                elementsToShow.push(this._lastVisibleToolbarElement.next);
                this._lastVisibleToolbarElement = this._lastVisibleToolbarElement.next;
                this.$.expandToolbarButton.classList.toggle("invisible", true);
            }
            //Iterate over the elemnts to show and add them to the host element (the element from which actions were slotted into this responsive toolbar)
            //The element should be added as:
            //1. Single specific element
            //2. Specific element (the one that is defined by end application user in the centre configuration) in the group.
            //3. Standart action (navigation and confg actions)
            elementsToShow.forEach(element => {
                element.parent.append(element.element);
            });
        }
    }
}

customElements.define('tg-responsive-toolbar', TgResponsiveToolbar);

