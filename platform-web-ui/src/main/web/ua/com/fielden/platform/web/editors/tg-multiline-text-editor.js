import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-autogrow-textarea/iron-autogrow-textarea.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

import { TgEditor,  createEditorTemplate} from '/resources/editors/tg-editor.js';

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
            }
            @apply --layout-flex;
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
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-keydown="_onKeydown"
            readonly$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"
            selectable-elements-container>
        </iron-autogrow-textarea>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

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
    
            _onMouseDown: {
                type: Function,
                value: function () {
                    return (function (event) {
                        if (this.shadowRoot.activeElement !== this.decoratedInput()) {
                            this.decoratedInput().textarea.select();
                            this._tearDownEventOnUp = true;
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
                    //   var parentFunction = TgEditorBehaviorImpl.properties._onKeydown.value.call(this);
                    //   parentFunction.call(this, event);
                    //console.log("_onKeydown (for text area):", event);
                        // TODO potentially, commit on CTRL+Enter?
                    }).bind(this);
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
    }
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString (value) {
        return value === null ? "" : "" + value;
    }

    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString (strValue) {
        return strValue === '' ? null : strValue;
    }
}

customElements.define('tg-multiline-text-editor', TgMultilineTextEditor);