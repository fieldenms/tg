import {LitElement, html, css} from 'lit';
import {customElement, property} from 'lit/decorators.js';

import '@fieldenms/tg-web-components/components/tg-accordion.js'

@customElement('element-with-accordion')
export class ElementWithAccordion extends LitElement {

    render () {
        return html`
        <tg-accordion heading="Test 1" opened>
            <slot></slot>
        </tg-accordion>`;
    }
}

declare global {
    interface HTMLElementTagNameMap {
      'element-with-accordion': ElementWithAccordion;
    }
  }