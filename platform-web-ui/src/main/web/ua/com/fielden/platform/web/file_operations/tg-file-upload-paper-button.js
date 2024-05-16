import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import { TgFileProcessingBehavior } from '/resources/file_operations/tg-file-processing-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>

        :host {
            display: inline-block;
            position: relative;
            box-sizing: border-box;
            text-align: center;
            font: inherit;
            outline: none;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-user-select: none;
            user-select: none;
            cursor: pointer;
            z-index: 0;
        }

        .over {
            background: var(--paper-green-100);
        }

        #fileSelect {
            cursor: pointer;
        }

        .meter-span {
            display: inline-block;
            height: 100%;
            border-top-right-radius: 2px;
            border-bottom-right-radius: 2px;
            border-top-left-radius: 2px;
            border-bottom-left-radius: 2px;
            background-color: var(--paper-blue-100);
            position: absolute;
            overflow: hidden;
            bottom: 0;
            left: 0;
            z-index: -1;
        }

        paper-spinner {
            position: absolute;
            top: 0px;
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

    <paper-button raised roll="button" id="fileSelect" on-tap='openFileDialog' on-dragenter='_dragenter' on-dragover='_dragover' on-dragleave='_dragleave' on-drop='_drop' style="width:100%" disabled$="[[_fpInProgress]]">
        <paper-spinner id="spinner" active="[[_working]]" class="blue" style="visibility: hidden;" alt="in progress">
        </paper-spinner>

        <!-- instead a span based approach is used to indicate the progress -->
        <span class="meter-span" id="progressBar" style="width: 0%"></span>
        <span>[[title]]</span>
    </paper-button>

    <!-- need also support some debug information on a selected file -->
    <template is='dom-if' if='[[debug]]'>
        <ul style='text-align: left;overflow: scroll'>
            <li>names of selected files: <span style='color: #6a1b9a'>[[_debug_fileNames]]</span></li>
            <li>number of selected files: <span style='color: #6a1b9a'>[[_debug_fileNum]]</span></li>
            <li>total size: <span style='color: #6a1b9a'>[[_debug_fileSize]]</span></li>
            <li>mime type(s): <span style='color: #6a1b9a'>[[_debug_mimeTypes]]</span></li>
        </ul>
    </template>
`;

Polymer({
    _template: template,

    is: 'tg-file-upload-paper-button',

    behaviors: [
        TgFileProcessingBehavior
    ],

    properties: {

        title: {
            type: String,
            value: 'Tap or D`n`D'
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
            this.$.progressBar.style.width = '0%';
            this.title = 'Tap or D`n`D';

            if (this.processResponse) {
                this.processResponse(e);
            }
        }.bind(this);

        this.fpErrorHandler = function (e) {
            this.title = 'Tap or D`n`D';
            this.$.progressBar.style.width = '0%';

            if (this.processError) {
                this.processError(e);
            }
        }.bind(this);

        this.fpAbortEventHandler = function (e) {
            this.title = 'Tap or D`n`D (aborted)';
            this.$.progressBar.style.width = '0%';
        }.bind(this);

        this.fpFileUploadingProgressEventHandler = function (prc) {
            this.title = 'Uploading...';
            this.$.progressBar.style.width = prc + '%';
        }.bind(this);

        this.fpFileProcessingProgressEventHandler = function (prc) {
            this.title = 'Processing...';
            this.$.progressBar.style.width = prc + '%';
        }

        this.fpFileUploadedEventHandler = function () {
            if (this.processFileUploadedEvent) {
                this.processFileUploadedEvent();
            }
        }.bind(this);

    },

    _dragenter: function (e) {
        tearDownEvent(e);
        this.$.fileSelect.classList.add('over');
    },

    _dragover: function (e) {
        tearDownEvent(e);
        this.$.fileSelect.classList.add('over');
    },

    _dragleave: function (e) {
        tearDownEvent(e);
        this.$.fileSelect.classList.remove('over');
    },

    _drop: function (e) {
        tearDownEvent(e);

        const dt = e.dataTransfer;
        const files = dt.files;

        this.$.fileSelect.classList.remove('over');

        // _handleFiles is mixed in from TgFileProcessingBehavior
        this._handleFiles(files);
    }

});