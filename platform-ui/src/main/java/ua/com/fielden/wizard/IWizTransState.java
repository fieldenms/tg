package ua.com.fielden.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A class that represents a single transition state that is associated with a wizard page. Transition state can be one of the states between the start and one of the finish
 * states.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IWizTransState<T extends AbstractEntity<?>> extends IWizState<T> {
    IWizState<T> prev();

    IWizState<T> next();

    IWizState<T> cancel();
}
