package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToDistributionTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

/**
 * A domain tree manager for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IPivotDomainTreeManager {
    private static final long serialVersionUID = -4155274305648154329L;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new PivotDomainTreeRepresentation(serialiser, rootTypes),null, new PivotAddToDistributionTickManager(), new PivotAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected PivotDomainTreeManager(final ISerialiser serialiser, final PivotDomainTreeRepresentation dtr, final Boolean visible, final PivotAddToDistributionTickManager firstTick, final PivotAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
	return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
	return (IPivotAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
	return (IPivotDomainTreeRepresentation) super.getRepresentation();
    }

    public static class PivotAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IPivotAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToDistributionTickManager() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return PivotDomainTreeRepresentation.getWidthByDefault(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    PivotDomainTreeRepresentation.setWidthByDefault(tr().getDtr(), root, property, width, propertiesWidths);
	}

	@Override
	protected PivotAddToDistributionTickRepresentation tr() {
	    return (PivotAddToDistributionTickRepresentation) super.tr();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final PivotAddToDistributionTickManager other = (PivotAddToDistributionTickManager) obj;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    return true;
	}
    }

    public static class PivotAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IPivotAddToAggregationTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToAggregationTickManager() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return PivotDomainTreeRepresentation.getWidthByDefault(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    PivotDomainTreeRepresentation.setWidthByDefault(tr().getDtr(), root, property, width, propertiesWidths);
	}

	@Override
	protected PivotAddToAggregationTickRepresentation tr() {
	    return (PivotAddToAggregationTickRepresentation) super.tr();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final PivotAddToAggregationTickManager other = (PivotAddToAggregationTickManager) obj;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    return true;
	}
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<PivotDomainTreeManager> {
	public PivotDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeManager read(final ByteBuffer buffer) {
	    final PivotDomainTreeRepresentation dtr = readValue(buffer, PivotDomainTreeRepresentation.class);
	    final PivotAddToDistributionTickManager firstTick = readValue(buffer, PivotAddToDistributionTickManager.class);
	    final PivotAddToAggregationTickManager secondTick = readValue(buffer, PivotAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    return new PivotDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick);
	}
    }
}
