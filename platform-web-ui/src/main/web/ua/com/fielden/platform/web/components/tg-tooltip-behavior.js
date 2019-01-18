import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/components/tg-tooltip.js';
/**
 * Provides tooltip support for component that uses this behaviour. In order to set tooltip for specific element in the component, one should
 * mark element with tooltip-text attribute.
 */
    
const toolTipElement = document.createElement("tg-tooltip");
/**
 * Returns the element in hierarchy that has tooltip-id attribute set otherwise returns null.
 */
const extractActiveElement = function (currentElement, thisElement) {
    if (currentElement && currentElement !== thisElement.parentElement) {
        if (currentElement.hasAttribute("tooltip-text")) {
            return currentElement;
        } else {
            return extractActiveElement(currentElement.parentElement, thisElement);
        }
    }
    return null;
};
//Adds tooltip element to document's body so that it only one for all tooltips.
document.body.appendChild(toolTipElement);

export const TgTooltipBehavior = {

    properties: {
        triggerManual: {
            type: String,
            observer: "_triggerManualChanged"
        },

        /**
         * The element that will be observed for mouse move events. This element or it's children should have tooltip-text attribute set in order the tooltip to be shown.
         */
        triggerElement: {
            type: Object,
            observer: "_triggerElementChanged"
        },

        /**
         * Saved mouse positions.
         */
        _mousePosX: Number,
        _mousePosY: Number,
        /**
         * Element under mouse pointer, for which tooltip should be shown. Also this element should be marked with tooltip-id attribute.
         */
        _activeComponent: {
            type: Object
        }
    },

    ready: function () {
        //Bind mouse events
        this._handleMouseMove = this._handleMouseMove.bind(this);
        this._handleTooltipAtMousePos = this._handleTooltipAtMousePos.bind(this);
        this._hideTooltip = this._hideTooltip.bind(this);
        //Bind touch events
        this._handleTouchStart = this._handleTouchStart.bind(this);
        this._handleTouchEnd = this._handleTouchEnd.bind(this);
        this.triggerElement = this;
        //Set the default values for properties.
        this.triggerManual = false;
    },

    /**
     * Displayes the tooltip with specified text at current mouse position.
     */
    showTooltip: function (tooltipText) {
        toolTipElement.show(tooltipText, this._mousePosX, this._mousePosY);
    },

    /**
     * Observer for trigger element changes.
     */
    _triggerElementChanged: function (newElement, oldElement) {
        this._setMouseEvents(newElement, oldElement, this.triggerManual);
    },

    /**
     * Observer for _triggerManual property.
     */
    _triggerManualChanged: function (newValue, oldValue) {
        this._setMouseEvents(this.triggerElement, this.triggerElement, newValue);
    },

    /**
     * Set the appropriate mouse event handlers for trigger element according to trigger policy.
     */
    _setMouseEvents: function (newTrigger, oldTrigger, manual) {
        if (oldTrigger) {
            //Unregister mouse move listener for old trigger element.
            oldTrigger.removeEventListener('mousemove', this._handleMouseMove);
            if (!manual) {
                //If tooltips were triggerd manually then don't remove anything.
                this._unregisterTooltipRelatedEvents(oldTrigger);
            }
        }
        if (newTrigger) {
            //Register mouse listeners for new trigger element.     
            newTrigger.addEventListener('mousemove', this._handleMouseMove);
            if (!manual) {
                this._registerTooltipRelatedEvents(newTrigger);
            } else {
                this._unregisterTooltipRelatedEvents(newTrigger);
            }
        }
    },

    _registerTooltipRelatedEvents: function (trigger) {
        //Register mouse listener for new trigger element in order to trigger tooltips automatically
        trigger.addEventListener('mousemove', this._handleTooltipAtMousePos);
        trigger.addEventListener('mouseleave', this._hideTooltip);
        //Register touch listener for new trigger element in order to trigger tooltips automatically
        trigger.addEventListener('touchstart', this._handleTouchStart);
        trigger.addEventListener('touchend', this._handleTouchEnd);
    },

    _unregisterTooltipRelatedEvents: function (trigger) {
        //Unregister mouse listener for new trigger element in order to trigger tooltips automatically
        trigger.removeEventListener('mousemove', this._handleTooltipAtMousePos);
        trigger.removeEventListener('mouseleave', this._hideTooltip);
        //Unregister touch listener for new trigger element in order to trigger tooltips automatically
        trigger.removeEventListener('touchstart', this._handleTouchStart);
        trigger.removeEventListener('touchend', this._handleTouchEnd);
    },

    _handleTouchStart: function (event) {
        this.touchEventTriggered = true;
        this._startTooltip();
    },

    _handleTouchEnd: function (event) {
        this._hideTooltip();
    },

    /**
     * Saves the mouse position in _mousePosX and _mousePosY properties.
     */
    _handleMouseMove: function (event) {
        this._mousePosX = event.pageX;
        this._mousePosY = event.pageY;
    },

    /**
     * Handler that determines when to show tooltip on mouse move event amd when to hide it.
     */
    _handleTooltipAtMousePos: function (event) {
        if (!this.touchEventTriggered) {
            this._startTooltip();
        } else {
            this.touchEventTriggered = false;
        }
    },

    _startTooltip: function (e) {
        const currentActiveElement = extractActiveElement(event.target, this.triggerElement);

        if (currentActiveElement !== this._activeComponent) {
            this._hideTooltip();
            this._activeComponent = currentActiveElement;
        }
        const tooltipText = this._activeComponent && this._activeComponent.getAttribute("tooltip-text");
        if (tooltipText !== null && tooltipText.length > 0) {
            this.showTooltip(tooltipText);
        }
    },

    /**
     * Hides the tooltip. Used as a mouse handler for mouse leave event.
     */
    _hideTooltip: function () {
        toolTipElement.hide();
    },
};