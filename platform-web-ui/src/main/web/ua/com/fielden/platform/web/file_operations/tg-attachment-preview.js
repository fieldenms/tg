
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-if.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';


import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import { isSupportedLink, openLink, canOpenLinkWithoutConfirmation, confirmLinkAndThen, processURL } from '/resources/components/tg-link-opener.js';
import { isMobileApp, isIPhoneOs, isIPadOs, isMacSafari } from '/resources/reflection/tg-polymer-utils.js';

const MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED = "This link points to an untrusted site. Only open it if you’re sure it’s safe.";
const MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED = "This link can’t be previewed. Open it to view the content.";
const MSG_ATTACHMENT_CANNOT_BE_VIEWED = "This file can’t be previewed. Download it to see the content.";
const MSG_ATTACHMENT_PDF_CANNOT_BE_PREVIEWED_INLINE = "Inline preview isn’t supported for PDFs in this browser. Open it to view the content.";

/** Defines types of attachment to preview. */
const ATTACHMENT_KIND = {
    IMAGE: 'IMAGE',
    PDF: 'PDF',
    HYPERLINK: 'HYPERLINK'
}

/**
 * Guesses the attachment kind from the URL's pathname extension.
 * Returns `PDF` for `.pdf`, `IMAGE` for common raster/vector image extensions, or `HYPERLINK` when the extension is unrecognised or the URL is malformed.
 *
 * Callers use the result directly as `_effectiveKind`: an unguessable URL ends up in the HYPERLINK alt view,
 * rather than being rendered with an arbitrary default renderer that would likely just fail.
 */
const guessAttachmentKindFromUrl = function (url) {
    try {
        const path = new URL(url).pathname.toLowerCase();
        if (path.endsWith('.pdf')) return ATTACHMENT_KIND.PDF;
        if (/\.(png|jpe?g|gif|webp|svg|bmp|ico|avif)$/.test(path)) return ATTACHMENT_KIND.IMAGE;
    } catch (e) {}
    return ATTACHMENT_KIND.HYPERLINK;
};

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
    <template is="dom-if" if="[[_isPdfVisible(_isPdfPreviewAvailable, _effectiveKind, _wasConfirmed, _attachmentUri)]]" restamp>
        <object id="pdfViewer" data$="[[_attachmentUri]]" type="application/pdf" on-error="_resourceLoadError" on-load="_resourceLoadSuccess"></object>
    </template>
    <div id="altView" hidden$="[[!_isAltVisible(_effectiveKind, _attachmentUri)]]">
        <span id="message">[[_getAltViewText(_linkCheckRes, _wasConfirmed, _kind, _isPdfPreviewAvailable)]]</span>
        <paper-button raised roll="button" on-tap="_downloadOrOpenAttachment" tooltip-text$="[[_getButtonTooltip(_linkCheckRes, _kind, _isPdfPreviewAvailable)]]" disabled$="[[_working]]">
            <span style="margin: 0 8px 0 8px">[[_getButtonText(_linkCheckRes, _kind, _isPdfPreviewAvailable)]]</span>
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
 * For hyperlink attachments, the initial renderer is picked from the URL extension (IMAGE or PDF); if the URL doesn't reveal a type,
 * the alt view is shown immediately with an OPEN button rather than speculatively attempting to render.
 * If the hyperlink is untrusted, the alt view prompts the user to confirm before attempting any preview.
 * After confirmation, a single render attempt is made; if it fails, the link opens automatically.
 * For already-trusted hyperlinks, the render runs on load; if it fails, the alt view shows with an OPEN button (no auto-open).
 *
 * Visibility invariant: exactly one of the image preview, PDF preview, or alt view is visible at any time.
 */
