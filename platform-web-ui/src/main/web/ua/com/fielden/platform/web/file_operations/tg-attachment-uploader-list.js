import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import '/resources/file_operations/tg-attachment-uploader.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            width: 100%;
            height: 100%;
            @apply --layout-vertical;
            @apply --layout-flex;
        }

        .over {
            background: var(--paper-blue-100);
        }

        #fileSelect {
            cursor: pointer;
        }

        #container {
            @apply --layout-vertical;
            @apply --layout-flex;
            padding: 8px;
            position: relative;
            box-sizing: border-box;
            background: white;
            overflow: auto; /* this is to make host scorable when needed */
            -webkit-overflow-scrolling: touch;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-user-select: none;
            user-select: none;
        }

        #openDialogOrDrop {
            display: flex;
            flex-direction: column;
            padding: 8px;
            margin-top: 4px;
            cursor: pointer;
        }

        .dropHere {
            flex:auto;
            text-align: center;
            font-weight: 700;
            color: var(--paper-grey-400);
        }

        .with-shadow {
            box-shadow: 0px -3px 6px -2px rgba(0, 0, 0, 0.7);
            z-index: 1;
        }
    </style>
    <div id="container" on-scroll="_contentChanged">
        <template is="dom-repeat" id="uploaders" items="[[_filesToUpload]]" as="f" rendered-item-count="{{_renderedCount}}">
            <tg-attachment-uploader style="width: 100%"
                id$="[[_makeId(index)]]"
                list-index="[[index]]"
                file="[[f]]"
                url="[[url]]"
                upload-size-limit-kb="[[uploadSizeLimitKb]]"
                mime-types-accepted="[[mimeTypesAccepted]]"
                process-uploading-stopped="[[_perUploaderListenerOnUploadingStopped]]"></tg-attachment-uploader>
        </template>
    </div>
    <div id="openDialogOrDrop" on-tap="_openFileDialog">
        <span id="dropAreaTitle" class="dropHere">Click here to select files or Drop files for upload...</span>
    </div>
