import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-in-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import {NeonAnimationRunnerBehavior} from '/resources/polymer/@polymer/neon-animation/neon-animation-runner-behavior.js';

const template = html`
    <style>
        :host {
            display: block;
            position: absolute;
            outline: none;
            z-index: 1002;
        }
        #tooltip {
            display: block;
            outline: none;
            font-size: 12px;
            background-color: #616161;
            opacity: 0.9;
            color: white;
            padding: 8px;
            border-radius: 2px;
        }
        .hidden {
            display: none !important;
        }
    </style>
    <div id="tooltip" class="hidden"></div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-tooltip',

    hostAttributes: {
        role: 'tooltip',
        tabindex: -1
    },

    behaviors: [NeonAnimationRunnerBehavior],
    
    properties: {

        animationConfig: {
            type: Object,
            value: function () {
                return {
                    'entry': [{
                        name: 'fade-in-animation',
                        node: this
                    }],
                    'exit': [{
                        name: 'fade-out-animation',
                        node: this
                    }]
                }
            }
        },
        
        /**
         * The handle for tooltip timer can be used to cancel tooltip timer. (Tooltip timer - the timer for tooltip to be shown).
         */
        _tooltipTimer: {
            type: Number
        },

        _showing: {
            type: Boolean,
            value: false
        }
    },

    listeners: {
        'neon-animation-finish': '_onAnimationFinish'
    },

    ready: function () {
        this._onCaptureKeyDown = this._onCaptureKeyDown.bind(this);
    },
    
    attached: function () {
        this._hideTooltipOnKeyDown = [];
        document.addEventListener('keydown', this._onCaptureKeyDown, true);
    },
    
    detached: function () {
        document.removeEventListener('keydown', this._onCaptureKeyDown, true);
    },
    
    _onCaptureKeyDown: function () {
        this.hide();
    },

    show: function (text, x, y) {
        if (!this._showing) {
            if (this._tooltipTimer) {
                this.cancelAsync(this._tooltipTimer);
                this._tooltipTimer = null;
            }
            this._tooltipTimer = this.async(function () {
                this.$.tooltip.innerHTML = text;
                this._showAt(x, y);
            }.bind(this), 1000);
        }
    },
    
    _showAt: function (x, y) {
        const w = this._getWindowWidth();
        const h = this._getWindowHeight();

        this.cancelAnimation();
        this.toggleClass('hidden', false, this.$.tooltip);
        this._clearPositionStyles();
        if (x + 100 >= w) {
            this.style.right = w - x + 'px';
        } else {
            this.style.left = x + 'px';
        }
        if (y + 100 >= h) {
            this.style.bottom =  h - y + 14 + 'px';
        } else {
            this.style.top = y + 14 + 'px';
        }
        this._showing = true;

        this.playAnimation('entry');
    },

    hide: function () {
        if (this._tooltipTimer) {
            this.cancelAsync(this._tooltipTimer);
            this._tooltipTimer = null;
        }
        if (this._showing) {
            this._showing = false;
            this.playAnimation('exit');
        }
    },

    _onAnimationFinish: function () {
        if (!this._showing) {
            this.toggleClass('hidden', true, this.$.tooltip);
        }
    },
    
    /**
     * Removes the position style properrties in order to position it correctly.
     */
    _clearPositionStyles: function() {
        this.style.removeProperty('top');
        this.style.removeProperty('left');
        this.style.removeProperty('bottom');
        this.style.removeProperty('right');
    },

    /**
     * Returns the viewable window width.
     */
    _getWindowWidth: function () {
        return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    },

    /**
     * Returns the viewable window height.
     */
    _getWindowHeight: function () {
        return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
    },
});