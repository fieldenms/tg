package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * This pre-action implementation should be used only with sequential edit action.
 *
 * @author TG Team
 *
 */
public class SequentialOpenPreAction implements IPreAction {

    private final String navigationType;

    public SequentialOpenPreAction(final String navigationType) {
        this.navigationType = navigationType;
    }

    @Override
    public JsCode build() {
        return new JsCode(String.format("%n"
                + "if (!action.supportsNavigation) {%n"
                + "    action.supportsNavigation = true;%n"
                + "    action.navigationType = '%s';%n"
                + "    action._setEntityAndReload = function (entity) {%n"
                + "        action.currentEntity = entity;%n"
                + "        const master = action._masterReferenceForTesting;"
                + "        if (master) {%n"
                + "            master.savingContext = action._createContextHolderForAction();"
                + "            master.retrieve(master.savingContext).then(function(ironRequest) {%n"
                + "                if (action.modifyFunctionalEntity) {%n"
                + "                    action.modifyFunctionalEntity(master._currBindingEntity, master, action);%n"
                + "                }%n"
                + "                return master.save();%n"
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
                + "        return entityIndex > 0;%n"
                + "    }.bind(self);%n"
                + "    action.hasNextEntry = function() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        return entityIndex >= 0 && entityIndex < this.$.egi.filteredEntities.length - 1;%n"
                + "    }.bind(self);%n"
                + "    action._updateNavigationProps = function (e) {%n"
                + "        action.set('_hasPrev', action.hasPreviousEntry());%n"
                + "        action.set('_hasNext', action.hasNextEntry());%n"
                + "    }.bind(self);%n"
                + "    self.addEventListener('tg-entity-centre-refreshed', action._updateNavigationProps);%n"
                + "}%n"
                + "if (self.$.egi.filteredEntities.length > 0) {%n"
                + "    action.currentEntity = self.$.egi.filteredEntities[0];%n"
                + "}%n"
                + "action._hasPrev = action.hasPreviousEntry();%n"
                + "action._hasNext = action.hasNextEntry();%n", navigationType));
    }

}
