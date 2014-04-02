package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

/**
 * Class, holding static utility methods for EGI-related classes.
 * 
 * @author yura
 * 
 */
public class EgiUtilities {

    public static <T extends AbstractEntity> ComponentFactory.IOnCommitAction[] convert(final T entity, final EntityGridInspector<T> egi, final IOnCommitAction[] onCommitActions) {
        final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = new ComponentFactory.IOnCommitAction[onCommitActions.length];
        for (int i = 0; i < onCommitActions.length; i++) {
            final IOnCommitAction<T> onCommitAction = onCommitActions[i];
            onCommitActionWrappers[i] = new ComponentFactory.IOnCommitAction() {
                @Override
                public void postCommitAction() {
                    onCommitAction.postCommitAction(entity, egi);
                }

                @Override
                public void postNotSuccessfulCommitAction() {
                    onCommitAction.postNotSuccessfulCommitAction(entity, egi);
                }

                @Override
                public void postSuccessfulCommitAction() {
                    onCommitAction.postSuccessfulCommitAction(entity, egi);
                }
            };
        }
        return onCommitActionWrappers;
    }

}
