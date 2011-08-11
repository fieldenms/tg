package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.tokens.QueryTokens;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QueryBuilder {
    private ConditionsBuilder where;
    private final QrySourcesBuilder from = new QrySourcesBuilder(null);
    private final QryYieldsBuilder select = new QryYieldsBuilder(null);
    private final QryGroupsBuilder groupBy = new QryGroupsBuilder(null);

    public QueryBuilder(final QueryModel qryModel) {
	super();
	ITokensBuilder active = null;

	for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
	    if (!TokenCategory.QUERY_TOKEN.equals(pair.getKey())) {
		if (active != null) {
		    active.add(pair.getKey(), pair.getValue());
		}
	    } else {
		switch ((QueryTokens) pair.getValue()) {
		case WHERE: //eats token
		    where = new ConditionsBuilder(null);
		    active = where;
		    break;
		case FROM: //eats token
		    active = from;
		    break;
		case YIELD: //eats token
		    active = select;
		    select.setChild(new YieldBuilder(select));
		    break;
		case GROUP_BY: //eats token
		    active = groupBy;
		    groupBy.setChild(new GroupBuilder(groupBy));
		    break;
		default:
		    break;
		}

	    }
	}
    }

    public QrySourcesBuilder getFrom() {
	return from;
    }

    public ConditionsBuilder getWhere() {
	return where;
    }

    public QryYieldsBuilder getSelect() {
	return select;
    }

    public EntQuery getQry() {
	return new EntQuery((EntQuerySourcesModel) from.getResult().getValue(), where != null ? (ConditionsModel) where.getResult().getValue() : null, (YieldsModel) select.getResult().getValue(), (GroupsModel) groupBy.getResult().getValue());
    }
}