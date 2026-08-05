import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';

/// The name of the window event that requests a message to be displayed in the application message panel.
/// The application shell (see `tg-app-template.js`) listens for this event.
///
export const SHOW_APP_MESSAGE_EVENT = 'tg-show-app-message';

/// Requests `message` to be displayed in the application message panel.
/// This works from anywhere in the application, with no need for the caller to have access to the panel.
/// See `showMessage` for the supported shape of `message`.
///
export const showAppMessage = function (message) {
    window.dispatchEvent(new CustomEvent(SHOW_APP_MESSAGE_EVENT, { detail: message }));
};

const template = html`
    <style>
        :host {
            padding: 8px;
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-justified;
        }
        .mesage-panel {
            font-size: 16px;
            color: var(--paper-grey-600);
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-centre-justified;
            @apply --layout-flex;
        }
        /* Default appearance of an actionable element in a message, marked with class "action". */
        /* A message may override this with inline styles. */
        .mesage-panel .action {
            height: 22px;
            color: var(--paper-grey-600);
        }
        .close-button {
            width: 22px;
            height: 22px;
            padding: 0px;
            color: var(--paper-grey-600);
        }
        .close-button[disabled] {
            color: var(--paper-grey-400);
        }
        .close-button:hover {
            color: var(--paper-grey-400);
        }
    </style>
    <div id="messageContainer" class="mesage-panel" on-tap="_handleMessageTap"></div>
    <paper-icon-button class="close-button" icon="icons:cancel" on-tap="_closeMessage" tooltip-text="Close Message"></paper-icon-button>`;

template.setAttribute('strip-whitespace', '');

const isMobile = {
    Windows: function() {
        return /IEMobile/i.test(navigator.userAgent);
    },
    Android: function() {
        return /Android/i.test(navigator.userAgent);
    },
    BlackBerry: function() {
        return /BlackBerry/i.test(navigator.userAgent);
    },
    iOS: function() {
        return /iPhone|iPad|iPod/i.test(navigator.userAgent);
    },
    any: function() {
        return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Windows());
    }
};

const isDesktop = {
    // Opera 8.0+
    isOpera: function () {
        return (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
    },

    // Firefox 1.0+
    isFirefox: function () {
        return typeof InstallTrigger !== 'undefined';
    },

    // Safari 3.0+ "[object HTMLElementConstructor]"
    isSafari: function () {
        return /constructor/i.test(window.HTMLElement) || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && safari.pushNotification));
    },

    // Internet Explorer 6-11
    isIE: function () {
        return /*@cc_on!@*/false || !!document.documentMode;
    },

    // Edge 20+
    isEdge: function () {
        return !isIE && !!window.StyleMedia;
    },

    // Chrome 1+
    isChrome: function () {
        return !!window.chrome;
    },

    // Blink engine detection
    isBlink: function () {
        return (isChrome || isOpera) && !!window.CSS;
    },

    any: function() {
        return (isDesktop.isOpera() || isDesktop.isFirefox() || isDesktop.isSafari() || isDesktop.isIE() || isDesktop.isEdge() || isDesktop.isChrome() || isDesktop.isBlink());
    }
}


Polymer({
    
    _template: template,

    is: "tg-message-panel",

    properties: {
        /// Indicates whether the user has dismissed the current message.
        ///
        closed: {
            type: Boolean,
            value: false
        },

        /// The message displayed by the panel, which may contain HTML markup.
        /// While empty the panel stays hidden.
        ///
        messageText: {
            type: String,
            value: '',
            observer: '_messageTextChanged'
        },

        /// The tooltip for the panel as a whole.
        /// Elements inside a message may carry their own `tooltip-text`, which takes precedence over this one.
        ///
        tooltipText: {
            type: String,
            value: '',
            observer: '_tooltipTextChanged'
        },

        /// Maps the `data-tap` identifiers, used in the current message, to their handler functions.
        ///
        _messageHandlers: {
            type: Object,
            value: () => ({})
        }
    },

    observers: ["_updateVisibility(messageText, closed)"],

    /// Makes tooltips work for this panel in its own right, including on pages such as `login.html`.
    /// Such pages may have no enclosing component to provide tooltip support.
    ///
    behaviors: [ TgTooltipBehavior ],

    ready: function () {
        // Warn when the browser is not one of the recommended clients.
        const isRecommendedClient = isMobile.any() || isDesktop.isSafari() || isDesktop.isChrome() || isDesktop.isFirefox();
        if (!isRecommendedClient) {
            if (isDesktop.isIE()) {
                this.showMessage({
                    text: "Application cannot be opened in Internet Explorer. A Chromium based browser is recommended.",
                    backgroundColor: "#FF8A80"
                });
            } else {
                this.showMessage({
                    text: "Chrome is highly recommended for this application.",
                    backgroundColor: "#FFFF8D"
                });
            }
        }
    },

    /// Displays `message` in this panel, replacing whatever was displayed before, and undoes any previous dismissal.
    ///
    /// `message.text` is the message itself and may contain HTML markup, including inline styles.
    /// An element in `message.text` becomes actionable by carrying a `data-tap` attribute.
    /// Its value identifies the handler function in `message.handlers`.
    /// Such an element may also carry its own `tooltip-text` attribute.
    ///
    /// `message.tooltip` is the tooltip for the panel as a whole.
    /// `message.backgroundColor` is the background colour of the panel, which conveys the severity of the message.
    ///
    showMessage: function (message) {
        const { text = '', tooltip = '', backgroundColor = '', handlers = {} } = message || {};
        this._messageHandlers = handlers;
        this.tooltipText = tooltip;
        this.style.backgroundColor = backgroundColor;
        this.messageText = text;
        this.closed = false;
    },

    /// Invokes the handler for the tapped element, identified by its `data-tap` attribute, if there is such a handler.
    ///
    _handleMessageTap: function (e) {
        const path = e.composedPath();
        // Only the part of the path inside the message is of interest, hence the search stops at the message container.
        const containerIdx = path.indexOf(this.$.messageContainer);
        const messagePath = containerIdx >= 0 ? path.slice(0, containerIdx) : path;
        const actionElement = messagePath.find(node => node.nodeType === Node.ELEMENT_NODE && node.hasAttribute("data-tap"));
        if (actionElement) {
            const handler = this._messageHandlers[actionElement.getAttribute("data-tap")];
            if (handler) {
                handler(e);
            }
        }
    },

    _closeMessage: function (e) {
        this.closed = true;
    },

    _messageTextChanged: function (messageText) {
        // Guarded because property defaults are applied before the local DOM gets stamped.
        if (this.$ && this.$.messageContainer) {
            this.$.messageContainer.innerHTML = messageText || "";
        }
    },

    _tooltipTextChanged: function (tooltipText) {
        if (tooltipText) {
            this.setAttribute("tooltip-text", tooltipText);
        } else {
            this.removeAttribute("tooltip-text");
        }
    },

    _updateVisibility: function (messageText, closed) {
        if (!messageText || closed) {
            this.setAttribute("hidden", true);
        } else {
            this.removeAttribute("hidden");
        }
    }

});