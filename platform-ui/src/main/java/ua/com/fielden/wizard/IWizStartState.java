package ua.com.fielden.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A class that represents a start state that is associated with a wizard page.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWizStartState<T extends AbstractEntity<?>> extends IWizState<T> {
    IWizState<T> next();
}
