import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js'
import '/resources/polymer/@polymer/paper-dialog-scrollable/paper-dialog-scrollable.js'
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-toast/paper-toast.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';

import { tearDownEvent, containsRestictedTags } from '/resources/reflection/tg-polymer-utils.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const paperToastStyle = html`
    <custom-style>
        <style>
            #toast {
                @apply --layout-horizontal;
                @apply --layout-center;
                max-width: 256px;
                left:0;
                bottom:0;
            }
            .more {
                padding-left: 8px;
                color: #03A9F4;
                font-weight: 800;
                cursor: pointer;
            }
            #toast paper-spinner {
                width: 1.5em;
                height: 1.5em;
                min-width: 1em;
                min-height: 1em;
                max-width: 2em;
                max-height: 2em;
                padding: 2px;
                margin-left: 1em;
                --paper-spinner-layer-1-color: var(--paper-blue-500);
                --paper-spinner-layer-2-color: var(--paper-blue-500);
                --paper-spinner-layer-3-color: var(--paper-blue-500);
                --paper-spinner-layer-4-color: var(--paper-blue-500);
            }
            .toast-dialog paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            .toast-dialog paper-button:hover {
                background: var(--paper-light-blue-50);
            }
        </style>
    </custom-style>`;
paperToastStyle.setAttribute('style', 'display: none;');
document.head.appendChild(paperToastStyle.content);

const template = html`
    <paper-toast id="toast" class="paper-toast" text="[[_text]]" on-tap="_showMoreIfPossible" allow-click-through always-on-top duration="0">
        <!-- TODO responsive-width="250px" -->
        <paper-spinner id="spinner" hidden$="[[_skipShowProgress]]" active alt="in progress..." tabIndex="-1"></paper-spinner>
        <div id='btnMore' hidden$="[[_skipShowMore(_showProgress, _hasMore)]]" class="more" on-tap="_showMessageDlg">MORE</div>
    </paper-toast>
