package ua.com.fielden.platform.web.centre.api.context;

import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;

public interface IEntityCentreContextSelectorFunction<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorDone<T> {

    IEntityCentreContextSelectorDone<T> withComputation(BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation);
}
