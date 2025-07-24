import { TgReflector } from '/app/tg-reflector.js';

/**
 * A function to initialise reference hierarchy support for 'self' centre (master) and 'action'.
 * This is intended to be invoked in 'preAction' of the corresponding 'action'.
 */
export function referenceHierarchy(action, self, useMasterEntity) {
    const reflector = new TgReflector();
    let entity = null;
    if (action.requireSelectedEntities === 'ONE') {
        entity = action.currentEntity();
    } else if (action.requireSelectedEntities === 'ALL' && self.$.egi.getSelectedEntities().length > 0) {
        entity = self.$.egi.getSelectedEntities()[0];
    } else if (action.requireMasterEntity === "true") {
        if (useMasterEntity) {
            entity = action.parentElement.entity['@@origin'];
        } else {
            const value = reflector.tg_getFullValue(action.parentElement.entity, action.parentElement.propertyName);
            entity = reflector.isEntity(value) ? value : action.parentElement.entity['@@origin'];
        }
    }
    if (entity) {
        action.shortDesc = reflector.getType(entity.constructor.prototype.type.call(entity).notEnhancedFullClassName()).entityTitle();
    }
}