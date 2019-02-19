import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-autogrow-textarea/iron-autogrow-textarea.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

import { TgEditorBehavior,  createEditorTemplate} from '/resources/editors/tg-editor-behavior.js';

const additionalTemplate = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        iron-autogrow-textarea {
            --iron-autogrow-textarea: {
                font-weight: 500;
            }
            @apply --layout-flex;
            min-height: fit-content;
            overflow: hidden;
        }
        .upper-case {
            --iron-autogrow-textarea: {
                text-transform: uppercase;
            };
        }
        paper-input-container {
            @apply --layout-vertical;
            flex: 1 0 auto;    
        }
        .main-container {
            @apply --layout-flex;
        }
    </style>`;
const customInputTemplate = html`
    <iron-autogrow-textarea
            id="input"
            class="paper-input-input custom-input multiline-text-input"
            max-rows="[[maxRows]]"
            bind-value="{{_editingValue}}"
            max-length="[[maxLength]]"
            on-input="_onInput"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            readonly$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off">
        </iron-autogrow-textarea>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

Polymer({
    _template: createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate),

    is: 'tg-multiline-text-editor',

    behaviors: [ TgEditorBehavior ],

    created: function () {
        this._editorKind = "MULTILINE_TEXT";

        // this.decorator().querySelector('#inputCounter').target = this.decorator().$.input;
    },

    ready: function () {
        const inputWrapper = this.decorator().$$(".input-wrapper");
        inputWrapper.style.flexGrow = "1";
        const labelAndInputContainer = this.decorator().$.labelAndInputContainer;
        labelAndInputContainer.style.alignSelf = "stretch";
        labelAndInputContainer.style.display = "flex";
        labelAndInputContainer.style.flexDirection = "column";
        labelAndInputContainer.style.overflow = "auto";
        const prefix = this.decorator().$$(".prefix");
        prefix.style.alignSelf = "flex-start";
        const suffix = this.decorator().$$(".suffix");
        suffix.style.alignSelf = "flex-start";
        this.decoratedInput().textarea.addEventListener("change", this._onChange);
    },

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * The maximum number of characters for this text editor
         */
        maxLength: {
            type: Number
        },

        /**
         * The maximum count for textarea rows.
         */
        maxRows: {
            type: Number,
            value: 5
        },

        _onTap: {
            type: Function,
            value: function () {
                return (function (event) {
                    if (this.shadowRoot.activeElement !== this.decoratedInput()) {
                        this.decoratedInput().textarea.select();
                        tearDownEvent(event);
                    }
                }).bind(this);
            }
        },

        /**
         * OVERRIDDEN FROM TgEditorBehavior: this specific textArea's event is invoked after some key has been pressed.
         *
         * Designated to be bound to child elements.
         */
        _onKeydown: {
            type: Function,
            value: function () {
                return (function (event) {
                // need to invoke base function-property? Just do it like this:
                //   var parentFunction = Polymer.TgBehaviors.TgEditorBehavior.properties._onKeydown.value.call(this);
                //   parentFunction.call(this, event);
                //console.log("_onKeydown (for text area):", event);
                    // TODO potentially, commit on CTRL+Enter?
                }).bind(this);
            }
        }
    },

    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString: function (value) {
        return value === null ? "" : "" + value;
    },

    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString: function (strValue) {
        return strValue === '' ? null : strValue;
    }
});