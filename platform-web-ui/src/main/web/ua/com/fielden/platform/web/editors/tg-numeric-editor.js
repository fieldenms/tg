import { TgEditor } from '/resources/editors/tg-editor.js';
import { truncateInsignificantZeros } from '/resources/reflection/tg-numeric-utils.js';

export class TgNumericEditor extends TgEditor {
    
    constructor () {
        super();
        this._hasLayer = true;
    }
    
    _formatText (_editingValue) {
        if (this.reflector().isEntity(this.entity)) {
            return this.reflector().tg_toString(this.convertFromString(_editingValue), this.entity.type(), this.propertyName, { bindingValue: true, display: true, locale: this.$.appConfig.locale });
        }
        return '';
    }
    
    /**
     * Overridden to provide value corrections.
     */
    _commitForDescendants () {
        const correctedValue = truncateInsignificantZeros(this._editingValue);
        if (!this.reflector().equalsEx(correctedValue, this._editingValue)) {
            this._editingValue = correctedValue;
        }
    }
    
}