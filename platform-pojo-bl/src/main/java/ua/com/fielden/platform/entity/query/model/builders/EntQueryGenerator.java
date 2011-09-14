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

public class EntQueryGenerator {
    private final DbVersion dbVersion;

    public EntQueryGenerator(final DbVersion dbVersion) {
	this.dbVersion = dbVersion;
    }

    public EntQuery generateEntQuery(final QueryModel qryModel) {
	ConditionsBuilder where = null;
	final QrySourcesBuilder from = new QrySourcesBuilder(null, dbVersion);
	final QryYieldsBuilder select = new QryYieldsBuilder(null, dbVersion);
	final QryGroupsBuilder groupBy = new QryGroupsBuilder(null, dbVersion);

	ITokensBuilder active = null;

	for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
	    if (!TokenCategory.QUERY_TOKEN.equals(pair.getKey())) {
		if (active != null) {
		    active.add(pair.getKey(), pair.getValue());
		}
	    } else {
		switch ((QueryTokens) pair.getValue()) {
		case WHERE: //eats token
		    where = new ConditionsBuilder(null, dbVersion);
		    active = where;
		    break;
		case FROM: //eats token
		    active = from;
		    break;
		case YIELD: //eats token
		    active = select;
		    select.setChild(new YieldBuilder(select, dbVersion));
		    break;
		case GROUP_BY: //eats token
		    active = groupBy;
		    groupBy.setChild(new GroupBuilder(groupBy, dbVersion));
		    break;
		default:
		    break;
		}

	    }
	}

	return new EntQuery((EntQuerySourcesModel) from.getResult().getValue(), where != null ? (ConditionsModel) where.getResult().getValue() : null, (YieldsModel) select.getResult().getValue(), (GroupsModel) groupBy.getResult().getValue());
    }
}