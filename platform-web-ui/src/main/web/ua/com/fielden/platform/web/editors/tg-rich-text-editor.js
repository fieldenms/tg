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

            /**
             * Value for originally appearing _editingValue after Toast UI transformations (e.g. 'hello world' => '<p>hello world</p>').
             * This value is used to compare current '_editingValue' with already transformed one -- not to show SAVE enabled for such purely editor-induced transformations.
             * This will affect 'identifyModification' logic and prevent making RichText editor '@editedProps'.
             *
             * Note, that binding value in '_currBindingEntity' and derived 'modifiedPropsHolder' ('_extractModifiedPropertiesHolder') are not affected in any way.
             * This means that binding values will be { ..., fomattedText: 'hello world' } for both '_currBindingEntity' and '_originalBindingEntity' and
             *   thus leaving them unchanged in terms of '_bindingEntityModified' and subsequent '_bindingEntityNotPersistentOrNotPersistedOrModified'.
             * Also this means that no change will be made on server against RichText property with untransformed value.
             */
            _transformedOriginalEditingValue: {
                type: String,
                value: null
            },

            /**
             * The entity that was last opened it is null only when first time opened or closed
             */
            _lastOpenedEntity: {
                type: Object,
                value: null,
                observer: "_lastOpenedEntityChanged"
            }
        }
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        this._lastOpenedEntity =  null;
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

     /**
     * Overridden to calculate union value type title on each arrival of binding entity.
     */
     _entityChanged (newValue, oldValue) {
        super._entityChanged(newValue, oldValue);
        if (newValue) {
            this._lastOpenedEntity = newValue;
        }
    }

    _lastOpenedEntityChanged (newValue, oldValue) {
        if ((newValue && !oldValue) || 
            !newValue || 
            (newValue.get("id") && oldValue.get("id") && newValue.get("id") !== oldValue.get("id"))) {
                this.decoratedInput().clearHistory();
            }
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
            const clipboardItem = new ClipboardItem({ 
                'text/html': new Blob([this._editingValue], { type: 'text/html' }),
                'text/plain': new Blob([this.decoratedInput().getText()], { type: 'text/plain' })
            });
            navigator.clipboard.write([clipboardItem]);
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

    /**
     * Initialises '_transformedOriginalEditingValue' in cases where '_editingValue' kept unchanged, i.e. '_editingValueChanged' not actually invoked.
     */
    _refreshCycleStartedChanged (newValue, oldValue) {
        if (oldValue === false && newValue === true) { // refreshCycle process just started
            this._transformedOriginalEditingValue = null; // transformed value not yet known -- reset it and wait until refreshCycle will be completed (especially important because of Entity Master caching)
        } else if (oldValue === true && newValue === false) { // refreshCycle process just completed
            this._transformedOriginalEditingValue = this._editingValue; // transformed value is already inside _editingValue
        }
    }

    /**
     * Initialises '_transformedOriginalEditingValue' with '_editingValue'.
     *  Note that there are two '_editingValueChanged' calls. Both of them are with already transformed value.
     *  First call is triggered by 'tg-rich-text-input._htmlContentChanged' during 'this.value = htmlText;'.
     *  The second are from 'this._editingValue = newEditingValue;' in `tg-editor._entityChanged`.
     */
    _editingValueChanged (newValue, oldValue) {
        if (this._refreshCycleStarted === true) {
            this._transformedOriginalEditingValue = newValue;
        }
        super._editingValueChanged(newValue, oldValue);
    }

    /**
     * Compares current '_editingValue' with already transformed by Toast UI original editing value. See 'identifyModification'.
     */
    _equalToOriginalValue (_editingValue, _originalEditingValue) {
        return this.reflector().equalsEx(_editingValue, this._transformedOriginalEditingValue === null ? _originalEditingValue : this._transformedOriginalEditingValue);
    }

}

customElements.define('tg-rich-text-editor', TgRichTextEditor);