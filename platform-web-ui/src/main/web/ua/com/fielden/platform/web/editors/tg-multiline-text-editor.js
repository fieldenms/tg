import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-autogrow-textarea/iron-autogrow-textarea.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { TgEditor,  createEditorTemplate} from '/resources/editors/tg-editor.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const additionalTemplate = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        iron-autogrow-textarea {
            min-height: -webkit-fit-content;
            min-height: -moz-fit-content;
            min-height: fit-content;
            overflow: hidden;
            --iron-autogrow-textarea: {
                font-weight: 500;
                padding: 0;
            };
            @apply --layout-flex;
        }
        .upper-case {
            --iron-autogrow-textarea: {
                font-weight: 500;
                text-transform: uppercase;
                padding: 0;
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
            on-keydown="_onKeydown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            readonly$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"
            autocomplete="off"
            selectable-elements-container>
        </iron-autogrow-textarea>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgMultilineTextEditor extends TgEditor {

    static get template () { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate);
    }

    static get properties() {
        return {
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
            
            /**
             * OVERRIDDEN FROM TgEditorBehavior: this specific textArea's event is invoked after some key has been pressed.
             *
             * Designated to be bound to child elements.
             */
            _onKeydown: {
                type: Function,
                value: function () {
                    return this._handleCopy.bind(this);
                }
            }
        };
    }

    constructor () {
        super();
        this._editorKind = "MULTILINE_TEXT";
    }

    ready () {
        super.ready();
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
        this.decoratedInput().textarea.style.cursor = "text";
        const _prevValueChanged = this.decoratedInput()._valueChanged.bind(this.decoratedInput());
        this.decoratedInput()._valueChanged = (newValue, oldValue) => {
            if (this.decoratedInput().bindValue === newValue) {
                setTimeout(() => {
                    this.decoratedInput().selectionStart = this.decoratedInput().selectionEnd = 0;
                }, 0);
                
            }
            _prevValueChanged(newValue, oldValue);
        };
    }

    _labelDownEventHandler (event) {
        if (this.shadowRoot.activeElement !== this.decoratedInput() && !this._disabled) {
            this.decoratedInput().textarea.focus();
        }
        tearDownEvent(event);
    }

    _disabledChanged (newValue, oldValue) {
        super._disabledChanged(newValue, oldValue);
        if (newValue) {
            this.$.input.textarea.setAttribute('disabled', newValue);
        } else {
            this.$.input.textarea.removeAttribute('disabled');
        }
        
    }

    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString (strValue) {
        return strValue === '' ? null : strValue;
    }

    /**
     * Overridden to avoid displaying the value, which may potentially contain unsafe HTML.
     */
    _formatTooltipText (value) {
        return "";
    }

    /**
     * Returns a concrete element in `_onFocus` `target` to be stored for further focusing.
     * In our case, it is a `textarea` inside `iron-autogrow-textarea`.
     */
    _focusTarget (target) {
        return target.textarea;
    }

}

customElements.define('tg-multiline-text-editor', TgMultilineTextEditor);