`;

Polymer({
    _template: template,

    is: 'tg-attachment-uploader-list',

    behaviors: [
        IronResizableBehavior,
        TgTooltipBehavior
    ],

    properties: {
        /* If enabled will output names, number, total size and mime types of the selected files. */
        debug: {
            type: Boolean,
            value: false
        },

        /* URI that points to a file processing resource. */
        url: {
            type: String,
        },

        /* The limit of data to be uploaded for processing in Kibibytes. */
        uploadSizeLimitKb: {
            type: Number,
            value: 20480 // 20 Mebibyte
        },

        /* Acceptable mime types for the files to be uploaded. */
        mimeTypesAccepted: {
            type: String,
            value: '*/*',
            observer: '_mimeTypesAcceptedChanged'
        },

        /* A callback, which gets invoked when uploading of all files stopped, either due to success, error or abortion. */
        processUploadingStopped: {
            type: Function
        },

        /* A callback, which gets invoked when uploading of the firt file in a sequence starts. Passes one argument -- the uploader performing file uploading. */
        processUploadingStarted: {
            type: Function
        },

        numberOfUploaded: {
            type: Number,
            value: 0
        },

        numberOfFailed: {
            type: Number,
            value: 0
        },

        numberOfAborted: {
            type: Number,
            value: 0
        },

        attachments: {
            type: Array,
            value: function () {
                return [];
            }
        },

        _uploadInput: {
            type: Object
        },

        /* An array of files that need to be uploaded.
         * Should NOT be manipulated directly -- only via methods pushValue.*/
        _filesToUpload: {
            type: Array,
            value: function () {
                return [];
            }
        },

        /* Removes all uploaded files from a list of to upload. */
        clearListOfFilesToUpload: {
            type: Function,
            value: function () {
                return function () {
                    if (this._filesToUpload.length > 0) {
                        this._uploadInput.value = '';
                        this._cleaning = true;
                        while (this.pop('attachments')) { }
                        while (this.pop('_filesToUpload')) { }

                        this.numberOfUploaded = 0;
                        this.numberOfFailed = 0;
                        this.numberOfAborted = 0;
                        this.uploadInProgress = false;
                    }
                }.bind(this);
            }
        },

        /* unfortunate state to indicate that cleaning is in progress and prevent the uploading related logic from executing */
        _cleaning: {
            type: Boolean,
            value: false
        },

        _perUploaderListenerOnUploadingStopped: {
            type: Function,
            value: function () {
                return function (uploader) {
                    // count successes and failures
                    if (uploader.status === uploader.statuses.COMPLETED) {
                        this.push('attachments', uploader.attachment);
                        this.numberOfUploaded = this.numberOfUploaded + 1;
                    } else if (uploader.status === uploader.statuses.ABORTED) {
                        this.numberOfAborted = this.numberOfAborted + 1;
                    } else if (uploader.status === uploader.statuses.ERROR) {
                        this.numberOfFailed = this.numberOfFailed + 1;
                    }

                    // if a PENDING uploader was aborted then there is already one in progress,
                    // and there is no need to instigate any new upload -- this will the responsibility of that other uploader
                    if (uploader.status !== uploader.statuses.ABORTED || uploader.prevStatus !== uploader.statuses.PENDING) {
                        // let's find the next PENDING uploader
                        let index = uploader.listIndex + 1;
                        let pendingUploader = null;
                        while (!pendingUploader && index < this.$.uploaders.items.length) {
                            const id = this._makeId(index);
                            const elem = this.shadowRoot.querySelector("#" + id);
                            if (elem && elem.status === elem.statuses.PENDING) {
                                pendingUploader = elem;
                            }
                            index = index + 1;
                        }

                        // if the next PENDING uploader was found then activate it
                        if (pendingUploader) {
                            pendingUploader.canUpload = true;
                        } else {
                            // if there is nothin else to upload then report that all uploading has stopped
                            this.uploadInProgress = false;
                            if (this.processUploadingStopped) {
                                this.processUploadingStopped();
                            }
                        }
                    }

                }.bind(this);
            }
        },

        /* A property that is bound to track the number of rendered attachment uploader components by dom-repeate component 'uploaders'.
         * Changes in the number of rendered uploaders is used to decide if there is a need to start the actual uploading. */
        _renderedCount: {
            type: Number,
            observer: '_renderedCountChanged'
        },

        /* Indicates whether there are any uploads that have not yet completed.
         * It is also used as part of '_renderedCountChanged' when deciding if a new upload should be started. */
        uploadInProgress: {
            type: Boolean,
            value: false
        },

        /**
         * Specifies if more than one file can be attached.
         */
        multi: {
            type: Boolean,
            value: true,
            observer: '_multiChanged'
        }

    },

    /* Gets invoked upon the change to the number of attachment uploaders that are rendered by dom-repeate component 'uploaders'.
     * It is used to instigate uploading of the first file in the added batch if there are no other uploads in progress. */
    _renderedCountChanged: function (newCount, oldCount) {
        // if cleaning then should not perform any uploading-related logic
        if (!this._cleaning && oldCount >= 0 && this.uploadInProgress === false) {
            this._contentChanged();
            this.uploadInProgress = true;
            const startId = this._makeId(oldCount);
            const elem = this.shadowRoot.querySelector("#" + startId);
            if (elem) {
                if (this.processUploadingStarted) {
                    this.processUploadingStarted(elem);
                }
                elem.canUpload = true;
            }
        }

        // if this is the last element to be cleaned then set _cleaning to false
        if (newCount == 0 && this._cleaning == true) {
            this._cleaning = false;
        }
    },

    _multiChanged: function (newMulti, oldMulti) {
        if (newMulti === true) {
            this._uploadInput.setAttribute('multiple', ''); // support selection of multiple files
        } else {
            this._uploadInput.removeAttribute('multiple'); // remove support for selection of multiple files
        }
    },

    created: function () {
        // let's create an invisible file input element to be used for opening a file dialog
        this._uploadInput = document.createElement('input');
        this._uploadInput.type = 'file';
        this._uploadInput.onchange = function () {
            this.submitUploading(this._uploadInput.files);
        }.bind(this);
    },

    ready: function () {
        this.addEventListener('iron-resize', this._resizeEventListener.bind(this));
        this.addEventListener('dragenter', this._dragenter.bind(this));
        this.addEventListener('dragover', this._dragover.bind(this));
        this.addEventListener('dragleave', this._dragleave.bind(this));
        this.addEventListener('drop', this._drop.bind(this));
    },

    /**
     * Makes a value for attribute id based on the provided index.
     * Such id values are used for <tg-attachment-uploader> HTML elements representing list of files for upload.
     */
    _makeId: function (index) {
        const id = 'tg-attachment-uploader-' + index;
        return id;
    },

    /* Pushes the specified file into the tail of attay _filesToUpload, which triggers its uploading */
    _pushFileForUpload: function (file) {
        this.push('_filesToUpload', file);
    },

    _canAcceptMoreFiles: function (optionalCondition) {
        return this.multi === true || (this.attachments.length === 0 && ((typeof optionalCondition === 'undefined') ? true : optionalCondition));
    },

    /* A function to be bound to on-tap of some visual element, trigerring openning of a file dialog to choose file for uploading */
    _openFileDialog: function (e) {
        if (this._canAcceptMoreFiles()) {
            // let give a chance for tap animation to do its work
            this.async(function () {
                if (this._uploadInput) {
                    this._uploadInput.click();
                } else {
                    throw new Error('this._uploadInput is not defined!');
                }
            }.bind(this), 200);
        }
    },

    /* Function responsible for submitting files into the queue for uploading. */
    submitUploading: function (fileList) {
        const self = this;
        Array.from(fileList).forEach(file => self._pushFileForUpload(file));
    },

    /* Provides a reason why the attachment uploader list cannot be left... */
    canLeave: function () {
        return {
            imperative: true,
            msg: 'Uploading is still in progress...'
        }
    },

    /* Ensures that shasow style is applied/removed depending on the size of the component. */
    _resizeEventListener: function (event, details) {
        this._contentChanged();
    },

    /* Ensures that shadow style is applied/removed depending on the content of the list of attachment uploaders. */
    _contentChanged: function (e) {
        const scrollTarget = this.$.container;
        const openDialogOrDrop = this.$.openDialogOrDrop;
        if (scrollTarget && openDialogOrDrop) {
            if (scrollTarget.scrollTop + scrollTarget.offsetHeight >= scrollTarget.scrollHeight) {
                openDialogOrDrop.classList.remove('with-shadow');
            } else {
                openDialogOrDrop.classList.add('with-shadow');
            }
        }
    },

    _mimeTypesAcceptedChanged: function (newValue, oldValue) {
        this._uploadInput.setAttribute('accept', newValue);
    },

    _dragenter: function (e) {
        tearDownEvent(e);
        if (this._canAcceptMoreFiles()) {
            this.$.openDialogOrDrop.classList.add('over');
        }
    },

    _dragover: function (e) {
        tearDownEvent(e);
        if (this._canAcceptMoreFiles()) {
            this.$.openDialogOrDrop.classList.add('over');
        }
    },

    _dragleave: function (e) {
        tearDownEvent(e);
        this.$.openDialogOrDrop.classList.remove('over');
    },

    _drop: function (e) {
        tearDownEvent(e);
        this.$.openDialogOrDrop.classList.remove('over');

        if (this._canAcceptMoreFiles(e.dataTransfer.files.length === 1)) {
            this.submitUploading(e.dataTransfer.files);
        }
    }

});