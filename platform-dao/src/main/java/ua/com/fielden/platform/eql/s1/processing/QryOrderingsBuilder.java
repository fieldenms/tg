package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.OrderBy;
import ua.com.fielden.platform.eql.s1.elements.OrderBys;
import ua.com.fielden.platform.utils.Pair;

public class QryOrderingsBuilder extends AbstractTokensBuilder {

    protected QryOrderingsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

//    @Override
//    public void add(final TokenCategory cat, final Object value) {
//	switch (cat) {
//	case SORT_ORDER: //eats token
//	    //getChild().get
//	    super.add(cat, value);
//	    finaliseChild();
//	    //setChild(new CompoundQrySourceBuilder(this, getQueryBuilder(), getParamValues(), cat, value));
//	    //new OrderByBuilder(orderBy, this, paramValues)
//	    //break;
//	default:
//	    super.add(cat, value);
//	    break;
//	}
//    }

    @Override
    public boolean isClosing() {
	return false;
    }

    public OrderBys getModel() {
	if (getChild() != null && getSize() == 0) {
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<OrderBy> models = new ArrayList<OrderBy>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    models.add((OrderBy) pair.getValue());
	}

	return new OrderBys(models);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
