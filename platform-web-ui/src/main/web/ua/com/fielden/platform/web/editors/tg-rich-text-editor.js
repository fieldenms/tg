import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/rich-text/tg-rich-text-input.js';

import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { GestureEventListeners } from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { TgDoubleTapHandlerBehavior } from '/resources/components/tg-double-tap-handler-behavior.js';
import { tearDownEvent, localStorageKey, getRelativePos, allDefined } from '/resources/reflection/tg-polymer-utils.js';

const additionalTemplate = html`
    <style>
        :host {
            min-width: 0;
        }
        :host([auto-resize]) {
            @apply --layout-vertical;
        }
        #input {
            cursor: text;
        }
        :host([auto-resize]) #input {
            @apply --layout-flex;
        }
        :host([auto-resize]) paper-input-container {
            @apply --layout-vertical;
            flex: 1 0 auto;    
        }
        paper-input-container[disabled] {
            --paper-input-container-label-focus: {
                color: var(--paper-input-container-color, var(--secondary-text-color));
            }
        }
        :host([auto-resize]) .main-container {
            @apply --layout-flex;
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
    </style>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
        disabled="[[_disabled]]"
        is-readonly="[[isReadonly]]" 
        value="{{_editingValue}}"
        toaster="[[toaster]]"
        change-event-handler="[[_onChange]]"
        key-down-handler="[[_onKeydown]]" 
        on-focus="_onFocus"
        on-blur="_outFocus"
        min-height="[[minHeight]]"
        height="[[_calcHeight(height, entityType, propertyName)]]"
        tabindex$='[[_tabIndex(_disabled)]]'>
    </tg-rich-text-input>
    <iron-icon id="resizer" icon="tg-icons:resize-bottom-right" on-tap="_resetHeight" on-down="_makeInputUnselectable" on-up="_makeInputSelectable" on-track="_resizeInput" tooltip-text="Drag to resize<br>Double tap to reset height" hidden$="[[autoResize]]"></iron-icon>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgRichTextEditor extends mixinBehaviors([TgDoubleTapHandlerBehavior], GestureEventListeners(TgEditor)) {

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
                value: "25px"
            },

            height: {
                type: String,
                value: "100%"
            },

            autoResize: {
                type: Boolean,
                computed: "_isAutoResizable(height)",
                observer: "_autoResizeChanged",
                reflectToAttribute: true
            },

            /**
             * Determines whether this editor is in read-only mode.
             * This state differs from `disabled` because it depends on the meta-state of the corresponding property in the bound entity.
             */
            isReadonly: {
                type: Boolean,
                computed: "_isReadonly(entity, propertyName)",
                readOnly: true,
            },

            /**
             * OVERRIDDEN FROM TgEditor: this specific event is invoked after some key has been pressed.
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

    ready() {
        super.ready();
        this._resetHeight = this._createDoubleTapHandler("_lastResizerTap", (e) => {
            localStorage.removeItem(this._generateKey());
            this.$.input.height = this.height;
        });
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

    focusDecoratedInput() {
        this.decoratedInput().focusInput();
    }

    get availableScanSeparators() {
        return ['\n', ' ', '\t'];
        
    }

    replaceText(text, start, end) {
        this.decoratedInput().replaceText(text, start, end);
    }

    insertText(text, where) {
        this.decoratedInput().insertText(text, where);
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

    /**
     * Calculates the read-only state for this editor.
     * 
     * @param {Object} entity - The entity bound to this rich text editor.
     * @param {String} propertyName - The name of the rich text property.
     */
    _isReadonly (entity, propertyName) {
        if (allDefined(arguments)) {
            return !this.reflector().isEntity(entity) || this.reflector().isDotNotated(propertyName) || !entity["@" + propertyName + "_editable"];
        }
        return true;
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
            return height === '100%' ? height : (this._readHeight() || height);
        }
    }

    _formatTooltipText(value) {
        return '';
    }

    _resizeInput (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer && !this.autoResize) {
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

    _makeInputUnselectable(e) {
        tearDownEvent(e);
        document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
    }

    _makeInputSelectable(e) {
        tearDownEvent(e);
        if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
            document.styleSheets[0].deleteRule(0);
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
            this.focusDecoratedInput();
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

    /**
     * Calculates tab index attribute for rich text input based on disabled state.
     * 
     * @param {Boolean} disabled determines whether editor is disabled or not
     */
    _tabIndex(disabled) {
        return disabled ? "-1": "0";
    }

    /**
     * Calculates value that determines whether rich text editor is auto-resiziable or has static height.
     * if height is equal to 100% it means that editor is autoresizable otherwise it has static height. 
     * 
     * @param {String} height - height of the rich text input. It might be '100%', 150px etc.
     */
    _isAutoResizable(height) {
        return height === '100%';
    }

    /**
     * Handles changes to autoResize property
     * 
     * @param {Boolean} newValue - new value for autResize property
     */
    _autoResizeChanged(newValue) {
        const inputWrapper = this.decorator().$$(".input-wrapper");
        const labelAndInputContainer = this.decorator().$.labelAndInputContainer;
        const prefix = this.decorator().$$(".prefix");
        const suffix = this.decorator().$$(".suffix");
        if (newValue) {
            inputWrapper.style.flexGrow = "1";
            labelAndInputContainer.style.alignSelf = "stretch";
            labelAndInputContainer.style.display = "flex";
            labelAndInputContainer.style.flexDirection = "column";
            prefix.style.alignSelf = "flex-start";
            suffix.style.alignSelf = "flex-start";
        } else {
            inputWrapper.style.removeProperty("flex-grow");
            labelAndInputContainer.style.removeProperty("align-self");
            labelAndInputContainer.style.removeProperty("display");
            labelAndInputContainer.style.removeProperty("flex-direction");
            prefix.style.removeProperty("flex-start");
            suffix.style.removeProperty("flex-start");
        }
    }
}

customElements.define('tg-rich-text-editor', TgRichTextEditor);