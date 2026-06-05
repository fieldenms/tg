import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js';
import { allDefined } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
    </style>`;
const customLabelTemplate = html`
    <label style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity, _scanAvailable)]]" slot="label">
        <span class="label-title" on-down="_labelDownEventHandler">[[propTitle]]</span>
        <iron-icon class="label-action" hidden$="[[noLabelFloat]]" id="copyIcon" icon="icons:content-copy" on-tap="_copyTap"></iron-icon>
        <iron-icon class="label-action" hidden$="[[!_canScan(hideQrCodeScanner, noLabelFloat, entity, propertyName)]]" id="scanIcon" icon="tg-icons:qrcode-scan" on-down="_preventFocusOut" on-tap="_scanTap"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper collectional-representor-input">
        <input
            id="input"
            class="custom-input"
            on-change="_onChange"
            on-input="_onInput"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-keydown="_onKeydown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, entity, _scanAvailable)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgCollectionalRepresentor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate, customLabelTemplate);
    }
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString (value) {
        if (value && !Array.isArray(value)) {
            throw 'Unsupported value has appeared inside collectional representor: ' + value;
        }
        return super.convertToString(value);
    }

    /**
     * This method promotes 'IRRELEVANT' into _acceptedValue which should not be a problem, since this 'representor' is not editable at all.
     */
    convertFromString (strValue) {
        return 'IRRELEVANT';
    }

    /**
     * This 'representor' is disabled for editing (just gives a view of the entity collection).
     */
    _isDisabled (currentState, bindingEntity, propertyName) {
        return true;
    }

    /**
     * This 'representor' is disabled for editing and therefore can't use QR / barcode scanning (see isDisabled).
     */
    _canScan (hideQrCodeScanner, noLabelFloat, entity, propertyName) {
        return false;
    }

    _getTooltip (_editingValue, entity, _scanAvailable) {
        if (!allDefined(arguments)) {
            return "";
        }
        if (entity !== null) {
            const fullEntity = this.reflector().tg_getFullEntity(entity);
            let valueToFormat = "";
            if (this.reflector().isError(fullEntity.prop(this.propertyName).validationResult())) {
                valueToFormat = _editingValue;
            } else {
                valueToFormat = fullEntity.get(this.propertyName);
            }
            return super._getTooltip(valueToFormat, _scanAvailable);
        }
        return "";
    }
    
    _formatTooltipText (valueToFormat) {
        if (valueToFormat !== null) {
            if (Array.isArray(valueToFormat)) {
                return this.reflector().tg_toString(valueToFormat, this.entity.type(), this.propertyName, { collection: true, asTooltip: true });
            } else {
                throw 'Unsupported value has appeared inside collectional representor: ' + valueToFormat;
            }
        }
        return '';
    }

    /**
     * Handler for converting original property value for this editor.
     * Overridden to use `_currBindingEntity['@@origin']` as a source for original values.
     */
    _originalEntityChanged (newValue, oldValue) {
        if (this.reflector().isEntity(newValue)) {
            // Lazy conversion of original property value performs here.
            // Previously it was done for all properties inside `tg-entity-binder-behavior`.

            // However, as a source for original values we specify `@@origin` (full entity) from `this.entity`, not `originalEntity`.
            // This is because collectional representer does not modify collections and is special.
            // Converted values for it on server are List<String> and we can't set List<String> into entity-typed collection.
            // The only case where modifHolder contains 'val' (and thus forced to be applied) is in:
            //   1. conflicting situation (on non-collectional) prop,
            //   2. coupled with actual collection change (with `This property has been recently changed.` message there).
            // But this is only because `@@origin` (full entity) for `originalEntity` takes from `previousOriginalBindingEntity`.
            // See `tg-entity-binder-behavior._extractOriginalBindingView` for more details.
            // That's why we override this behaviour and take newest full entity (as if there were no conflicting errors).
            // See also `tg-entity-binder-behavior._postEntityReceived` and how `isEntityStale` is calculated.
            this._convertPropertyValue(newValue, this.propertyName, true /* original? */, this.reflector().tg_getFullEntity(this.entity));
        }
    }

}

customElements.define('tg-collectional-representor', TgCollectionalRepresentor);