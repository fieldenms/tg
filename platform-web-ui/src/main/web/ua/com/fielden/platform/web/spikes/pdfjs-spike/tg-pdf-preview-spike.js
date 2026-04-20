/**
 * `tg-pdf-preview-spike`
 *
 * A spike element that renders a PDF inline using PDF.js (Mozilla) rather than
 * the browser's built-in `<object>`/`<embed>` PDF viewer.
 *
 * Motivation
 * ----------
 * The production `tg-attachment-preview` element uses `<object type="application/pdf">`
 * to embed PDFs. This works on desktop (Chrome, Firefox, Edge, Safari) but has
 * serious limitations on mobile browsers:
 *  - iOS Safari and Android Chrome do not render PDFs inline via `<object>`.
 *  - No `error` event is fired when rendering fails, so we cannot react.
 *  - The native "Open externally" button may be hidden behind our loading overlay.
 *
 * PDF.js renders PDFs by parsing them in JavaScript and drawing each page
 * to an HTML `<canvas>`. Because the output is a canvas (not a native
 * browser PDF viewer), it works consistently on desktop and mobile.
 *
 * What this spike demonstrates
 * ----------------------------
 *  1. Loading PDF.js as an ES module from CDN (for production, this would be
 *     bundled via the existing rollup pipeline in `src/main/resources`).
 *  2. Scrollable multi-page preview via `<iron-list>` — only the cells in the
 *     viewport are stamped, so very large PDFs don't blow up memory.
 *  3. Prev / next navigation (scrolls iron-list to the next/previous page).
 *  4. Zoom in / zoom out (re-renders all stamped cells).
 *  5. Fit-to-width rendering that adapts to container size via ResizeObserver.
 *  6. Reacting to load errors (network failure, corrupt PDF, password-protected).
 *  7. Cleanly tearing down the document when the source URL changes.
 *
 * Limitations of this spike
 * -------------------------
 *  - Loads PDF.js from jsdelivr rather than bundling it. Production integration
 *    must add `pdfjs-dist` to `package.json` and update `rollup.config.js`.
 *  - Uses page 1's aspect ratio for all pages so iron-list has a stable cell
 *    height before the async render completes. PDFs with mixed page sizes
 *    (e.g., mixed portrait/landscape) will render correctly but iron-list's
 *    virtualisation estimates may be approximate for the non-first aspect.
 *  - No text-layer support (so text cannot be selected/copied). This can be
 *    added later if required — the API exposes `page.getTextContent()`.
 *  - No printing-specific rendering.
 */
