import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-dialog-scrollable/paper-dialog-scrollable.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-repeat.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import { containsRestrictedTags } from '/resources/reflection/tg-polymer-utils.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { ExpectedError } from '/resources/components/tg-global-error-handler.js';

const confirmationDialogStyle = html`
    <custom-style>
        <style>
            .confirm-dialog .title {
                font-size: 1.5rem;
                font-weight: bold;
                padding: 0 34px;
            }
            .confirm-dialog paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            .confirm-dialog paper-button:hover {
                background: var(--paper-light-blue-50);
            }
            .confirm-dialog paper-button[disabled] {
                color: var(--paper-grey-500);
            }
            .confirm-dialog paper-button.red {
                color: var(--google-red-500);
                --paper-button-flat-focus-color: var(--google-red-100);
            }
            .confirm-dialog paper-button.red:hover {
                background: var(--google-red-100);
            }
            .confirm-dialog paper-dialog-scrollable {
                @apply --layout-vertical;
            }
            .confirm-dialog #opts {
                @apply --layout-vertical;
            }
            .confirm-dialog paper-checkbox {
                margin-bottom: 15px;
                --paper-checkbox-checked-color: var(--paper-light-blue-700);
                --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
            }
            .confirm-dialog #spinner {
                position: absolute;
                top: 50%;/*position Y halfway in*/
                left: 50%;/*position X halfway in*/
                transform: translate(-50%,-50%);/*move it halfway back(x,y)*/
                padding: 2px;
                margin: 0px;
                width: 24px;
                height: 24px;
                --paper-spinner-layer-1-color: var(--paper-blue-500);
                --paper-spinner-layer-2-color: var(--paper-blue-500);
                --paper-spinner-layer-3-color: var(--paper-blue-500);
                --paper-spinner-layer-4-color: var(--paper-blue-500);
            }
        </style>
    </custom-style>`;
confirmationDialogStyle.setAttribute('style', 'display: none;');
document.head.appendChild(confirmationDialogStyle.content);

const dialogModel = document.createElement('dom-bind');
dialogModel.innerHTML = `
    <template>
        <paper-dialog id="confirmDialog" class="confirm-dialog layout vertical"
            modal
            always-on-top
            entry-animation="scale-up-animation"
            exit-animation="fade-out-animation"
            on-iron-overlay-canceled="rejectDialog"
            on-iron-overlay-opened="dialogOpened"
            on-iron-overlay-closed="dialogClosed">
            <div class="title" hidden$="[[!title]]">[[title]]</div>
            <paper-dialog-scrollable style="padding: 10px;">
                <div id="msgPar" style="white-space:break-spaces"></div>
                <div id="opts" style="padding-top: 15px;" hidden$="[[!options]]">
                    <template is="dom-repeat" items="[[options]]">
                        <paper-checkbox checked="[[item.checked]]" on-change="_optionChanged">[[item.msg]]</paper-checkbox>
                    </template>
                </div>
            </paper-dialog-scrollable>
            <div class="buttons">
                <template is="dom-repeat" items="[[buttons]]">
                    <paper-button class$="[[item.classes]]" disabled$="[[spinnerActive]]" style$="[[item.style]]" dialog-confirm$="[[_dialogConfirm(item.confirm, withProgress)]]" dialog-dismiss$="[[!item.confirm]]" autofocus$="[[item.autofocus]]" on-tap="_action">
                        <span>[[item.name]]</span>
                        <paper-spinner id="spinner" hidden$="[[_spinnerHidden(item.confirm, spinnerActive)]]" active="[[spinnerActive]]" alt="in progress"></paper-spinner>
                    </paper-button>
                </template>
            </div>
        </paper-dialog>
    </template>
`;

dialogModel.showDialog = function () {
    const dialog = dialogModel.$.confirmDialog;

    dialog.noCancelOnEscKey = false;

    dialog.async(function () {
        dialog.open();
    }, 1);
}

document.body.appendChild(dialogModel);

