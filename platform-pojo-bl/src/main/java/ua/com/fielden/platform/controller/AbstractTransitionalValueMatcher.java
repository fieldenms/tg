package ua.com.fielden.platform.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.Finder;

/**
 * Provides a base class for convenient implementation of value matchers specific to transitional logic such as work order status transitions.
 *
 * @author TG Team
 *
 * @param <M>
 * @param <T>
 * @param <C>
 */
public abstract class AbstractTransitionalValueMatcher<M extends AbstractEntity<?>, T extends AbstractEntity<?>, C extends ITransitionController<T>> implements IValueMatcher<T> {

    private final String propertyName;
    private final C transController;
    private final boolean includeOriginal;

    public AbstractTransitionalValueMatcher(final String propertyName, final C transController) {
	this(propertyName, transController, true);
    }

    public AbstractTransitionalValueMatcher(final String propertyName, final C transController, final boolean includeOriginal) {
	this.propertyName = propertyName;
	this.transController = transController;
	this.includeOriginal = includeOriginal;
    }

    @Override
    public List<T> findMatches(final String value) {

	final T origValue = (T) Finder.findMetaProperty(getPropertyOwner(), propertyName).getOriginalValue(); // could be null

	// get transition values from  the current value and add the current to the resultant set
	final SortedSet<T> result = new TreeSet<T>();
	result.addAll(transController.transitionsFor(origValue));
	// add current status
	if (includeOriginal && getPropertyOwner().isPersisted()) {
	    final T currValue = (T) getPropertyOwner().get(propertyName);
	    if (currValue != null) {
		result.add(currValue);
	    }
	}
	// add original value to allow user switching back to the original value
	if (includeOriginal && origValue != null) {
	    result.add(origValue);
	}
	return new ArrayList<T>(result);
    }

    /**
     * Should be implemented to provide a way for obtaining an instance of property owner at runtime from the required context.
     *
     * @return
     */
    protected abstract M getPropertyOwner();

    @Override
    public List<T> findMatchesWithModel(final String value) {
	return findMatches(value);
    }


    @Override
    public <FT extends AbstractEntity<?>> fetch<FT> getFetchModel() {
	return null;
    }

    @Override
    public <FT extends AbstractEntity<?>> void setFetchModel(final fetch<FT> fetchModel) {
    }

    @Override
    public Integer getPageSize() {
	return null;
    }
}