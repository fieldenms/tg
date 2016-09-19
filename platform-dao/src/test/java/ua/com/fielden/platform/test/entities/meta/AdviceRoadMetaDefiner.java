package ua.com.fielden.platform.test.entities.meta;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.test.domain.entities.Advice;

/**
 * Implements the rule that define properties for meta-property associated with property <code>road</code> of entity {@link Advice}.
 * 
 * @author 01es
 * 
 */
public class AdviceRoadMetaDefiner implements IAfterChangeEventHandler {

    /**
     * The ability to modify property <code>carrier</code> is driven by the value of property <code>road</code>.
     */
    @Override
    public void handle(final MetaProperty property, final Object entityPropertyValue) {
        final Advice advice = (Advice) property.getEntity();
        final Boolean road = (Boolean) entityPropertyValue;
        System.out.println("DEFINE RULE IS BEING EXECUTED ON PROPETY " + property.getName() + " FOR " + advice);
        advice.getProperty("carrier").setEditable(!road && !advice.isDispatched());
    }
}
