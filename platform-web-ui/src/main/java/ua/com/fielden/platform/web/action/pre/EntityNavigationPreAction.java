package ua.com.fielden.platform.web.action.pre;

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
                + "    action._oldRestoreActiveElement = action.restoreActiveElement;"
                + "    action.restoreActiveElement = function () {"
                + "        action._oldRestoreActiveElement();%n"
                + "        this.$.egi.editEntity(null);%n"
                + "    }.bind(self);%n"
                + "    action._setEntityAndReload = function (entity) {%n"
                + "        action.currentEntity = entity;%n"
                + "        this.$.egi.editEntity(entity);%n"
                + "        const master = action._masterReferenceForTesting;"
                + "        if (master) {%n"
                + "            master.savingContext = action._createContextHolderForAction();"
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
                + "    action._updateNavigationProps = function () {%n"
                + "        const egi = this.$.egi;%n"
                + "        action._hasPrev  = action.hasPreviousEntry();%n"
                + "        action._hasNext = action.hasNextEntry();%n"
                + "        action._count = egi.filteredEntities.length;%n"
                + "        action._entInd = egi.findEntityIndex(action.currentEntity);%n"
                + "        action.fire('tg-action-navigation-changed', {%n"
                + "            hasPrev: action._hasPrev,%n"
                + "            hasNext: action._hasNext,%n"
                + "            count: action._count,%n"
                + "            entInd: action._entInd%n"
                + "        });%n"
                + "    }.bind(self);%n"
                + "    self.addEventListener('tg-entity-centre-refreshed', action._updateNavigationProps);%n"
                + "}%n"
                + "if (self.$.egi.filteredEntities.length > 0) {%n"
                + "    action.currentEntity = self.$.egi.filteredEntities[0];%n"
                + "    self.$.egi.editEntity(action.currentEntity);%n"
                + "}%n"
                + "action.hasPrev = action.hasPreviousEntry();%n"
                + "action.hasNext = action.hasNextEntry();%n"
                + "action.count = self.$.egi.filteredEntities.length;%n"
                + "action.entInd = 0;%n", navigationType));
    }

}
