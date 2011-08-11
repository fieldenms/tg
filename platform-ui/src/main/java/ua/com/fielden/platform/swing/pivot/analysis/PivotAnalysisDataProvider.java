package ua.com.fielden.platform.swing.pivot.analysis;

import static java.math.RoundingMode.HALF_EVEN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTableNode;
import ua.com.fielden.platform.swing.review.analysis.PivotAnalysisQueryExtender;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisDataProvider<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IPivotDataProvider {

    private final PivotAnalysisReportModel<T, DAO> reportModel;

    private final List<IBindingEntity> items = new ArrayList<IBindingEntity>();

    private final GroupItem rootNode = new GroupItem();

    public PivotAnalysisDataProvider(final PivotAnalysisReportModel<T, DAO> reportModel) {
	this.reportModel = reportModel;
    }

    public void loadData(final List<? extends IBindingEntity> entities) {
	items.clear();
	items.addAll(entities);
	final GroupItem rootItem = createBasicTreeFrom(entities);
	rootNode.clearChildren();
	if (!rootItem.getChildren().isEmpty()) {
	    rootNode.putChild("Grand totals", rootItem);
	}
    }

    public GroupItem createBasicTreeFrom(final List<? extends IBindingEntity> entities) {
	final GroupItem rootItem = new GroupItem();
	for (final IBindingEntity entity : entities) {
	    GroupItem nextItem = rootItem;
	    for (final IDistributedProperty group : reportModel.getSelectedDistributionProperties()) {
		final String alias = getAliasFor(group);
		if (alias != null) {
		    final Object eValue = entity.get(alias);
		    final Object key = eValue == null ? PivotTreeTableNode.NULL_USER_OBJECT : eValue;
		    GroupItem child = nextItem.getChild(key);
		    if (child == null) {
			child = new GroupItem();
			nextItem.putChild(key, child);
		    }
		    nextItem = child;
		}
	    }
	    nextItem.addEntity(entity);
	}
	calculate(rootItem);
	return rootItem;
    }

    public void calculate() {
	calculate(rootNode.getChild("Grand totals"));
    }

    public void calculate(final IAggregatedProperty aggregationProperty) {
	calculate(rootNode.getChild("Grand totals"), aggregationProperty);
    }

    public void calculate(final GroupItem rootItem) {
	if (rootItem != null) {
	    for (final IAggregatedProperty total : reportModel.getSelectedAggregationProperties()) {
		calculate(rootItem, total);
	    }
	}
    }

    public void calculate(final GroupItem rootItem, final IAggregatedProperty aggregationProperty) {
	if (rootItem != null) {
	    rootItem.calculate(getAliasFor(aggregationProperty), getTotalFunction(aggregationProperty));
	}
    }

    public void reload() {
	loadData(new ArrayList<IBindingEntity>(items));
    }

    @Override
    public GroupItem getData() {
	return rootNode;
    }

    @Override
    public String getAliasFor(final IDistributedProperty property) {
	if (property instanceof IAggregatedProperty) {
	    final IAggregatedProperty aggregationProperty = (IAggregatedProperty) property;
	    if (aggregationProperty.getAggregationFunction() == AnalysisPropertyAggregationFunction.DISTINCT_COUNT) {
		return reportModel.getReportView().getModel().getAliasFor(PivotAnalysisQueryExtender//
		.createDistributionPropertyFor(reportModel.getReportView().getModel().getEntityClass(), aggregationProperty));
	    }
	}
	return reportModel.getReportView().getModel().getAliasFor(property);
    }

    @Override
    public Class<?> getReturnTypeFor(final IAggregatedProperty aggregationProperty) {
	if (StringUtils.isEmpty(aggregationProperty.getActualProperty())) {
	    return aggregationProperty.getAggregationFunction().getReturnedType(reportModel.getReportView().getModel().getEntityClass());
	}
	final Class<?> type = PropertyTypeDeterminator.determinePropertyType(reportModel.getReportView().getModel().getEntityClass(), aggregationProperty.getActualProperty());
	return aggregationProperty.getAggregationFunction().getReturnedType(type);
    }

    private ITotalFunction getTotalFunction(final IAggregatedProperty aggregationProperty) {
	switch (aggregationProperty.getAggregationFunction()) {
	case COUNT:
	case SUM:
	    return createSumCountFunction(getReturnTypeFor(aggregationProperty), getAliasFor(aggregationProperty));
	case MIN:
	case MAX:
	    return createMinMaxFunction(getReturnTypeFor(aggregationProperty), getAliasFor(aggregationProperty)//
	    , aggregationProperty.getAggregationFunction() == AnalysisPropertyAggregationFunction.MIN);
	case AVG:
	    return createAvgFunction(getReturnTypeFor(aggregationProperty), getAliasFor(aggregationProperty), //
	    getAliasFor(PivotAnalysisQueryExtender.createAggregationPropertyFor(aggregationProperty, AnalysisPropertyAggregationFunction.COUNT)));
	case DISTINCT_COUNT:
	    return createDistinctCountFunction(getAliasFor(aggregationProperty));
	}
	return null;
    }

    private ITotalFunction createSumCountFunction(final Class<?> valueType, final String alias) {
	return new ITotalFunction() {

	    @Override
	    public Object calculateProperty(final GroupItem item) {
		final Object value = Number.class.isAssignableFrom(valueType) ? BigDecimal.ZERO : (Money.class.isAssignableFrom(valueType) ? BigDecimal.ZERO : null);
		FunctionValue sumCount = new FunctionValue(value, valueType);
		if (!item.getChildren().isEmpty()) {
		    for (final GroupItem child : item.getChildren()) {
			sumCount = sumCount.plus(new FunctionValue(child.getValueFor(alias), valueType));
		    }
		} else {
		    for (final IBindingEntity entity : item.getEntities()) {
			final Object nextValue = alias == null ? null : entity.get(alias);
			if (nextValue != null) {
			    sumCount = sumCount.plus(new FunctionValue(nextValue, valueType));
			}
		    }
		}
		return sumCount.getValue();
	    }

	};
    }

    private ITotalFunction createMinMaxFunction(final Class<?> valueType, final String alias, final boolean min) {
	return new ITotalFunction() {

	    @Override
	    public Object calculateProperty(final GroupItem item) {
		FunctionValue minMax = null;
		if (!item.getChildren().isEmpty()) {
		    final List<GroupItem> items = new ArrayList<GroupItem>(item.getChildren());
		    final Pair<Object, Integer> minMaxPair = findFirstMinMax(items, alias);
		    if (minMaxPair.getKey() == null) {
			return null;
		    }
		    minMax = new FunctionValue(minMaxPair.getKey(), valueType);
		    for (int itemIndex = minMaxPair.getValue().intValue(); itemIndex < items.size(); itemIndex++) {
			final Object nextValue = items.get(itemIndex).getValueFor(alias);
			if (nextValue != null) {
			    final FunctionValue anotherValue = new FunctionValue(nextValue, valueType);
			    if ((min && minMax.compareTo(anotherValue) > 0) || (!min && minMax.compareTo(anotherValue) < 0)) {
				minMax = anotherValue;
			    }
			}
		    }
		} else {
		    final List<IBindingEntity> entities = item.getEntities();
		    final Pair<Object, Integer> minMaxPair = getFirstMinMax(entities, alias);
		    if (minMaxPair.getKey() == null) {
			return null;
		    }
		    minMax = new FunctionValue(minMaxPair.getKey(), valueType);
		    for (int itemIndex = minMaxPair.getValue().intValue(); itemIndex < entities.size(); itemIndex++) {
			final Object nextValue = alias == null ? null : entities.get(itemIndex).get(alias);
			if (nextValue != null) {
			    final FunctionValue anotherValue = new FunctionValue(nextValue, valueType);
			    if ((min && minMax.compareTo(anotherValue) > 0) || (!min && minMax.compareTo(anotherValue) < 0)) {
				minMax = anotherValue;
			    }
			}
		    }
		}
		return minMax.getValue();
	    }

	    private Pair<Object, Integer> getFirstMinMax(final List<IBindingEntity> entities, final String alias) {
		for (int index = 0; index < entities.size(); index++) {
		    final Object value = alias == null ? null : entities.get(index).get(alias);
		    if (value != null) {
			return new Pair<Object, Integer>(value, Integer.valueOf(index + 1));
		    }
		}
		return new Pair<Object, Integer>(null, entities.size());
	    }

	    private Pair<Object, Integer> findFirstMinMax(final List<GroupItem> items, final String alias) {
		for (int index = 0; index < items.size(); index++) {
		    final Object value = items.get(index).getValueFor(alias);
		    if (value != null) {
			return new Pair<Object, Integer>(value, Integer.valueOf(index + 1));
		    }
		}
		return new Pair<Object, Integer>(null, items.size());
	    }

	};
    }

    private ITotalFunction createAvgFunction(final Class<?> avgType, final String avgAlias, final String countAlais) {
	return new ITotalFunction() {

	    @Override
	    public Object calculateProperty(final GroupItem item) {
		final Object value = Number.class.isAssignableFrom(avgType) ? BigDecimal.ZERO : (Money.class.isAssignableFrom(avgType) ? BigDecimal.ZERO : null);
		FunctionValue sum = new FunctionValue(value, avgType);
		FunctionValue count = new FunctionValue(BigDecimal.ZERO, BigDecimal.class);
		for (final IBindingEntity entity : item.getEntities()) {
		    final Object nextValue = avgAlias == null ? null : entity.get(avgAlias);
		    if (nextValue != null) {
			final FunctionValue itemCount = new FunctionValue(entity.get(countAlais), BigDecimal.class);
			final FunctionValue itemAvg = new FunctionValue(nextValue, avgType);
			sum = sum.plus(itemAvg.multiply(itemCount));
			count = count.plus(itemCount);
		    }
		}
		return sum.divide(count).getValue();
	    }

	};
    }

    private ITotalFunction createDistinctCountFunction(final String alias) {
	return new ITotalFunction() {

	    @Override
	    public Object calculateProperty(final GroupItem item) {
		final Set<Object> distinctValues = new HashSet<Object>();
		for (final IBindingEntity entity : item.getEntities()) {
		    if (alias != null) {
			distinctValues.add(entity.get(alias));
		    }
		}
		return Integer.valueOf(distinctValues.size());
	    }

	};
    }

    private static class FunctionValue implements Comparable<FunctionValue> {

	private final Object value;
	private final Class<?> valueType;

	public FunctionValue(final Object value, final Class<?> valueType) {
	    this.valueType = valueType;
	    if (isNumber()) {
		this.value = new BigDecimal(value.toString());
	    } else if (isMoney()) {
		this.value = new Money(new BigDecimal(value instanceof Money ? ((Money) value).getAmount().toString() : value.toString()));
	    } else {
		this.value = value;
	    }

	}

	public boolean isNumber() {
	    return Number.class.isAssignableFrom(valueType);
	}

	public boolean isMoney() {
	    return Money.class.isAssignableFrom(valueType);
	}

	public boolean isComparable() {
	    return Comparable.class.isAssignableFrom(valueType);
	}

	public FunctionValue plus(final FunctionValue value) {
	    if (isNumber() && value.isNumber()) {
		final BigDecimal result = ((BigDecimal) getValue()).add((BigDecimal) value.getValue());
		return new FunctionValue(result, getValueType());
	    } else if (isMoney() && value.isMoney()) {
		final Money result = ((Money) getValue()).plus((Money) value.getValue());
		return new FunctionValue(result.getAmount(), getValueType());
	    } else {
		throw new UnsupportedOperationException(" The '+' operation is not supported for: " + getValueType().getSimpleName() + ", " + value.getValueType().getSimpleName()
			+ " types.");
	    }
	}

	public FunctionValue minus(final FunctionValue value) {
	    if (isNumber() && value.isNumber()) {
		final BigDecimal result = ((BigDecimal) getValue()).subtract((BigDecimal) value.getValue());
		return new FunctionValue(result, getValueType());
	    } else if (isMoney() && value.isMoney()) {
		final Money result = ((Money) getValue()).minus((Money) value.getValue());
		return new FunctionValue(result.getAmount(), getValueType());
	    } else {
		throw new UnsupportedOperationException(" The '-' operation is not supported for: " + getValueType().getSimpleName() + ", " + value.getValueType().getSimpleName()
			+ " types.");
	    }
	}

	public FunctionValue multiply(final FunctionValue value) {
	    if (isNumber() && value.isNumber()) {
		final BigDecimal result = ((BigDecimal) getValue()).multiply((BigDecimal) value.getValue());
		return new FunctionValue(result, getValueType());
	    } else if (isMoney() && value.isNumber()) {
		final Money result = ((Money) getValue()).multiply((BigDecimal) value.getValue());
		return new FunctionValue(result.getAmount(), getValueType());
	    } else {
		throw new UnsupportedOperationException(" The '*' operation is not supported for: " + getValueType().getSimpleName() + ", " + value.getValueType().getSimpleName()
			+ " types.");
	    }
	}

	public FunctionValue divide(final FunctionValue value) {
	    if (isNumber() && value.isNumber()) {
		try {
		    final BigDecimal result = ((BigDecimal) getValue()).divide((BigDecimal) value.getValue(), HALF_EVEN);
		    return new FunctionValue(result, getValueType());
		} catch (final ArithmeticException ex) {
		    return new FunctionValue(BigDecimal.ZERO, getValueType());
		}
	    } else if (isMoney() && value.isNumber()) {
		try {
		    final Money result = ((Money) getValue()).divide((BigDecimal) value.getValue());
		    return new FunctionValue(result.getAmount(), getValueType());
		} catch (final ArithmeticException ex) {
		    return new FunctionValue(BigDecimal.ZERO, getValueType());
		}
	    } else {
		throw new UnsupportedOperationException(" The '/' operation is not supported for: " + getValueType().getSimpleName() + ", " + value.getValueType().getSimpleName()
			+ " types.");
	    }
	}

	public Object getValue() {
	    return value;
	}

	public Class<?> getValueType() {
	    return valueType;
	}

	@Override
	public int compareTo(final FunctionValue o) {
	    if (isComparable()) {
		return ((Comparable) getValue()).compareTo(o.getValue());
	    }
	    throw new UnsupportedOperationException("Value types are not supported:");
	}
    }

}
