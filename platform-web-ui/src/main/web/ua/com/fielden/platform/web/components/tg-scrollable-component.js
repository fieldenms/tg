import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/app/tg-app-config.js'

import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';
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
        .webkit-scroll-inertia {
            -webkit-overflow-scrolling: touch;
        }
        #shadowContainer {
            @apply --layout-fit;
            pointer-events: none;
        }
    </style>
    <div id="scrollablePanel" on-scroll="_contentScrolled" class="webkit-scroll-inertia">
        <slot id="content_selector"></slot>
    </div>
    <div id="shadowContainer"></div>
    <tg-app-config id="appConfig"></tg-app-config>`;

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

        if (this.$.appConfig.mobile === true && this.$.appConfig.iPhoneOs()) { // TODO perhaps MacOs webkit browsers are also affected, then it needs to be fixed here too
            // In webkit-based browsers we use '-webkit-overflow-scrolling: touch' css fix to enable scroll inertia (all other browsers implement that natively).
            // However, this causes completely broken scrolling in cases where inner content changes its sizes.
            // Specifically the size change should be following: at the beginng the content becomes small and non-scrollable and then, again, big and scrollable.
            // See this post http://patrickmuff.ch/blog/2014/10/01/how-we-fixed-the-webkit-overflow-scrolling-touch-bug-on-ios/ and its comments for more information.
            // Also this post http://jstn.name/bug-fix-for-overflow-scrolling-on-orientation-change/ helps too.
            this.$.scrollablePanel.classList.remove('webkit-scroll-inertia');
            this.async(function () {
                this.$.scrollablePanel.classList.add('webkit-scroll-inertia');
            }.bind(this), 200); // need magic async and wait at least 200 ms (this works on iOs 11.3 Safari 11.0)
        }
    },

    _contentScrolled: function (e) {
        const scrollTarget = this.$.scrollablePanel;
        const shadowTarget = this.$.shadowContainer;
        if (scrollTarget) {
            let shadowStyle = "";
            if (scrollTarget.scrollTop) {
                shadowStyle += "inset 0 3px 6px -2px rgba(0,0,0,0.7)";
            }
            if (Math.ceil(scrollTarget.scrollTop + scrollTarget.offsetHeight) < scrollTarget.scrollHeight) {
                shadowStyle += (shadowStyle ? ", " : "") + "inset 0 -3px 6px -2px rgba(0,0,0,0.7)";
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