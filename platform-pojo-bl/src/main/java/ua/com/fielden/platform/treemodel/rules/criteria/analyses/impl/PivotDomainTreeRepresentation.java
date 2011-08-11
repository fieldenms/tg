package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements IPivotDomainTreeRepresentation {
    private static final long serialVersionUID = 6796573637187428691L;

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new PivotAddToDistributionTickRepresentation(), new PivotAddToAggregationTickRepresentation());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected PivotDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final IPivotAddToDistributionTickRepresentation firstTick, final IPivotAddToAggregationTickRepresentation secondTick) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickRepresentation getFirstTick() {
	return (IPivotAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickRepresentation getSecondTick() {
	return (IPivotAddToAggregationTickRepresentation) super.getSecondTick();
    }

    public static class PivotAddToDistributionTickRepresentation extends AbstractAnalysisAddToDistributionTickRepresentation implements IPivotAddToDistributionTickRepresentation {
	private static final long serialVersionUID = 4243970952493957297L;

	private final Map<Pair<Class<?>, String>, Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public PivotAddToDistributionTickRepresentation() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidthByDefault(final Class<?> root, final String property) {
	    return PivotDomainTreeRepresentation.getWidthByDefault(getDtr(), root, property, propertiesWidths);
	}

	@Override
	public void setWidthByDefault(final Class<?> root, final String property, final int width) {
	    PivotDomainTreeRepresentation.setWidthByDefault(getDtr(), root, property, width, propertiesWidths);
	}
    }

    public static class PivotAddToAggregationTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements IPivotAddToAggregationTickRepresentation {
	private static final long serialVersionUID = 4629386477984565938L;

	private final Map<Pair<Class<?>, String>, Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public PivotAddToAggregationTickRepresentation() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidthByDefault(final Class<?> root, final String property) {
	    return PivotDomainTreeRepresentation.getWidthByDefault(getDtr(), root, property, propertiesWidths);
	}

	@Override
	public void setWidthByDefault(final Class<?> root, final String property, final int width) {
	    PivotDomainTreeRepresentation.setWidthByDefault(getDtr(), root, property, width, propertiesWidths);
	}
    }

    /**
     * Returns property's (column's) <b>default</b> <i>widths</i>. <br><br>
     *
     * Throws {@link IllegalArgumentException}. if the specified property is excluded.<br><br>
     *
     * @param dtr -- specified {@link IDomainTreeRepresentation} that allows one to determine whether specified property is excluded or not.
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param propertiesWidths -- holds all specified properties' default width
     * @return
     */
    private static int getWidthByDefault(final IDomainTreeRepresentation dtr,final Class<?> root, final String property,final Map<Pair<Class<?>, String>, Integer> propertiesWidths){
	illegalExcludedProperties(dtr, root, property, "Could not ask a 'width by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : 0;
    }

    /**
     * Sets the <b>default</b> <i>width</i> for specified property (column). <br><br>
     *
     * Throws {@link IllegalArgumentException}. if the specified property is excluded.<br><br>
     *
     * @param dtr -- specified {@link IDomainTreeRepresentation} that allows one to determine whether specified property is excluded or not.
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param width -- specified width to set.
     * @param propertiesWidths -- holds all specified properties' default width
     *
     */
    private static void setWidthByDefault(final IDomainTreeRepresentation dtr,final Class<?> root, final String property, final int width,final Map<Pair<Class<?>, String>, Integer> propertiesWidths){
	illegalExcludedProperties(dtr, root, property, "Could not ask a 'width by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	propertiesWidths.put(key(root, property), width);
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<PivotDomainTreeRepresentation> {
	public PivotDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Set<Pair<Class<?>, String>> excludedProperties = readValue(buffer, HashSet.class);
	    final PivotAddToDistributionTickRepresentation firstTick = readValue(buffer, PivotAddToDistributionTickRepresentation.class);
	    final PivotAddToAggregationTickRepresentation secondTick = readValue(buffer, PivotAddToAggregationTickRepresentation.class);
	    return new PivotDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick);
	}
    }
}
