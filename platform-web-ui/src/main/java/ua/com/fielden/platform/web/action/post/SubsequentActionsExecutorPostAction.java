package ua.com.fielden.platform.web.action.post;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * A standard post-action that should be used to invoke additional actions sequentially.
 * 
 * @author TG Team
 *
 */
public class SubsequentActionsExecutorPostAction implements IPostAction {
    private final List<Pair<EntityActionConfig, String>> subsequentActions;
    
    public SubsequentActionsExecutorPostAction(final List<Pair<EntityActionConfig, String>> subsequentActions) {
        this.subsequentActions = subsequentActions;
    }

    @Override
    public JsCode build() {
        final JsCode jsCode = new JsCode(
                "console.debug('SEQUENTIAL ACTION STARTS FOR IDENTIFIER: ', '" + subsequentActions.get(0).getValue() + "', functionalEntity, self, master);\n"
              + "var subsequentAction = self.querySelector('tg-ui-action[identifier=\"" + subsequentActions.get(0).getValue() + "\"]');\n"
              //    + "master.entityType = 'TgExportFunctionalEntity';\n"
              + "subsequentAction.createContextHolder = master._createContextHolder;\n"
              + "subsequentAction._run();\n"
              );
        return jsCode;
    }
    
    public List<Pair<EntityActionConfig, String>> getSubsequentActions() {
        return Collections.unmodifiableList(subsequentActions);
    }
}
