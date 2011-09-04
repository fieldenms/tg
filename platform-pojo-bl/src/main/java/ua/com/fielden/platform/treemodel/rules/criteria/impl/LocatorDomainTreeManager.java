package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeManager;

/**
 * Locator domain tree manager. <br><br>
 *
 * Includes implementation of "checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeManager extends CriteriaDomainTreeManager implements ILocatorDomainTreeManager {
    private static final long serialVersionUID = 7832625541851145438L;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LocatorDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new LocatorDomainTreeRepresentation(serialiser, rootTypes), new AddToCriteriaTickManager(serialiser, rootTypes), new AddToResultTickManager(), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LocatorDomainTreeManager(final ISerialiser serialiser, final ILocatorDomainTreeRepresentation dtr, final AddToCriteriaTickManager firstTick, final AddToResultTickManager secondTick, final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses, final Boolean runAutomatically) {
	super(serialiser, dtr, firstTick, secondTick, persistentAnalyses, runAutomatically);
    }

    @Override
    public ILocatorDomainTreeRepresentation getRepresentation() {
        return (ILocatorDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public SearchBy getSearchBy() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isUseForAutocompletion() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion) {
	// TODO Auto-generated method stub
	return null;
    }

//    @Override
//    public boolean isRunAutomatically() {
//        // TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//	// TODO please implement default value to true!
//        return true; // super.isRunAutomatically();
//    }
//
//    @Override
//    public int getColumnsNumber() {
//        // TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	// TODO please implement default value to 1!
//	return columnsNumber == null ? 2 : columnsNumber;
//    }

    /**
     * A specific Kryo serialiser for {@link LocatorDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class LocatorDomainTreeManagerSerialiser extends AbstractDomainTreeManagerSerialiser<LocatorDomainTreeManager> {
	public LocatorDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorDomainTreeManager read(final ByteBuffer buffer) {
	    final LocatorDomainTreeRepresentation dtr = readValue(buffer, LocatorDomainTreeRepresentation.class);
	    final AddToCriteriaTickManager firstTick = readValue(buffer, AddToCriteriaTickManager.class);
	    final AddToResultTickManager secondTick = readValue(buffer, AddToResultTickManager.class);
	    final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses = readValue(buffer, HashMap.class);
	    final Boolean runAutomatically = readValue(buffer, Boolean.class);
	    return new LocatorDomainTreeManager(kryo(), dtr, firstTick, secondTick, persistentAnalyses, runAutomatically);
	}

	@Override
	public void write(final ByteBuffer buffer, final LocatorDomainTreeManager manager) {
//	    super.write(buffer, manager);
	    writeValue(buffer, manager.getRepresentation());
	    writeValue(buffer, manager.getFirstTick());
	    writeValue(buffer, manager.getSecondTick());

	    writeValue(buffer, manager.persistentAnalyses());
	    writeValue(buffer, manager.runAutomatically());
	}
    }
}
