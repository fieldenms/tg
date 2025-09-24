import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/paper-dialog-scrollable/paper-dialog-scrollable.js';
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';

import '/resources/components/tg-paper-toast.js';

import { tearDownEvent, containsRestrictedTags } from '/resources/reflection/tg-polymer-utils.js';
import { TgToastBehavior } from '/resources/components/tg-toast-behavior.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <tg-paper-toast id="toast" text="[[_text]]" _has-more="[[_hasMore]]" on-tap="_showMoreIfPossible" allow-click-through always-on-top duration="0">
        <!-- TODO responsive-width="250px" -->
        <paper-spinner id="spinner" hidden$="[[_skipShowProgress]]" active alt="in progress..." tabIndex="-1"></paper-spinner>
        <div id='btnMore' hidden$="[[_skipShowMore(_showProgress, _hasMore)]]" class="toast-btn" on-tap="_showMessageDlg">MORE</div>
    </tg-paper-toast>
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

        /**
         * Indicates whether the toast has MORE button to show a dialog with expanded message.
         */
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

        /**
         * Indicates whether the toast has MORE button to show a dialog with expanded message.
         * This is already a committed value, actually used in UI ('hasMore' is only an intention, that may be discarded).
         */
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

    behaviors: [TgToastBehavior],

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
                    <paper-dialog id="msgDialog" class="toast-dialog"
                        with-backdrop
                        always-on-top
                        entry-animation="scale-up-animation"
                        exit-animation="fade-out-animation"
                        on-iron-overlay-closed="_dialogClosed">
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
                if (containsRestrictedTags(_msgText) === true) {
                    domBind.$.msgPar.textContent = _msgText;
                } else {
                    domBind.$.msgPar.innerHTML = _msgText;
                }
                domBind.$.msgDialog.addEventListener('keydown', e => { // will be removed along with domBind in _dialogClosed
                    // ensures on-Enter closing even if Close button is not focused, i.e. tapped on dialog somewhere
                    if (e.keyCode === 13) {
                       domBind.$.msgDialog.close();
                    }
                }, true);
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

    show: function () {
        const previousToast = this.getDocumentToast('toast');
        if (!previousToast) {
            // initial values
            this.$.toast.error = false;
            this.$.toast._autoCloseCallBack = null; // must NOT interfere with _autoClose of paper-toast
            // Override refit function for paper-toast which behaves really weird (Maybe next releas of paper-toast iron-fit-behavior and iron-overlay-behavior will change this weird behaviour).
            this.$.toast.refit = function () { };
            this.getToastContainer().prepend(this.$.toast);
            this._showNewToast();
        } else if (previousToast.error === true && previousToast.opened && this.isCritical === false) { // discard new toast if existing toast is critical and new one is not; however if new one is critical -- do not discard it -- show overridden information
            console.warn('    toast show: DISCARDED: text = ', this.text + ', critical = ' + this.isCritical);
        }
        // Discard new toast if existing toast is with MORE and new one is not.
        // However if new one is with MORE -- do not discard it -- show overridden information.
        else if (previousToast.error === false && previousToast.opened && this.isCritical === false && previousToast._hasMore && !this.hasMore) {
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
            if (this.getToastContainer().firstChild !== previousToast) {
                this.getToastContainer().prepend(previousToast);
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
        if (this.$.toast._autoCloseCallBack !== null) {
            this.$.toast.cancelAsync(this.$.toast._autoCloseCallBack);
            this.$.toast._autoCloseCallBack = null;
        }
        this.$.toast.error = false;
    },

    _toast: function () {
        return this.$.toast;
    }
});