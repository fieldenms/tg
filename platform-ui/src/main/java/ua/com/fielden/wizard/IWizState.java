package ua.com.fielden.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A class that represents a single state that is associated with a wizard page.
 * The state serves as a model for associated with it view, which can be obtained by using method <code>view()</code>.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWizState<T extends AbstractEntity<?>> {

    /**
     * Returns a view associated with this state.
     *
     * @return
     */
    AbstractWizardPage<T> view();

    /**
     * Returns an entity instance that serves as a wizard's model.
     *
     * @return
     */
    T model();

    /**
     * Returns a state that led to this state.
     * Could be <code>null</code> if the current state represents the start.
     *
     * @return
     */
    IWizState<T> getTransitionedFrom();

    /**
     * Set the actual previous state that led to this one.
     * @param state
     */
    void setTransitionedFrom(final IWizState<T> state);

    /**
     * Should return a unique name of the state.
     *
     * @return
     */
    String name();
}
