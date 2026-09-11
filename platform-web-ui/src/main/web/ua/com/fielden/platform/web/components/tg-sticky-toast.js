import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

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
            /* The message itself carries the emphasis, so that it stands out from the detail below it. */
            /* Roboto is loaded with weights 300, 400, 500 and 700 only, hence 500 rather than an intermediate weight. */
            .sticky-toast-text {
                font-weight: 500;
                color: white;
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
            /* An action is expected to be a paper-button, whose labels are uppercased by default, as was the norm before Material Design 3. */
            .sticky-toast .action {
                margin-left: 8px;
                color: var(--paper-light-blue-500);
                font-weight: 500;
                text-transform: none;
                cursor: pointer;
            }
            .sticky-toast a {
                color: var(--paper-light-blue-500);
            }
        </style>
    </custom-style>`;
stickyToastStyle.setAttribute('style', 'display: none;');
document.head.appendChild(stickyToastStyle.content);

const template = html`
    <tg-paper-toast id="stickyToast" class="sticky-toast" allow-click-through always-on-top duration="0">
        <div id="messageContainer" on-tap="_handleMessageTap">
            <div id="messageText" class="sticky-toast-text"></div>
            <div id="messageDetail" class="sticky-toast-detail"></div>
            <div id="messageActions" class="sticky-toast-actions"></div>
        </div>
    </tg-paper-toast>`;

/// Brings `message` to the shape used internally, applying defaults.
///
/// The identity of a message defaults to its text.
/// This is what makes a condition that gets reported repeatedly, such as a layout reset upon opening each of several affected centres, a single message.
///
const normaliseMessage = function (message) {
    const { id, text = '', detail = '', actions = '', handlers = {} } = message || {};
    return { id: id || text, text, detail, actions, handlers };
};

/// A toast that stays visible until it gets dismissed, intended for messages that a user must not miss.
///
/// Unlike [tg-toast], which is transient and shares a single slot with all other transient messages, this toast has a slot of its own.
/// It therefore never gets overridden by, and never overrides, transient messages.
/// It is placed at the bottom of the shared toast container, so that all transient toasts are shifted above it.
///
/// There is a single sticky toast per application, so its own messages stack in it rather than replace one another.
/// The most recent one is displayed, and dismissing it reveals the one beneath, so that no message is lost.
/// This matters most right after a deployment, when an update prompt and a layout reset notice arise together.
///
/// The most recent message is displayed first because it is the one that explains what has just happened, such as a layout reset upon opening a centre.
/// An earlier message, such as a prompt to reload, remains just as relevant however long it waits beneath.
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
            },

            /// Messages that have not been dismissed yet, the most recent first.
            /// The first of them is the one being displayed.
            ///
            _stack: {
                type: Array,
                value: () => []
            }
        };
    }

    ready() {
        super.ready();
        // The refit function of paper-toast behaves erratically, hence it is disabled, as in other TG toasts.
        this.$.stickyToast.refit = function () {};
    }

    /// Displays `message`, keeping whatever was displayed before to be revealed once `message` gets dismissed.
    ///
    /// `message.text` is the message itself and may contain HTML markup, including inline styles and links.
    /// `message.detail` is an optional less emphasised second row, also supporting markup.
    /// `message.actions` is an optional row of actionable elements, displayed at the end of the message.
    ///
    /// An element in any of the above becomes actionable by carrying a `data-tap` attribute.
    /// Its value identifies the handler function in `message.handlers`.
    ///
    /// `message.id` identifies the message, defaulting to its text.
    /// A message that is already displayed, or that is still waiting beneath, gets ignored.
    ///
    showMessage (message) {
        const msg = normaliseMessage(message);
        if (this._stack.some(pending => pending.id === msg.id)) {
            return;
        }
        this._stack.unshift(msg);
        this._display(msg);
    }

    /// Dismisses the message being displayed, and displays the one beneath it, if any.
    ///
    dismiss () {
        this._stack.shift();
        if (this._stack.length > 0) {
            this._display(this._stack[0]);
        } else {
            this._hide();
        }
    }

    /// Renders `msg` and makes this toast visible.
    ///
    _display (msg) {
        this._messageHandlers = msg.handlers;
        this.$.messageText.innerHTML = msg.text;
        this.$.messageDetail.innerHTML = msg.detail;
        this.$.messageActions.innerHTML = msg.actions;
        this.$.messageDetail.hidden = !msg.detail;
        this.$.messageActions.hidden = !msg.actions;
        this.show();
    }

    /// Clears the message, so that neither its DOM nor the closures of its handlers stay reachable once it has been dismissed.
    ///
    _clear () {
        this._messageHandlers = {};
        this.$.messageText.textContent = '';
        this.$.messageDetail.textContent = '';
        this.$.messageActions.textContent = '';
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

    /// Closes this toast, discarding the message being displayed along with any beneath it.
    /// An action that dismisses a single message should invoke `dismiss` instead.
    ///
    _hide () {
        this._stack = [];
        this.$.stickyToast.close();
        this._clear();
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

/// A single sticky toast for the whole application.
/// It is created similarly as it is done for tg-delayed-action-toast.
///
const stickyToastElement = document.createElement('tg-sticky-toast');
document.body.appendChild(stickyToastElement);

/// Displays `message` in the application sticky toast, above any message that has not been dismissed yet.
/// This works from anywhere in the application, with no need for the caller to have access to the toast.
/// See `showMessage` of `tg-sticky-toast` for the supported shape of `message`.
///
export const showStickyToast = function (message) {
    stickyToastElement.showMessage(message);
};

/// Dismisses the message being displayed in the application sticky toast, revealing the one beneath it, if any.
/// This is what an action that closes a message should invoke.
///
export const hideStickyToast = function () {
    stickyToastElement.dismiss();
};
