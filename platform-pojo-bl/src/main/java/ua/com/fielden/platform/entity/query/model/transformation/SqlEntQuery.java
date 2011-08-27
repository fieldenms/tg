package ua.com.fielden.platform.entity.query.model.transformation;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class SqlEntQuery implements IQuerySource {

    private final SortedMap<String, IYieldedItem> yields = new TreeMap<String, IYieldedItem>();
    private final IQuerySource from;
    private IQuerySource predecessor;
    private SqlEntQuery master;

    public SqlEntQuery(final IQuerySource from) {
	this.from = from;
    }

//    @Override
//    public IQuerySourceItem getSourceItem(final String dotNotatedName) {
//	return yields.get(dotNotatedName);
//    }

    @Override
    public String sqlAlias() {
	int sourceIndex = 0;
	IQuerySource currSource = this;
	while (currSource != null) {
	    sourceIndex = sourceIndex + 1;
	    currSource = currSource.getPredecessor();
	}
	int masterIndex = 0;
	SqlEntQuery currMaster = this.master;
	while (currMaster != null) {
	    masterIndex = masterIndex + 1;
	    currMaster = currMaster.master;
	}

	return "Q" + sourceIndex + (masterIndex == 0 ? "" : ("L" + masterIndex));
    }

    @Override
    public IQuerySource getPredecessor() {
	return predecessor;
    }

    public String sqlBody() {
	return "(" + querySql() + ")";
    }

    public String querySql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("SELECT ");
	for (final Iterator<IYieldedItem> iterator = yields.values().iterator(); iterator.hasNext();) {
	    final IYieldedItem yield = iterator.next();
	    sb.append(yield.getYieldStmt());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(" FROM ");
	sb.append(from.sqlBody() + " AS " + from.sqlAlias());

	return sb.toString();
    }

    public SortedMap<String, IYieldedItem> getYields() {
        return yields;
    }
}
