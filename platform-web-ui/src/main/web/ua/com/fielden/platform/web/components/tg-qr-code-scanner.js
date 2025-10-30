
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { Html5Qrcode } from '/resources/polymer/lib/html5-qrcode-lib.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';

import '/resources/components/tg-dropdown-switch.js';
import '/resources/editors/tg-singleline-text-editor.js';
import '/resources/editors/tg-boolean-editor.js';

import { tearDownEvent, localStorageKey, createStubBindingEntity, isMobileApp} from '/resources/reflection/tg-polymer-utils.js';
import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';

import {TgReflector} from '/app/tg-reflector.js';

const SCAN_AND_APPLY = 'scanAndApply';
const CAMERA_ID = 'cameraId';
const SEPARATOR = 'scanSeparator';

const DEFAULT_SCANNER_SIZE = 400;

function calculateAspectRatio(width, height) {
    return width/height;
}


function qrboxFunction(viewfinderWidth, viewfinderHeight) {
    const minEdgePercentage = 0.7; // 70%
    const minEdgeSize = Math.min(viewfinderWidth, viewfinderHeight);
    const qrboxSize = Math.min(Math.floor(minEdgeSize * minEdgePercentage), 250);
    
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

function getSeparator() {
    return localStorage.getItem(localStorageKey(SEPARATOR)) || '';
}

function saveSeparator(separator) {
    if (separator) {
        localStorage.setItem(localStorageKey(SEPARATOR), separator);
    } else {
        localStorage.removeItem(localStorageKey(SEPARATOR));
    }
}

const template = html`
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <style>
        paper-dialog {
            overflow: hidden;
            @apply --layout-vertical;
        }
        #videoSlot {
            position: relative;
            overflow: hidden;
            @apply --layout-vertical;
            @apply --layout-center;
        }
        #controls {
            margin: 0;
            padding: 20px;
            @apply --layout-vertical;
        }
        #cameraLoadingView {
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0; 
            padding: 20px;
            background-color: white;
            font-size: 18px;
            color: var(--paper-grey-400);
            @apply --layout-horizontal;
            @apply --layout-center-center;
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
            <div id="cameraLoadingView" hidden$="[[!_showCameraLoadingView]]">[[_loadingMsg]]</div>
        </div>
        <div id="controls" class="no-padding">
            <tg-dropdown-switch id="cameraSelector" class ="editor" style="padding-bottom: 12px" views="[[_cameras]]" dropdown-button-tooltip-text="Select camera" raised make-drop-down-width-the-same-as-button change-current-view-on-select on-tg-centre-view-change="_changeCamera"></tg-dropdown-switch>
            <tg-singleline-text-editor id="textEditor" class ="editor" entity="[[_entity]]" property-name="scannedValue" prop-title="Scanned Value" 
                    prop-desc="Contains text scanned from a QR or barcode" current-state="EDIT" 
                    validation-callback="[[_validate]]" toaster="[[toaster]]" hide-qr-code-scanner></tg-singleline-text-editor>
            <div class="layout horizontal justified">
                <tg-boolean-editor id="scanAndApplyEditor" class ="editor flex" style="margin-right:20px;" entity="[[_entity]]" property-name="scanAndApply" prop-title="Scan & apply?" 
                        prop-desc="Determines whether the scanned value should be applied immediately or not" current-state="EDIT" 
                        validation-callback="[[_validate]]" toaster="[[toaster]]"></tg-boolean-editor>
                <tg-singleline-text-editor id="separatorEditor" class ="editor flex" entity="[[_entity]]" property-name="separator" prop-title="Append w/ Separator (\\s, \\n, \\t)"
                        prop-desc="Separator to prepend to the scanned value.<br>A comma or a semicolon is good choice.<br>Use \\s for a space, \\n for a new line, \\t for a tab." current-state="EDIT"
                        validation-callback="[[_validate]]" toaster="[[toaster]]" hide-qr-code-scanner></tg-singleline-text-editor>
            </div>
            <div class="buttons">
                <paper-button raised roll="button" tooltip-text="Close dialog" on-tap="_cancelScan"><span>CLOSE</span></paper-button>
                <paper-button raised roll="button" tooltip-text="Auto-restart scanner" on-tap="_scanAgain"><span>SCAN</span></paper-button>
                <paper-button raised roll="button" tooltip-text="Accept scanned value" class="blue" on-tap="_applyScan"><span>APPLY</span></paper-button>
            </div>
        </div>
    </paper-dialog>`; 

class TgQrCodeScanner extends mixinBehaviors([TgTooltipBehavior], PolymerElement) {

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

            //Determines whether to show loading camera view after resize started
            _showCameraLoadingView:{
                type: Boolean,
                value: false
            },

            //Indicates whether this qr code scanner can be closed.
            _canClose: {
                type: Boolean,
                value: false
            },

            //Message displayed when _showCameraLoadingView property is true (i.e. when camera loading view is visible)
            _loadingMsg: String,

            _scanner: Object,

            _validate: Function,

            _entity: Object,
            
            _reflector: Object
        }
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._entity = createStubBindingEntity('QrCodeScannerEntity',
            {'scannedValue': {value: '', editable: false},
             'scanAndApply': {value: false, editable: true},
             'separator': {value: '', editable: true}},
             (name) => {
                return {
                    type: () => name === 'scanAndApply' ? 'boolean' : 'string'
                }
            }
        );
        this._validate = () => {};
        this.addEventListener('addon-attached', this._onAddonAttached.bind(this));
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
        //Handle resize event to adjust dimensions of video feed element
        const oldResize = this.$.qrCodeScanner._onIronResize.bind(this.$.qrCodeScanner);
        this.$.qrCodeScanner._onIronResize = (e) => {
            oldResize();
            const target = e.target;
            setTimeout(() => this._qrCodeScannerResized(target), 1);
        }
        //Override refit to show correct size when dialog get opened instead of changing it's size after it was opened.
        const oldRefit = this.$.qrCodeScanner.refit.bind(this.$.qrCodeScanner);
        this.$.qrCodeScanner.refit = (e) => {
            oldRefit();
            const windowWidth = window.innerWidth;
            const windowHeight = window.innerHeight;

            const controlDimension = this.$.controls.getBoundingClientRect();
            const scannerStyles = window.getComputedStyle(this.$.qrCodeScanner);
            const dims = isMobileApp() ? 
                    {width: windowWidth, height: Math.max(0, windowHeight - controlDimension.height)} :
                    {width: Math.min(parseInt(scannerStyles.maxWidth), DEFAULT_SCANNER_SIZE), height: Math.max(0, Math.min(parseInt(scannerStyles.maxHeight) - controlDimension.height, DEFAULT_SCANNER_SIZE))};
            this._videoFeedElement.style.width = dims.width + 'px';
            this._videoFeedElement.style.height = dims.height + 'px';
            oldRefit();
        }
        // This listener is needed to close the camera when the user switches to another page.  
        // The user can start the camera again upon returning to this page and pressing "SCAN".
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
                this._qrCodeScannerResized(this.$.qrCodeScanner);
            } 
        });
    }

    open() {
        if (this._scanner) {
            this._resetState();
            this.$.qrCodeScanner.open();
        } else {
            this._showError('Scanner error', 'Please specify element for camera feed inside tg-qr-code-scanner with slot attribute equal to "scanner"');
        }
    }

    get scannedText() {
        return this._entity['scannedValue'] ? this._entity['scannedValue'] : '';
    }

    get separator() {
        return this._entity['separator'] ? this._entity['separator'] : '';
    }

    _changeCamera(e) {
        const cameraIdx = e.detail;
        if (this._cameras && this._cameras[cameraIdx]) {
            saveCamera(this._cameras[cameraIdx]);
            if (this._scanner) {
                if (!this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState) {
                    let stopPromise = null;
                    if (!this._scanner.isScanning) {
                        stopPromise = Promise.resolve();
                    } else {
                        stopPromise = this._scanner.stop();
                    }
                    const width = parseInt(this._videoFeedElement.style.width);
                    const height = parseInt(this._videoFeedElement.style.height);
                    stopPromise && stopPromise.then(() => {
                        this._startCamera(width, height, calculateAspectRatio(width, height));
                    });
                } else {
                    setTimeout(() => this._changeCamera(e), 100);
                }
            }
        }
    }

    _qrCodeScannerOpened(e) {
        if (this._scanner && e.target === this.$.qrCodeScanner) {
            this._showBlockingPane("Loading...");
            // This method will trigger user permissions
            Html5Qrcode.getCameras().then(devices => {
                /**
                 * devices would be an array of objects of type:
                 * { id: "id", label: "label" }
                 */
                if (devices && devices.length) {
                    this._cameras = devices.map((device, idx) => {return {index: idx, id: device.id, title: device.label, desc: device.label};});
                    this.$.cameraSelector.viewIndex = getCameraIndex(this._cameras);
                    const width = parseInt(this._videoFeedElement.style.width);
                    const height = parseInt(this._videoFeedElement.style.height);
                    this._startCamera(width, height, calculateAspectRatio(width, height));
                } else {
                    this._showError('No cammera error', 'There is no cameras to scan QR or Bar code');
                }
            }).catch(err => {
                this._showError('Camera error', err);
            });
        }
    }

    _qrCodeScannerResized(target) {
        if (target === this.$.qrCodeScanner && this.$.qrCodeScanner.opened && 
            this._scanner && !this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState && this._scanner.isScanning) {
                this._scanner.stop().then(() => {
                    this._showBlockingPane("Tap SCAN, to start scanning");
                    this._canClose = true;
                });
        }
    }

    _startCamera(width, height, aspectRatio) {
        if (qrboxFunction.bind(this)(width, height).width < 50) {
            this._showError('Camera error', 'The size of the scanner box is less than 50px. Please adjust your camera to make the video feed area larger.');
        } else if (this._scanner && this._cameras && this._cameras.length > 0 && !this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState && !this._scanner.isScanning) {
            this._showBlockingPane("Loading...");
            this._scanner.start(this._cameras[this.$.cameraSelector.viewIndex].id,  { fps: 10, aspectRatio: aspectRatio, qrbox: qrboxFunction.bind(this) },
                this._successfulScan.bind(this), this._faildScan.bind(this)
            ).then(() => {
                this._scanner.applyVideoConstraints({
                    width: this._videoFeedElement.clientWidth,
                    height: this._videoFeedElement.clientHeight
                });
                this._hideBlockingPane();
            }).catch((err) => {
                this._showError('Camera error', err);
            });
        } else {
            this._showError("Camera error", "Could not start camera");
        }
    }

    _showBlockingPane(msg) {
        this._loadingMsg = msg;
        this._showCameraLoadingView = true;
    }

    _showError(shortMsg, extMsg) {
        this.toaster && this.toaster.openToastForError(shortMsg, extMsg.message || extMsg, true);
        this._canClose = true;
        this._showBlockingPane(extMsg.message || extMsg);
    } 

    _hideBlockingPane() {
        this._showCameraLoadingView = false;
    }

    _qrCodeScannerClosed(e) {
        if (e.target === this.$.qrCodeScanner) {
            if (this._scanner && this._scanner.isScanning) {
                this._scanner.stop();
            }
            if (this.closeCallback) {
                this.closeCallback();
            } 
        }
    }

    _successfulScan (decodedText, decodedResult) {
        this.$.textEditor.assignConcreteValue(decodedText, this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
        if (this._scanner.isScanning) {
            this._scanner.pause(true);
            if (this._entity['scanAndApply']) {
                this._applyScan();
            }
        }
    }

    _resetState() {
        this.$.textEditor.assignConcreteValue('', this._reflector.tg_convert.bind(this._reflector));
        this.$.textEditor.commitIfChanged();
        this.$.scanAndApplyEditor.assignConcreteValue(localStorage.getItem(localStorageKey(SCAN_AND_APPLY)) === 'true' || false, this._reflector.tg_convert.bind(this._reflector));
        this.$.scanAndApplyEditor.commitIfChanged();
        this.$.separatorEditor.assignConcreteValue(getSeparator(), this._reflector.tg_convert.bind(this._reflector));
        this.$.separatorEditor.commitIfChanged();
        this._canClose = false;
        this._hideBlockingPane();
    }

    _scanAgain(e) {
        if (!this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState) {
            if (!this._scanner.isScanning) {
                const width = parseInt(this._videoFeedElement.style.width);
                const height = parseInt(this._videoFeedElement.style.height);
                this._canClose = false;
                this._startCamera(width, height, calculateAspectRatio(width, height));
            } else if (this._scanner.stateManagerProxy.isPaused()) {
                this._canClose = false;
                this._scanner.resume();
            }
        }
    }
            
    _cancelScan() {
        if (this._scanner && !this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState && (this._scanner.isScanning || this._canClose)) {
            this.$.qrCodeScanner.cancel();
        }
    }
    
    _applyScan() {
        if (this._scanner && !this._scanner.stateManagerProxy.stateManager.onGoingTransactionNewState && (this._scanner.isScanning || this._canClose)) {
            this.$.textEditor.commitIfChanged();
            this.$.separatorEditor.commitIfChanged();
            saveSeparator(this.separator);
            this.$.qrCodeScanner.close();
            if (this.applyCallback) {
                this.applyCallback(this.scannedText, this.separator);
            }
        }
    }

    _onAddonAttached(e) {
        tearDownEvent(e);
    }
    
    _faildScan(error) {}
}

customElements.define('tg-qr-code-scanner', TgQrCodeScanner);