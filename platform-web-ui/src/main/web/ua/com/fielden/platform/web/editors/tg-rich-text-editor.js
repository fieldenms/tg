import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/rich-text/tg-rich-text-input.js';

import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';

import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { tearDownEvent, localStorageKey, getRelativePos } from '/resources/reflection/tg-polymer-utils.js';

const additionalTemplate = html`
    <style>
        :host {
            min-width: 0;
        }
        #input {
            cursor: text;
        }
        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            z-index: 21;
            --iron-icon-fill-color: var(--paper-grey-600);
        }
        #resizer:hover {
            cursor: ns-resize;
        }
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently
               supported by Chrome and Opera */
        }
    </style>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
        disabled="[[_disabled]]" 
        value="{{_editingValue}}"
        change-event-handler="[[_onChange]]"
        key-down-handler="[[_onKeydown]]" 
        on-focus="_onFocus"
        on-blur="_outFocus"
        min-height="[[minHeight]]"
        height="[[_calcHeight(height, entityType, propertyName)]]"
        tabindex='0'>
    </tg-rich-text-input>
    <iron-icon id="resizer" icon="tg-icons:resize-bottom-right" on-tap="_resetHeight" on-down="_makeInputUnselectable" on-up="_makeInputSelectable" on-track="_resizeInput" tooltip-text="Drag to resize<br>Double tap to reset height"></iron-icon>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgRichTextEditor extends GestureEventListeners(TgEditor) {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate);
    }

    static get properties() {
        return {

            entityType: {
                type: String
            },

            minHeight: {
                type: String,
                value: "16px"
            },

            height: {
                type: String,
                value: "100px"
            },

            withoutResizing: {
                type: Boolean,
                value: false
            },

            /**
             * OVERRIDDEN FROM TgEditor: this specific event is invoked after some key has been pressed.
             *
             */
            _onKeydown: {
                type: Function,
                value: function () {
                    return this._handleCopy.bind(this);
                }
            },
        }
    }

    convertToString (value) {
        return (value && value.formattedText) || '';
    }
    /**
     * This method converts string value to rich test object
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        }

        return {coreText: '', 'formattedText': strValue};
    }

    _calcHeight(height, entityType, propertyName) {
        if (height && entityType && propertyName) {
            return this._readHeight() || height;
        }
    }

    _formatTooltipText(value) {
        return '';
    }

    _resizeInput (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer && !this.withoutResizing) {
            switch (event.detail.state) {
                case 'start':
                    break;
                case 'track':
                    const prevHeight = parseInt(this.$.input.getHeight());
                    let newHeight = prevHeight + event.detail.ddy;
                    //Adjust height if mouse is out of the scroll container
                    const scrollContainer = this._getScrollingParent();
                    const mousePos = scrollContainer && getRelativePos(event.detail.x, event.detail.y, scrollContainer);
                    if (scrollContainer && mousePos) {
                        if (mousePos.y > scrollContainer.offsetHeight) {
                            newHeight += mousePos.y - scrollContainer.offsetHeight;
                        } else if (mousePos.y < 0) {
                            newHeight += mousePos.y;
                        }
                    }
                    //Adjust new height if it less then resizer icon or min height of this editor
                    if (newHeight < this.$.resizer.offsetHeight) {
                        newHeight = this.$.resizer.offsetHeight;
                    }
                    if (newHeight < parseInt(this.minHeight)) {
                        newHeight = parseInt(this.minHeight);
                    }
                    this.$.input.height = newHeight + "px";
                    //scroll if needed
                    if (scrollContainer && mousePos) {
                        if (mousePos.y > scrollContainer.offsetHeight || mousePos.y < 0) {
                            scrollContainer.scrollTop += newHeight - prevHeight;
                        }
                    }
                    break;
                case 'end':
                    this._saveHeight(this.$.input.getHeight());
                    break;
            }
        }
        tearDownEvent(event);  
    }

    _getScrollingParent() {
        let parent = this;
        while (parent && parent.offsetHeight === parent.scrollHeight) {
            // go through parent elements (including going out from shadow DOM)
            parent = parent.assignedSlot || parent.parentElement || parent.getRootNode().host;
        }
        return parent;
    }

    _makeInputUnselectable () {
        this.$.input.classList.toggle("noselect", true);
        this.$.input.makeEditable(false);
        document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
    }

    _makeInputSelectable () {
        this.$.input.classList.toggle("noselect", false);
        this.$.input.makeEditable(!this._disabled);
        if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
            document.styleSheets[0].deleteRule(0);
        }
    }

    _resetHeight(e) {
        if (e.detail.sourceEvent.detail && e.detail.sourceEvent.detail === 2) {
            localStorage.removeItem(this._generateKey());
            this.$.input.height = this.height;
        }
    }

    _saveHeight(height) {
        localStorage.setItem(this._generateKey(), height);
    }

    _readHeight() {
        return localStorage.getItem(this._generateKey());
    }

    _generateKey() {
        return localStorageKey(`${this.entityType}_${this.propertyName}_height`);
    }

    _copyTap() {
        // copy to clipboard should happen only if there is something to copy
        if (navigator.clipboard && this._editingValue) {
            navigator.clipboard.writeText(this._editingValue);
            this._showCheckIconAndToast(`<div class="toastui-editor-contents">${this._editingValue}</div>`);
        } else if (this.toaster) {
            this.toaster.openToastWithoutEntity("Nothing to copy", true, "There was nothing to copy.", false);
        }
    }

    _labelDownEventHandler (event) {
        if (!this.decoratedInput().shadowRoot.activeElement && !this._disabled) {
            this.decoratedInput().focusInput();
        }
        tearDownEvent(event);
    }

     // @Override
    _equalToOriginalValue (_editingValue, _originalEditingValue) {
        // _editingValue is obtained from the ToastUi editor.
        // _originalEditingValue may be the value retrieved from a database.
        // Even without the user changing anything, _editingValue may be different from the original one due to being
        // transformed by the ToastUI editor.
        // Therefore, the original value has to be transformed as well, before comparison.
        return this.reflector().equalsEx(_editingValue, this.$.input.convertToEditorValue(_originalEditingValue));
    }

    _asTransformed (value) { return value; }

}

customElements.define('tg-rich-text-editor', TgRichTextEditor);
