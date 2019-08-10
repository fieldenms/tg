import {PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import './tg-scrollable-component.js';

class TgScrollableComponentExample extends PolymerElement {
    
    static get template () {
        return html`
            <style>
                .high-region {
                    height: 450px;
                }
            </style>
            <style is="custom-style" include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
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