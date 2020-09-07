import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-progress/paper-progress.js';

import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';
import { TgFileProcessingBehavior } from '/resources/file_operations/tg-file-processing-behavior.js';

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
        }

        .over {
            background: var(--paper-green-100);
        }

        #fileSelect {
            cursor: pointer;
        }

        #container {
            display: flex;
            flex-direction: column;
        }
        
        .labelHolder {
            display:flex;
            flex-wrap: nowrap;
            align-items: center;
            padding-left: 8px;
        }
        
        .status {
            order: 1;
            padding-right: 4px;
            text-align: left; 
            float: left;
            text-transform: uppercase;
            font-weight: 700;
            color: var(--paper-grey-400);
        }
        
        .status::after {
            content: ':';
        }
        
        .fileName {
            flex: auto;
            order: 2;
            text-align: left;
            overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                padding-right: 4px;
        }
        
        paper-progress {
            width: auto;
        }

        paper-progress.UPLOADING {
            --paper-progress-active-color: var(--paper-blue-700);
            --paper-progress-secondary-color: var(--paper-blue-200);
        }

        paper-progress.PROCESSING {
            --paper-progress-active-color: var(--paper-blue-700);
            --paper-progress-secondary-color: var(--paper-blue-200);
        }

        paper-progress.ERROR {
            --paper-progress-active-color: var(--paper-red-700);
            --paper-progress-secondary-color: var(--paper-red-200);
        }
        
        paper-progress.ABORTED {
            --paper-progress-active-color: var(--paper-grey-700);
            --paper-progress-secondary-color: var(--paper-grey-200);
        }

        paper-progress.COMPLETED {
            --paper-progress-active-color: var(--paper-green-500);
            --paper-progress-secondary-color: var(--paper-green-200);
        }
        
        paper-progress.PENDING {
            --paper-progress-active-color: var(--paper-grey-500);
            --paper-progress-secondary-color: var(--paper-grey-200);
        }
        
        .errorMsg {
            font-size: 12px;
            font-weight: 400;			
            color: var(--paper-input-container-invalid-color, var(--error-color));
            text-align: left;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            padding: 4px 8px 4px 8px;
        }
        
    </style>

    <div id="container" style="width:100%" tooltip-text$="[[_fileNameTooltip(fileName, _errorMsg)]]">
        <div class="labelHolder">
            <span class="status">[[status]]</span>
            <span class="fileName">[[fileName]]</span>
            <div style="order: 3">
                <paper-icon-button id="abortButton" disabled$="[[_abortDisabled(status)]]" on-tap="abortUploadIfPossible" icon="icons:cancel" class="abort-button custom-icon-buttons" tabIndex="-1" tooltip-text="Abort file uploading."></paper-icon-button>
            </div>
        </div>
        <paper-progress id="progressBar"></paper-progress>
        <span class="errorMsg" hidden$="[[!_errorMsg]]">[[_errorMsg]]</span>
    </div>
    
    <!-- need also support some debug information on a selected file -->
    <template is="dom-if" if="[[debug]]">
        <ul style="text-align: left; overflow: scroll">
            <li>names of selected files: <span style="color: #6a1b9a">[[_debug_fileNames]]</span></li>
            <li>number of selected files: <span style="color: #6a1b9a">[[_debug_fileNum]]</span></li>
            <li>total size: <span style="color: #6a1b9a">[[_debug_fileSize]]</span></li>
            <li>mime type(s): <span style="color: #6a1b9a">[[_debug_mimeTypes]]</span></li>
        </ul>
    </template>