class TgAttachmentPreview extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            /** The bound entity whose attachment is being previewed. Expected to expose `attachment`, `attachmentUri`, and `kind`. */
            entity:{
                type: Object,
                observer: "_entityChanged"
            },

            /** An externally assigned function that accepts an instance of type Attachment as an argument and starts the download of the associated file. */
            downloadAttachment: {
                type: Function
            },

            /**
             * The result of `canOpenLinkWithoutConfirmation` for hyperlink attachments.
             * Null for non-hyperlink or unsupported links. Drives OPEN-vs-DOWNLOAD semantics and the confirmation flow.
             */
            _linkCheckRes: {
                type: Object,
                value: null
            },

            /**
             * Whether the current hyperlink has been accepted by the user (or needs no confirmation).
             * Always true for non-hyperlink attachments.
             */
            _wasConfirmed: {
                type: Boolean,
                value: true
            },

            /** The URI of the currently displayed attachment; null when nothing is bound. */
            _attachmentUri: {
                type: String,
                value: null
            },

            /** The rendering kind chosen by the server-side producer: "IMAGE", "PDF", "HYPERLINK", or HYPERLINK when the kind couldn't be determined. */
            _kind: {
                type: String,
                value: null
            },

            /**
             * The active rendering state:
             *  - `null` — no attachment bound (loading view is shown);
             *  - `IMAGE` / `PDF` — the corresponding inline preview is active;
             *  - `HYPERLINK` — the alt view is shown, covering: untrusted-hyperlink confirmation,
             *    a failed render attempt, or an attachment whose type cannot be determined.
             * For file attachments this starts as `_kind`; a PDF on a browser without inline PDF support is immediately collapsed to `HYPERLINK` by the observer.
             * For trusted hyperlinks it starts at IMAGE/PDF (guessed from URL) or HYPERLINK (when the URL reveals nothing), and collapses to HYPERLINK on any render failure.
             */
            _effectiveKind: {
                type: String,
                value: null,
                observer: "_effectiveKindChanged"
            },

            /**
             * Whether the current browser can reliably render an inline PDF via `<object type="application/pdf">`.
             * False on mobile, iPhone, iPad, and macOS Safari, where inline PDF rendering is not supported.
             * When false, PDFs fall through to the alt view with an OPEN button that opens the file in a new tab.
             */
            _isPdfPreviewAvailable: {
                type: Boolean,
                value: !(isMobileApp() || isIPhoneOs() || isIPadOs() || isMacSafari())
            },

            /** Indicates that the link should auto-open if the inline render attempt fails after a fresh confirmation. */
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
     * Handles load errors from the `<img>` and `<object>` elements.
     * Any render failure collapses to the alt view (HYPERLINK), regardless of attachment kind — there's no "try another renderer" step.
     */
    _resourceLoadError() {
        this._fallbackToHyperlink();
    }

    _resourceLoadSuccess() {
        console.log("PDF was loaded");
    }

    /**
     * Collapses `_effectiveKind` to HYPERLINK (alt view).
     * When a freshly-confirmed hyperlink fails to render inline, the link is opened automatically so the user isn't asked to click OPEN again.
     */
    _fallbackToHyperlink() {
        this._effectiveKind = ATTACHMENT_KIND.HYPERLINK;
        if (this._pendingLinkOpen && this._wasConfirmed) {
            this._pendingLinkOpen = false;
            this._openAttachment();
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

    /**
     * Observer for `_effectiveKind`.
     * When the active renderer becomes PDF but the browser can't display PDFs inline, collapses to the alt view (OPEN/DOWNLOAD button).
     */
    _effectiveKindChanged(newKind) {
        if (newKind === ATTACHMENT_KIND.PDF && !this._isPdfPreviewAvailable) {
            this._fallbackToHyperlink();
        }
    }

    /**
     * Resolves the URL/target for the current attachment and opens it in a new tab.
     * Prefers the validated URL from `_linkCheckRes` when available; otherwise processes the raw attachment URI.
     */
    _openAttachment() {
        const url = (this._linkCheckRes && this._linkCheckRes.urlAndHostname.url.href) || processURL(this._attachmentUri).url.href;
        const target = (this._linkCheckRes && this._linkCheckRes.target) || "_blank";
        openLink(url, target);
    }

    /** Initialises preview state for the given attachment: determines the rendering kind, checks link trust, and picks the initial renderer if applicable. */
    _updateAttachmentPreviewProperties(attachment) {
        this._linkCheckRes = null;
        this._wasConfirmed = true;
        this._attachmentUri = null;
        this._kind = null;
        this._effectiveKind = null;
        this._pendingLinkOpen = false;
        if (attachment && attachment.attachmentUri) {
            this._attachmentUri = attachment.attachmentUri;
            // Unknown or missing kinds are treated as HYPERLINK so the code below runs the hyperlink flow (trust check, alt view with OPEN) rather than leaving the element in a loading state.
            this._kind = ATTACHMENT_KIND[attachment.kind] || ATTACHMENT_KIND.HYPERLINK;
            if (this._kind === ATTACHMENT_KIND.HYPERLINK && isSupportedLink(this._attachmentUri)) {
                this._linkCheckRes = canOpenLinkWithoutConfirmation(this._attachmentUri);
                if (this._linkCheckRes && !this._linkCheckRes.canOpenWithoutConfirmation) {
                    this._wasConfirmed = false;
                    this._effectiveKind = ATTACHMENT_KIND.HYPERLINK;
                } else {
                    this._effectiveKind = guessAttachmentKindFromUrl(this._attachmentUri);
                }
            } else {
                // Either an IMAGE/PDF file attachment (render directly) or a HYPERLINK whose URI isn't a supported link (show alt view).
                this._effectiveKind = this._kind;
            }
        }
    }

    /** Alt-view button label: OPEN when there is a link to follow or a PDF that can't be previewed inline; DOWNLOAD otherwise. */
    _getButtonText(_linkCheckRes, _kind, _isPdfPreviewAvailable) {
        if (_linkCheckRes || (_kind === ATTACHMENT_KIND.PDF && !_isPdfPreviewAvailable)) {
            return "OPEN";
        }
        return "DOWNLOAD";
    }

    /** Alt-view button tooltip; mirrors the OPEN/DOWNLOAD branching of {@link _getButtonText}. */
    _getButtonTooltip(_linkCheckRes, _kind, _isPdfPreviewAvailable) {
        if (_linkCheckRes || (_kind === ATTACHMENT_KIND.PDF && !_isPdfPreviewAvailable)) {
            return "Opens the attachment.";
        }
        return "Downloads the attachment.";
    }

    /** Whether the `<img>` preview should be visible: requires IMAGE effective kind, confirmed state, and a bound URI. */
    _isImageVisible (_effectiveKind, _wasConfirmed, _attachmentUri) {
        return !!(_effectiveKind === ATTACHMENT_KIND.IMAGE && _wasConfirmed && _attachmentUri);
    }

    /**
     * Whether the `<object>` PDF preview should be visible.
     * Requires PDF effective kind, confirmed state, a bound URI, and a browser that supports inline PDF rendering.
     */
    _isPdfVisible (_isPdfPreviewAvailable, _effectiveKind, _wasConfirmed, _attachmentUri) {
        return !!(_isPdfPreviewAvailable && _effectiveKind === ATTACHMENT_KIND.PDF && _wasConfirmed && _attachmentUri);
    }

    /**
     * Whether the alt view should be visible.
     * True when either `_effectiveKind` is HYPERLINK (trust prompt, failed render, unknown type, or unsupported URI)
     * or no URI is bound (non-previewable file such as `.docx`, where the server returns a null preview URI).
     */
    _isAltVisible (_effectiveKind, _attachmentUri) {
        return _effectiveKind === ATTACHMENT_KIND.HYPERLINK || !_attachmentUri;
    }

    /**
     * Alt-view message text, selected by attachment state:
     *  - untrusted hyperlink → trust warning;
     *  - trusted hyperlink with no viable renderer → "link can't be previewed";
     *  - PDF file on a browser without inline PDF support → "inline preview isn't supported";
     *  - any other non-previewable file → "download to view".
     */
    _getAltViewText(_linkCheckRes, _wasConfirmed, _kind, _isPdfPreviewAvailable) {
        if (_linkCheckRes) {
            if (!_wasConfirmed) {
                return MSG_ATTACHMENT_LINK_IS_NOT_TRUSTED;
            }
            return MSG_ATTACHMENT_LINK_CANNOT_BE_VIEWED;
        }
        if (_kind === ATTACHMENT_KIND.PDF && !_isPdfPreviewAvailable) {
            return MSG_ATTACHMENT_PDF_CANNOT_BE_PREVIEWED_INLINE;
        }
        return MSG_ATTACHMENT_CANNOT_BE_VIEWED;
    }

    /**
     * An on-tap event handler for the DOWNLOAD/OPEN button.
     */
    _downloadOrOpenAttachment(e) {
        // If the button represents the OPEN state, there would be a link check result.
        // Tapping the OPEN button should open the link, but with security verification.
        if (this._linkCheckRes || (this._kind === ATTACHMENT_KIND.PDF && !this._isPdfPreviewAvailable)) {
            if (this._wasConfirmed) {
                // Either the inline render failed, or the URL gave no hint about the resource type.
                // Open the link instead.
                this._openAttachment();
            }
            // If the link is not yet trusted, confirm it first.
            else if (this._linkCheckRes) {
                confirmLinkAndThen(this._linkCheckRes, opt => {
                    this._wasConfirmed = true;
                    this._pendingLinkOpen = true;
                    const guessed = guessAttachmentKindFromUrl(this._attachmentUri);
                    if (guessed === ATTACHMENT_KIND.HYPERLINK) {
                        // No inline render to attempt — open the link directly (handles both the
                        // "URL reveals no type" case and the "_effectiveKind was already HYPERLINK" case
                        // where a bare assignment wouldn't fire the observer).
                        this._fallbackToHyperlink();
                    } else {
                        // Try the inline renderer. If it errors, _resourceLoadError falls back to
                        // HYPERLINK and _pendingLinkOpen causes the link to open automatically.
                        this._effectiveKind = guessed;
                    }
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