package ua.com.fielden.platform.web.action;

import ua.com.fielden.platform.entity.functional.IFunctionalEntity;
import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * Represents the action that is to be invoked from Web UI to the server resource represented with {@link IFunctionalEntity}. Also provides a contract for implementing "before" and
 * "after" action execution.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebAction<F extends IFunctionalEntity> {

    private final Class<F> funcEntityClass;
    private final String caption;

    /**
     * Creates new instance of {@link AbstractWebAction} for specified {@link IFunctionalEntity}.
     *
     * @param funcEntityClass
     */
    public AbstractWebAction(final String caption, final Class<F> funcEntityClass) {
        this.caption = caption;
        this.funcEntityClass = funcEntityClass;
    }

    public Class<F> getFuncEntityClass() {
        return funcEntityClass;
    }

    public String getCaption() {
        return caption;
    }

    /**
     * Will be invoked before sending request to {@link IFunctionalEntity} server resource.
     */
    public abstract JsCode preAction();

    /**
     * Will be invoked after receiving result from {@link IFunctionalEntity} server resource. The name of the result should be 'detail.response' by convenience.
     */
    public abstract JsCode postAction();

    /**
     * Will be invoked when exception raised during action execution or pre/post action execution. The name of the error should be 'error' by convenience.
     */
    public abstract JsCode onError();
}
