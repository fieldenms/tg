package ua.com.fielden.platform.web.centre.api.context;

import java.util.function.Function;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface IEntityCentreContextSelectorFunction<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorDone<T> {

    IEntityCentreContextSelectorDone<T> withComputation(Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computation);
}
