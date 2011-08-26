package ua.com.fielden.platform.entity.query.model.transformation;


public class YieldedQuery implements IYieldedItem {

    private final SqlEntQuery yieldedQuery;
    private final String name;
    private final SqlEntQuery query;


    public YieldedQuery(final SqlEntQuery query, final SqlEntQuery yieldedQuery, final String name) {
	this.yieldedQuery = yieldedQuery;
	this.name = name;
	this.query = query;
    }

    @Override
    public String name() {
	return name;
    }

    private int getYieldPosition() {
	int position = 0;
	for (final String itemName : query.getYields().keySet()) {
	    position  = position + 1;
	    if (name.equals(itemName)) {
		return position;
	    }
	}
	throw new RuntimeException("Unable to determine yield position within query yields");
    }


    @Override
    public String sql() {
	return "(" + yieldedQuery.sql() + ")" + " AS C" + getYieldPosition();
    }
}
