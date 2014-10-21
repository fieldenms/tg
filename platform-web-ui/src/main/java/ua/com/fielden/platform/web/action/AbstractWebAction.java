package ua.com.fielden.platform.web.action;

import ua.com.fielden.platform.entity.functional.IFunctionalEntity;

/**
 * Represents the action that is to be invoked from Web UI to the server resource represented with {@link IFunctionalEntity}.
 * Also provides a contract for implementing "before" and "after" action execution.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebAction<F extends IFunctionalEntity> {

    private final Class<F> funcEntityClass;

    /**
     * Creates new instance of {@link AbstractWebAction} for specified {@link IFunctionalEntity}.
     *
     * @param funcEntityClass
     */
    public AbstractWebAction(final Class<F> funcEntityClass) {
	this.funcEntityClass = funcEntityClass;
    }

    public Class<F> getFuncEntityClass() {
	return funcEntityClass;
    }

    /**
     * Will be invoked before sending request to {@link IFunctionalEntity} server resource.
     */
    public abstract String preAction();

    /**
     * Will be invoked after receiving result from {@link IFunctionalEntity} server resource.
     * The name of the result should be 'result' by convenience.
     */
    public abstract String postAction();

    /**
     * Will be invoked when exception raised during action execution or pre/post action execution.
     * The name of the error should be 'error' by convenience.
     */
    public abstract String onError();

    /**
     * The generated counterpart of this method sets the <code>result</code> value for the scope object's property with the specified <code>propertyName</code>.
     *
     * @param propertyName
     * @param result
     */
    protected void setScopeProperty(final String propertyName, final F result) {
	// TODO Auto-generated method stub
    }
}
