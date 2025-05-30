import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-dialog-scrollable/paper-dialog-scrollable.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-repeat.js';

import { containsRestrictedTags } from '/resources/reflection/tg-polymer-utils.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { ExpectedError } from '/resources/components/tg-global-error-handler.js';

const confirmationDialogStyle = html`
    <custom-style>
        <style>
            .title {
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
            .confirm-dialog paper-button.red {
                color: var(--google-red-500);
                --paper-button-flat-focus-color: var(--google-red-100);
            }
            .confirm-dialog paper-button.red:hover {
                background: var(--google-red-100);
            }
            paper-dialog-scrollable {
                @apply --layout-vertical;
            }
            #opts {
                @apply --layout-vertical;
            }
            paper-checkbox {
                margin-bottom: 15px;
                --paper-checkbox-checked-color: var(--paper-light-blue-700);
                --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
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
                <div id="msgPar"></div>
                <div id="opts" style="padding-top: 15px;" hidden$="[[!options]]">
                    <template is="dom-repeat" items="[[options]]">
                        <paper-checkbox checked="[[item.checked]]" on-change="_optionChanged">[[item.msg]]</paper-checkbox>
                    </template>
                </div>
            </paper-dialog-scrollable>
            <div class="buttons">
                <template is="dom-repeat" items="[[buttons]]">
                    <paper-button class$="[[item.classes]] "style$="[[item.style]]"dialog-confirm$="[[item.confirm]]" dialog-dismiss$="[[!item.confirm]]" autofocus$="[[item.autofocus]]" on-tap="_action">[[item.name]]</paper-button>
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

dialogModel.dialogOpened = function (e) {
    document.addEventListener('keydown', this._onCaptureKeyDown, true);
}

dialogModel.dialogClosed = function (e) {
    document.removeEventListener('keydown', this._onCaptureKeyDown, true);
}

document.body.appendChild(dialogModel);

export const TgConfirmationDialog = Polymer({
    is: 'tg-confirmation-dialog',

    behaviors: [TgFocusRestorationBehavior],

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

            dialogModel._onCaptureKeyDown = function (e) {
                // ensures on-Enter closing even if no button is focused, i.e. tapped on dialog somewhere or even outside dialog
                if (e.keyCode === 13) {
                    dialogModel.$.confirmDialog.close();
                    resolve(dialogModel.options ? getOptions(dialogModel.options): "ENTER");
                    restoreActiveElement();
                }
            }

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
             */
            dialogModel.$.confirmDialog.closeDialog = function () {
                this.close();
                dialogModel.rejectDialog();
            };

            dialogModel._action = function (e) {
                const button = e.model.item;
                if (button.confirm) {
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

            if (options) {
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
            } else {
                dialogModel.options = null;
            }

            dialogModel.showDialog();
        });
    }
});