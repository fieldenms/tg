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
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.stage1.elements.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentTypeWithCalcProps;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSyntheticType;
import ua.com.fielden.platform.utils.EntityUtils;
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

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias() ||
               isSubqueriesAsSource() || isSubqueriesAsSourceWithoutAlias();
    }

    private Pair<TokenCategory, Object> buildResultForQrySourceBasedOnEntityType() {
        final Class<AbstractEntity<?>> resultType = (Class<AbstractEntity<?>>) firstValue();
        if (isPersistedEntityType(resultType)) {
            if (EntityUtils.hasCalcProps(resultType)) {
                return pair(QRY_SOURCE, new QrySource1BasedOnPersistentTypeWithCalcProps(resultType, (String) secondValue()));    
            } else {
                return pair(QRY_SOURCE, new QrySource1BasedOnPersistentType(resultType, (String) secondValue()));    
            }
        } else if (isSyntheticEntityType(resultType) || isSyntheticBasedOnPersistentEntityType(resultType)) {
            return pair(QRY_SOURCE, new QrySource1BasedOnSyntheticType(resultType, (String) secondValue()));
        } else {
            throw new EqlStage1ProcessingException("Not yet.");
        }
    }

    private Pair<TokenCategory, Object> buildResultForQrySourceBasedOnSubqueries() {
        final List<EntQuery1> queries = new ArrayList<>();
        final String alias = secondValue();
        final List<QueryModel<AbstractEntity<?>>> models = firstValue();
        for (final QueryModel<AbstractEntity<?>> qryModel : models) {
            queries.add(getQueryBuilder().generateEntQueryAsSourceQuery(qryModel, /*resultType = */ Optional.empty()));
        }

        return pair(QRY_SOURCE, new QrySource1BasedOnSubqueries(alias, queries));
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isEntityTypeAsSource() || isEntityTypeAsSourceWithoutAlias()) {
            return buildResultForQrySourceBasedOnEntityType();
        } else if (isSubqueriesAsSource() || isSubqueriesAsSourceWithoutAlias()) {
            return buildResultForQrySourceBasedOnSubqueries();
        } else {
            throw new RuntimeException("Unable to get result - unrecognised state.");
        }
    }
}