import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import '/resources/components/tg-scrollable-component.js';

const criterionBehaviorStyle = html`
    <custom-style>
        <style>
            .meta-value-editor paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            
            .meta-value-editor paper-button:hover {
                background: var(--paper-light-blue-50);
            }
            
            .metavalue-editor > * {
                flex-grow : 0;
                flex-shrink: 0;
            }
            
            .meta-value-editor > tg-scrollable-component {
                margin: 0;
                padding: 0;
                --tg-scrollable-layout: {
                    padding: 0 24px;
                };
            }
            
            .metavalue-editor {
                padding-top: 24px;
            }
        </style>
    </custom-style>
`;
criterionBehaviorStyle.setAttribute('style', 'display: none;');
document.head.appendChild(criterionBehaviorStyle.content);

const TgAbstractCriterionBehaviorImpl = {

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * This callback should be used for custom validation action after some meta-value has been changed.
         */
        validationCallback: {
            type: Object
        },

        /**
         * Indicates whether criterion for entity property has critOnly annotation or not.
         */
        critOnly: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether mnemonics should be visible or not.
         */
        mnemonicsVisible: {
            type: Boolean,
            value: false
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////// INNER PROPERTIES ///////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix and default values specified in 'value' specificator of the property definition (or,       //
        //   alternatively, computing function needs to be specified). 									       //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _showMetaValuesEditor: Function,
        _acceptMetaValuesForBinding: Function,
        _cancelMetaValuesForBinding: Function,
        _computeIconButtonStyleForBinding: Function
    },

    observers: [
        '_updateIconButtonStyle(orNull, not)'
    ],

    ready: function () {
        this._showMetaValuesEditor = (function (e) {
            const mve = this._createMetaValuesDialog();
            document.body.appendChild(mve);

            mve.open();
        }).bind(this);

        this._acceptMetaValuesForBinding = this._acceptMetaValues.bind(this);
        this._cancelMetaValuesForBinding = this._cancelMetaValues.bind(this);
        this._computeIconButtonStyleForBinding = this._computeIconButtonStyle.bind(this);
    },

    /**
     * Clears all meta value parameters for this criterion.
     */
    clearMetaValues: function () {
        this._datePrefix = null;
        this._dateMnemonic = null;
        this._andBefore = null;
        this._exclusive = false;
        this._exclusive2 = false;
        this._orNull = false;
        this._not = false;
        this._acceptMetaValues(false);
    },

    /**
     * Creates dynamically the 'dom-bind' template, which hold the dialog with all meta value editors.
     *
     * Uses the method '_createMetaValueEditors' (which can be overridden in descendants) to insert specific meta-value editors inside the dialog.
     */
    _createMetaValuesDialog: function () {
        const self = this;
        const domBind = document.createElement('dom-bind');

        domBind._orNullBind = self._orNull;
        domBind._notBind = self._not;
        domBind._exclusiveBind = self._exclusive;
        domBind._exclusive2Bind = self._exclusive2;
        domBind._datePrefixBind = self._datePrefix;
        domBind._dateMnemonicBind = self._dateMnemonic;
        domBind._andBeforeBind = self._andBefore;

        domBind._onCaptureKeyDown = function (e) {
            const dialog = this.$.metaValueEditor;
            if (e.keyCode === 13) {
                this._acceptMetaValuesBind();
                dialog.close();
            }
        }.bind(domBind);

        domBind._openedBind = function (e) {
            document.addEventListener('keydown', this._onCaptureKeyDown, true);
        }.bind(domBind);

        domBind._closedBind = function (e) {
            const dialog = this.$.metaValueEditor;
            //Remove registered listeners on document.
            document.removeEventListener('keydown', this._onCaptureKeyDown, true);

            document.body.removeChild(dialog);
            document.body.removeChild(this);
            //Restoring focus and icon button state.
            self._dom().$.iconButton.disabled = false;
            self.restoreActiveElement();
        }.bind(domBind);

        domBind._clearMetaValues = function () {
            domBind._orNullBind = false;
            domBind._notBind = false;
            domBind._exclusiveBind = false;
            domBind._exclusive2Bind = false;
            domBind._datePrefixBind = null;
            domBind._dateMnemonicBind = null;
            domBind._andBeforeBind = null;
        };

        domBind._cancelMetaValuesBind = function () {
            self._cancelMetaValues();
            domBind._orNullBind = self._orNull;
            domBind._notBind = self._not;
            domBind._exclusiveBind = self._exclusive;
            domBind._exclusive2Bind = self._exclusive2;
            domBind._datePrefixBind = self._datePrefix;
            domBind._dateMnemonicBind = self._dateMnemonic;
            domBind._andBeforeBind = self._andBefore;
        };

        domBind._acceptMetaValuesBind = function () {
            self._datePrefix = domBind._datePrefixBind;
            self._dateMnemonic = domBind._dateMnemonicBind;
            self._andBefore = domBind._andBeforeBind;
            self._exclusive = domBind._exclusiveBind;
            self._exclusive2 = domBind._exclusive2Bind;
            self._orNull = domBind._orNullBind;
            self._not = domBind._notBind;
            self._acceptMetaValues(true);
        };

        domBind.open = function () {
            const dialog = this.$.metaValueEditor;
            self.persistActiveElement();
            self._dom().$.iconButton.disabled = true;
            document.body.appendChild(dialog);
            // let's open the dialog with magical async...
            // this ensures that the dialog is opened after its relocation to body
            self.async(function () {
                dialog.open();
            }.bind(self), 1);
        }.bind(domBind);

        domBind.innerHTML = `
            <template>
                <paper-dialog id="metaValueEditor" 
                    class="layout vertical meta-value-editor"
                    with-backdrop no-cancel-on-outside-click
                    entry-animation="scale-up-animation" exit-animation="fade-out-animation"
                    on-iron-overlay-closed="_closedBind"
                    on-iron-overlay-opened="_openedBind">
                    <tg-scrollable-component class="relative">
                        <div class="metavalue-editor layout vertical">`
                            + self._createMetaValueEditors() +
                        `</div>
                    </tg-scrollable-component>
                    <div class="layout horizontal justified" style="flex-shrink:0; flex-grow:0; padding:8px; margin:0;">
                        <paper-button class="blue" on-tap="_clearMetaValues">Clear</paper-button>
                        <div class="layout horizontal">
                            <paper-button class="blue" dialog-dismiss affirmative on-tap="_cancelMetaValuesBind">Cancel</paper-button>
                            <paper-button class="blue" dialog-confirm affirmative autofocus on-tap="_acceptMetaValuesBind">Ok</paper-button>
                        </div>
                    </div>
                </paper-dialog>
            </template>`;
        return domBind;
    },

    /**
     * Creates the string representation for meta value editors DOM (to be inserted into dynamic meta-value dialog).
     */
    _createMetaValueEditors: function () {
        console.log("tg-abstract-criterion-behavior: _createMetaValueEditors");
        return '';
    },

    /**
     * Accepts all new meta-values. Should be overridden to provide acceptance in specific descendants.
     */
    _acceptMetaValues: function (validate) {
        if (validate) {
            this.validationCallback();
        }
    },

    /**
     * Cancels all new meta-values. Should be overridden to provide cancellation in specific descendants.
     */
    _cancelMetaValues: function () { },

    /**
     * Returns 'true' if criterion has no meta values assigned, 'false' otherwise. Should be overridden to provide functionality in specific descendants.
     */
    _hasNoMetaValues: function (orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return true;
    },

    _updateIconButtonStyle: function (orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        this._dom().$.iconButton.setAttribute('style', this._computeIconButtonStyle(this.orNull, this.not, this.exclusive, this.exclusive2, this.datePrefix, this.dateMnemonic, this.andBefore));
    },

    _computeIconButtonStyle: function (orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return 'visibility:' + (!this._iconButtonVisible() ? 'hidden' : 'inherit') + '; color:' + (this._hasNoMetaValues(orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) ? '#757575' : '#0288D1') + ';';
    },

    /**
     * Returns 'true' if the icon button of this criterion editor should be visible, 'false' otherwise.
     */
    _iconButtonVisible: function () {
        return this.mnemonicsVisible;
    },

    /**
     * Returns the reference to main 'dom' element (of type 'tg-abstract-criterion').
     */
    _dom: function () {
        throw 'not implemented';
    }
};

export const TgAbstractCriterionBehavior = [
    TgFocusRestorationBehavior,
    TgAbstractCriterionBehaviorImpl
];