import { PolymerElement, html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';
import './tg-pdf-page-spike.js';

// For the spike we pull PDF.js directly from a CDN. In production code this
// would be imported from a bundled path such as `/resources/polymer/lib/pdfjs-lib.js`
// after adding `pdfjs-dist` to `package.json` and updating `rollup.config.js`.
const PDFJS_VERSION = '4.10.38';
const PDFJS_MODULE = `https://cdn.jsdelivr.net/npm/pdfjs-dist@${PDFJS_VERSION}/build/pdf.min.mjs`;
const PDFJS_WORKER = `https://cdn.jsdelivr.net/npm/pdfjs-dist@${PDFJS_VERSION}/build/pdf.worker.min.mjs`;

// Lazy, one-shot import of the PDF.js module. Shared across all instances of
// the element so we don't reload the library for every PDF preview.
let pdfjsLibPromise = null;
const loadPdfJsLib = () => {
    if (pdfjsLibPromise === null) {
        pdfjsLibPromise = import(PDFJS_MODULE).then(lib => {
            lib.GlobalWorkerOptions.workerSrc = PDFJS_WORKER;
            return lib;
        });
    }
    return pdfjsLibPromise;
};

// Horizontal padding applied inside each iron-list cell (see tg-pdf-page-spike
// CSS — `padding: 6px 12px`). We subtract this from the iron-list content
// width to get the target canvas display width.
const CELL_HORIZONTAL_PADDING = 24;

const template = html`
    <style>
        :host {
            display: flex;
            flex-direction: column;
            position: relative;
            background: #f5f5f7;
            font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif;
            color: #222;
        }
        #toolbar {
            flex: 0 0 auto;
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 6px 8px;
            background: #fff;
            border-bottom: 1px solid #ddd;
            font-size: 13px;
        }
        #toolbar button {
            font: inherit;
            padding: 4px 10px;
            border: 1px solid #bbb;
            background: #fafafa;
            border-radius: 3px;
            cursor: pointer;
        }
        #toolbar button[disabled] {
            opacity: 0.5;
            cursor: not-allowed;
        }
        #toolbar .spacer { flex: 1; }
        #main {
            flex: 1 1 auto;
            min-height: 0;
            position: relative;
        }
        iron-list {
            height: 100%;
            /* Reserve the scrollbar gutter so clientWidth is stable whether or
               not the scrollbar is visible — otherwise the fit-to-width
               computation could oscillate as cells grow/shrink. */
            scrollbar-gutter: stable;
            padding: 6px 0;
            box-sizing: border-box;
        }
        #status {
            position: absolute;
            inset: 0;
            padding: 16px;
            background: #f5f5f7;
            color: #666;
            font-style: italic;
        }
        #status.error {
            color: #c00;
            font-style: normal;
            font-weight: bold;
        }
    </style>
    <div id="toolbar">
        <button id="prev" on-click="_prevPage" disabled$="[[_atFirstPage(_visiblePage)]]">&laquo; Prev</button>
        <span>Page [[_visiblePage]] / [[_pageCount]]</span>
        <button id="next" on-click="_nextPage" disabled$="[[_atLastPage(_visiblePage, _pageCount)]]">Next &raquo;</button>
        <span class="spacer"></span>
        <button on-click="_zoomOut">&minus;</button>
        <span>[[_zoomLabel(_scale)]]</span>
        <button on-click="_zoomIn">&plus;</button>
    </div>
    <div id="main">
        <iron-list id="pages" items="[[_pages]]" as="pageNum">
            <template>
                <tg-pdf-page-spike
                    page-num="[[pageNum]]"
                    pdf-doc="[[_pdfDoc]]"
                    aspect-ratio="[[_aspectRatio]]"
                    fit-width="[[_fitWidth]]"
                    scale="[[_scale]]">
                </tg-pdf-page-spike>
            </template>
        </iron-list>
        <div id="status" hidden$="[[_noStatus(_statusMessage)]]" class$="[[_statusClass]]">[[_statusMessage]]</div>
    </div>
`;

class TgPdfPreviewSpike extends PolymerElement {

    static get is() { return 'tg-pdf-preview-spike'; }

    static get template() { return template; }

    static get properties() {
        return {
            src: {
                type: String,
                observer: '_srcChanged'
            },
            _pdfDoc:        { type: Object, value: null },
            // The iron-list items array: simply [1, 2, ..., N].
            _pages:         { type: Array,  value: () => [] },
            _pageCount:     { type: Number, value: 0 },
            // Aspect ratio (width / height) of the first page, reused as the
            // approximation for all pages so iron-list cells get a stable
            // height before the async PDF render completes.
            _aspectRatio:   { type: Number, value: 0 },
            // Target CSS width in pixels for each page at zoom 1.0.
            _fitWidth:      { type: Number, value: 0 },
            // 1-based page number currently at the top of the viewport.
            _visiblePage:   { type: Number, value: 1 },
            // User zoom multiplier (1.0 = fit-to-width).
            _scale:         { type: Number, value: 1.0 },
            _statusMessage: { type: String, value: 'No PDF loaded' },
            _statusClass:   { type: String, value: '' }
        };
    }

    connectedCallback() {
        super.connectedCallback();
        // Re-compute fit-to-width when the container resizes (window resize,
        // phone rotation, entering/leaving full screen, sidebar toggle, etc.).
        this._resizeObserver = new ResizeObserver(() => this._updateFitWidth());
        Promise.resolve().then(() => {
            if (this._resizeObserver && this.$.pages) {
                this._resizeObserver.observe(this.$.pages);
                this._updateFitWidth();
            }
            // Track scroll position to keep "Page X of Y" live as the user scrolls.
            if (this.$.pages) {
                this._onScroll = () => {
                    if (!this._pageCount) return;
                    const first = this.$.pages.firstVisibleIndex;
                    if (first != null) this._visiblePage = first + 1;
                };
                this.$.pages.addEventListener('scroll', this._onScroll);
            }
        });
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        if (this._resizeObserver) {
            this._resizeObserver.disconnect();
            this._resizeObserver = null;
        }
        if (this._onScroll && this.$.pages) {
            this.$.pages.removeEventListener('scroll', this._onScroll);
            this._onScroll = null;
        }
    }

    _updateFitWidth() {
        const list = this.$.pages;
        if (!list) return;
        const fit = Math.max(0, list.clientWidth - CELL_HORIZONTAL_PADDING);
        if (fit > 0 && fit !== this._fitWidth) {
            this._fitWidth = fit;
            // iron-list may need to re-measure cells after they resize.
            Promise.resolve().then(() => list.notifyResize && list.notifyResize());
        }
    }

    _srcChanged(newSrc) {
        // Dispose any previously open document so memory doesn't leak.
        if (this._pdfDoc) {
            try { this._pdfDoc.destroy(); } catch (_) { /* best-effort */ }
            this._pdfDoc = null;
        }
        this._pages = [];
        this._pageCount = 0;
        this._aspectRatio = 0;
        this._visiblePage = 1;
        this._scale = 1.0;

        if (!newSrc) {
            this._setStatus('No PDF loaded');
            return;
        }
        this._setStatus('Loading PDF…');

        loadPdfJsLib()
            // `withCredentials: true` is required so PDF.js sends session cookies on the
            // underlying fetch. TG's `/download-attachment/<id>/<sha1>` endpoint is
            // session-authenticated, and without this the server returns 401/403 and
            // PDF.js surfaces an "UnexpectedResponseException".
            .then(lib => lib.getDocument({ url: newSrc, withCredentials: true }).promise)
            .then(doc => {
                // Guard against a newer `src` having been set while we waited.
                if (this.src !== newSrc) { doc.destroy(); return; }
                // Read page 1 to capture the aspect ratio used for all cells.
                return doc.getPage(1).then(firstPage => {
                    if (this.src !== newSrc) { doc.destroy(); return; }
                    const v = firstPage.getViewport({ scale: 1 });
                    this._aspectRatio = v.width / v.height;
                    this._pdfDoc = doc;
                    this._pageCount = doc.numPages;
                    this._pages = Array.from({ length: doc.numPages }, (_, i) => i + 1);
                    this._visiblePage = 1;
                    this._setStatus('');
                    // Make sure we start at the top and iron-list has fresh metrics.
                    Promise.resolve().then(() => {
                        if (this.$.pages) {
                            if (this.$.pages.notifyResize) this.$.pages.notifyResize();
                            if (this.$.pages.scrollToIndex) this.$.pages.scrollToIndex(0);
                        }
                    });
                    this.dispatchEvent(new CustomEvent('pdf-load-success', {
                        detail: { pageCount: doc.numPages },
                        bubbles: false, composed: false
                    }));
                });
            })
            .catch(err => {
                console.error('[tg-pdf-preview-spike] Failed to load PDF:', err);
                // PDF.js exposes typed errors: PasswordException, InvalidPDFException, MissingPDFException, UnexpectedResponseException.
                const reason = (err && err.name === 'PasswordException') ? 'PDF is password-protected.'
                             : (err && err.name === 'InvalidPDFException') ? 'PDF is corrupt or not a valid PDF.'
                             : (err && err.name === 'MissingPDFException') ? 'PDF could not be found.'
                             : 'Failed to load PDF.';
                this._setStatus(reason, /*isError=*/true);
                this.dispatchEvent(new CustomEvent('pdf-load-error', {
                    detail: { error: err, reason },
                    bubbles: false, composed: false
                }));
            });
    }

    _prevPage() {
        if (!this._pageCount) return;
        this._scrollToPage(Math.max(1, this._visiblePage - 1));
    }

    _nextPage() {
        if (!this._pageCount) return;
        this._scrollToPage(Math.min(this._pageCount, this._visiblePage + 1));
    }

    _scrollToPage(pageNum) {
        const list = this.$.pages;
        if (!list || !list.scrollToIndex) return;
        list.scrollToIndex(pageNum - 1);
        this._visiblePage = pageNum;
    }

    _zoomIn() {
        this._scale = Math.min(4.0, +(this._scale + 0.25).toFixed(2));
        this._notifyListResize();
    }

    _zoomOut() {
        this._scale = Math.max(0.25, +(this._scale - 0.25).toFixed(2));
        this._notifyListResize();
    }

    _notifyListResize() {
        // Stamped cells resize themselves synchronously via the `scale`
        // binding's observer. Defer notifyResize to the next microtask so
        // iron-list remeasures after the DOM has been updated.
        Promise.resolve().then(() => {
            if (this.$.pages && this.$.pages.notifyResize) {
                this.$.pages.notifyResize();
            }
        });
    }

    _setStatus(msg, isError = false) {
        this._statusMessage = msg;
        this._statusClass = isError ? 'error' : '';
    }

    _noStatus(msg) { return !msg; }
    _hasStatus(msg) { return !!msg; }
    _atFirstPage(cur) { return !cur || cur <= 1; }
    _atLastPage(cur, total) { return !total || cur >= total; }
    _zoomLabel(scale) { return Math.round(scale * 100) + '%'; }
}

customElements.define(TgPdfPreviewSpike.is, TgPdfPreviewSpike);
