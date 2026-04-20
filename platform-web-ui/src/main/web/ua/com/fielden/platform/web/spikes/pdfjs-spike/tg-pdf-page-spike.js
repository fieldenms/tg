/**
 * `tg-pdf-page-spike`
 *
 * Renders a single PDF page to a `<canvas>`. Designed to be stamped inside
 * `<iron-list>` so that large PDFs can be scrolled with virtualised rendering —
 * iron-list recycles cells, and whenever this element's `pageNum` changes (or
 * any other input), the observer re-renders the canvas for the new page.
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
                canvas {
                    display: block;
                    margin: 0 auto;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                    background: #fff;
                }
            </style>
            <canvas id="canvas"></canvas>
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
        const dpr = window.devicePixelRatio || 1;

        // Give the canvas a stable size immediately so that iron-list has a
        // concrete cell height to measure, even before the async PDF render
        // has produced a bitmap. Uses the supplied aspectRatio (captured from
        // page 1 by the parent) as the approximation.
        const displayW = fitWidth * scale;
        const displayH = displayW / aspectRatio;
        canvas.style.width = displayW + 'px';
        canvas.style.height = displayH + 'px';
        canvas.width = Math.floor(displayW * dpr);
        canvas.height = Math.floor(displayH * dpr);

        // Cancel any in-flight render — happens when iron-list reassigns this
        // cell to a different page while we're still rendering the previous one.
        if (this._renderTask) {
            try { this._renderTask.cancel(); } catch (_) { /* best-effort */ }
            this._renderTask = null;
        }

        const requested = pageNum;
        pdfDoc.getPage(requested).then(page => {
            // Guard against the cell's bound page having changed while we awaited.
            if (this.pageNum !== requested) return;
            const natural = page.getViewport({ scale: 1 });
            const fitScale = fitWidth / natural.width;
            const effective = fitScale * scale;
            const viewport = page.getViewport({ scale: effective * dpr });
            canvas.width = Math.floor(viewport.width);
            canvas.height = Math.floor(viewport.height);
            canvas.style.width = Math.floor(viewport.width / dpr) + 'px';
            canvas.style.height = Math.floor(viewport.height / dpr) + 'px';
            this._renderTask = page.render({
                canvasContext: canvas.getContext('2d'),
                viewport
            });
            return this._renderTask.promise;
        }).catch(err => {
            if (err && err.name === 'RenderingCancelledException') return;
            console.error('[tg-pdf-page-spike] render failed for page ' + requested, err);
        });
    }
}

customElements.define(TgPdfPageSpike.is, TgPdfPageSpike);
