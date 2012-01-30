package ua.com.fielden.platform.entity.query.model.elements;

import java.util.SortedMap;


public class YieldsModel {
    private final SortedMap<String, YieldModel> yields;

    @Override
    public String toString() {
        return yields.toString();
    }

    public void assignSqlAliases() {
	int yieldIndex = 0;
	for (final YieldModel yield : yields.values()) {
	    yieldIndex = yieldIndex + 1;
	    yield.assignSqlAlias("C" + yieldIndex);
	}
    }

    public YieldsModel(final SortedMap<String, YieldModel> yields) {
	this.yields = yields;
    }

    public SortedMap<String, YieldModel> getYields() {
        return yields;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((yields == null) ? 0 : yields.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof YieldsModel)) {
	    return false;
	}
	final YieldsModel other = (YieldsModel) obj;
	if (yields == null) {
	    if (other.yields != null) {
		return false;
	    }
	} else if (!yields.equals(other.yields)) {
	    return false;
	}
	return true;
    }
}