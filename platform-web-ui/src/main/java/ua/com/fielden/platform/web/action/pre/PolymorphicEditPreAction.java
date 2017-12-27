package ua.com.fielden.platform.web.action.pre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.web.action.exceptions.ActionConfigurationException;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Pre-action implementation to support editing of polymorphic entities.
 * <p>
 * To support editing through several different masters: a) add one concrete action b) add several children actions to it,
 * and c) use {@link PolymorphicEditPreAction} with JavaScript cases which action (child or 'this') needs to be used for editing.
 *
 * @author TG Team
 *
 */
public class PolymorphicEditPreAction implements IPreAction {
    private final Map<String, String> predicateAndActions = new LinkedHashMap<>();
    
    /**
     * Creates resultant {@link JsCode} for this pre-action.
     */
    @Override
    public JsCode build() {
        final StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("const parentSelector = '[parent-element-alias=\"' + action.elementAlias + '\"]';\n");
        codeBuilder.append("const entity = action.currentEntity;\n");
        codeBuilder.append("const chosenProperty = action.chosenProperty;\n");
        final Iterator<Entry<String, String>> iterator = predicateAndActions.entrySet().iterator();
        if (iterator.hasNext()) {
            codeBuilder.append(predicateAndActionCode(iterator.next(), true));
            while (iterator.hasNext()) {
                codeBuilder.append(predicateAndActionCode(iterator.next(), false));
            }
            codeBuilder.append("}\n");
        }
        codeBuilder.append("throw 'There is no suitable case for entity [' + entity + '] (polymorphic editing).';\n");
        return new JsCode(codeBuilder.toString());
    }
    
    /**
     * Generates JS code for concrete polymorphic editing case that glues together with previous case through '} else ' part
     * (or does not have '} else ' part if <code>predicateAndAction</code> represents first case).
     * 
     * @param predicateAndAction
     * @param first
     * @return
     */
    private String predicateAndActionCode(final Entry<String, String> predicateAndAction, final boolean first) {
        final String ifPart = (first ? "" : "} else ") + "if (" + predicateAndAction.getKey() + ") {\n";
        if ("this".equalsIgnoreCase(predicateAndAction.getValue())) {
            return ifPart
                + "    return true;\n";
        } else {
            return ifPart
                + "    const concreteAction = self.querySelector('tg-ui-action' + parentSelector + '[child-name=\"" + predicateAndAction.getValue() + "\"]');\n"
                + "    concreteAction.currentEntity = action.currentEntity;\n"
                + "    concreteAction.chosenProperty = action.chosenProperty;\n"
                + "    concreteAction._run();\n"
                + "    return false;\n";
        }
    }

    /**
     * Adds sequential JS case for opening polymorphic master depending on currently clicked entity and chosen property.
     * 
     * @param predicate -- JS predicate for JavaScript <code>entity</code> and <code>chosenProperty</code>
     * @param actionNameOrThis -- action name (key for corresponding child action) or 'this' (for this action itself) that represents the action to be invoked if the predicate satisfies
     * @return
     */
    public PolymorphicEditPreAction caseWhen(final String predicate, final String actionNameOrThis) {
        if (isEmpty(predicate)) {
            throw new ActionConfigurationException("The predicate for polymorphic editing is empty.");
        }
        if (isEmpty(actionNameOrThis)) {
            throw new ActionConfigurationException("The actionName for polymorphic editing is empty.");
        }
        predicateAndActions.put(predicate, actionNameOrThis);
        return this;
    }
}
