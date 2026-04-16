
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-if.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';


import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import { isSupportedLink, openLink, canOpenLinkWithoutConfirmation, confirmLinkAndThen } from '/resources/components/tg-link-opener.js';

const MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED = "This link points to an untrusted site. Only open it if you’re sure it’s safe.";
const MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED = "This link can’t be previewed. Open it to view the content.";
const MSG_ATTACHMENT_CANNOT_BE_VIEWED = "This file can’t be previewed. Download it to see the content.";

/** Defines types of attachment to preview. */
const ATTACHMENT_KIND = {
    IMAGE: 'IMAGE',
    PDF: 'PDF',
    HYPERLINK: 'HYPERLINK'
}

/** The rendering pipeline for hyperlink attachments: each renderer is tried in order until one succeeds. */
const HYPERLINK_RENDER_PIPELINE = [ATTACHMENT_KIND.IMAGE, ATTACHMENT_KIND.PDF];

const template = html`
    <style include="iron-positioning"></style>
    <style>
        :host {
            padding:8px;
            box-sizing:border-box;
            position: relative;
        }
        img {
            object-fit: contain;
            width: 100%;
            height:100%;
            @apply --layout-relative;
        }
        #altView {
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
        #pdfViewer {
            width: 100%;
            height: 100%;
            border: 0;
            @apply --layout-relative;
        }
        #loadingView {
            position: absolute;
            top: 8px;
            bottom: 8px;
            left: 8px;
            right: 8px;
            padding: 20px;
            background-color: white;
            font-size: 18px;
            color: var(--paper-grey-400);
            text-align: center;
            @apply --layout-horizontal;
            @apply --layout-center-center;
        }
    </style>
    <div id="loadingView">Loading preview…</div>
    <template is="dom-if" if="[[_isImageVisible(_effectiveKind, _wasConfirmed, _attachmentUri)]]" restamp>
        <img id="imageLoader" src$="[[_attachmentUri]]" on-error="_resourceLoadError"/>
    </template>
    <template is="dom-if" if="[[_isPdfVisible(_effectiveKind, _wasConfirmed, _attachmentUri)]]" restamp>
        <object id="pdfViewer" data$="[[_attachmentUri]]" type="application/pdf" on-error="_resourceLoadError"></object>
    </template>
    <div id="altView" hidden$="[[!_isAltVisible(_effectiveKind, _wasConfirmed, _attachmentUri)]]">
        <span id="message">[[_getAltViewText(_linkCheckRes, _wasConfirmed)]]</span>
        <paper-button raised roll="button" on-tap="_downloadOrOpenAttachment" tooltip-text$="[[_getButtonTooltip(_linkCheckRes)]]" disabled$="[[_working]]">
            <span style="margin: 0 8px 0 8px">[[_getButtonText(_linkCheckRes)]]</span>
            <paper-spinner id="spinner" active="[[_working]]" class="blue" style="display: none;" alt="in progress"></paper-spinner>
        </paper-button>
    </div>`; 

