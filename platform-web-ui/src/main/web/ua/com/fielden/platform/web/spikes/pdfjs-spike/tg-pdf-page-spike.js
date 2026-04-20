/**
 * `tg-pdf-page-spike`
 *
 * Renders a single PDF page to a `<canvas>` with an overlaid text layer, so
 * users can select and copy text from the PDF. Designed to be stamped inside
 * `<iron-list>` so that large PDFs can be scrolled with virtualised rendering
 * — iron-list recycles cells, and whenever this element's `pageNum` changes
 * (or any other input), the observer re-renders both the canvas bitmap and
 * the text layer for the new page.
 *
 * DOM structure
 * -------------
 *   <div class="pageContainer">
 *       <canvas/>                        <- rasterised page
 *       <div class="textLayer"/>         <- transparent spans for selection
 *   </div>
 *
 * The text layer sits on top of the canvas and holds invisible spans whose
 * positions match the glyphs drawn on the canvas. Browser text selection then
 * falls through to those spans, letting the user copy text from the PDF.
 *
 * Inputs
 * ------
 *  - `pdfDoc`      — the PDFDocumentProxy returned by `pdfjsLib.getDocument(...)`.
 *  - `pageNum`     — 1-based page number to render.
 *  - `aspectRatio` — width / height of the source page (used for an immediate
 *                    stable canvas size before the async render completes, so
 *                    iron-list has something to measure).
 *  - `fitWidth`    — target CSS width in pixels at scale 1 (the "fit" width).
 *  - `scale`       — user zoom multiplier on top of the fit width (1.0 = fit).
 */
