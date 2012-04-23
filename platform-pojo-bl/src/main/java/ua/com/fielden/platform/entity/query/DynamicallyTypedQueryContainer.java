package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;

/**
 *
 * This is a container class to be used for carrying around an instance of {@link QueryExecutionModel} and dynamically generated types (as a list of byte arrays) referenced by this
 * query model.
 *
 * @author TG Team
 *
 */
public class DynamicallyTypedQueryContainer {
    private final List<byte[]> dynamicallyGeneratedTypes = new ArrayList<byte[]>();
    private final QueryExecutionModel<?, ?> qem;

    public DynamicallyTypedQueryContainer(final List<byte[]> dynamicallyGeneratedTypes, final QueryExecutionModel<?, ?> qem) {
	this.dynamicallyGeneratedTypes.addAll(dynamicallyGeneratedTypes);
	this.qem = qem;
    }

    public List<byte[]> getDynamicallyGeneratedTypes() {
	return dynamicallyGeneratedTypes;
    }

    public QueryExecutionModel<?, ?> getQem() {
	return qem;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	for (final byte[] bytes : dynamicallyGeneratedTypes) {
	    result = prime * result + Arrays.hashCode(bytes);
	}
	result = prime * result + ((qem == null) ? 0 : qem.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof DynamicallyTypedQueryContainer)) {
	    return false;
	}

	final DynamicallyTypedQueryContainer that = (DynamicallyTypedQueryContainer) obj;
	if (dynamicallyGeneratedTypes == null) {
	    if (that.dynamicallyGeneratedTypes != null) {
		return false;
	    }
	} else if (dynamicallyGeneratedTypes.size() != that.dynamicallyGeneratedTypes.size()) {
	    return false;
	} else {
	    for (int index = 0; index < dynamicallyGeneratedTypes.size(); index++) {
		if (!Arrays.equals(dynamicallyGeneratedTypes.get(index), that.dynamicallyGeneratedTypes.get(index)) ) {
		    return false;
		}
	    }
	}
	if (qem == null) {
	    if (that.qem != null) {
		return false;
	    }
	} else if (!qem.equals(that.qem)) {
	    return false;
	}
	return true;
    }

}
