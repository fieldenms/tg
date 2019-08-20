import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgSseBehavior } from '/resources/sse/tg-sse-behavior.js';
import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';

const TgFileProcessingBehaviorImpl = {

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
            type: Number
        },

        /* Acceptable mime types for the files to be uploaded. */
        mimeTypesAccepted: {
            type: String,
            observer: '_mimeTypesAcceptedChanged'
        },

        fpResponseHandler: {
            type: Function
        },

        fpErrorHandler: {
            type: Function
        },

        fpFileUploadedEventHandler: {
            type: Function
        },

        fpFileUploadingProgressEventHandler: {
            type: Function
        },

        fpFileProcessingProgressEventHandler: {
            type: Function
        },

        fpAbortEventHandler: {
            type: Function
        },

        /* a function to abort file upload or processing */
        abort: {
            type: Function
        },

        /* Indicates whether file processing is in progres. */
        _fpInProgress: {
            type: Boolean,
            value: false
        },

        _debug_fileNames: {
            type: String,
            value: '[none]'
        },

        _debug_fileNum: {
            type: Number,
            value: '0'
        },

        _debug_fileSize: {
            type: String,
            value: '[none]'
        },

        _debug_mimeTypes: {
            type: String,
            value: '[none]'
        },

        _uploadInput: {
            type: Object,
        },

        /* A function to be bound to on-tap of some visual element, trigerring openning of a file dialog to choose file for uploading */
        openFileDialog: {
            type: Function,
            value: function () {
                return function (e) {
                    // let give a chance for tap animation to do its work
                    this.async(function () {
                        if (this._uploadInput) {
                            this._uploadInput.click();
                        } else {
                            throw new Error('this._uploadInput is not defined!');
                        }
                    }.bind(this), 200);
                }
            }
        }

    },

    /* A create callback to perform initialisation. */
    created: function () {
        // need to assign SSE data handler to reflect the server side file processing progress
        this.dataHandler = function (msg) {
            if (this.fpFileProcessingProgressEventHandler) {
                this.fpFileProcessingProgressEventHandler(msg.prc);
            }
        }.bind(this);

        // let's create a dummy file input element to be used for opening a file dialog
        this._uploadInput = document.createElement('input');
        this._uploadInput.type = 'file';
        this._uploadInput.onchange = function () {
            this._handleFiles(this._uploadInput.files);
        }.bind(this);
    },

    attached: function () {
        // cleanup to complete component initialisation
        this._cleanup();
    },

    _mimeTypesAcceptedChanged: function (newValue, oldValue) {
        this._uploadInput.setAttribute('accept', newValue);
    },

    /* A helper method to clean up resources post files processing or error handling. */
    _cleanup: function () {
        this._fpInProgress = false;
        this.abort = () => {
            if (this.fpAbortEventHandler) {
                this.fpAbortEventHandler();
            }
            return 'ok';
        }
        // remove previously selected files
        // the file input onchange is not invoked when assigning the value
        this._uploadInput.value = '';
    },

    /**
     * Accepts array of files, which should contain only 1 file.
     * This one file gets uploaded.
     *
     * If therre is less or more than 1 file then an error if thrown.
     *
     * The use of an array instead of a file is mainly to keep backward compatibility with some older code.
     *
     */
    _handleFiles: function (oFiles) {
        if (this._fpInProgress === true) {
            throw new Error('File uploading is in progress...');
        }

        const nFiles = oFiles.length;
        if (nFiles !== 1) {
            this._cleanup();
            throw new Error('File list contains ' + nFiles + ' files. Exactly one file is expected for upload.');
        }

        this._fpInProgress = true;

        // let's start analysing the file
        const file = oFiles[0];
        const fileNames = file.name;
        const nBytes = file.size;

        // under some circumstance file type can be empty
        // need it identify the type from the file extension in those rare cases
        // the map between file exentions and mime types needs to be expanded as required
        let mimeTypes = file.type;
        if (mimeTypes === '') {  // the mime type could not be determined by the browser
            if (file.name.endsWith('.vsd')) {
                mimeTypes = 'application/vnd.visio';
            } else if (file.name.endsWith('.vsdx')) {
                mimeTypes = 'application/vnd.ms-visio.drawing';
            } else if (file.name.endsWith('.xls')) {
                mimeTypes = 'application/vnd.ms-excel';
            } else if (file.name.endsWith('.msg')) {
                mimeTypes = 'application/vnd.ms-outlook';
            }
        }

        let sOutput = nBytes + ' bytes';
        // optional code for multiples approximation
        const aMultiples = ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
        for (let nMultiple = 0, nApprox = nBytes / 1024; nApprox > 1; nApprox /= 1024, nMultiple++) {
            sOutput = nApprox.toFixed(3) + ' ' + aMultiples[nMultiple] + ' (' + nBytes + ' bytes)';
        }
        // end of optional code

        var maxAllowedBytes = 1024 * this.uploadSizeLimitKb;
        this._debug_fileNames = fileNames;
        this._debug_fileNum = nFiles;
        this._debug_fileSize = sOutput + ' our of allowed ' + this.uploadSizeLimitKb + ' KiB ' + '(' + (maxAllowedBytes) + ' bytes)';
        this._debug_mimeTypes = mimeTypes + ' (acceptable are ' + this.mimeTypesAccepted + ')';

        // if the total number of bytes to be uploaded is greater than the limit
        // then stop further processing by throwing an error
        if (nBytes > maxAllowedBytes) {
            this._cleanup();
            throw new Error('The maximum upload size is exceeded.');
        }

        if (nBytes === 0) {
            this._cleanup();
            throw new Error('Uploading of empty files is prohibited.');
        }

        // end of size and mime type validation

        // we can only have at most 1 file for upload
        if (nFiles === 1) {
            const file = oFiles[0];
            const xhr = new XMLHttpRequest();
            xhr.open('PUT', this.url, true);

            // unique job UUID is required to register a progress update event source
            // which is provided to the server as a header value
            const jobUid = generateUUID();
            xhr.setRequestHeader('jobUid', jobUid);
            // and also let's provide the original file meta-data
            xhr.setRequestHeader('origFileName', encodeURIComponent(file.name));
            xhr.setRequestHeader('lastModified', file.lastModified);
            xhr.setRequestHeader('mime', mimeTypes);

            // let's add onload handler to return the element to its original look
            // and to invoke an external response handler if provided
            // onload occurs also if there was an error processing the file at the server side
            // therefore, it is necessary to analyse the response for any error
            xhr.onload = function (e) {
                this.closeEventSource();
                this._cleanup();

                // if the status is not success then need to call error handler
                // otherwise, call response handler
                if (e.currentTarget.status && e.currentTarget.status != 200) {
                    if (this.fpErrorHandler) {
                        this.fpErrorHandler(e);
                    }
                } else if (this.fpResponseHandler) {
                    this.fpResponseHandler(e);
                }
            }.bind(this);

            // let's also monitor and provide indicattion of the file upload...
            xhr.upload.onprogress = function (event) {
                var prc = event.loaded / event.total * 100;
                if (this.fpFileUploadingProgressEventHandler) {
                    this.fpFileUploadingProgressEventHandler(prc);
                }

                if (prc >= 100) {
                    if (this.fpFileUploadedEventHandler) {
                        this.fpFileUploadedEventHandler();
                    }

                    // can now subscribe to a server side processing progress eventing source
                    this.uri = this.url + "/sse/" + jobUid;
                }

            }.bind(this);

            // file uploading/processing error might also need to be handled externally
            // and to invoke an external response handler if provided
            xhr.onerror = function (e) {
                this.closeEventSource();
                this._cleanup();

                if (this.fpErrorHandler) {
                    this.fpErrorHandler(e);
                }
            }.bind(this);

            // let's clean up if the call was aborted
            xhr.onabort = function (e) {
                this.closeEventSource();
                this._cleanup();

                if (this.fpAbortEventHandler) {
                    this.fpAbortEventHandler(e);
                }
            }.bind(this);


            this.abort = () => { xhr.abort(); return 'ok'; }
            xhr.send(file);
        }
    },

}; // end of behaviour implementation

// define the behavior
export const TgFileProcessingBehavior = [
    TgSseBehavior,
    TgFileProcessingBehaviorImpl
];