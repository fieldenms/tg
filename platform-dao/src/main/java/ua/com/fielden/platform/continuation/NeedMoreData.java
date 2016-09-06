package ua.com.fielden.platform.continuation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.error.Result;

/**
 * A special kind of {@link Result} that represent a need for additional information in the context of some companion's {@link IEntityDao#save(ua.com.fielden.platform.entity.AbstractEntity)} method in order for that method to continue its execution.
 * An instance of {@link NeedMoreData} should be thrown very much the same way as an ordinary exception to inform the execution context of method <code>save</code> that additional information is required.
 * The thrown instance should contain a functional entity type (i.e. descendant of {@link AbstractFunctionalEntityWithCentreContext}) that represents the needed data, and a key that this data should be register with as part of the companion.
 * The key is then used to access the provided data upon subsequent execution of the interrupted method <code>save</code>.  
 * 
 * @author TG Team
 *
 */
public class NeedMoreData extends Result {
    private static final long serialVersionUID = 1L;

    public NeedMoreData(final String customMessage, final Class<? extends IContinuationData> dataType, final String dataKey) {
        super(new NeedMoreDataException(customMessage, dataType, dataKey));
    }
    
    public NeedMoreData(final Class<? extends IContinuationData> dataType, final String dataKey) {
        super(new NeedMoreDataException(dataType, dataKey));
    }
    
}
