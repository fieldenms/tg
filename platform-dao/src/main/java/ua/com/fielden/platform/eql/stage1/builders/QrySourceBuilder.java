package ua.com.fielden.platform.eql.stage1.builders;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_MODELS_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE_ALIAS;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.stage1.elements.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSyntheticType;
import ua.com.fielden.platform.eql.stage1.elements.QueryBasedSource1;
import ua.com.fielden.platform.utils.Pair;

public class QrySourceBuilder extends AbstractTokensBuilder {

    protected QrySourceBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    private boolean isEntityTypeAsSource() {
        return getSize() == 2 && ENTITY_TYPE_AS_QRY_SOURCE == firstCat() && QRY_SOURCE_ALIAS == secondCat();
    }

    private boolean isEntityTypeAsSourceWithoutAlias() {
        return getSize() == 1 && ENTITY_TYPE_AS_QRY_SOURCE == firstCat();
    }

    private boolean isQueryModelAsSource() {
        return getSize() == 2 && QRY_MODELS_AS_QRY_SOURCE == firstCat() && QRY_SOURCE_ALIAS == secondCat();
    }

    private boolean isQueryModelAsSourceWithoutAlias() {
        return getSize() == 1 && QRY_MODELS_AS_QRY_SOURCE == firstCat();
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias() ||
               isQueryModelAsSource() || isQueryModelAsSourceWithoutAlias();
    }

    private Pair<TokenCategory, Object> getResultForEntityTypeAsSource() {
        final Class<AbstractEntity<?>> resultType = (Class) firstValue();
        if (isPersistedEntityType(resultType)) {
            return pair(QRY_SOURCE, new QrySource1BasedOnPersistentType(resultType, (String) secondValue()));
        } else if (isSyntheticEntityType(resultType) || isSyntheticBasedOnPersistentEntityType(resultType)) {
//            final List<QueryModel> readyModels = new ArrayList<QueryModel>();
//            readyModels.addAll(((ModelledEntityMetadata) entityMetadata).getModels());
//            return getResultForEntityModelAsSource(readyModels, (String) secondValue(), resultType);
            return pair(QRY_SOURCE, new QrySource1BasedOnSyntheticType(resultType, (String) secondValue()));
        } else {
            throw new EqlStage1ProcessingException("Not yet.");
        }
    }

    private Pair<TokenCategory, Object> getResultForEntityModelAsSource(final List<QueryModel> readyModels, final String readyAlias, final Class readyResultType) {
        final List<QueryModel> models = readyModels != null ? readyModels : (List<QueryModel>) firstValue();
        final String alias = readyAlias != null ? readyAlias : (String) secondValue();
        final Class resultType = readyResultType != null ? readyResultType : null;
        final List<EntQuery1> queries = new ArrayList<EntQuery1>();
        for (final QueryModel qryModel : models) {
            queries.add(getQueryBuilder().generateEntQueryAsSourceQuery(qryModel, resultType));
        }

        return new Pair<TokenCategory, Object>(QRY_SOURCE, new QueryBasedSource1(alias, queries.toArray(new EntQuery1[] {})));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias()) {
            return getResultForEntityTypeAsSource();
        } else if (isQueryModelAsSource() || isQueryModelAsSourceWithoutAlias()) {
            return getResultForEntityModelAsSource(null, null, null);
        } else {
            throw new RuntimeException("Unable to get result - unrecognised state.");
        }
    }
}