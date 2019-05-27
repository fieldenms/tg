import { LitElement, html } from '/resources/lit-element/lit-element.js';
import '/resources/editors/mdc.textfield.js';
import {TgReflector} from '/app/tg-reflector.js';

class TgLitEditor extends LitElement {

    static get properties () {
        return {
            propTitle: String,
            entity: Object,
            propertyName: String,
            _editingValue: {
                type: String,
                attribute: false
            }
        }
    }

    constructor () {
        super();
        //Init lit element properties
        this._editingValue = this._defaultEditingValue();
        //Init other class properties
        this._reflector = new TgReflector();
    }

    connectedCallback() {
        if (!this.hasUpdated) {
            
        }
        super.connectedCallback();
    }

    set entity (newEntity) {
        if (!this._reflector.isDotNotated(this.propertyName)) {
            this._reflector.convertPropertyValue(newEntity, this.propertyName, newEntity["@@origin"], null);
        }
        const convertedValue = this._reflector.getBindingValue.bind(this._reflector)(newEntity, this.propertyName);
        this._editingValue = this.convertToString(convertedValue);
    }

    _defaultEditingValue () {
        return '';
    }

    render () {
        return html `
            <link rel="stylesheet" href="/resources/editors/mdc.textfield.css">
            <style>
                :host {
                    display: flex;
                    flex-direction: row;
                }
                .mdc-text-field {
                    flex: 1;
                }
            </style>
            <div class="mdc-text-field">
                <input type="text" id="myinput" .value=${this._editingValue} class="mdc-text-field__input">
                <label class="mdc-floating-label" for="myinput">${this.propTitle}</label>
                <div class="mdc-line-ripple"></div>
            </div>`;
    }

    firstUpdated(changedProperties) {
        this.addEventListener("input", this._inputHandler);
        new mdc.textfield.MDCTextField(this.shadowRoot.querySelector('.mdc-text-field'));
    }

    updated(changedProperties) { 
    }

    _inputHandler(e) {
        this._editingValue = this.shadowRoot.querySelector("#myinput").value;
    }

    convertToString (value) {
        return value === null ? "" : "" + value;
    }
}

customElements.define('tg-lit-editor', TgLitEditor);