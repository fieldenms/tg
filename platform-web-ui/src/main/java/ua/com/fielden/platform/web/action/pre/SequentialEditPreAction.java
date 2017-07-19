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
                + "    const selectedEntitiesToEdit = self.$.egi.getSelectedEntities();\n"
                + "    self.seqEditIds = selectedEntitiesToEdit.length > 0 ? selectedEntitiesToEdit : self.$.egi.entities.slice();\n"
                + "    if (self.seqEditIds.length > 0) {\n"
                + "        action.currentEntity = self.seqEditIds.shift();\n"
                + "    }\n"
                + "    const cancelEditing = (function (data) {\n"
                + "        delete this.seqEditIds;\n"
                + "        this.seqEditSuccessPostal.unsubscribe();\n"
                + "        this.seqEditCancelPostal.unsubscribe();\n"
                + "    }).bind(self);\n"
                + "    const updateCacheAndContinueSeqSaving = (function (data) {\n"
                + "        const nextEntity = this.seqEditIds.shift();\n"
                + "        if (nextEntity) {\n"
                + "            action.currentEntity = nextEntity;\n"
                + "            action._run();\n"
                + "        } else {\n"
                + "            cancelEditing(data);\n"
                + "        }\n"
                + "    }).bind(self);\n"
                + "    action.continuous = true;\n"
                + "    action.skipNext = function() {\n"
                + "        updateCacheAndContinueSeqSaving();\n"
                + "    };\n"
                + "    self.seqEditSuccessPostal = postal.subscribe({\n"
                + "        channel: self.uuid,\n"
                + "        topic: 'save.post.success',\n"
                + "        callback: updateCacheAndContinueSeqSaving\n"
                + "    });\n"
                + "    self.seqEditCancelPostal = postal.subscribe({\n"
                + "        channel: self.uuid,\n"
                + "        topic: 'refresh.post.success',\n"
                + "        callback: cancelEditing"
                + "    });\n"
                + "}\n");
    }

}
