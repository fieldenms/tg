package ua.com.fielden.platform.dao;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T3.t3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

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
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

/**
 * This class should be extended by companion objects to various {@code Open<EntityName>MaserAction} entities that model an action for opening Compound Entity Masters.
 * The main purpose of that would be to support indication for those master menu items that represent one-2-many associations (i.e. embedded centres) with actually some associations present.
 * Such menu items would become highlighted upon opening a compound master.
 * <p>
 * Binding between the menu items and respective logic for determining the existence of the underlying data is achieved by using methods {@code addViewBinding} as part of the companion's constructor.
 * For example, a companion to open master action of entity {@code Category} may have the following constructor:
 * <pre>
 * {@code
 *   public OpenCategoryMasterActionDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
 *       super(filter, coAggregates);
 *       addViewBinding(SYSTEMS, Systema.class, "category");
 *   }
 * }
 * </pre>
 * In the above snippet, line {@code addViewBinding(SYSTEMS, Systema.class, "category");} binds the logic to check existence of entities {@code Systema} for a given {@code Category}.
 * The Web UI configuration for the master would have a corresponding view defined. For example:
 * <pre>
 * {@code
 *         compoundMaster = CompoundMasterBuilder.<Category, OpenCategoryMasterAction>create(injector, builder)
            .forEntity(OpenCategoryMasterAction.class)
            .withProducer(OpenCategoryMasterActionProducer.class)
            // potentially many other menu items...
            // but we're interested in SYSTEMS menu item
            .addMenuItem(CategoryMaster_OpenSystema_MenuItem.class)
                .icon("icons:view-module")
                .shortDesc(SYSTEMS)
                .longDesc("Systems in this category")
                .withView(createSystemaCentre(editSystemaAction))
            .done();
 * }
 * </code>
 * Internally {@code addViewBindings} use on of the overloaded utility functions {@code enhanceEmbededCentreQuery}, which should also be used as part of the Web UI configuration of corresponding embedded centres for implementing {@link IQueryEnhancer}.
 * In case of the already established example, we could have something like this:
 * <pre>
 * {@code
    private static class CategoryMaster_SystemaCentre_QueryEnhancer implements IQueryEnhancer<Systema> {
        @Override
        public ICompleted<Systema> enhanceQuery(final IWhere0<Systema> where, final Optional<CentreContext<Systema, ?>> context) {
            return enhanceEmbededCentreQuery(where, "category", context.get().getMasterEntity().getKey());
        }
    }
 * }
 * </pre>
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractOpenCompoundMasterDao<T extends AbstractFunctionalEntityToOpenCompoundMaster<?>> extends CommonEntityDao<T>{
    private static final String THIS = "this";

    private final IEntityAggregatesOperations coAggregates;
    private final List<T3<String, Class<? extends AbstractEntity<?>>, BiFunction<IWhere0<? extends AbstractEntity<?>>, Object, ICompleted<? extends AbstractEntity<?>>>>> compoundMasterConfig = new ArrayList<>();
    private final List<T3<String, Class<? extends AbstractEntity<?>>, Map<String, Function<Object, Object>>>> parameters = new ArrayList<>();
    private final Map<String, Function<Object, Object>> additionalParameters = new HashMap<>();

    @Inject
    protected AbstractOpenCompoundMasterDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
        super(filter);
        this.coAggregates = coAggregates;
    }

    @Override
    public T save(final T entity) {
        // if entity is a brand new not yet persisted instance then there can be no related one-2-many associations with it
        entity.setCalculated(true);
        if (!entity.getKey().isPersisted()) {
            entity.setEntityPresence(emptyPresence());
            return entity;
        }
        // otherwise let's construct an EQL statement to check existence of one-2-many associations with entity
        ISubsequentCompletedAndYielded<AbstractEntity<?>> queryPart = select().yield().val(1).as("#common_one#");
        for(final T3<String, Class<? extends AbstractEntity<?>>, BiFunction<IWhere0<? extends AbstractEntity<?>>, Object, ICompleted<? extends AbstractEntity<?>>>> pair: compoundMasterConfig) {
            queryPart = queryPart.yield().caseWhen().exists(pair._3.apply(select(pair._2).where(), entity.getKey()).model())
                    .then().val(1).otherwise().val(0).endAsInt().as(pair._1);
        }
        for (final T3<String, Class<? extends AbstractEntity<?>>, Map<String, Function<Object, Object>>> pair : parameters) {
            queryPart = queryPart.yield().caseWhen().exists(select(pair._2).model())
                    .then().val(1).otherwise().val(0).endAsInt().as(pair._1);
        }
        final Map<String, Object> queryParams = new HashMap<>();
        parameters.stream().map(paramMap -> enhanceParametersWithCustomValue(paramMap._3, entity.getKey()))
                .flatMap(m -> m.entrySet().stream())
                .forEach(entry -> queryParams.put(entry.getKey(), entry.getValue()));
        additionalParameters.entrySet().stream().forEach(entry -> queryParams.put(entry.getKey(), entry.getValue().apply(entity.getKey())));
        final EntityAggregates existEntity = coAggregates.getEntity(from(queryPart.modelAsAggregate()).with(queryParams).model());
        final Map<String, Integer> newPresence = new HashMap<>();
        compoundMasterConfig.stream().forEach(pair -> newPresence.put(pair._1, existEntity.get(pair._1)));
        parameters.stream().forEach(pair -> newPresence.put(pair._1, existEntity.get(pair._1)));
        entity.setEntityPresence(newPresence);
        return entity;
    }

    private Map<String, Integer> emptyPresence() {
        final Map<String, Integer> newPresence = new HashMap<>();
        compoundMasterConfig.stream().forEach(pair -> newPresence.put(pair._1, 0));
        parameters.stream().forEach(pair -> newPresence.put(pair._1, 0));
        return newPresence;
    }

    @SafeVarargs
    public final void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final String propertyName, final T2<String, String>... paramConfig) {
        addViewBinding(binding, type, (where, value) -> enhanceEmbededCentreQuery(where, propertyName, value), paramConfig);
    }

    @SafeVarargs
    public final void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final String propertyName, final String relativeValueProperty, final T2<String, String>... paramConfig) {
        addViewBinding(binding, type, (where, value) -> enhanceEmbededCentreQuery(where, propertyName, value, relativeValueProperty), paramConfig);
    }

    @SafeVarargs
    public final void addViewBinding(
            final String binding,
            final Class<? extends AbstractEntity<?>> type,
            final BiFunction<IWhere0<? extends AbstractEntity<?>>, Object, ICompleted<? extends AbstractEntity<?>>> queryEnhnacer,
            final T2<String, String>... paramConfig) {
        compoundMasterConfig.add(t3(binding, type, queryEnhnacer));
        additionalParameters.putAll(Arrays.stream(paramConfig).collect(toMap(entry -> entry._1, entry -> value -> getValue(value, entry._2))));
    }

    @SafeVarargs
    public final void addViewBinding(final String binding, final Class<? extends AbstractEntity<?>> type, final T2<String, String>... parameters) {
        this.parameters.add(t3(binding, type, Arrays.stream(parameters).collect(toMap(parameter -> parameter._1, parameter -> value -> getValue(value, parameter._2)))));
    }

    public static <K extends AbstractEntity<?>> ICompleted<K> enhanceEmbededCentreQuery(final IWhere0<K> where, final String prop, final Object value, final String relativeValuePropertyName) {
        return where.prop(prop).eq().val(((AbstractEntity<?>)value).get(relativeValuePropertyName));
    }

    public static <K extends AbstractEntity<?>> ICompleted<K> enhanceEmbededCentreQuery(final IWhere0<K> where, final String prop, final Object value) {
        return where.prop(prop).eq().val(value);
    }

    public static <K extends AbstractEntity<?>> Map<String, Object> enhanceParameters(final Map<String, String> parameters, final K entity) {
        final Map<String, Function<Object, Object>> functionMap = parameters.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> value -> getValue(value, entry.getValue())));
        return enhanceParametersWithCustomValue(functionMap, entity);
    }

    public static <K extends AbstractEntity<?>> Map<String, Object> enhanceParametersWithCustomValue(final Map<String, Function<Object, Object>> parameters, final K entity) {
        final Map<String, Object> queryParams = new HashMap<>();
        parameters.entrySet().stream().forEach(entry -> {
            queryParams.put(entry.getKey(), entry.getValue().apply(entity));
        });
        return queryParams;
    }

    private static Object getValue(final Object value, final String propertyName) {
        return THIS.equals(propertyName) ? value : ((AbstractEntity<?>)value).get(propertyName);
    }
}
