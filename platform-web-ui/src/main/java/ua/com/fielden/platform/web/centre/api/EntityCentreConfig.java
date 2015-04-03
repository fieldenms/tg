package ua.com.fielden.platform.web.centre.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IMultiValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.ISingleValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.layout.FlexLayout;

/**
 *
 * Represents a final structure of an entity centre as produced by means of using Entity Centre DSL.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfig<T extends AbstractEntity<?>> {
    /////////////////////////////////////////////
    ///////////// TOP LEVEL ACTIONS /////////////
    /////////////////////////////////////////////


    /////////////////////////////////////////////
    ////////////// SELECTION CRIT ///////////////
    /////////////////////////////////////////////

    /**
     * A list of properties that have been added to selection criteria in the added sequential order.
     * <p>
     * It is not important whether a property was added as multi-valued, single-valued or range criterion simply because
     * an appropriate selection criteria kind gets determined automatically from property declaration at the entity type level.
     * <p>
     * The part of Entity Centre DSL that provides developer with ability to pick the kind (i.e. <code>multi()</code>, <code>single()</code> or <code>range()</code>)is there only to facilitate definition fluency and readability.
     */
    private final List<String> selectionCriteria = new ArrayList<>();

    /**
     * Default value assigner for various kind and types of selection criteria.
     */
    private final Map<String, Class<? extends IMultiValueAssigner<MultiCritStringValueMnemonic, T>>> defaultMultiValueAssignersForEntityAndStringSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IMultiValueAssigner<MultiCritBooleanValueMnemonic, T>>> defaultMultiValueAssignersForBooleanSelectionCriteria = new HashMap<>();

    private final Map<String, Class<? extends IRangeValueAssigner<RangeCritDateValueMnemonic, T>>> defaultRangeValueAssignersForDateSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>> defaultRangeValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>> defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>> defaultSingleValueAssignersForEntitySelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<String>, T>>> defaultSingleValueAssignersForStringSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>> defaultSingleValueAssignersForBooleanSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>> defaultSingleValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>> defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends ISingleValueAssigner<SingleCritDateValueMnemonic, T>>> defaultSingleValueAssignersForDateSelectionCriteria = new HashMap<>();

    /**
     * Default values. At the DSL level default values and assigners are mutually exclusive.
     */
    private final Map<String, MultiCritStringValueMnemonic> defaultMultiValuesForEntityAndStringSelectionCriteria = new HashMap<>();
    private final Map<String, MultiCritBooleanValueMnemonic> defaultMultiValuesForBooleanSelectionCriteria = new HashMap<>();

    private final Map<String, RangeCritDateValueMnemonic> defaultRangeValuesForDateSelectionCriteria = new HashMap<>();
    private final Map<String, RangeCritOtherValueMnemonic<Integer>> defaultRangeValuesForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, RangeCritOtherValueMnemonic<BigDecimal>> defaultRangeValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    private final Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> defaultSingleValuesForEntitySelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<String>> defaultSingleValuesForStringSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<Boolean>> defaultSingleValuesForBooleanSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<Integer>> defaultSingleValuesForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<BigDecimal>> defaultSingleValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritDateValueMnemonic> defaultSingleValuesForDateSelectionCriteria = new HashMap<>();


    /**
     * A map between selection criteria properties and their custom value matchers.
     * If a matcher for some criterion is not provided then a default instance of type
     * {@link FallbackValueMatcherWithCentreContext} should be used.
     */
    private final Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>> valueMatchersForSelectionCriteria = new HashMap<>();

    /**
     * Represents the layout settings for selection criteria.
     * <p>
     * TODO Please fill in this object using the following syntax:
     * <p>
     * TODO layout.whenMedia(device, orientation).set(flexString);
     */
    private final FlexLayout selectionCriteriaLayout = new FlexLayout();


    /////////////////////////////////////////////
    ////////////////// RESULT SET ///////////////
    /////////////////////////////////////////////

    /**
     * Contains action configurations for actions that are associated with individual properties of retrieved entities, which are represented in the result set. This map can be
     * empty if there is no need to provide custom actions specific for represented in the result set properties. In this case, the default actions would still get associated with
     * all not listed in this map, but added to the result set properties. In order to skip the creation even of default actions, <code>no action</code> configuration needs to be
     * provided for a property.
     * <p>
     * Custom properties that are added as instances of {@link PropDef} are represented in this map by their titles. This means that titles should be unique, which is only a
     * natural requirement and is not a restrictive constraint.
     */
    private final Map<String, EntityActionConfig> resultSetPropActions = new HashMap<>();

    /**
     * A primary entity action configuration that is associated with every retrieved and present in the result set entity. It can be <code>null</code> if no primary entity action
     * is needed.
     */
    private final EntityActionConfig primaryEntityAction = null;

    /**
     * A list of secondary action configurations that are associated with every retrieved and present in the result set entity. It can be empty if no secondary action are
     * necessary.
     */
    private final List<EntityActionConfig> secondaryEntityActions = new ArrayList<>();


    ////////////////////////////////////////////////
    ///////// QUERY ENHANCER AND FETCH /////////////
    ////////////////////////////////////////////////

    private final Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig = null;
    private final IFetchProvider<T> fetchProvider = null;

    ///////////////////////////////////////////
    /////////////// GETTERS ///////////////////
    ///////////////////////////////////////////
    /**
     * Provides access to the layout settings of selection criteria.
     * @return
     */
    public FlexLayout getSelectionCriteriaLayout() {
        return selectionCriteriaLayout;
    }

    public Optional<Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>>> getQueryEnhancerConfig() {
        return Optional.ofNullable(queryEnhancerConfig);
    }

    public Optional<IFetchProvider<T>> getFetchProvider() {
        return Optional.ofNullable(fetchProvider);
    }

    public Optional<EntityActionConfig> getPrimaryEntityAction() {
        return Optional.ofNullable(primaryEntityAction);
    }

    public Optional<List<EntityActionConfig>> getSecondaryEntityActions() {
        if (secondaryEntityActions == null || secondaryEntityActions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(secondaryEntityActions);
    }

    public Optional<Map<String, EntityActionConfig>> getResultSetPropActions() {
        if (resultSetPropActions == null || resultSetPropActions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultSetPropActions);
    }

    public Optional<Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>>> getValueMatchersForSelectionCriteria() {
        if (valueMatchersForSelectionCriteria == null || valueMatchersForSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(valueMatchersForSelectionCriteria);
    }

    public Optional<List<String>> getSelectionCriteria() {
        if (selectionCriteria == null || selectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(selectionCriteria);
    }

    public Optional<Map<String, Class<? extends IMultiValueAssigner<MultiCritStringValueMnemonic, T>>>> getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria() {
        if (defaultMultiValueAssignersForEntityAndStringSelectionCriteria == null || defaultMultiValueAssignersForEntityAndStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(defaultMultiValueAssignersForEntityAndStringSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends IRangeValueAssigner<RangeCritDateValueMnemonic, T>>>> getDefaultRangeValueAssignersForDateSelectionCriteria() {
        if (defaultRangeValueAssignersForDateSelectionCriteria == null || defaultRangeValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValueAssignersForDateSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>>> getDefaultRangeValueAssignersForIntegerSelectionCriteria() {
        if (defaultRangeValueAssignersForIntegerSelectionCriteria == null || defaultRangeValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValueAssignersForIntegerSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria == null || defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>>> getDefaultSingleValueAssignersForEntitySelectionCriteria() {
        if (defaultSingleValueAssignersForEntitySelectionCriteria == null || defaultSingleValueAssignersForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForEntitySelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<String>, T>>>> getDefaultSingleValueAssignersForStringSelectionCriteria() {
        if (defaultSingleValueAssignersForStringSelectionCriteria == null || defaultSingleValueAssignersForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForStringSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>>> getDefaultSingleValueAssignersForBooleanSelectionCriteria() {
        if (defaultSingleValueAssignersForBooleanSelectionCriteria == null || defaultSingleValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForBooleanSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>>> getDefaultSingleValueAssignersForIntegerSelectionCriteria() {
        if (defaultSingleValueAssignersForIntegerSelectionCriteria == null || defaultSingleValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForIntegerSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria == null || defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria);
    }

    public Optional<Map<String, Class<? extends ISingleValueAssigner<SingleCritDateValueMnemonic, T>>>> getDefaultSingleValueAssignersForDateSelectionCriteria() {
        if (defaultSingleValueAssignersForDateSelectionCriteria == null || defaultSingleValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValueAssignersForDateSelectionCriteria);
    }

    public Optional<Map<String, Class<? extends IMultiValueAssigner<MultiCritBooleanValueMnemonic, T>>>> getDefaultMultiValueAssignersForBooleanSelectionCriteria() {
        if (defaultMultiValueAssignersForBooleanSelectionCriteria == null || defaultMultiValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultMultiValueAssignersForBooleanSelectionCriteria);
    }

    public Optional<Map<String, MultiCritStringValueMnemonic>> getDefaultMultiValuesForEntityAndStringSelectionCriteria() {
        if (defaultMultiValuesForEntityAndStringSelectionCriteria == null || defaultMultiValuesForEntityAndStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultMultiValuesForEntityAndStringSelectionCriteria);
    }

    public Optional<Map<String, MultiCritBooleanValueMnemonic>> getDefaultMultiValuesForBooleanSelectionCriteria() {
        if (defaultMultiValuesForBooleanSelectionCriteria == null || defaultMultiValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultMultiValuesForBooleanSelectionCriteria);
    }

    public Optional<Map<String, RangeCritDateValueMnemonic>> getDefaultRangeValuesForDateSelectionCriteria() {
        if (defaultRangeValuesForDateSelectionCriteria == null || defaultRangeValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValuesForDateSelectionCriteria);
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<Integer>>> getDefaultRangeValuesForIntegerSelectionCriteria() {
        if (defaultRangeValuesForIntegerSelectionCriteria == null || defaultRangeValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValuesForIntegerSelectionCriteria);
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<BigDecimal>>> getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValuesForBigDecimalAndMoneySelectionCriteria == null || defaultRangeValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultRangeValuesForBigDecimalAndMoneySelectionCriteria);
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>> getDefaultSingleValuesForEntitySelectionCriteria() {
        if (defaultSingleValuesForEntitySelectionCriteria == null || defaultSingleValuesForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForEntitySelectionCriteria);
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<String>>> getDefaultSingleValuesForStringSelectionCriteria() {
        if (defaultSingleValuesForStringSelectionCriteria == null || defaultSingleValuesForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForStringSelectionCriteria);
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Boolean>>> getDefaultSingleValuesForBooleanSelectionCriteria() {
        if (defaultSingleValuesForBooleanSelectionCriteria == null || defaultSingleValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForBooleanSelectionCriteria);
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Integer>>> getDefaultSingleValuesForIntegerSelectionCriteria() {
        if (defaultSingleValuesForIntegerSelectionCriteria == null || defaultSingleValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForIntegerSelectionCriteria);
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<BigDecimal>>> getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValuesForBigDecimalAndMoneySelectionCriteria == null || defaultSingleValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForBigDecimalAndMoneySelectionCriteria);
    }

    public Optional<Map<String, SingleCritDateValueMnemonic>> getDefaultSingleValuesForDateSelectionCriteria() {
        if (defaultSingleValuesForDateSelectionCriteria == null || defaultSingleValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(defaultSingleValuesForDateSelectionCriteria);
    }
}