/**
 * A preview element for attachments.
 *
 * Supports three attachment kinds: IMAGE (rendered via `<img>`), PDF (rendered via `<object>`),
 * and HYPERLINK (a URL that may point to an image, PDF, or an arbitrary web resource).
 *
 * For file attachments (IMAGE, PDF), the element renders the appropriate viewer directly.
 * If loading fails, it falls back to an alt view with a DOWNLOAD button.
 *
 * For hyperlink attachments, a rendering pipeline (IMAGE -> PDF) is tried in order.
 * If the hyperlink is untrusted, the alt view prompts the user to confirm before attempting any preview.
 * After confirmation, the pipeline runs; if all renderers fail, the link opens automatically.
 * For already-trusted hyperlinks, the pipeline runs on load; if all renderers fail,
 * the alt view shows with an OPEN button (no auto-open).
 *
 * Visibility invariant: exactly one of the image preview, PDF preview, or alt view is visible at any time.
 */
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

            /** An externally assigned function that accepts an instance of type Attachment as an argument and starts the download of the associated file. */
            downloadAttachment: {
                type: Function
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

            /** The rendering kind chosen by the server-side producer: "IMAGE", "PDF", "HYPERLINK", or null when no preview is available. */
            _kind: {
                type: String,
                value: null
            },

            /** The active rendering mode. For file attachments, equals _kind. For hyperlinks, cycles through HYPERLINK_RENDER_PIPELINE. */
            _effectiveKind: {
                type: String,
                value: null
            },

            /** Indicates that the link should auto-open if the rendering pipeline exhausts after a fresh confirmation. */
            _pendingLinkOpen: {
                type: Boolean,
                value: false
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

    /** A timer callback that performs spinner activation. */
    _startSpinnerCallback() {
        // The approach with using `display: none` ensure
        // that the spinner gets hidden just before the button becomes enabled.
        this.$.spinner.style.display = null;
    }

    /**
     * Handles load errors from `<img>` and `<object>` elements.
     * For confirmed hyperlinks, advances the rendering pipeline to the next renderer.
     * For file attachments, falls back to the alt view so the user can still download the file.
     */
    _resourceLoadError() {
        if (this._kind === ATTACHMENT_KIND.HYPERLINK && this._wasConfirmed) {
            this._advanceHyperlinkPipeline();
        } else if (this._kind !== ATTACHMENT_KIND.HYPERLINK) {
            this._effectiveKind = null;
        }
    }

    /** For hyperlinks, advances _effectiveKind to the next renderer in HYPERLINK_RENDER_PIPELINE, or null when all have been exhausted. */
    _advanceHyperlinkPipeline() {
        const idx = HYPERLINK_RENDER_PIPELINE.indexOf(this._effectiveKind);
        if (idx >= 0 && idx < HYPERLINK_RENDER_PIPELINE.length - 1) {
            this._effectiveKind = HYPERLINK_RENDER_PIPELINE[idx + 1];
        } else {
            this._effectiveKind = null;
            // If the pipeline exhausted right after a fresh confirmation, open the link automatically
            // instead of making the user click OPEN again.
            if (this._pendingLinkOpen && this._wasConfirmed) {
                this._pendingLinkOpen = false;
                openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
            }
        }
    }

    /**
     * Handles entity (re)binding.
     *
     * Re-initialises the preview only when the incoming entity's `attachmentUri` differs from the currently displayed one.
     * This covers `null → attachment`, `attachment → different attachment`, and `attachment → null` (entity cleared).
     *
     * When the URI is unchanged, this short-circuits to avoid a reused-element race:
     * the reset phase of `_updateAttachmentPreviewProperties` churns `_effectiveKind` through `null → IMAGE`, which re-triggers the loading overlay.
     * Because the `<img>`'s `src` attribute is being set to its current value, the browser fires no fresh `load` event,
     * so the overlay would remain visible indefinitely on top of an already-loaded image.
     */
    _entityChanged(newEntity) {
        const newUri = newEntity && newEntity.attachmentUri;
        if (newUri !== this._attachmentUri) {
            this._updateAttachmentPreviewProperties(newEntity);
        }
    }

    /** Initialises preview state for the given attachment: determines the rendering kind, checks link trust, and starts the pipeline if applicable. */
    _updateAttachmentPreviewProperties(attachment) {
        this._linkCheckRes = null;
        this._wasConfirmed = true;
        this._attachmentUri = null;
        this._kind = null;
        this._effectiveKind = null;
        this._pendingLinkOpen = false;
        if (attachment && attachment.attachmentUri) {
            this._attachmentUri = attachment.attachmentUri;
            this._kind = ATTACHMENT_KIND[attachment.kind] || null;
            if (this._kind === ATTACHMENT_KIND.HYPERLINK && isSupportedLink(this._attachmentUri)) {
                this._linkCheckRes = canOpenLinkWithoutConfirmation(this._attachmentUri);
                if (this._linkCheckRes && !this._linkCheckRes.canOpenWithoutConfirmation) {
                    this._wasConfirmed = false;
                    this._effectiveKind = ATTACHMENT_KIND.HYPERLINK;
                } else {
                    this._effectiveKind = HYPERLINK_RENDER_PIPELINE[0];
                }
            } else {
                this._effectiveKind = this._kind;
            }
        }
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

    _isImageVisible (_effectiveKind, _wasConfirmed, _attachmentUri) {
        return !!(_effectiveKind === ATTACHMENT_KIND.IMAGE && _wasConfirmed && _attachmentUri);
    }

    _isPdfVisible (_effectiveKind, _wasConfirmed, _attachmentUri) {
        return !!(_effectiveKind === ATTACHMENT_KIND.PDF && _wasConfirmed && _attachmentUri);
    }

    _isAltVisible (_effectiveKind, _wasConfirmed, _attachmentUri) {
        // altView shows whenever neither the <img> preview nor the <object> PDF preview is showing.
        return !HYPERLINK_RENDER_PIPELINE.includes(_effectiveKind) || !_wasConfirmed || !_attachmentUri;
    }

    _getAltViewText(_linkCheckRes, _wasConfirmed) {
        if (_linkCheckRes) {
            if (!_wasConfirmed) {
                return MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED;
            }
            return MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED;
        }
        return MSG_ATTACHMENT_CANNOT_BE_VIEWED;
    }

    /**
     * An on-tap event handler for the DOWNLOAD/OPEN button.
     */
    _downloadOrOpenAttachment(e) {
        // If the button represents the OPEN state, there would be a link check result.
        // Tapping the OPEN button should open the link, but with security verification.
        if (this._linkCheckRes) {
            if (this._wasConfirmed) {
                // No renderer in the pipeline could preview the linked resource.
                // Open the link instead.
                openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
               
            }
            // If the link is not yet trusted, confirm it first.
            else {
                confirmLinkAndThen(this._linkCheckRes, opt => {
                    // Mark the attachment as confirmed if the user accepts it.
                    // The next assignments will trigger the rendering pipeline to try each renderer in order.
                    // If all renderers fail, _pendingLinkOpen causes the link to open automatically.
                    this._effectiveKind = HYPERLINK_RENDER_PIPELINE[0];
                    this._pendingLinkOpen = true;
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
            this.downloadAttachment(this.entity.attachment).catch(e => {
                // No action needed; errors are gracefully handled within the downloadAttachment function.
            }).finally(() => {
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