package ua.com.fielden.platform.continuation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.error.Result;

/// A special kind of [Result] that represent a need for additional information in the context of the companion's method [#save(AbstractEntity)], so that it could continue its execution.
/// An instance of [NeedMoreData] should be thrown very much the same way as an ordinary exception to inform the execution context of method `save` that additional information is required.
///
/// The thrown instance should contain an action-entity instance or its type (i.e. a descendant of [AbstractFunctionalEntityWithCentreContext]) that represents the data needed,
/// and a key that this data should be registered under for the companion to access it.
///
public class NeedMoreData extends Result {

    public <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreData(final String customMessage, final Class<T> dataType, final String dataKey) {
        super(new NeedMoreDataException(customMessage, dataType, dataKey));
    }
    
    public <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreData(final Class<T> dataType, final String dataKey) {
        super(new NeedMoreDataException(dataType, dataKey));
    }

    public <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreData(final String customMessage, final T data, final String dataKey) {
        super(new NeedMoreDataException(customMessage, data, dataKey));
    }

    public <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreData(final T data, final String dataKey) {
        super(new NeedMoreDataException(data, dataKey));
    }

}