export const TgConfirmationDialog = Polymer({
    is: 'tg-confirmation-dialog',

    behaviors: [TgFocusRestorationBehavior],

    /**
     * Displays a confirmation dialog with the specified message and buttons.
     * Optionally, it can include a list of checkbox options that the user can select.
     * The selected options are returned when the promise is resolved.
     *
     * @param {String} message - The message displayed in the confirmation dialog.
     *
     * @param {Array<Object>} buttons - The list of buttons displayed in the dialog.
     * Each button is represented as an object with the following properties:
     *   `name` {String}      - The button label.
     *   [`confirm`] {Boolean}  - If true, resolves the returned promise; otherwise, rejects it.
     *   [`autofocus`] {Boolean}- If true, the button receives focus by default.
     *   [`style`] {String}     - Inline CSS styles applied to the button.
     *   [`classes`] {String}   - Comma-separated list of CSS classes applied to the button.
     *
     * Example:
     * [
     *   { name: 'Cancel' },
     *   { name: 'Ok', confirm: true, autofocus: true, style: 'background-color: green' }
     * ]
     *
     * @param {Object} [options] - Optional configuration object. May contain:
     *   `withProgress` {Boolean} - Indicates whether the dialog displays a progress indicator (spinner).
     *                              This property is mutually exclusive with `options`.
     *   `options` {Array<String>}- List of checkbox labels displayed below the message.
     *   `single` {Boolean}       - If true, only one checkbox can be selected at a time.
     *                              Has effect only if `options` is provided.
     *
     * Example:
     * { single: true, options: ["Don't show this again for this link", "Don't show this again for this site"] }
     *
     * or:
     * { withProgress: true }
     *
     * @param {String} [title] - The confirmation dialog title.
     *
     * @returns {Promise<Object|ExpectedError>}
     * A promise that is resolved or rejected depending on the button pressed.
     * If `options.options` is provided, the resolved value contains an object
     * where each key corresponds to a checkbox label and the value indicates
     * whether the checkbox was selected.
     *
     * Example resolved value:
     * {
     *   "Don't show this again for this link": true,
     *   "Don't show this again for this site": false
     * }
     * 
     * If the promise is rejected, the rejection reason is an `ExpectedError`
     * containing a string that indicates how the promise was rejected
     * (e.g., by button action or by pressing the ESC key).
     */
    showConfirmationDialog: function (message, buttons, options, title) {
        this.persistActiveElement();
        if (this._lastPromise) {
            this._lastPromise = this._lastPromise.then(
                value => this._showConfirmationDialog(message, buttons, options, title),
                reason => this._showConfirmationDialog(message, buttons, options, title));
        } else {
            this._lastPromise = this._showConfirmationDialog(message, buttons, options, title);
        }
        return this._lastPromise;
    },

    _showConfirmationDialog: function (message, buttons, options, title) {
        const self = this;
        const restoreActiveElement = function () {
            self.async(function () {
                self.restoreActiveElement();
            }, 1);
        };
        const getOptions = function (opts) {
            const obj = {};
            opts.forEach(opt => obj[opt.msg] = opt.checked);
            return obj;
        }
        return new Promise(function (resolve, reject) {

            /**
             * Rejects confirmation dialog promise and restores previously active element.
             */
            dialogModel.rejectDialog = function (e) {
                reject(new ExpectedError("ESC"));
                restoreActiveElement();
            };

            /**
             * Custom method for confirmation dialog closing.
             * It closes corresponding <paper-dialog>, but also rejects confirmation dialog promise and restores previously active element,
             *  as if ESC or non-confirming button was pressed.
             * 
             * This custom logic is necessary in tg-app-template _closeDialog logic, where BACK button must reject confirmation dialog (mobile profile only).
             * Otherwise, _lastPromise will be forever pending and all other confirmation dialogs would never open.
             * 
             * Returns a resolved promise because the dialog is always closed.
             */
            dialogModel.$.confirmDialog.closeDialog = function () {
                this.close();
                dialogModel.rejectDialog();
                return Promise.resolve(true);
            };

            dialogModel._action = function (e) {
                const button = e.model.item;
                if (button.confirm) {
                    // If confirmation dialog has `withProgress` option...
                    if (dialogModel.withProgress) {
                        // Turn on spinner (and disable buttons).
                        dialogModel.spinnerActive = true;
                        // Disallow cancelling using ESC key.
                        dialogModel.$.confirmDialog.noCancelOnEscKey = true;
                    }
                    resolve(dialogModel.options ? getOptions(dialogModel.options) : button.name);
                } else {
                    reject(new ExpectedError(button.name));
                }
                restoreActiveElement();
            };

            dialogModel.title = title;

            if (containsRestrictedTags(message) === true) {
                dialogModel.$.msgPar.textContent = message;
            } else {
                dialogModel.$.msgPar.innerHTML = message;
            }
            
            dialogModel.buttons = buttons;

            // Only mark buttons with `dialog-confirm` (which closes dialog) if not configured with `withProgress` option.
            dialogModel._dialogConfirm = (confirm, withProgress) => confirm && !withProgress;
            // Hide (and stop) spinner for all non-confirming buttons and for confirming ones if they are not currently in progress.
            dialogModel._spinnerHidden = (confirm, spinnerActive) => !confirm || !spinnerActive;

            // Dialog's `withProgress` is false by default.
            dialogModel.withProgress = false;
            if (options) {
                if (options.withProgress) {
                    // Propagate `withProgress` from `options` to `dialog`.
                    dialogModel.withProgress = options.withProgress;
                    dialogModel.options = null;
                }
                // If boolean `options` are present, configure them in `dialog`.
                else if (options.options) {
                    dialogModel.options = options.options.map(option => {
                        return {checked: false, msg: option};
                    });
                    dialogModel._optionChanged = function (e) {
                        const index = e.model.index;
                        const target = e.target || e.srcElement;
                        dialogModel.set(`options.${index}.checked`, target.checked);
                        if (options.single && target.checked) {
                            dialogModel.options.forEach((opt, idx) => {
                                if (idx !== index) {
                                    dialogModel.set(`options.${idx}.checked`, false);
                                }
                            });
                        }
                    }.bind(dialogModel);
                }
            } else {
                dialogModel.options = null;
            }

            dialogModel.showDialog();
        });
    },

    /// Manually closes confirmation dialog.
    /// Resets progress indicator (spinner) and other `withProgress` configuration.
    ///
    close: function () {
        dialogModel.$.confirmDialog.close();
        dialogModel.spinnerActive = false;
        dialogModel.$.confirmDialog.noCancelOnEscKey = false;
    }

});