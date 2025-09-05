
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { Html5Qrcode } from '/resources/polymer/lib/html5-qrcode-lib.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';

import '/resources/components/tg-dropdown-switch.js';
import '/resources/editors/tg-singleline-text-editor.js';
import '/resources/editors/tg-boolean-editor.js';

import { tearDownEvent, localStorageKey, createDummyBindingEntity, isMobileApp} from '/resources/reflection/tg-polymer-utils.js';

import {TgReflector} from '/app/tg-reflector.js';

const SCAN_AND_APPLY = "scanAndApply";
const CAMERA_ID = "cameraId";

function calculatePrefferedVideoSize(scannerElement, mobile) {
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;

    const controlDimension = scannerElement.$.controls.getBoundingClientRect();
    //TODO shaould take into account the maximum dimension of a window
    const dims = mobile ? 
            {width: windowWidth, height: windowHeight - controlDimension.height} :
            {width: Math.min(windowWidth - windowWidth * 0.1, 600), height: Math.min(windowHeight - windowHeight * 0.1 - controlDimension.height, 600)};
    dims.aspectRatio = calculateAspectRation(dims.width, dims.height);

    return dims;
}

function calculateAspectRation(width, height) {
    return width < height ? height/width : width/height;
}


function qrboxFunction(viewfinderWidth, viewfinderHeight) {
    let minEdgePercentage = 0.7; // 70%
    let minEdgeSize = Math.min(viewfinderWidth, viewfinderHeight);
    let qrboxSize = Math.min(Math.floor(minEdgeSize * minEdgePercentage), 250);
    return {
        width: qrboxSize,
        height: qrboxSize
    };
}

function getCameraIndex(cameras) {
    if (cameras && cameras.length > 0) {
        const savedCamera = localStorage.getItem(localStorageKey(CAMERA_ID));
        if (savedCamera) {
            const idx = cameras.findIndex(camera => camera.id === savedCamera);
            return idx >= 0 ? idx : 0;
        }
        return 0;
    }
}

function saveCamera(camera) {
    if (camera) {
        localStorage.setItem(localStorageKey(CAMERA_ID), camera.id);
    }
}

const template = html`
    <style>
        paper-dialog {
             @apply --layout-vertical;
        }
        #videoSlot {
            @apply --layout-vertical;
            @apply --layout-center
        }
        #controls {
            @apply --layout-vertical;
            padding: 0 20px;
        }
        .buttons {
            padding: 20px;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
        }
        paper-button {
            margin-top: 20px;
            margin-left: 20px;
            width: 80px;
        }
        paper-button:first-child {
            margin-left: 0;
        }
        paper-button.blue {
            color: var(--paper-light-blue-500);
            --paper-button-flat-focus-color: var(--paper-light-blue-50);
        }
        paper-button:hover {
            background: var(--paper-light-blue-50);
        }
        [mobile] {
            position: fixed;
            margin: 0;
            top: 0;
            left: 0;
            min-width: 100%;
            min-height: 100%;
        }
    </style>
    <paper-dialog id="qrCodeScanner"
        modal
        always-on-top
        entry-animation="scale-up-animation"
        exit-animation="fade-out-animation"
        on-iron-overlay-opened="_qrCodeScannerOpened"
        on-iron-overlay-closed="_qrCodeScannerClosed"
        mobile$="[[mobile]]">
        <div id="videoSlot" class="no-padding">
            <slot id="scannerSlot" name="scanner"></slot>
        </div>
        <div id="controls" class="no-padding">
            <tg-dropdown-switch id="camearSelector" class ="editor" views="[[_cameras]]" raised make-drop-down-width-the-same-as-button change-current-view-on-select on-tg-centre-view-change="_changeCamera"></tg-dropdown-switch>
            <tg-singleline-text-editor id="textEditor" class ="editor" entity='[[_entity]]' property-name='scannedValue' prop-title='Scanned value' 
                    prop-desc='Contains text scanned from Bar or QR code' current-state='EDIT' 
                    validation-callback='[[_validate]]' toaster='[[toaster]]'></tg-singleline-text-editor>
            <tg-boolean-editor id='scanAndApplyEditor' class ="editor" entity='[[_entity]]' property-name='scanAndApply' prop-title='Scan & apply?' 
                        prop-desc='Determines whether the scanned value should be applied immediately or not.' current-state='EDIT' 
                        validation-callback='[[_validate]]' toaster='[[toaster]]'></tg-boolean-editor>
            <div class="buttons">
                <paper-button raised roll="button" on-tap="_cancelScan">CLOSE</paper-button>
                <paper-button raised roll="button" on-tap="_scanAgain">SCAN</paper-button>
                <paper-button raised roll="button" class="blue" on-tap="_applyScane">APPLY</paper-button>
            </div>
        </div>
    </paper-dialog>`; 

