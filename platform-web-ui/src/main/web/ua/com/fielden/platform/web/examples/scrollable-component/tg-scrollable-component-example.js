import {PolymerElement} from '@fieldenms/tg-polymer/polymer/polymer-element.js';
import {html} from '@fieldenms/tg-polymer/polymer/lib/utils/html-tag.js';
import '@fieldenms/tg-polymer/paper-styles/paper-styles-classes.js';
import '@fieldenms/tg-polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '@fieldenms/tg-web-components/components/tg-scrollable-component.js';

class TgScrollableComponentExample extends PolymerElement {
    
    static get template () {
        return html`
            <style>
                .high-region {
                    height: 450px;
                }
            </style>
            <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
            <tg-scrollable-component class="fit">
                <div class="layout vertical" style="min-height:fit-content">
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div class="high-region"></div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                    <div>TEST</div>
                </div>
            </tg-scrollable-component>`;
      }
}

customElements.define('tg-scrollable-component-example', TgScrollableComponentExample);