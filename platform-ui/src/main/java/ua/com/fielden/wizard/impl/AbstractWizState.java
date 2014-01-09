package ua.com.fielden.wizard.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.wizard.IWizState;

/**
 * An abstract implementation of {@link IWizState} that can be used as a super type for implementing concrete wizard states.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractWizState<T extends AbstractEntity<?>> implements IWizState<T> {

    protected final T model;

    protected AbstractWizState(final T model) {
	this.model  = model;
    }

    @Override
    public T model() {
        return model;
    }

}