class TgQrCodeScanner extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            mobile: {
                type: Boolean,
                value: isMobileApp(),
                readOnly: true,
                reflectToAttribute: true
            },
            toaster: Object,

            closeCallback: Function,

            applyCallback: Function,

            //private properties
            _videoFeedElement: Object,

            _cameras: Array,

            _cameraIndex: {
                type: Number,
                value: 0
            },

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
            {'scannedValue': {value: '', editable: true},
             'scanAndApply': {value: false, editable: true}},
             (name) => {
                return {
                    type: () => name === 'scannedValue' ? 'string' : 'boolean'
                }
            }
        );
        this._validate = function () {};
        this.addEventListener("addon-attached", this._onAddonAttached.bind(this));
    }

    ready() {
        super.ready();
        //Override onChange method on scanAndApply boolean editor to save value into localstorage after it was changed.
        const oldOnChang = this.$.scanAndApplyEditor._onChange.bind(this.$.scanAndApplyEditor);
        this.$.scanAndApplyEditor._onChange = (e) => {
            oldOnChang(e);
            localStorage.setItem(localStorageKey(SCAN_AND_APPLY), this.$.scanAndApplyEditor._editingValue);
        }
        //Creates scanner if node in the light DOM of this element has id attribute
        //The node in the light DOM should be attributed with slot="scanner"
        const elements = this.$.scannerSlot.assignedNodes();
        if (elements.length > 0) {
            this._videoFeedElement = elements[0];
            const scannerId = this._videoFeedElement.getAttribute("id");
            this._scanner = scannerId && new Html5Qrcode(scannerId);
        }
    }

    open() {
        if (this._scanner) {
            // This method will trigger user permissions
            Html5Qrcode.getCameras().then(devices => {
                /**
                 * devices would be an array of objects of type:
                 * { id: "id", label: "label" }
                 */
                if (devices && devices.length) {
                    this._cameras = devices.map((device, idx) => {return {index: idx, id: device.id, title: device.label, desc: device.label};});
                    this.$.camearSelector.viewIndex = getCameraIndex(this._cameras);
                    //TODO remove next line after testing
                    this._cameras.forEach(camera => console.log(`cameraId: ${camera.index}, camear label: ${camera.title}`));
                    this._resetState();
                    this.$.scanAndApplyEditor._editingValue = localStorage.getItem(localStorageKey(SCAN_AND_APPLY)) || 'false';
                    this.$.scanAndApplyEditor.commitIfChanged();
                    this.$.qrCodeScanner.open();
                } else {
                    this.toaster && this.toaster.openToastForError('No cammera error', 'There is no cameras to scan QR or Bar code', true);
                }
            }).catch(err => {
                this.toaster && this.toaster.openToastForError('Camera error', err, true);
            });
        } else {
            this.toaster && this.toaster.openToastForError('Scanner error', 'Please specify element for camera feed inside tg-qr-code-scanner with attribute slot equal to "scanner"', true);
        }
    }

    get scannedText() {
        return this._entity['scannedValue'] ? this._entity['scannedValue'] : '';
    }

    _changeCamera(e) {
        const cameraIdx = e.detail;
        if (this._cameras && this._cameras[cameraIdx]) {
            saveCamera(this._cameras[cameraIdx]);
            if (this._scanner) {
                const width = parseInt(this._videoFeedElement.style.width);
                const height = parseInt(this._videoFeedElement.style.height);
                let stopPromise = null;
                if (!this._scanner.stateManagerProxy.isScanning()) {
                    stopPromise = Promise.resolve();
                } else {
                    stopPromise = this._scanner.stop();
                }
                stopPromise && stopPromise.then(() => {
                    return this._scanner.start(this._cameras[cameraIdx].id,  { fps: 10, aspectRatio: calculateAspectRation(width, height), qrbox: qrboxFunction },
                        this._successfulScan.bind(this), this._faildScan.bind(this)
                    );
                }).catch(err => {
                    this.toaster && this.toaster.openToastForError('Camera error', err, true);
                }) 
                
            }
        }
    }

    _qrCodeScannerOpened(e) {
        if (this._scanner && e.target === this.$.qrCodeScanner) {
            const dims = calculatePrefferedVideoSize(this);
            this._videoFeedElement.style.width = dims.width + "px";
            this._videoFeedElement.style.height = dims.height + "px";
            this.$.qrCodeScanner.refit();
            this._scanner.start(this._cameras[this.$.camearSelector.viewIndex].id,  { fps: 10, aspectRatio: dims.aspectRatio, qrbox: qrboxFunction },
                this._successfulScan.bind(this), this._faildScan.bind(this)
            ).catch((err) => {
                this.toaster && this.toaster.openToastForError('Camera error', err, true);
            });
        }
    }

    _qrCodeScannerClosed(e) {
        if (e.target === this.$.qrCodeScanner) {
            this._scanner.stop();
            if (this.closeCallback) {
                this.closeCallback();
            } 
        }
    }

    _successfulScan (decodedText, decodedResult) {
        this.$.textEditor.assignConcreteValue(decodedText, this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
        if (this._entity['scanAndApply']) {
            this._applyScane();
        } else {
            this._scanner.pause(true);
        }
    }

    _resetState() {
        this.$.textEditor.assignConcreteValue('', this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
    }

    _scanAgain(e) {
        if (this._scanner.stateManagerProxy.isPaused()) {
            this._resetState();
            this._scanner.resume();
        }
    }
            
    _cancelScan() {
        this.$.qrCodeScanner.cancel();
    }
    
    _applyScane() {
        this.$.textEditor.commitIfChanged();
        this.$.qrCodeScanner.close();
        if (this.applyCallback) {
            this.applyCallback(this.scannedText);
        }
    }

    _onAddonAttached(e) {
        tearDownEvent(e);
    }
    
    _faildScan(error) {
        //console.error(error);
    }
}

customElements.define('tg-qr-code-scanner', TgQrCodeScanner);