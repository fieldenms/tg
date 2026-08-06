import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/components/tg-paper-toast.js';

import { TgToastBehavior } from '/resources/components/tg-toast-behavior.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

/// Styles for the sticky toast are defined at the document level rather than in the component.
/// This is because the toast element gets relocated into the shared toast container, which resides in the document body.
/// Once relocated, the styles of this component no longer apply to it.
///
/// Backticks must not be used in this style sheet as they would terminate the enclosing template literal.
///
const stickyToastStyle = html`
    <custom-style>
        <style>
            tg-paper-toast.sticky-toast {
                max-width: 420px;
            }
            .sticky-toast-detail {
                margin-top: 2px;
                font-size: 12px;
                color: var(--paper-grey-400);
            }
            .sticky-toast-actions {
                margin-top: 8px;
                @apply --layout-horizontal;
                @apply --layout-center;
                @apply --layout-end-justified;
            }
            /* The layout mixin above assigns a display value, which would otherwise defeat the hidden attribute. */
            .sticky-toast-actions[hidden] {
                display: none;
            }
            /* Default appearance of an actionable element in a message, marked with class "action". */
            /* A message may override this with inline styles. */
            .sticky-toast .action {
                margin-left: 8px;
                color: #03A9F4;
                font-weight: 500;
                cursor: pointer;
            }
            .sticky-toast a {
                color: #03A9F4;
            }
        </style>
    </custom-style>`;
stickyToastStyle.setAttribute('style', 'display: none;');
document.head.appendChild(stickyToastStyle.content);

const template = html`
    <tg-paper-toast id="stickyToast" class="sticky-toast" allow-click-through always-on-top duration="0">
        <div id="messageContainer" on-tap="_handleMessageTap">
            <div id="messageText"></div>
            <div id="messageDetail" class="sticky-toast-detail"></div>
            <div id="messageActions" class="sticky-toast-actions"></div>
        </div>
    </tg-paper-toast>`;

/// A toast that stays visible until it gets dismissed, intended for messages that a user must not miss.
///
/// Unlike [tg-toast], which is transient and shares a single slot with all other transient messages, this toast has a slot of its own.
/// It therefore never gets overridden by, and never overrides, other messages.
/// It is placed at the bottom of the shared toast container, so that all transient toasts are shifted above it.
///
class TgStickyToast extends mixinBehaviors([TgToastBehavior], PolymerElement) {

    static get template() {
        return template;
    }

    static get properties() {
        return {
            /// Maps the `data-tap` identifiers, used in the current message, to their handler functions.
            ///
            _messageHandlers: {
                type: Object,
                value: () => ({})
            }
        };
    }

    ready() {
        super.ready();
        // The refit function of paper-toast behaves erratically, hence it is disabled, as in other TG toasts.
        this.$.stickyToast.refit = function () {};
    }

    get opened() {
        return this.$.stickyToast.opened;
    }

    /// Displays `message`, replacing whatever was displayed before.
    ///
    /// `message.text` is the message itself and may contain HTML markup, including inline styles and links.
    /// `message.detail` is an optional less emphasised second row, also supporting markup.
    /// `message.actions` is an optional row of actionable elements, displayed at the end of the message.
    ///
    /// An element in any of the above becomes actionable by carrying a `data-tap` attribute.
    /// Its value identifies the handler function in `message.handlers`.
    ///
    showMessage (message) {
        const { text = '', detail = '', actions = '', handlers = {} } = message || {};
        this._messageHandlers = handlers;
        this.$.messageText.innerHTML = text;
        this.$.messageDetail.innerHTML = detail;
        this.$.messageActions.innerHTML = actions;
        this.$.messageDetail.hidden = !detail;
        this.$.messageActions.hidden = !actions;
        this.show();
    }

    /// Makes this toast visible, relocating it into the shared toast container if needed.
    ///
    /// The toast is appended rather than prepended, which is what other TG toasts do.
    /// This keeps it at the bottom of the container, so that all transient toasts are shifted above it.
    ///
    show () {
        if (!this.getDocumentToast('stickyToast')) {
            this.getToastContainer().appendChild(this.$.stickyToast);
        }
        this.$.stickyToast.open();
    }

    hide () {
        this.$.stickyToast.close();
    }

    /// Invokes the handler for the tapped element, identified by its `data-tap` attribute, if there is such a handler.
    ///
    _handleMessageTap (e) {
        const path = e.composedPath();
        // Only the part of the path inside the message is of interest, hence the search stops at the message container.
        const containerIdx = path.indexOf(this.$.messageContainer);
        const messagePath = containerIdx >= 0 ? path.slice(0, containerIdx) : path;
        const actionElement = messagePath.find(node => node.nodeType === Node.ELEMENT_NODE && node.hasAttribute('data-tap'));
        if (actionElement) {
            const handler = this._messageHandlers[actionElement.getAttribute('data-tap')];
            if (handler) {
                handler(e);
            }
        }
    }

    _toast () {
        return this.$.stickyToast;
    }

}

customElements.define('tg-sticky-toast', TgStickyToast);

// A single sticky toast is created for the whole application, as is done for tg-delayed-action-toast.
const stickyToastElement = document.createElement('tg-sticky-toast');
document.body.appendChild(stickyToastElement);

/// Displays `message` in the application sticky toast.
/// This works from anywhere in the application, with no need for the caller to have access to the toast.
/// See `showMessage` of `tg-sticky-toast` for the supported shape of `message`.
///
export const showStickyToast = function (message) {
    stickyToastElement.showMessage(message);
};

/// Dismisses the application sticky toast.
///
export const hideStickyToast = function () {
    stickyToastElement.hide();
};
