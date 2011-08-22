package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QrySourceBuilder extends AbstractTokensBuilder {

    protected QrySourceBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
    }

    private boolean isEntityTypeAsSourceTest() {
	return getSize() == 2 && TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE.equals(firstCat()) && TokenCategory.QRY_SOURCE_ALIAS.equals(secondCat());
    }

    private boolean isEntityModelAsSourceTest() {
	return getSize() == 2 && TokenCategory.QRY_MODEL_AS_QRY_SOURCE.equals(firstCat()) && TokenCategory.QRY_SOURCE_ALIAS.equals(secondCat());
    }

    @Override
    public boolean isClosing() {
	return isEntityTypeAsSourceTest() || isEntityModelAsSourceTest();
    }

    private Pair<TokenCategory, Object> getResultForEntityTypeAsSource() {
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_SOURCE, new EntQuerySourceAsEntity((Class) firstValue(), (String) secondValue()));
    }

    private Pair<TokenCategory, Object> getResultForEntityModelAsSource() {
	final List<QueryModel> models = (List<QueryModel>) firstValue();
	final List<EntQuery> queries = new ArrayList<EntQuery>();
	for (final QueryModel qryModel : models) {
	    queries.add(getQueryBuilder().getQry(qryModel));
	}

	return new Pair<TokenCategory, Object>(TokenCategory.QRY_SOURCE, new EntQuerySourceAsModel((String) secondValue(), queries.toArray(new EntQuery[]{})));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (isEntityTypeAsSourceTest()) {
	    return getResultForEntityTypeAsSource();
	} else {
	    return getResultForEntityModelAsSource();
	}
    }
}
