package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for sentinel analyses.
 *
 * @author TG Team
 *
 */
public class SentinelDomainTreeRepresentation extends AnalysisDomainTreeRepresentation implements ISentinelDomainTreeRepresentation {
    public final static String countOfSelfName = "countOfSelfDashboard";

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public SentinelDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new SentinelAddToDistributionTickRepresentation(), new SentinelAddToAggregationTickRepresentation(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected SentinelDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final SentinelAddToDistributionTickRepresentation firstTick, final SentinelAddToAggregationTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public ISentinelAddToDistributionTickRepresentation getFirstTick() {
	return (ISentinelAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public ISentinelAddToAggregationTickRepresentation getSecondTick() {
	return (ISentinelAddToAggregationTickRepresentation) super.getSecondTick();
    }

    @Override
    public boolean isExcludedImmutably(final Class<?> root, final String property) {
	// inject an enhanced type into method implementation
	final Class<?> managedType = managedType(root);

	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	// overridden to exclude non-sentinel properties
	return (super.isExcludedImmutably(managedType, property)) || // base TG analysis domain representation usage
	!(isSentinel(root, managedType, property) || isEntityItself || countOfSelfName.equals(property)); // exclude crit-only properties
    }

    private boolean isSentinel(final Class<?> root, final Class<?> managedType, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, property);
	final Calculated calculatedAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(Calculated.class, managedType, property);
	final String upperCasedAndTrimmedExpr = calculatedAnnotation != null ? calculatedAnnotation.value().trim().toUpperCase() : null;
	return calculatedAnnotation != null && EntityUtils.isString(propertyType) && //
		upperCasedAndTrimmedExpr.startsWith("CASE WHEN ") && upperCasedAndTrimmedExpr.endsWith(" END") && upperCasedAndTrimmedExpr.contains(" \"GREEN\" ") && upperCasedAndTrimmedExpr.contains(" \"RED\" ");
		// expr.startsWith("\"1 + 7");
    }

    public static class SentinelAddToDistributionTickRepresentation extends AnalysisAddToDistributionTickRepresentation implements ISentinelAddToDistributionTickRepresentation {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public SentinelAddToDistributionTickRepresentation() {
	}
    }

    public static class SentinelAddToAggregationTickRepresentation extends AnalysisAddToAggregationTickRepresentation implements ISentinelAddToAggregationTickRepresentation {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public SentinelAddToAggregationTickRepresentation() {
	}
    }

    /**
     * A specific Kryo serialiser for {@link SentinelDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class SentinelDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<SentinelDomainTreeRepresentation> {
	public SentinelDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public SentinelDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final SentinelAddToDistributionTickRepresentation firstTick = readValue(buffer, SentinelAddToDistributionTickRepresentation.class);
	    final SentinelAddToAggregationTickRepresentation secondTick = readValue(buffer, SentinelAddToAggregationTickRepresentation.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new SentinelDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
