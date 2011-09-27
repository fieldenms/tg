package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToCategoriesTickRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToDistributionTickRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree manager for lifecycle analyses.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeManager extends AbstractAnalysisDomainTreeManager implements ILifecycleDomainTreeManager {
    private static final long serialVersionUID = -4155274305648154329L;

    private Pair<Class<?>, String> lifecycleProperty;
    private Date from, to;
    private Boolean total;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LifecycleDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new LifecycleDomainTreeRepresentation(serialiser, rootTypes), null, new LifecycleAddToDistributionTickManager(), new LifecycleAddToCategoriesTickManager(), null, null, null, null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LifecycleDomainTreeManager(final ISerialiser serialiser, final ILifecycleDomainTreeRepresentation dtr, final Boolean visible, final LifecycleAddToDistributionTickManager firstTick, final LifecycleAddToCategoriesTickManager secondTick, final Pair<Class<?>, String> lifecycleProperty, final Date from, final Date to, final Boolean total) {
	super(serialiser, dtr, visible, firstTick, secondTick);

	this.lifecycleProperty = lifecycleProperty;
	this.from = from;
	this.to = to;
	this.total = total;
    }

    @Override
    public ILifecycleAddToDistributionTickManager getFirstTick() {
	return (ILifecycleAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public ILifecycleAddToCategoriesTickManager getSecondTick() {
	return (ILifecycleAddToCategoriesTickManager) super.getSecondTick();
    }

    @Override
    public ILifecycleDomainTreeRepresentation getRepresentation() {
	return (ILifecycleDomainTreeRepresentation) super.getRepresentation();
    }

    public static class LifecycleAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements ILifecycleAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public LifecycleAddToDistributionTickManager() {
	}

	@Override
	protected LifecycleAddToDistributionTickRepresentation tr() {
	    return (LifecycleAddToDistributionTickRepresentation) super.tr();
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

    public static class LifecycleAddToCategoriesTickManager extends AbstractAnalysisAddToAggregationTickManager implements ILifecycleAddToCategoriesTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public LifecycleAddToCategoriesTickManager() {
	}

	@Override
	protected LifecycleAddToCategoriesTickRepresentation tr() {
	    return (LifecycleAddToCategoriesTickRepresentation) super.tr();
	}
    }

    /**
     * A specific Kryo serialiser for {@link LifecycleDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class LifecycleDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<LifecycleDomainTreeManager> {
	public LifecycleDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LifecycleDomainTreeManager read(final ByteBuffer buffer) {
	    final LifecycleDomainTreeRepresentation dtr = readValue(buffer, LifecycleDomainTreeRepresentation.class);
	    final LifecycleAddToDistributionTickManager firstTick = readValue(buffer, LifecycleAddToDistributionTickManager.class);
	    final LifecycleAddToCategoriesTickManager secondTick = readValue(buffer, LifecycleAddToCategoriesTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    final Pair<Class<?>, String> lifecycleProperty = readValue(buffer, Pair.class);
	    final Date from = readValue(buffer, Date.class);
	    final Date to = readValue(buffer, Date.class);
	    final Boolean total = readValue(buffer, Boolean.class);
	    return new LifecycleDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick, lifecycleProperty, from, to, total);
	}

	@Override
	public void write(final ByteBuffer buffer, final LifecycleDomainTreeManager manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.lifecycleProperty);
	    writeValue(buffer, manager.from);
	    writeValue(buffer, manager.to);
	    writeValue(buffer, manager.total);
	}
    }

    @Override
    public Pair<Class<?>, String> getLifecycleProperty() {
	return lifecycleProperty;
    }

    @Override
    public ILifecycleDomainTreeManager setLifecycleProperty(final Pair<Class<?>, String> lifecycleProperty) {
	final boolean isEntityItself = "".equals(lifecycleProperty.getValue()); // empty property means "entity itself"
	if (isEntityItself || !AnnotationReflector.isPropertyAnnotationPresent(Monitoring.class, lifecycleProperty.getKey(), lifecycleProperty.getValue())) { // can not set a property of non-lifecycle nature
	    throw new IllegalArgumentException("Could not set a property of non-lifecycle nature [" + lifecycleProperty.getValue() + "] in type [" + lifecycleProperty.getKey().getSimpleName() + "].");
	}
	this.lifecycleProperty = lifecycleProperty;
	return this;
    }

    @Override
    public Date getFrom() {
	return from;
    }

    @Override
    public ILifecycleDomainTreeManager setFrom(final Date from) {
	this.from = from;
	return this;
    }

    @Override
    public Date getTo() {
	return to;
    }

    @Override
    public ILifecycleDomainTreeManager setTo(final Date to) {
	this.to = to;
	return this;
    }

    @Override
    public boolean isTotal() {
	return total != null ? total : false; // should be FALSE by default;
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setTotal(final boolean total) {
	this.total = total;
	return this;
    }
}
