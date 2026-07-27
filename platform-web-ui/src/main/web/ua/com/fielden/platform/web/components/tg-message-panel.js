import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

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
        .reload-button {
            height: 28px;
            padding: 2px 8px;
            margin: 0 4px;
            color: var(--paper-grey-700);
            font-weight: 500;
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
    <div class="mesage-panel">[[messageText]]</div>
    <paper-button hidden="[[!showReload]]" class="reload-button" on-tap="_reloadPage">Reload</paper-button>
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
        // Indicates whether the user has dismissed the current message.
        closed: {
            type: Boolean,
            value: false
        },

        // The text displayed by the panel; while empty the panel stays hidden.
        messageText: {
            type: String,
            value: ""
        },

        // Whether the Reload button is displayed; enabled for the application-update message.
        showReload: {
            type: Boolean,
            value: false
        }
    },

    observers: ["_updateVisibility(messageText, closed)"],

    ready: function () {
        // Warn when the browser is not one of the recommended clients.
        const isRecommendedClient = isMobile.any() || isDesktop.isSafari() || isDesktop.isChrome() || isDesktop.isFirefox();
        if (!isRecommendedClient) {
            if (isDesktop.isIE()) {
                this.messageText = "Application cannot be opened in Internet Explorer. A Chromium based browser is recommended.";
                this.style.backgroundColor = "#FF8A80";
            } else {
                this.messageText = "Chrome is highly recommended for this application.";
                this.style.backgroundColor = "#FFFF8D";
            }
        }
    },

    /// Displays a dismissible yellow banner prompting the user to reload after a newer application version has been deployed.
    ///
    showUpdateMessage: function (version) {
        this.messageText = `A new application version (${version}) is available. Please reload to update.`;
        // Yellow warning colour, matching the browser-recommendation warning.
        this.style.backgroundColor = "#FFFF8D";
        this.showReload = true;
        this.closed = false;
    },

    _reloadPage: function (e) {
        window.location.reload();
    },

    _closeMessage: function (e) {
        this.closed = true;
    },

    _updateVisibility: function (messageText, closed) {
        if (!messageText || closed) {
            this.setAttribute("hidden", true);
        } else {
            this.removeAttribute("hidden");
        }
    }

});