package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.OrderBy1;
import ua.com.fielden.platform.eql.s1.elements.OrderBys1;
import ua.com.fielden.platform.utils.Pair;

public class QryOrderingsBuilder1 extends AbstractTokensBuilder1 {

    protected QryOrderingsBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
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

    public OrderBys1 getModel() {
	if (getChild() != null && getSize() == 0) {
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<OrderBy1> models = new ArrayList<OrderBy1>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    models.add((OrderBy1) pair.getValue());
	}

	return new OrderBys1(models);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
