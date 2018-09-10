package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * This pre-action implementation should be used only with sequential edit action.
 *
 * @author TG Team
 *
 */
public class EntityNavigationPreAction implements IPreAction {

    private final String navigationType;

    public EntityNavigationPreAction(final String navigationType) {
        this.navigationType = navigationType;
    }

    @Override
    public JsCode build() {
        return new JsCode(String.format("%n"
                + "if (!action.supportsNavigation) {%n"
                + "    action.supportsNavigation = true;%n"
                + "    action.navigationType = '%s';%n"
                + "    action._oldRestoreActiveElement = action.restoreActiveElement;%n"
                + "    action.restoreActiveElement = function () {%n"
                + "        action._oldRestoreActiveElement();%n"
                + "        this.$.egi.editEntity(null);%n"
                + "        action.currentEntity = null;%n"
                + "    }.bind(self);%n"
                + "    action._setEntityAndReload = function (entity) {%n"
                + "        action.currentEntity = entity;%n"
                + "        this.$.egi.editEntity(entity);%n"
                + "        const master = action._masterReferenceForTesting;%n"
                + "        if (master) {%n"
                + "            master.savingContext = action._createContextHolderForAction();%n"
                + "            master.retrieve(master.savingContext).then(function(ironRequest) {%n"
                + "                if (action.modifyFunctionalEntity) {%n"
                + "                    action.modifyFunctionalEntity(master._currBindingEntity, master, action);%n"
                + "                }%n"
                + "                master.save().then(function (ironRequest) {%n"
                + "                    action._updateNavigationProps();%n"
                + "                }.bind(self));%n"
                + "            }.bind(self));%n"
                + "        }%n"
                + "    }.bind(self);%n"
                + "    action.firstEntry = function() {%n"
                + "        if (this.$.egi.filteredEntities.length > 0) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[0]);%n"
                + "        }%n"
                + "    }.bind(self);%n"
                + "    action.previousEntry = function() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        if (entityIndex > 0) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[entityIndex - 1]);%n"
                + "        } else if (this.$.egi.filteredEntities.length > 0) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[0]);%n"
                + "        }%n"
                + "    }.bind(self);%n"
                + "    action.nextEntry = function() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        if (entityIndex >= 0 && entityIndex < this.$.egi.filteredEntities.length - 1) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[entityIndex + 1]);%n"
                + "        } else if (this.$.egi.filteredEntities.length > 0) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[0]);%n"
                + "        }%n"
                + "    }.bind(self);%n"
                + "    action.lastEntry = function() {%n"
                + "        if (this.$.egi.filteredEntities.length > 0) {%n"
                + "            action._setEntityAndReload(this.$.egi.filteredEntities[this.$.egi.filteredEntities.length - 1]);%n"
                + "        }%n"
                + "    }.bind(self);%n"
                + "    action.hasPreviousEntry = function() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        return (entityIndex < 0 && this.$.egi.filteredEntities.length > 0) || entityIndex > 0;%n"
                + "    }.bind(self);%n"
                + "    action.hasNextEntry = function() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        return (entityIndex < 0 && this.$.egi.filteredEntities.length > 0) || (entityIndex >= 0 && entityIndex < this.$.egi.filteredEntities.length - 1);%n"
                + "    }.bind(self);%n"
                + "    action._updateNavigationProps = function (typeChanged) {%n"
                + "        const egi = this.$.egi;%n"
                + "        action.hasPrev  = action.hasPreviousEntry();%n"
                + "        action.hasNext = action.hasNextEntry();%n"
                + "        action.count = egi.filteredEntities.length;%n"
                + "        action.entInd = egi.findEntityIndex(action.currentEntity);%n"
                + "        action.fire('tg-action-navigation-changed', {%n"
                + "            hasPrev: action.hasPrev,%n"
                + "            hasNext: action.hasNext,%n"
                + "            count: action.count,%n"
                + "            entInd: action.entInd,%n"
                + "        });%n"
                + "    }.bind(self);%n"
                + "    self.addEventListener('tg-entity-centre-refreshed', action._updateNavigationProps);%n"
                + "}%n"
                + "if (action.currentEntity) {%n"
                + "    self.$.egi.editEntity(action.currentEntity);%n"
                + "    action.entInd = self.$.egi.findEntityIndex(action.currentEntity);%n"
                + "} else if (self.$.egi.filteredEntities.length > 0) {%n"
                + "    action.currentEntity = self.$.egi.filteredEntities[0];%n"
                + "    self.$.egi.editEntity(action.currentEntity);%n"
                + "    action.entInd = 0;%n"
                + "}%n"
                + "action.hasPrev = action.hasPreviousEntry();%n"
                + "action.hasNext = action.hasNextEntry();%n"
                + "action.count = self.$.egi.filteredEntities.length;%n", navigationType, EntityNavigationAction.class.getName()));
    }

}
