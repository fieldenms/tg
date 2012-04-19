package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;

/**
 *
 * This is a container class to be used for carrying around an instance of {@link QueryExecutionModel} and dynamically generated types (as a list of byte arrays) referenced by this query model.
 *
 * @author TG Team
 *
 */
public class DynamicallyTypedQueryContainer {
    private final List<byte[]> dynamicallyGeneratedTypes = new ArrayList<byte[]>();
    private final QueryExecutionModel<?, ?> qem;

    public DynamicallyTypedQueryContainer(final List<byte[]> dynamicallyGeneratedTypes, final QueryExecutionModel<?, ?> qem) {
	dynamicallyGeneratedTypes.addAll(dynamicallyGeneratedTypes);
	this.qem = qem;
    }

    public List<byte[]> getDynamicallyGeneratedTypes() {
        return dynamicallyGeneratedTypes;
    }

    public QueryExecutionModel<?, ?> getQem() {
        return qem;
    }
}
