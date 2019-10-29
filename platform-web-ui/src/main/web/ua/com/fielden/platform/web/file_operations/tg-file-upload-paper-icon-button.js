import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/iron-icons/iron-icons.js';
import '/resources/polymer/paper-styles/color.js';

import { TgFileProcessingBehavior } from '/resources/file_operations/tg-file-processing-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <paper-icon-button icon="folder-open" on-tap="openFileDialog" on-dragenter="_dragenter" on-dragover="_dragover" on-dragleave="_dragleave" on-drop="_drop" disabled$="[[_fpInProgress]]"></paper-icon-button>
`;

Polymer({
    _template: template,

    is: 'tg-file-upload-paper-icon-button',

    behaviors: [
        TgFileProcessingBehavior
    ],

    properties: {

        progressUpdater: {
            type: Function
        },

        processResponse: {
            type: Function
        },

        processError: {
            type: Function
        },

        processFileUploadedEvent: {
            type: Function
        }

    },

    ready: function () {

        this.fpResponseHandler = function (e) {
            this._updateProgress(0, '', false);
            if (this.processResponse) {
                this.processResponse(e);
            }
        }.bind(this);

        this.fpErrorHandler = function (e) {
            this._updateProgress(0, '', false);
            if (this.processError) {
                this.processError(e);
            }
        }.bind(this);

        this.fpAbortEventHandler = function (e) {
            this._updateProgress(0, '', false);
        }.bind(this);

        this.fpFileUploadingProgressEventHandler = function (prc) {
            this._updateProgress(prc, 'uploading', true);
        }.bind(this);

        this.fpFileProcessingProgressEventHandler = function (prc) {
            this._updateProgress(prc, 'processing', true);
        }.bind(this);

        this.fpFileUploadedEventHandler = function () {
            this._updateProgress(0, '', false);
            if (this.processFileUploadedEvent) {
                this.processFileUploadedEvent();
            }
        }.bind(this);

    },

    _updateProgress: function (prc, clazz, isVisible) {
        if (this.progressUpdater) {
            this.progressUpdater(prc, clazz, isVisible);
        }
    },

    _dragenter: function (e) {
        tearDownEvent(e);
    },

    _dragover: function (e) {
        tearDownEvent(e);
    },

    _dragleave: function (e) {
        tearDownEvent(e);
    },

    _drop: function (e) {
        tearDownEvent(e);

        const dt = e.dataTransfer;
        const files = dt.files;

        // _handleFiles is mixed in from TgFileProcessingBehavior
        this._handleFiles(files);
    }

});