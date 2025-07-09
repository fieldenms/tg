import { getParentAnd } from '/resources/reflection/tg-polymer-utils.js';

/**
 * A function to bind saved `property` value from a `functionalEntity` to parent Entity Master (likely `self` or above it).
 * This is intended to be invoked in 'postActionSuccess|Error' of the corresponding action.
 *
 * @param erroneous - true if `functionalEntity` saving was successful, false otherwise
 */
export function bindSavedProperty(functionalEntity, property, self, erroneous) {
    const parentMasterName = `tg-${functionalEntity.type().prop(property).type()._simpleClassName()}-master`;
    const parentMaster = getParentAnd(self, parent => parent.matches(parentMasterName));
    const masterEntity = functionalEntity.get(property);
    if (erroneous) {
        parentMaster._provideExceptionOccurred(masterEntity, functionalEntity.exceptionOccurred());
    }
    // in successful case leave propertyActionIndices as previously;
    //  we are not able to calculate them in companion 'save' methods because multi-action selectors are UI concept;
    //  still, this temporal unsyncing is not a problem;
    //  this is because propertyActionIndices will be updated on parentMaster 'validate' process following immediately after postActionSuccess (see tg-ui-action._onExecuted.postSaved postal publish)
    // in unsuccessful case it is even more important to leave propertyActionIndices as previously (not to clear them or something);
    //  this is because parentMaster update will not be performed and, in case of clearing, all actions on parentMaster will disappear (at this stage even non-multi action should have zero index)
    parentMaster._postSavedDefault([masterEntity, { propertyActionIndices: parentMaster._propertyActionIndices }]);
}