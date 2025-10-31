
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';


import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import { isSupportedLink, openLink, canOpenLinkWithoutConfirmation, confirmLinkAndThen } from '/resources/components/tg-link-opener.js';

const MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED = "This link points to an untrusted site. Only open it if you’re sure it’s safe.";
const MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED = "This link can’t be previewed. Open it to view the content.";
const MSG_ATTACHMENT_CANNOT_BE_VIEWED = "This file can’t be previewed. Download it to see the content.";

const template = html`
    <style include="iron-positioning"></style>
    <style>
        :host {
            padding:8px;
            box-sizing:border-box;
        }
        img {
            object-fit: contain;
            background-color: white;
            width: 100%;
            height:100%;
            @apply --layout-relative;
        }
        #altImage {
            background-color: white;
            width: 100%;
            height: 100%;
            @apply --layout-relative;
            @apply --layout-vertical;
            @apply --layout-center-center;
        }
        #message {
            font-size: 18px;
            color: #BDBDBD;
            margin: 24px;
            text-align: center;
        }
        paper-button {
            font-size: 14px;
            font-weight: 500;
            color: #000000DE;
            position: relative;
        }
        #spinner {
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
    <img id="imageLoader" src$="[[_getImageUri(_linkCheckRes, _wasConfirmed, _attachmentUri)]]" hidden$="[[!_isImageVisible(_loadingError, _attachmentUri)]]" on-load="_imageLoaded" on-error="_imageLoadeError"/>
    <div id="altImage" hidden$="[[_isImageVisible(_loadingError, _attachmentUri)]]">
        <span id="message">[[_getAltImageText(_linkCheckRes, _wasConfirmed)]]</span>
        <paper-button raised on-tap="_downloadOrOpenAttachment" tooltip-text$="[[_getButtonTooltip(_linkCheckRes)]]" disabled$="[[_working]]">
            <span>[[_getButtonText(_linkCheckRes)]]</span>
            <paper-spinner id="spinner" active="[[_working]]" class="blue" style="display: none;" alt="in progress"></paper-spinner>
        </paper-button>
    </div>`; 

class TgAttachmentPreview extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            entity:{
                type: Object,
                observer: "_entityChanged"
            },

            /** An extrenally assigned function that accepts an instance of type Attachment as an argument and starts the download of the associated file. */
            downloadAttachment: {
                type: Function
            },

            _loadingError: {
                type: Boolean,
                value: false,
            },

            _linkCheckRes: {
                type: Object,
                value: null
            },

            _wasConfirmed: {
                type: Boolean,
                value: true
            },

            _attachmentUri: {
                type: String,
                value: null
            },

            /** A timer to prevent spinner from activating for quick actions. */
            _startSpinnerTimer: {
                type: Object,
                value: null
            },

            /** Indicates whether the download action is in progress. */
            _working: {
                type: Boolean,
                value: false
            }
        }
    }

    /* A timer callback that performs spinner activation. */
    _startSpinnerCallback() {
        // The approach with using `display: none` ensure
        // that the spinner gets hidden just before the button becomes enabled.
        this.$.spinner.style.display = null;
    }

    _imageLoaded() {
        this._loadingError = false;
    }

    _imageLoadeError() {
        this._loadingError = true;
    }

    _entityChanged(newEntity) {
        this._updateAttachmentPreviewProperties(newEntity);
    }

    _updateAttachmentPreviewProperties(attachment) {
        this._linkCheckRes = null;
        this._wasConfirmed = true;
        this._attachmentUri = null;
        if (attachment && attachment.attachmentUri) {
            this._attachmentUri = attachment.attachmentUri;
            if (isSupportedLink(this._attachmentUri)) {
                this._linkCheckRes = canOpenLinkWithoutConfirmation(this._attachmentUri);
                if (this._linkCheckRes && !this._linkCheckRes.canOpenWithoutConfirmation) {
                    this._wasConfirmed = false;
                }
            }
        }
    }

    _getImageUri(_linkCheckRes, _wasConfirmed, _attachmentUri) {
        if (_linkCheckRes && !_wasConfirmed) {
            return "broken_link";
        }
        return _attachmentUri;
    }

    _getButtonText(_linkCheckRes) {
        if (_linkCheckRes) {
            return "OPEN";
        }
        return "DOWNLOAD";
    }

    _getButtonTooltip(_linkCheckRes) {
        if (_linkCheckRes) {
            return "Opens the attachment.";
        }
        return "Downloads the attachment.";
    }

    _isImageVisible (_loadingError, _attachmentUri) {
        return !!(!_loadingError && _attachmentUri);
    }

    _getAltImageText(_linkCheckRes, _wasConfirmed) {
        if (_linkCheckRes) {
            if (!_wasConfirmed) {
                return MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED;
            }
            return MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED;
        }
        return MSG_ATTACHMENT_CANNOT_BE_VIEWED;
    }

    /**
     * An on-tap even handler for the DOWNLOAD/OPEN button.
     */
    _downloadOrOpenAttachment(e) {
        // If the button represent the OPEN state, there would be a link check result.
        // Tapping the OPEN button should open the link, but with security verification.
        if (this._linkCheckRes) {
            if (this._wasConfirmed) {
                if (this._loadingError) {
                    // The link does not represent an image, which caused the loadingError.
                    // Open the link instead.
                    openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
                }
                // Otherwise image should be visible.
            }
            // If the link is not yet trusted, confirm it first.
            else {
                // After the link becomes trusted, opening it as an image may cause an error,
                // which means the link does not represent an image.
                // In that case, the link should be opened as a regular one.
                const afterImageErrorListener = e => {
                    openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
                    removeImageListeners();
                };
                const afterImageLoadListener = e => {
                    removeImageListeners();
                };
                const removeImageListeners = () => {
                    this.$.imageLoader.removeEventListener("error", afterImageErrorListener);
                    this.$.imageLoader.removeEventListener("load", afterImageLoadListener);
                }
                confirmLinkAndThen(this._linkCheckRes, opt => {
                    // Mark the attachment as confirmed if the user accepts it.
                    // The next assignment will trigger recalculation of the image URI and, consequently, image loading.
                    // Image loading may fail, indicating that the link should be opened as a regular one in the image loading error handler.
                    this.$.imageLoader.addEventListener("error", afterImageErrorListener);
                    this.$.imageLoader.addEventListener("load", afterImageLoadListener);
                    this._wasConfirmed = true;
                });
            }
        }
        // Otherwise, the button represents the DOWNLOAD state, which should be the only possible alternative.
        // However, because function `this.downloadAttachment` is assigned outside, we should be a bit more defensive,
        // and check whether it was assigned.
        else if (this.downloadAttachment) {
            if (this._startSpinnerTimer) {
                clearTimeout(this._startSpinnerTimer);
            }
            this._startSpinnerTimer = setTimeout(this._startSpinnerCallback.bind(this), 700);
            this._working = true;
            this.downloadAttachment(this.entity.attachment).finally(() => {
                this._working = false;
                // Clear timeout to prevent not yet activated spinner from activating.
                if (this._startSpinnerTimer) {
                    clearTimeout(this._startSpinnerTimer);
                }
                // Make spinner invisible
                this.$.spinner.style.display = 'none';
            });
        }
    }
}

customElements.define('tg-attachment-preview', TgAttachmentPreview);