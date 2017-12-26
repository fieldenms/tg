package ua.com.fielden.platform.web.action.pre;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Pre-action implementation to support editing of polymorphic entities.
 *
 * @author TG Team
 *
 */
public class PolymorphicEditPreAction implements IPreAction {
    private final Map<String, String> predicateAndActions = new LinkedHashMap<>();
    
    @Override
    public JsCode build() {
        final StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("const parentSelector = '[parent-element-alias=\"' + action.elementAlias + '\"]';\n");
        codeBuilder.append("const entity = action.currentEntity;\n");
        final Iterator<Entry<String, String>> iterator = predicateAndActions.entrySet().iterator();
        if (iterator.hasNext()) {
            final Entry<String, String> firstPredicateAndAction = iterator.next();
            codeBuilder.append(predicateAndActionCode(firstPredicateAndAction, true));
            while (iterator.hasNext()) {
                codeBuilder.append(predicateAndActionCode(iterator.next(), false));
            }
            codeBuilder.append("}\n");
        }
        codeBuilder.append("throw 'There is no suitable case for entity [' + entity + '] (polymorphic editing).';\n");
        return new JsCode(codeBuilder.toString());
    }
    
    private String predicateAndActionCode(final Entry<String, String> predicateAndAction, final boolean first) {
        if ("this".equalsIgnoreCase(predicateAndAction.getValue())) {
            return ""
                + (first ? "" : "} else ") + "if (" + predicateAndAction.getKey() + ") {\n"
                + "    return true;\n";
        } else {
            return ""
                + (first ? "" : "} else ") + "if (" + predicateAndAction.getKey() + ") {\n"
                + "    const concreteAction = self.querySelector('tg-ui-action' + parentSelector + '[child-name=\"" + predicateAndAction.getValue() + "\"]');\n"
                + "    concreteAction.currentEntity = action.currentEntity;\n"
                + "    concreteAction.chosenProperty = action.chosenProperty;\n"
                + "    concreteAction._run();\n"
                + "    return false;\n";
        }
    }

    /**
     * Adds sequential JS case for opening polymorphic master depending on currently clicked entity.
     * 
     * @param predicate -- JS predicate for JavaScript 'entity' constant
     * @param actionNameOrThis -- action name (child key for parent action) or 'this' (for parent action itself)
     * @return
     */
    public PolymorphicEditPreAction caseWhen(final String predicate, final String actionNameOrThis) {
        predicateAndActions.put(predicate, actionNameOrThis);
        return this;
    }
}
