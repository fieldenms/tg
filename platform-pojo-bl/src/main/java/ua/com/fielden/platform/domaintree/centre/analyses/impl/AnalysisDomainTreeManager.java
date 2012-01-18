package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToDistributionTickRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

/**
 * A domain tree manager for analyses.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IAnalysisDomainTreeManager {
    private static final long serialVersionUID = -4155274305648154329L;

    private Integer visibleDistributedValuesNumber;


    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public AnalysisDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new AnalysisDomainTreeRepresentation(serialiser, rootTypes), null, new AnalysisAddToDistributionTickManager(), new AnalysisAddToAggregationTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AnalysisDomainTreeManager(final ISerialiser serialiser, final AnalysisDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick, final Integer visibleDistributedValuesNumber) {
	super(serialiser, dtr, visible, firstTick, secondTick);

	this.visibleDistributedValuesNumber = visibleDistributedValuesNumber;
    }

    @Override
    public IAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IAnalysisDomainTreeRepresentation getRepresentation() {
	return (IAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    public static class AnalysisAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IAnalysisAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AnalysisAddToDistributionTickManager() {
	}

	@Override
	protected AnalysisAddToDistributionTickRepresentation tr() {
	    return (AnalysisAddToDistributionTickRepresentation) super.tr();
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    final List<String> listOfUsedProperties = getAndInitUsedProperties(root, property);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.clear();
		listOfUsedProperties.add(property);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	}
    }

    public static class AnalysisAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IAnalysisAddToAggregationTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AnalysisAddToAggregationTickManager() {
	}

	@Override
	protected AnalysisAddToAggregationTickRepresentation tr() {
	    return (AnalysisAddToAggregationTickRepresentation) super.tr();
	}
    }

    /**
     * A specific Kryo serialiser for {@link AnalysisDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class AnalysisDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<AnalysisDomainTreeManager> {
	public AnalysisDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public AnalysisDomainTreeManager read(final ByteBuffer buffer) {
	    final AnalysisDomainTreeRepresentation dtr = readValue(buffer, AnalysisDomainTreeRepresentation.class);
	    final AnalysisAddToDistributionTickManager firstTick = readValue(buffer, AnalysisAddToDistributionTickManager.class);
	    final AnalysisAddToAggregationTickManager secondTick = readValue(buffer, AnalysisAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    final Integer visibleDistributedValuesNumber = readValue(buffer, Integer.class);
	    return new AnalysisDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick, visibleDistributedValuesNumber);
	}

	@Override
	public void write(final ByteBuffer buffer, final AnalysisDomainTreeManager manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.visibleDistributedValuesNumber);
	}
    }

    @Override
    public int getVisibleDistributedValuesNumber() {
	return visibleDistributedValuesNumber == null ? 0 : visibleDistributedValuesNumber;
    }

    @Override
    public IAnalysisDomainTreeManager setVisibleDistributedValuesNumber(final int visibleDistributedValuesNumber) {
	this.visibleDistributedValuesNumber = Integer.valueOf(visibleDistributedValuesNumber);
	return this;
    }

    protected Integer visibleDistributedValuesNumber() {
	return visibleDistributedValuesNumber;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((visibleDistributedValuesNumber == null) ? 0 : visibleDistributedValuesNumber.hashCode());
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
	final AnalysisDomainTreeManager other = (AnalysisDomainTreeManager) obj;
	if (visibleDistributedValuesNumber == null) {
	    if (other.visibleDistributedValuesNumber != null)
		return false;
	} else if (!visibleDistributedValuesNumber.equals(other.visibleDistributedValuesNumber))
	    return false;
	return true;
    }
}
