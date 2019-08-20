import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-repeat.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';

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
dialogModel.innerHTML = '<template><paper-dialog id="confirmDialog" class="confirm-dialog layout vertical" modal always-on-top entry-animation="scale-up-animation" exit-animation="fade-out-animation" on-iron-overlay-canceled="dialogCanceled" on-iron-overlay-opened="dialogOpened" on-iron-overlay-closed="dialogClosed">' +
    '<div style="padding: 20px;" inner-h-t-m-l="[[message]]"></div>' +
    '<div class="buttons">' +
    '<template is="dom-repeat" items="[[buttons]]">' +
    '<paper-button dialog-confirm$="[[item.confirm]]" dialog-dismiss$="[[!item.confirm]]" autofocus$="[[item.autofocus]]" on-tap="_action">[[item.name]]</paper-button>' +
    '</template>' +
    '</div>' +
    '</paper-dialog></template>';

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
                var dialog = dialogModel.$.confirmDialog;
                if (e.keyCode === 13) {
                    dialog.close();
                    resolve("ENTER");
                    restoreActiveElement();
                }
            }

            dialogModel.dialogCanceled = function (e) {
                reject("ESC");
                restoreActiveElement();
            };
            dialogModel.closeDialog = function () {
                reject("ESC");
                restoreActiveElement();
            };

            dialogModel._action = function (e) {
                const button = e.model.item;
                if (button.confirm) {
                    resolve(button.name);
                } else {
                    reject(button.name);
                }
                restoreActiveElement();
            };

            dialogModel.message = message;
            dialogModel.buttons = buttons;
            dialogModel.showDialog();
        });
    }
});