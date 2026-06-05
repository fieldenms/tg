import { TgEditor } from '/resources/editors/tg-editor.js';
import { truncateInsignificantZeros } from '/resources/reflection/tg-numeric-utils.js';

export class TgNumericEditor extends TgEditor {
    
    constructor () {
        super();
        this._hasLayer = true;
    }
    
    _formatText (_editingValue) {
        if (this.reflector().isEntity(this.entity)) {
            try {
                return this.reflector().tg_toString(this.convertFromString(_editingValue), this.entity.type(), this.propertyName, { bindingValue: true, display: true });
            } catch (error) {
                return _editingValue;
            }
        }
        return '';
    }

    _copyTap () {
        // copy to clipboard should happen only if there is something to copy
        if (navigator.clipboard && this.$.inputLayer.innerText) {
            navigator.clipboard.writeText(this.$.inputLayer.innerText);
            this._showCheckIconAndToast(this.$.inputLayer.innerText);
        } else if (this.toaster) {
            this.toaster.openToastWithoutEntity("Nothing to copy", true, "There was nothing to copy.", false);
        }
    }
    
    /**
     * Overridden to provide value adjustment for zeroes.
     */
    _commitForDescendants () {
        const adjustedValue = truncateInsignificantZeros(this._editingValue);
        if (!this.reflector().equalsEx(adjustedValue, this._editingValue)) {
            this._editingValue = adjustedValue;
        }
    }
    
}