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
                + "    if (self.$.egi.getSelectedEntities() == 0) {\n"
                + "        self.$.egi.selectAll(true);\n"
                + "    }\n"
                + "    self.seqEditIds = self.$.egi.getSelectedEntities();\n"
                + "    action.currentEntity = self.seqEditIds.shift();\n"
                + "    const cancelEditing = (function (data) {\n"
                + "        delete this.seqEditIds;\n"
                + "        this.seqEditSuccessPostal.unsubscribe();\n"
                + "        this.seqEditCancelPostal.unsubscribe();\n"
                + "    }).bind(self);\n"
                + "    const updateCacheAndContinueSeqSaving = (function (shouldUnselect) {\n"
                + "        const nextEntity = this.seqEditIds.shift();\n"
                + "        if (shouldUnselect !== false) {\n"
                + "            self.$.egi.selectEntity(action.currentEntity, false);\n"
                + "        }\n"
                + "        if (nextEntity) {\n"
                + "            action.currentEntity = nextEntity;\n"
                + "            action._run();\n"
                + "        } else {\n"
                + "            cancelEditing(shouldUnselect);\n"
                + "        }\n"
                + "    }).bind(self);\n"
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
