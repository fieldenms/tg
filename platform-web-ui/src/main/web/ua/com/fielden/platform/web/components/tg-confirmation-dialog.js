import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
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
            .confirm-dialog paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            .confirm-dialog paper-button:hover {
                background: var(--paper-light-blue-50);
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
            <paper-dialog-scrollable>
                <p id="msgPar" style="padding: 10px;white-space: break-spaces;"></p>
            </paper-dialog-scrollable>
            <div class="buttons">
                <template is="dom-repeat" items="[[buttons]]">
                    <paper-button dialog-confirm$="[[item.confirm]]" dialog-dismiss$="[[!item.confirm]]" autofocus$="[[item.autofocus]]" on-tap="_action">[[item.name]]</paper-button>
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

Polymer({
    _template: html``,

    is: 'tg-confirmation-dialog',

    behaviors: [TgFocusRestorationBehavior],

    showConfirmationDialog: function (message, buttons) {
        this.persistActiveElement();
        if (this._lastPromise) {
            this._lastPromise = this._lastPromise.then(
                value => this._showConfirmationDialog(message, buttons),
                reason => this._showConfirmationDialog(message, buttons));
        } else {
            this._lastPromise = this._showConfirmationDialog(message, buttons);
        }
        return this._lastPromise;
    },

    _showConfirmationDialog: function (message, buttons) {
        const self = this;
        const restoreActiveElement = function () {
            self.async(function () {
                self.restoreActiveElement();
            }, 1);
        };
        return new Promise(function (resolve, reject) {

            dialogModel._onCaptureKeyDown = function (e) {
                // ensures on-Enter closing even if no button is focused, i.e. tapped on dialog somewhere or even outside dialog
                if (e.keyCode === 13) {
                    dialogModel.$.confirmDialog.close();
                    resolve("ENTER");
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
                    resolve(button.name);
                } else {
                    reject(new ExpectedError(button.name));
                }
                restoreActiveElement();
            };

            if (containsRestrictedTags(message) === true) {
                dialogModel.$.msgPar.textContent = message;
            } else {
                dialogModel.$.msgPar.innerHTML = message;
            }
            dialogModel.buttons = buttons;
            dialogModel.showDialog();
        });
    }
});