`;

const PROGRESS_DURATION = 3600000; // 1 hour
const CRITICAL_DURATION = 5000; // 5 seconds
const MORE_DURATION = 4000; // 4 seconds
const STANDARD_DURATION = 2000; // 2 seconds

Polymer({
    // attributes="msgHeading -- TODO was this ever needed?"
    _template: template,

    is: 'tg-toast',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        text: {
            type: String
        },

        msgText: {
            type: String,
            value: ''
        },

        showProgress: {
            type: Boolean
        },

        hasMore: {
            type: Boolean
        },

        isCritical: {
            type: Boolean,
            value: false
        },
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _text: {
            type: String
        },

        _msgText: {
            type: String,
            value: ''
        },

        _showProgress: {
            type: Boolean
        },

        _hasMore: {
            type: Boolean,
            observer: '_hasMoreChanged'
        },

        _isCritical: {
            type: Boolean,
            value: false
        },

        _skipShowProgress: {
            type: Boolean,
            computed: '_shouldSkipProgress(_showProgress, _hasMore)'
        }
    },

    ready: function () {
        //Indicates whether toast overlay can be closed via history (back button or not)
        this.$.toast.skipHistoryAction = true;

        // Styles to truncate the toast text.
        const label = this.$.toast.$$('#label');
        label.style.flex = '1';
        label.style.whiteSpace = 'nowrap';
        label.style.overflow = 'hidden';
        label.style.textOverflow = 'ellipsis';
    },

    _hasMoreChanged: function (newValue, oldValue) {
        // let's set a cursor for the whole toast if it can be "clicked"
        const cursor = newValue === true ? 'pointer' : 'inherit';
        this.$.toast.style.cursor = cursor;
    },

    _showMoreIfPossible: function (e) {
        if (this._hasMore) { // show dialog with 'more' information and close toast
            this._showMessageDlg(e);
        } else if (!this._showProgress && this._isCritical !== true) { // close toast on tap; however, do not close if it represents progress indication or if it is critical, but does not have MORE button (rare cases)
            this._closeToast();
        }
    },

    _showMessageDlg: function (event) {
        const self = this;
        const _msgText = self._msgText; // provide strong guarantees on _msgText immutability here -- this will be used later for msgDialog message text (after two async calls)
        // need to open dialog asynchronously for it to open on mobile devices
        this.async(function () {
            // build and display the dialog
            const domBind = document.createElement('dom-bind');

            domBind._dialogClosed = function () {
                document.body.removeChild(this);
            }.bind(domBind);

            domBind.innerHTML = `
                <template>
                    <paper-dialog id="msgDialog" class="toast-dialog" on-iron-overlay-closed="_dialogClosed" always-on-top with-backdrop entry-animation="scale-up-animation" exit-animation="fade-out-animation">
                        <paper-dialog-scrollable>
                            <p id="msgPar" style="padding: 10px;white-space: break-spaces;"></p>
                        </paper-dialog-scrollable>
                        <div class="buttons">
                            <paper-button dialog-confirm affirmative autofocus>
                                <span>Close</span>
                            </paper-button>
                        </div>
                    </paper-dialog>
                </template>
            `;
            document.body.appendChild(domBind);

            this.async(function () {
                // please note that domBind.$.msgPar is rendered after body.appendChild(domBind), but has been put here (into async(100)) to provide stronger guarantees along with msgDialog.open()
                if (containsRestictedTags(_msgText) === true) {
                    domBind.$.msgPar.textContent = _msgText;
                } else {
                    domBind.$.msgPar.innerHTML = _msgText;
                }
                // actual msgDialog opening
                domBind.$.msgDialog.open();
                self.$.toast.close(); // must close paper-toast after msgDialog is opened; this is because other fast toast messages can interfere -- paper-toast should still be opened to prevent other messages early opening (see '... && previousToast.opened && ...' condition in 'show' method)
            }, 100);

        }, 100);

        tearDownEvent(event);
    },

    _shouldSkipProgress: function (progress, hasMore) {
        return !progress || hasMore;
    },

    _skipShowMore: function (progress, hasMore) {
        return progress || !hasMore;
    },

    _getPreviousToast() {
        const toasts = document.querySelectorAll('#toast');
        let toast = null;
        let existingToastCount = 0;
        for (let index = 0; index < toasts.length; index++) {
            const currToast = toasts.item(index);
            if (currToast.parentNode === document.body) {
                existingToastCount++;
                if (existingToastCount > 1) {
                    throw 'More than one toast exist in body direct children.';
                }
                toast = currToast;
            }
        }
        return toast;
    },

    show: function () {
        const previousToast = this._getPreviousToast();
        if (!previousToast) {
            // initial values
            this.$.toast.error = false;
            this.$.toast._autoCloseCallBack = null; // must NOT interfere with _autoClose of paper-toast
            // Override refit function for paper-toast which behaves really weird (Maybe next releas of paper-toast iron-fit-behavior and iron-overlay-behavior will change this weird behaviour).
            this.$.toast.refit = function () { };
            document.body.appendChild(this.$.toast);
            this._showNewToast();
        } else if (previousToast.error === true && previousToast.opened && this.isCritical === false) { // discard new toast if existing toast is critical and new one is not; however if new one is critical -- do not discard it -- show overridden information
            console.warn('    toast show: DISCARDED: text = ', this.text + ', critical = ' + this.isCritical);
        } else {
            // '__dataHost' is used to detemine 'tg-toast' instance from 'previousToast' found on body (parent of 'previousToast' is body, that is why there is a need to use other accessing method).
            // WARNING: '__dataHost' is not a public Polymer API.
            const previousTgToast = previousToast.__dataHost;

            if (previousTgToast !== this) {
                previousTgToast.text = this.text;
                previousTgToast.msgText = this.msgText;
                previousTgToast.showProgress = this.showProgress;
                previousTgToast.hasMore = this.hasMore;
                previousTgToast.isCritical = this.isCritical;
            }
            previousTgToast._showNewToast();
        }
    },

    _showNewToast: function () {
        this._text = this.text;
        this._msgText = this.msgText;
        this._showProgress = this.showProgress;
        this._hasMore = this.hasMore;
        this._isCritical = this.isCritical;

        let customDuration;
        if (this._showProgress && !this._hasMore) {
            customDuration = PROGRESS_DURATION;
        } else if (this._isCritical === true) {
            customDuration = CRITICAL_DURATION;
        } else if (this._hasMore === true) {
            customDuration = MORE_DURATION;
        } else {
            customDuration = STANDARD_DURATION;
        }
        if (this._isCritical === true || this.$.toast.error === false) { // if critical toast arrived then delay its closing; also delay closing if toast is not critical but old toast is not critical too
            if (this.$.toast._autoCloseCallBack !== null) {
                this.$.toast.cancelAsync(this.$.toast._autoCloseCallBack);
                this.$.toast._autoCloseCallBack = null;
            }
            this.$.toast._autoCloseCallBack = this.$.toast.async(this._closeToast.bind(this), customDuration);
        }

        this.$.toast.error = this._isCritical;
        if (this._isCritical === true) {
            this.$.toast.style.background = '#D50000';
            this.$.btnMore.style.color = '#FFCDD2';
        } else {
            this.$.toast.style.background = 'black';
            this.$.btnMore.style.color = '#03A9F4';
        }
        this.$.toast.show();
    },

    _closeToast: function () {
        this.$.toast.close();
        this.$.toast._autoCloseCallBack = null;
        this.$.toast.error = false;
    }
});