`;

Polymer({
    _template: template,

    is: 'tg-attachment-uploader',

    behaviors: [
        TgFileProcessingBehavior
    ],

    observers: [
        '_canTryFileUploading(file, url, uploadSizeLimitKb, mimeTypesAccepted, processUploadingStopped, canUpload, listIndex)'
    ],

    properties: {

        statuses: {
            type: Object,
            readOnly: true,
            value: function () {
                return {
                    READY: 'READY',
                    PENDING: 'PENDING',
                    UPLOADING: 'UPLOADING',
                    PROCESSING: 'PROCESSING',
                    COMPLETED: 'COMPLETED', // indicate completion of the file upload
                    ERROR: 'ERROR',         // indicate completion of the file upload
                    ABORTED: 'ABORTED'
                };
            }   // indicate completion of the file upload
        },

        /* The current status of the uploader. */
        status: {
            type: String,
            value: function () {
                return 'PENDING';
            },
            observer: '_statusChange'
        },

        /* Captures the status value that the current status was changed from. */
        prevStatus: {
            type: String
        },

        /* A short file name (i.e. not a path). If observer _canTryFileUploading fires, which accepts a valid file object then this property get assigned value of file.name. */
        fileName: {
            type: String,
            value: 'no file was provided for upload'
        },

        /* An object representing the error if it occurres. */
        error: {
            type: Object
        },

        /* A value of the error message. */
        _errorMsg: {
            type: String,
            value: ''
        },

        /* 
         * The file that needs to be uploaded. 
         * It is primarily required for automatic instigation of file uploading based on observer _canTryFileUploading.
         */
        file: {
            type: Object,
            observer: '_fileAssigned'
        },

        /* This property is used by <tg-attachment-uploader-list> to record the position of this instance in the list for each of access. */
        listIndex: {
            type: Number
        },

        /* This property is used as part of oberver _canTryFileUploading to control when file uploading can start. */
        canUpload: {
            type: Boolean,
            value: false
        },

        /* Represents the attachment entity, which was part of a successful upload response. */
        attachment: {
            type: Object
        },

        /*
         * A callback function that accepts one argument -- an instance of 'this'.
         * It is invoked upon completion of the file upload, be that successful completion, error or abortion
         */
        processUploadingStopped: {
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

    /* This is an observer that initiates file upload upon the provising of all listed property values. */
    _canTryFileUploading: function (file, url, uploadSizeLimitKb, mimeTypesAccepted, processUploadingStopped, canUpload, listIndex) {
        // just to assigne the file name if the file was provided
        if (file instanceof File) {
            this.fileName = file.name;
        }

        // should the upload start?
        if (file instanceof File && this.status === this.statuses.PENDING && this.canUpload === true) {
            this._uploadFile();
        }
    },

    _fileAssigned: function (newFile, oldFile) {
        if (newFile instanceof File) {
            this.fileName = newFile.name;
        }
    },

    _statusChange: function (newStatus, oldStatus) {
        const progressBar = this.$.progressBar;
        this.prevStatus = oldStatus;

        // let's remove any styles associated with previous statuses
        progressBar.classList.remove('slow');
        for (let key in this.statuses) {
            progressBar.classList.remove(this.statuses[key]);
        }

        // if the status becomes PENDING then progress bar should become indeterminate with appropriate styling
        progressBar.indeterminate = newStatus === this.statuses.PENDING;
        if (newStatus === this.statuses.PENDING) {
            progressBar.classList.add('slow');
        }

        // update styling
        progressBar.classList.add(newStatus);
        progressBar.updateStyles();

        if (this.processUploadingStopped &&
            (newStatus === this.statuses.COMPLETED || newStatus === this.statuses.ERROR || newStatus === this.statuses.ABORTED)) {
            this.processUploadingStopped(this);
        }

    },

    _updateProgress: function (percentage) {
        // update progress bar if 0 <= % <= 100, which is only applicable to PROCESSING and UPLOADING statuses
        if (percentage >= 0 && percentage <= 100) {
            const progressBar = this.$.progressBar;
            if (this.status === this.statuses.PROCESSING) {
                progressBar.value = percentage;
            } else if (this.status === this.statuses.UPLOADING) {
                progressBar.value = 0;
                progressBar.secondaryProgress = percentage;
            } else if (this.status === this.statuses.COMPLETED) { // percentage should really be 100%
                progressBar.value = percentage;
                progressBar.secondaryProgress = percentage;
            }
        }
    },

    _abortDisabled: function (status) {
        return this.status !== this.statuses.PENDING && status !== this.statuses.UPLOADING;
    },

    abortUploadIfPossible: function () {
        // check if we can abort before aborting...
        if (this.status === this.statuses.PENDING || this.status === this.statuses.UPLOADING) {
            // PENDING means uploading has not started yet, so technically, there is nothing to abort,
            // but the status needs to be changed to prevent any pending upload requests...
            // UPLOADIG means that the upload in already in progress with an active XHR request,
            // which mean that abort call needs to be delegated to the file processing behaviour.
            // In both cases, setting the status to ABORTED is the responsibility of this.fpAbortEventHandler.
            this.abort();
        }
    },

    _fileNameTooltip: function (fileName, errorMsg) {
        return fileName + '<br><span>' + errorMsg + '</span>';
    },

    created: function () {
        this._serialiser = new TgSerialiser();
    },

    ready: function () {
        // a helper lambda to conver a response to entity Result
        const toResult = (response) => {
            const resultAsObj = JSON.parse(response);
            return this._serialiser.deserialise(resultAsObj);
        }

        this.fpResponseHandler = function (e) {
            // first parse e.currentTarget.response as a JSON object and pass it for deserialisation,
            // which should produce an entity of type Result that contains an instance of AttachmentUploader with its key as Attachment
            // and assign the returned attachment to property attachment
            const getAttachmentUploader = (response) => {
                try {
                    return toResult(response).instance;
                } catch (parsingError) {
                    console.error(parsingError);
                    return null; // return as null
                }
            }

            const attachmentUploader = getAttachmentUploader(e.currentTarget.response);
            if (attachmentUploader) {
                this.attachment = attachmentUploader.key;
            }

            this.status = this.statuses.COMPLETED;
            this._updateProgress(100);

            if (this.processResponse) {
                this.processResponse(e);
            }
        }.bind(this);

        this.fpErrorHandler = function (e) {
            const getResult = (response) => {
                try {
                    return toResult(response);
                } catch (parsingError) {
                    console.error(parsingError);
                    return { message: response }; // return as is
                }
            }

            this.error = getResult(e.currentTarget.response);
            this._errorMsg = this.error.message;

            this.status = this.statuses.ERROR;
            if (this.processError) {
                this.processError(result);
            }
        }.bind(this);

        this.fpAbortEventHandler = function (e) {
            this.status = this.statuses.ABORTED;
        }.bind(this);

        this.fpFileUploadingProgressEventHandler = function (prc) {
            this.status = this.statuses.UPLOADING;
            this._updateProgress(prc);
        }.bind(this);

        this.fpFileProcessingProgressEventHandler = function (prc) {
            this.status = this.statuses.PROCESSING;
            this._updateProgress(prc);
        }

        this.fpFileUploadedEventHandler = function () {
            this._updateProgress(100); // make sure UPLOADING progress is at 100%
            this.status = this.statuses.PROCESSING;
            this._updateProgress(3); // set some small value such as 3% just to provide some feedback to about processing starting

            if (this.processFileUploadedEvent) {
                this.processFileUploadedEvent();
            }
        }.bind(this);
    },

    /* Instigates file uploading. It is not intended for direct use. Insetead property file should be used to set the file for upload. */
    _uploadFile: function () {
        if (this.file instanceof File) {
            // _handleFiles requires FileList or an array of files for legacy reasons
            try {
                // set progress to 0 as a new file is being uploaded
                this.$.progressBar.value = 0;
                this.$.progressBar.secondaryProgress = 0;
                this._errorMsg = '';
                this.error = { message: 'no errors' };
                this._handleFiles([this.file]);
            } catch (error) {
                console.error(error);
                this._errorMsg = error;
                this.error = { message: error };
                this.status = this.statuses.ERROR;
                if (this.processError) {
                    this.processError({ message: error });
                }
            }
        }
    }
});