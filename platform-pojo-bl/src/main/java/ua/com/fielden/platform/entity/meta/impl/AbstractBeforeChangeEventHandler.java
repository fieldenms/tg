package ua.com.fielden.platform.entity.meta.impl;

import com.google.inject.Inject;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

/**
 * A convenient base class for implementing {@link IBeforeChangeEventHandler} that would benefit from easy access to various companion objects.
 * It is envisaged that all BCE handlers are instantiated by means of IoC, which is required to properly initialise instances of {@link AbstractBeforeChangeEventHandler}.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractBeforeChangeEventHandler<T> implements IBeforeChangeEventHandler<T> {

    @Inject
    private ICompanionObjectFinder coFinder;
    
    @Override
    public <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co(final Class<E> type) {
        if (coFinder == null) {
            throw new BceOrAceInitException("Companion object finder has not been instantiatiated.");
        }
        
        return coFinder.findAsReader(type, true);
    }

    
}
