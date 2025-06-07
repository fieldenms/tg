import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            min-height: 0;
            @apply --layout-vertical;
        }
        #scrollablePanel {
            overflow: auto;
            @apply --tg-scrollable-layout;
        }
        #shadowContainer {
            @apply --layout-fit;
            pointer-events: none;
        }
    </style>
    <div id="scrollablePanel" on-scroll="_contentScrolled" class="webkit-scroll-inertia">
        <slot id="content_selector"></slot>
    </div>
    <div id="shadowContainer"></div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-scrollable-component',

    behaviors: [IronResizableBehavior],
    
    properties: {
        endOfScroll: {
            type: Function
        }
    },

    ready: function () {
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
        //Add layout finished in case if this container may slot the tg-flex-layout component.
        this.addEventListener("layout-finished", this._resizeEventListener.bind(this));
    },

    attached: function () {
        this.async(this._resizeEventListener, 1);
    },

    _resizeEventListener: function (event, details) {
        this._contentScrolled();
    },

    _contentScrolled: function (e) {
        const scrollTarget = this.$.scrollablePanel;
        const shadowTarget = this.$.shadowContainer;
        if (scrollTarget) {
            let shadowStyle = "";
            if (scrollTarget.scrollTop) {
                shadowStyle += "inset 0 6px 6px -6px rgba(0,0,0,0.7)";
            }
            if (Math.ceil(scrollTarget.scrollTop + scrollTarget.offsetHeight) < scrollTarget.scrollHeight) {
                shadowStyle += (shadowStyle ? ", " : "") + "inset 0 -6px 6px -6px rgba(0,0,0,0.7)";
            } else if (e && this.endOfScroll) {
                this.endOfScroll(e);
            }
            if (shadowStyle) {
                shadowTarget.style.boxShadow = shadowStyle;
            } else {
                shadowTarget.style.removeProperty('box-shadow');
            }
        }
    }
});