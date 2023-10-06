package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.eql.stage1.etc.OrderBys1.emptyOrderBys;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.etc.OrderBy1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.utils.Pair;

public class QryOrderingsBuilder extends AbstractTokensBuilder {

    protected QryOrderingsBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
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
        if (getChild() != null && getTokens().isEmpty()) {
            finaliseChild();
            //throw new RuntimeException("Unable to produce result - unfinished model state!");
        }
        
        if (getTokens().isEmpty()) {
            return emptyOrderBys;
        }
        
        final List<OrderBy1> models = new ArrayList<>();
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
