package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.types.tuples.T3;

public class CommonOpenCompoundMasterDao<T extends AbstractFunctionalEntityToOpenCompoundMaster<?>> extends CommonEntityDao<T> implements IOpenCompoundMasterAction<T> {

    private final IEntityAggregatesOperations coAggregates;
    private final List<T3<String, Class<? extends AbstractEntity<?>>, String>> compoundMasterConfig = new ArrayList<>();

    @Inject
    protected CommonOpenCompoundMasterDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
        super(filter);
        this.coAggregates = coAggregates;
    }

    @Override
    public T save(final T entity) {
        if (!entity.isCalculated()) {
            ISubsequentCompletedAndYielded<AbstractEntity<?>> queryPart = select().yield().val(1).as("#common_one#");
            for(final T3<String, Class<? extends AbstractEntity<?>>, String> pair: compoundMasterConfig) {
                queryPart = queryPart.yield().caseWhen().exists(enhnaceEmbededCentreQuery(select(pair._2).where(), pair._3, entity.getKey()).model())
                        .then().val(1).otherwise().val(0).endAsInt().as(pair._1);
            }
            final EntityAggregates existEntity = coAggregates.getEntity(from(queryPart.modelAsAggregate()).model());
            final Map<String, Integer> newPresence = new HashMap<>();
            compoundMasterConfig.stream().forEach(pair -> {
                newPresence.put(pair._1, existEntity.get(pair._1));
            });
            entity.setEntityPresence(newPresence);
            entity.setCalculated(true);
        }
        return super.save(entity);
    }

    public void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final String propertyName) {
        compoundMasterConfig.add(T3.t3(binding, type, propertyName));
    }
}
