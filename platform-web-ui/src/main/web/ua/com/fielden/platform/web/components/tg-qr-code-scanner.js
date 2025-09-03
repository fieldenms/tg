
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { Html5QrcodeScanner } from '/resources/polymer/lib/html5-qrcode-lib.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';

import '/resources/editors/tg-singleline-text-editor.js';

import { tearDownEvent, createDummyBindingEntity} from '/resources/reflection/tg-polymer-utils.js';

import {TgReflector} from '/app/tg-reflector.js';

const template = html`
    <style>
        paper-dialog {
             @apply --layout-vertical;
        }
        .buttons {
            @apply --layout-horizontal;
            @apply --layout-center-justified;
        }
        paper-button.blue {
            color: var(--paper-light-blue-500);
            --paper-button-flat-focus-color: var(--paper-light-blue-50);
        }
        paper-button:hover {
            background: var(--paper-light-blue-50);
        }
        tg-singleline-text-editor {
            margin-top: 0;
            padding: 0 20px 20px 20px;
        }
    </style>
    <paper-dialog id="qrCodeScanner"
        modal
        always-on-top
        entry-animation="scale-up-animation"
        exit-animation="fade-out-animation"
        on-iron-overlay-canceled="_rejectQrScanner"
        on-iron-overlay-opened="_qrCodeScannerOpened"
        on-iron-overlay-closed="_qrCodeScannerClosed">
        <slot id="scannerSlot" name="scanner"></slot>
        <tg-singleline-text-editor id="textEditor" entity='[[_entity]]' property-name='scannedValue' prop-title='Scanned text' 
                prop-desc='Contains text scanned from Bar or QR code' current-state='EDIT' 
                validation-callback='[[_validate]]' toaster='[[toaster]]'></tg-singleline-text-editor>
        <div class="buttons">
            <paper-button raised roll="button" on-tap="_cancelScan">CLOSE</paper-button>
            <paper-button raised roll="button" on-tap="_scanAgain">SCAN</paper-button>
            <paper-button raised roll="button" class="blue" on-tap="_applyScane">APPLY</paper-button>
        </div>
    </paper-dialog>`; 

class TgQrCodeScanner extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            toaster: Object,

            closeCallback: Function,

            applyCallback: Function,

            //private properties
            _scanner: Object,

            _validate: Function,

            _entity: Object,
            
            _reflector: Object
        }
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._entity = createDummyBindingEntity(
            {'scannedValue': {value: '', editable: true}},
            (name) => {
                return {
                    type: () => 'string'
                }
            }
        );
        this._validate = function () {};
        this.addEventListener("addon-attached", this._onAddonAttached.bind(this));
    }

    ready() {
        super.ready();
        //Creates scanner if node in the light DOM of this element has id attribute
        //The node in the light DOM should be attributed with slot="scanner"
        const elements = this.$.scannerSlot.assignedNodes();
        if (elements.length > 0) {
            const scannerElement = elements[0];
            const scannerId = scannerElement.getAttribute("id");
            if (scannerId) {
                this._scanner = new Html5QrcodeScanner(
                    scannerId,
                    { fps: 10, qrbox: {width: 250, height: 250} },
                    /* verbose= */ false);
            }
        }
    }

    open() {
        if (this._scanner) {
            this._resetState();
            this.$.qrCodeScanner.open();
        }
    }

    get scannedText() {
        return this._entity['scannedValue'] ? this._entity['scannedValue'] : '';
    }

    _qrCodeScannerOpened() {
        this._scanner.render(this._successfulScan.bind(this), this._faildScan.bind(this));
    }

    _qrCodeScannerClosed() {
        if (this.closeCallback) {
            this.closeCallback();
        }
    }

    _rejectQrScanner() {

    }

    _successfulScan (decodedText, decodedResult) {
        this.$.textEditor.assignConcreteValue(decodedText, this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
        this._scanner.pause(true);
    }

    _resetState() {
        this.$.textEditor.assignConcreteValue('', this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
    }

    _scanAgain(e) {
        this._resetState();
        this._scanner.resume();
    }
            
    _cancelScan() {
        this._scanner.html5Qrcode.stop().finally(() => {
            this.$.qrCodeScanner.cancel();
        });
    }
    
    _applyScane() {
        this._scanner.html5Qrcode.stop().finally(() => {
            this.$.textEditor.commitIfChanged();
            this.$.qrCodeScanner.close();
            if (this.applyCallback) {
                this.applyCallback(this.scannedText);
            }
        });
    }

    _onAddonAttached(e) {
        tearDownEvent(e);
    }
    
    _faildScan(error) {
        //console.error(error);
    }
}

customElements.define('tg-qr-code-scanner', TgQrCodeScanner);