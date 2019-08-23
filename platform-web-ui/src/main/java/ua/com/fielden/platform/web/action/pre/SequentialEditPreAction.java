package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * This pre-action implementation should be used only with sequential edit action.
 *
 * @author TG Team
 *
 */
public class SequentialEditPreAction implements IPreAction {

    @Override
    public JsCode build() {
        return new JsCode("\n"
                + "if(!self.seqEditIds) {\n"
                + "    if (self.$.egi.getSelectedEntities().length == 0) {\n"
                + "        self.$.egi.selectAll(true);\n"
                + "    }\n"
                + "    self.seqEditIds = self.$.egi.getSelectedEntities();\n"
                + "    action.currentEntity = self.seqEditIds.shift();\n"
                + "    const cancelEditing = (function (data) {\n"
                + "        delete this.seqEditIds;\n"
                + "        this.seqEditSuccessPostal.unsubscribe();\n"
                + "        this.seqEditCancelPostal.unsubscribe();\n"
                + "        const master = action._masterReferenceForTesting;\n"
                + "        master.publishCloseForcibly();\n"
                + "        action.currentEntity = null;\n"
                + "    }).bind(self);\n"
                + "    const updateCacheAndContinueSeqSaving = (function (shouldUnselect) {\n"
                + "        const nextEntity = this.seqEditIds.shift();\n"
                + "        if (shouldUnselect !== false) {\n"
                + "            self.$.egi.selectEntity(action.currentEntity, false);\n"
                + "        }\n"
                + "        if (nextEntity) {\n"
                + "            setEntityAndReload(nextEntity, shouldUnselect ? null : 'skipNext');\n"
                + "        } else {\n"
                + "            cancelEditing(shouldUnselect);\n"
                + "        }\n"
                + "    }).bind(self);\n"
                + "    const setEntityAndReload = function (entity, spinnerInvoked) {\n"
                + "        if (entity) {\n"
                + "            action.currentEntity = entity;\n"
                + "            const master = action._masterReferenceForTesting;\n"
                + "            if (master) {\n"
                + "                master.fire('tg-action-navigation-invoked', {spinner: spinnerInvoked});\n"
                + "                master.savingContext = action._createContextHolderForAction();\n"
                + "                master.retrieve(master.savingContext).then(function(ironRequest) {\n"
                + "                    if (action.modifyFunctionalEntity) {\n"
                + "                        action.modifyFunctionalEntity(master._currBindingEntity, master, action);\n"
                + "                    }\n"
                + "                    master.addEventListener('binding-entity-loaded-and-focused', restoreNavigationButtonState);\n"
                + "                    master.save().then(function(value) {}, function (error) {\n"
                + "                        fireNavigationChangeEvent(true);\n"
                + "                    }.bind(self));\n"
                + "                }.bind(self), function (error) {\n"
                + "                    fireNavigationChangeEvent(true);\n"
                + "                }.bind(self));\n"
                + "            }\n"
                + "         }\n"
                + "    }.bind(self),\n"
                + "    fireNavigationChangeEvent = function (shouldResetSpinner) {\n"
                + "        const master = action._masterReferenceForTesting;\n"
                + "        if (master) {\n"
                + "            master.fire('tg-action-navigation-changed', {\n"
                + "                shouldResetSpinner: shouldResetSpinner\n,"
                + "            });\n"
                + "        }\n"
                + "    }.bind(self),\n"
                + "    restoreNavigationButtonState = function (e) {\n"
                + "        fireNavigationChangeEvent(false);\n"
                + "        const master = action._masterReferenceForTesting;\n"
                + "        master.removeEventListener('binding-entity-loaded-and-focused', restoreNavigationButtonState);\n"
                + "    }.bind(self);\n"
                + "    action.continuous = true;\n"
                + "    action.skipNext = function() {\n"
                + "        updateCacheAndContinueSeqSaving(false);\n"
                + "    };\n"
                + "    self.seqEditSuccessPostal = postal.subscribe({\n"
                + "        channel: self.uuid,\n"
                + "        topic: 'save.post.success',\n"
                + "        callback: updateCacheAndContinueSeqSaving\n"
                + "    }).defer();\n"
                + "    self.seqEditCancelPostal = postal.subscribe({\n"
                + "        channel: self.uuid,\n"
                + "        topic: 'refresh.post.success',\n"
                + "        callback: cancelEditing"
                + "    }).defer();\n"
                + "}\n");
    }

}
