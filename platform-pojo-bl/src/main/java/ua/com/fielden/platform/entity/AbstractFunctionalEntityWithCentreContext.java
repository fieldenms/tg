package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A base class for functional entities that are intended to be used on entity centres. The main difference with {@link AbstractEntity} is that it has property <code>context</code>
 * of type {@link CentreContext}, which represents an execution context as provided when a functional entity gets actioned.
 * <p>
 * It is assumed of course that entity companion is capable of taking advantage of the provided context information for functional entity computations.
 *
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractFunctionalEntityWithCentreContext<K extends Comparable> extends AbstractEntity<K> {
    private static final long serialVersionUID = 1L;

    public static final String CONTEXT = "context";

    @IsProperty
    @Title(value = "Context", desc = "Context")
    @Invisible
    private CentreContext<?, ?> context;

    @Observable
    public AbstractFunctionalEntityWithCentreContext<K> setContext(final CentreContext<?, ?> context) {
        this.context = context;
        return this;
    }

    public CentreContext<?, ?> getContext() {
        return context;
    }
}
