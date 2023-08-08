package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_MODELS_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE_ALIAS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VALUES_AS_QRY_SOURCE;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.EntityTypeInfo;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.JoinLeaf1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnSubqueries;
import ua.com.fielden.platform.utils.Pair;

/**
 * This builder is responsible for converting EQL tokens (effectively an AST of sorts) starting with the part that represents FROM or JOIN, and completing at the level of ALIAS (which could be absent).
 * 
 * @author TG Team
 *
 */
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

    private boolean isSubqueriesAsSource() {
        return getSize() == 2 && QRY_MODELS_AS_QRY_SOURCE == firstCat() && QRY_SOURCE_ALIAS == secondCat();
    }

    private boolean isSubqueriesAsSourceWithoutAlias() {
        return getSize() == 1 && QRY_MODELS_AS_QRY_SOURCE == firstCat();
    }

    private boolean isNothingAsSourceWithoutAlias() {
        return getSize() == 1 && VALUES_AS_QRY_SOURCE.equals(firstCat());
    }
    
    @Override
    public boolean isClosing() {
        return false;
    }

    private Pair<TokenCategory, Object> buildResultForQrySourceBasedOnEntityType() {
        final Class<AbstractEntity<?>> resultType = (Class<AbstractEntity<?>>) firstValue();
        if (isPersistedEntityType(resultType)) {
            return pair(QRY_SOURCE, new JoinLeaf1(new Source1BasedOnPersistentType(resultType, (String) secondValue(), getQueryBuilder().nextSourceId())));    
        } else if (isSyntheticEntityType(resultType) || isSyntheticBasedOnPersistentEntityType(resultType) || isUnionEntityType(resultType)) {
            return pair(QRY_SOURCE, buildQrySourceBasedOnSyntheticEntityType(resultType, (String) secondValue()));
        } else {
            throw new EqlStage1ProcessingException("Not yet.");
        }
    }
    
    private <T extends AbstractEntity<?>> JoinLeaf1 buildQrySourceBasedOnSyntheticEntityType(final Class<T> resultType, final String alias) {
        final EntityTypeInfo<?> parentInfo = getEntityTypeInfo(resultType);
        final List<SourceQuery1> queries = new ArrayList<>();
        for (final QueryModel<?> qryModel : parentInfo.entityModels) {
            queries.add(getQueryBuilder().generateAsUncorrelatedSourceQuery(qryModel));
        }
        return new JoinLeaf1(new Source1BasedOnSubqueries(queries, alias, getQueryBuilder().nextSourceId(), resultType));
    }
    
    private Pair<TokenCategory, Object> buildResultForQrySourceBasedOnSubqueries() {
        final List<SourceQuery1> queries = new ArrayList<>();
        final String alias = secondValue();
        final List<QueryModel<AbstractEntity<?>>> models = firstValue();
        for (final QueryModel<AbstractEntity<?>> qryModel : models) {
            queries.add(getQueryBuilder().generateAsCorrelatedSourceQuery(qryModel));
        }

        return pair(QRY_SOURCE, new JoinLeaf1(new Source1BasedOnSubqueries(queries, alias, getQueryBuilder().nextSourceId(), null)));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias()) {
            return buildResultForQrySourceBasedOnEntityType();
        } else if (isSubqueriesAsSource() || isSubqueriesAsSourceWithoutAlias()) {
            return buildResultForQrySourceBasedOnSubqueries();
        } else if (isNothingAsSourceWithoutAlias()) {
            return pair(QRY_SOURCE, null);
        } else {
            throw new RuntimeException("Unable to get result - unrecognised state.");
        }
    }
}