import { PolymerElement, html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { loadPdfJsLib } from './pdfjs-loader.js';

class TgPdfPageSpike extends PolymerElement {

    static get is() { return 'tg-pdf-page-spike'; }

    static get template() {
        return html`
            <style>
                :host {
                    display: block;
                    box-sizing: border-box;
                    padding: 6px 12px;
                }
                .pageContainer {
                    position: relative;
                    margin: 0 auto;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                    background: #fff;
                }
                canvas {
                    display: block;
                }
                /* Text layer: absolutely positioned over the canvas so its
                   transparent spans line up with the rasterised glyphs below.
                   The browser's native text selection lands on the spans and
                   produces visible feedback via '::selection'. CSS below is
                   kept close to pdfjs-dist's text_layer_builder.css so the
                   behaviour matches what the PDF.js TextLayer class expects. */
                .textLayer {
                    position: absolute;
                    text-align: initial;
                    left: 0;
                    top: 0;
                    right: 0;
                    bottom: 0;
                    overflow: hidden;
                    opacity: 1;
                    line-height: 1;
                    -webkit-text-size-adjust: none;
                    text-size-adjust: none;
                    forced-color-adjust: none;
                    transform-origin: 0 0;
                    caret-color: CanvasText;
                    z-index: 2;
                }
                .textLayer span,
                .textLayer br {
                    color: transparent;
                    position: absolute;
                    white-space: pre;
                    cursor: text;
                    transform-origin: 0% 0%;
                }
                /* Pdfjs-dist pattern: promote direct-child spans (and the
                   non-marked-content spans nested inside a .markedContent
                   wrapper) above the '.endOfContent' sentinel at z-index: 0,
                   so pointer events reach the text rather than the sentinel. */
                .textLayer > :not(.markedContent),
                .textLayer .markedContent span:not(.markedContent) {
                    z-index: 1;
                }
                .textLayer span.markedContent {
                    top: 0;
                    height: 0;
                }
                .textLayer span[role=img] {
                    user-select: none;
                    cursor: default;
                }
                .textLayer ::selection {
                    background: rgba(0, 0, 255, 0.25);
                }
                .textLayer ::-moz-selection {
                    background: rgba(0, 0, 255, 0.25);
                }
                /* Chromium-only: without this, selecting across multiple lines
                   also highlights the intervening <br> glyphs and produces
                   horizontal "interlace" stripes across the page. */
                .textLayer br::selection {
                    background: transparent;
                }
                /* PDF.js appends a sentinel '.endOfContent' div inside the text
                   layer. While the user is actively dragging (PDF.js toggles
                   '.selecting' on the layer), this sentinel snaps to 'top: 0'
                   so the selection can extend beyond the last rendered glyph.
                   Otherwise it sits below the page, out of the way. */
                .textLayer .endOfContent {
                    display: block;
                    position: absolute;
                    left: 0;
                    right: 0;
                    top: 100%;
                    bottom: 0;
                    z-index: 0;
                    cursor: default;
                    user-select: none;
                }
                .textLayer.selecting .endOfContent {
                    top: 0;
                }
            </style>
            <div id="pageContainer" class="pageContainer">
                <canvas id="canvas"></canvas>
                <div id="textLayer" class="textLayer"></div>
            </div>
        `;
    }

    static get properties() {
        return {
            pageNum:     { type: Number },
            pdfDoc:      { type: Object },
            aspectRatio: { type: Number },
            fitWidth:    { type: Number },
            scale:       { type: Number }
        };
    }

    static get observers() {
        return [ '_renderPage(pageNum, pdfDoc, fitWidth, scale, aspectRatio)' ];
    }

    _renderPage(pageNum, pdfDoc, fitWidth, scale, aspectRatio) {
        if (!pdfDoc || !pageNum || !fitWidth || !scale || !aspectRatio) return;
        const canvas = this.$.canvas;
        const pageContainer = this.$.pageContainer;
        const textLayerDiv = this.$.textLayer;
        const dpr = window.devicePixelRatio || 1;

        // Give the container (and canvas inside it) a stable size immediately
        // so that iron-list has a concrete cell height to measure, even before
        // the async PDF render has produced a bitmap. Uses the supplied
        // aspectRatio (captured from page 1 by the parent) as an approximation.
        const displayW = fitWidth * scale;
        const displayH = displayW / aspectRatio;
        pageContainer.style.width = displayW + 'px';
        pageContainer.style.height = displayH + 'px';
        canvas.style.width = displayW + 'px';
        canvas.style.height = displayH + 'px';
        canvas.width = Math.floor(displayW * dpr);
        canvas.height = Math.floor(displayH * dpr);

        // Clear any stale text spans left from a previous render of this cell.
        // Iron-list recycles cells, so a cell that previously showed page 3
        // must not leak those spans when it's reassigned to page 10.
        while (textLayerDiv.firstChild) textLayerDiv.removeChild(textLayerDiv.firstChild);

        // Cancel any in-flight canvas or text-layer render for the previous
        // page — happens when iron-list reassigns this cell while the earlier
        // render is still in progress.
        if (this._renderTask) {
            try { this._renderTask.cancel(); } catch (_) { /* best-effort */ }
            this._renderTask = null;
        }
        if (this._textLayer) {
            try { this._textLayer.cancel(); } catch (_) { /* best-effort */ }
            this._textLayer = null;
        }

        const requested = pageNum;
        Promise.all([loadPdfJsLib(), pdfDoc.getPage(requested)]).then(([pdfjsLib, page]) => {
            // Guard against the cell's bound page having changed while we awaited.
            if (this.pageNum !== requested) return;
            const natural = page.getViewport({ scale: 1 });
            const fitScale = fitWidth / natural.width;
            const effective = fitScale * scale;
            // Two viewports: the canvas bitmap is rendered at 'effective * dpr'
            // for HiDPI crispness, but the text layer uses CSS pixels so the
            // selectable spans line up with what the user visually sees.
            const viewport = page.getViewport({ scale: effective * dpr });
            const cssViewport = page.getViewport({ scale: effective });
            const cssW = Math.floor(cssViewport.width);
            const cssH = Math.floor(cssViewport.height);
            pageContainer.style.width = cssW + 'px';
            pageContainer.style.height = cssH + 'px';
            canvas.width = Math.floor(viewport.width);
            canvas.height = Math.floor(viewport.height);
            canvas.style.width = cssW + 'px';
            canvas.style.height = cssH + 'px';

            // Kick off canvas render and text-layer render in parallel. Text
            // selection is a "nice to have"; if the text layer fails (e.g. the
            // PDF's content stream disallows text extraction), we still want
            // the bitmap visible, so we swallow text-layer errors locally
            // instead of propagating them to the outer catch.
            this._renderTask = page.render({
                canvasContext: canvas.getContext('2d'),
                viewport
            });
            this._textLayer = new pdfjsLib.TextLayer({
                textContentSource: page.streamTextContent(),
                container: textLayerDiv,
                viewport: cssViewport
            });
            return Promise.all([
                this._renderTask.promise,
                this._textLayer.render().catch(err => {
                    console.warn('[tg-pdf-page-spike] text layer render failed for page ' + requested, err);
                })
            ]);
        }).catch(err => {
            if (err && err.name === 'RenderingCancelledException') return;
            console.error('[tg-pdf-page-spike] render failed for page ' + requested, err);
        });
    }
}

customElements.define(TgPdfPageSpike.is, TgPdfPageSpike);
