package ua.com.fielden.platform.eql.s1.processing;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_MODELS_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE_ALIAS;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.AbstractEntityMetadata;
import ua.com.fielden.platform.dao.ModelledEntityMetadata;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSource1;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource1;
import ua.com.fielden.platform.utils.Pair;

public class QrySourceBuilder1 extends AbstractTokensBuilder1 {

    protected QrySourceBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
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
        final AbstractEntityMetadata entityMetadata = getQueryBuilder().getDomainMetadataAnalyser().getEntityMetadata(resultType);
        if (entityMetadata instanceof PersistedEntityMetadata) {
            return new Pair<TokenCategory, Object>(QRY_SOURCE, new TypeBasedSource1(resultType, (String) secondValue()));
        } else {
            final List<QueryModel> readyModels = new ArrayList<QueryModel>();
            readyModels.addAll(((ModelledEntityMetadata) entityMetadata).getModels());
            return getResultForEntityModelAsSource(readyModels, (String) secondValue(), resultType);
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