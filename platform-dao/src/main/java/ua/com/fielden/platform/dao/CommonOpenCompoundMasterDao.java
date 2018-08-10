package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T3.t3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

public class CommonOpenCompoundMasterDao<T extends AbstractFunctionalEntityToOpenCompoundMaster<?>> extends CommonEntityDao<T>{

    private final IEntityAggregatesOperations coAggregates;
    private final List<T3<String, Class<? extends AbstractEntity<?>>, String>> compoundMasterConfig = new ArrayList<>();
    private final List<T3<String, Class<? extends AbstractEntity<?>>, Map<String, String>>> parameters = new ArrayList<>();

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
            for (final T3<String, Class<? extends AbstractEntity<?>>, Map<String, String>> pair : parameters) {
                queryPart = queryPart.yield().caseWhen().exists(select(pair._2).model())
                        .then().val(1).otherwise().val(0).endAsInt().as(pair._1);
            }
            final Map<String, Object> queryParams = new HashMap<>();
            parameters.stream().map(paramMap -> enhanceParameters(paramMap._3, entity.getKey()))
                    .flatMap(m -> m.entrySet().stream())
                    .forEach(entry -> queryParams.put(entry.getKey(), entry.getValue()));
            final EntityAggregates existEntity = coAggregates.getEntity(from(queryPart.modelAsAggregate()).with(queryParams).model());
            final Map<String, Integer> newPresence = new HashMap<>();
            compoundMasterConfig.stream().forEach(pair -> {
                newPresence.put(pair._1, existEntity.get(pair._1));
            });
            parameters.stream().forEach(pair -> {
                newPresence.put(pair._1, existEntity.get(pair._1));
            });
            entity.setEntityPresence(newPresence);
            entity.setCalculated(true);
        }
        return super.save(entity);
    }

    public void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final String propertyName) {
        compoundMasterConfig.add(t3(binding, type, propertyName));
    }

    @SuppressWarnings("unchecked")
    public void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final T2<String, String>... parameters) {
        this.parameters.add(t3(binding, type, Arrays.stream(parameters).collect(Collectors.toMap(parameter -> parameter._1, parameter -> parameter._2))));
    }

    public static <K extends AbstractEntity<?>> ICompleted<K> enhnaceEmbededCentreQuery(final IWhere0<K> where, final String prop, final Object value) {
        return where.prop(prop).eq().val(value);
    }

    public static <K extends AbstractEntity<?>> Map<String, Object> enhanceParameters(final Map<String, String> parameters, final K entity) {
        final Map<String, Object> queryParams = new HashMap<>();
        parameters.entrySet().stream().forEach(entry -> {
            queryParams.put(entry.getKey(), "this".equals(entry.getValue()) ? entity : entity.get(entry.getValue()));
        });
        return queryParams;
    }
}
