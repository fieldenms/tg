package ua.com.fielden.platform.entity.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_MODELS_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE_ALIAS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VALUES_AS_QRY_SOURCE;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.NothingBasedSource;
import ua.com.fielden.platform.entity.query.generation.elements.QueryBasedSource;
import ua.com.fielden.platform.entity.query.generation.elements.TypeBasedSource;
import ua.com.fielden.platform.entity.query.metadata.AbstractEntityMetadata;
import ua.com.fielden.platform.entity.query.metadata.ModelledEntityMetadata;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.utils.Pair;

public class QrySourceBuilder extends AbstractTokensBuilder {

    protected QrySourceBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    private boolean isEntityTypeAsSource() {
        return getSize() == 2 && ENTITY_TYPE_AS_QRY_SOURCE.equals(firstCat()) && QRY_SOURCE_ALIAS.equals(secondCat());
    }

    private boolean isEntityTypeAsSourceWithoutAlias() {
        return getSize() == 1 && ENTITY_TYPE_AS_QRY_SOURCE.equals(firstCat());
    }

    private boolean isEntityModelAsSource() {
        return getSize() == 2 && QRY_MODELS_AS_QRY_SOURCE.equals(firstCat()) && QRY_SOURCE_ALIAS.equals(secondCat());
    }

    private boolean isEntityModelAsSourceWithoutAlias() {
        return getSize() == 1 && QRY_MODELS_AS_QRY_SOURCE.equals(firstCat());
    }

    private boolean isNothingAsSourceWithoutAlias() {
        return getSize() == 1 && VALUES_AS_QRY_SOURCE.equals(firstCat());
    }
    
    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return isEntityTypeAsSource() || isEntityModelAsSource() || isEntityModelAsSourceWithoutAlias() || isEntityTypeAsSourceWithoutAlias() || isNothingAsSourceWithoutAlias();
    }

    private Pair<TokenCategory, Object> getResultForEntityTypeAsSource() {
        final Class<AbstractEntity<?>> resultType = (Class) firstValue();
        final AbstractEntityMetadata<?> entityMetadata = getQueryBuilder().getDomainMetadataAnalyser().getEntityMetadata(resultType);
        if (entityMetadata instanceof PersistedEntityMetadata) {
            return pair(QRY_SOURCE, new TypeBasedSource((PersistedEntityMetadata<?>) entityMetadata, (String) secondValue(), getQueryBuilder().getDomainMetadataAnalyser()));
        } else {
            final List<QueryModel<?>> readyModels = new ArrayList<>();
            readyModels.addAll(((ModelledEntityMetadata<?>) entityMetadata).getModels());
            return getResultForEntityModelAsSource(readyModels, (String) secondValue(), resultType);
        }
    }

    private Pair<TokenCategory, Object> getResultForEntityModelAsSource(final List<QueryModel<?>> readyModels, final String readyAlias, final Class<?> readyResultType) {
        final List<QueryModel<?>> models = readyModels != null ? readyModels : (List<QueryModel<?>>) firstValue();
        final String alias = readyAlias != null ? readyAlias : (String) secondValue();
        final Class<?> resultType = readyResultType != null ? readyResultType : null;
        final List<EntQuery> queries = new ArrayList<>();
        for (final QueryModel<?> qryModel : models) {
            queries.add(getQueryBuilder().generateEntQueryAsSourceQuery(qryModel, getParamValues(), resultType != null ? resultType : qryModel.getResultType()));
        }

        return pair(QRY_SOURCE, new QueryBasedSource(alias, getQueryBuilder().getDomainMetadataAnalyser(), queries.toArray(new EntQuery[] {})));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias()) {
            return getResultForEntityTypeAsSource();
        } else if (isEntityModelAsSource() || isEntityModelAsSourceWithoutAlias()) {
            return getResultForEntityModelAsSource(null, null, null);
        } else if (isNothingAsSourceWithoutAlias()) {
            return pair(QRY_SOURCE, new NothingBasedSource(getQueryBuilder().getDomainMetadataAnalyser()));
        } else {
            throw new EqlException("Unable to get result - unrecognised state.");
        }
    }
}