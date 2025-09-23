import '/resources/polymer/@polymer/iron-input/iron-input.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor,  createEditorTemplate} from '/resources/editors/tg-editor.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
        #input.upper-case {
            text-transform: uppercase;
        }
    </style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input singleline-text-input"
            on-change="_onChange"
            on-input="_onInput"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-keydown="_onKeydown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgSinglelineTextEditor extends TgEditor {
    
    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate);
    }
    
    static get properties () {
        return {
            /**
             * Number of milliseconds after user input to trigger auto-committing of _editingValue.
             * If zero or undefined then no auto-committing will be performed on user input and traditional tab-off / Enter / blur -driven committing is to be used.
             */
            autoCommitMillis: {
                type: Number
            },
            
            /**
             * Provides auto-commit handling for the case where 'autoCommitMillis' property is defined.
             * This auto-commit occurs if the value is edited by the user explicitly or pasted from some external source e.g. bar code scanner, date time picker etc.
             * Auto-commit is triggered in 'autoCommitMillis' milliseconds after that action.
             */
            _onInput: {
                type: Function,
                value: function () {
                    return event => {
                        if (this.autoCommitMillis && this.autoCommitMillis >= 0) { // if autoCommitMillis is defined and value resembles proper milliseconds then
                            if (typeof this._autoCommitTimeoutId === 'number') { // if there is in-progress timeout handling auto-commit
                                clearTimeout(this._autoCommitTimeoutId); // then cancel this timeout immediately
                            }
                            this._autoCommitTimeoutId = setTimeout(() => { // create and start new timeout to be fulfilled in autoCommitMillis time
                                this.commitIfChanged(); // commit the current _editingValue if it is changed; after that timeout has been fulfilled and served its purpose
                                delete this._autoCommitTimeoutId; // remove timeout reference not to keep garbage; this is to avoid unnecessary clearTimeout calls for already completed timeouts
                            }, this.autoCommitMillis);
                        }
                    };
                }
            }
        };
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

}

customElements.define('tg-singleline-text-editor', TgSinglelineTextEditor);