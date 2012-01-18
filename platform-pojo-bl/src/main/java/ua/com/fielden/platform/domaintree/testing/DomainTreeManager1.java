package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

public class DomainTreeManager1 extends AbstractDomainTreeManager {
    private static final long serialVersionUID = 1860664025057907572L;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    public DomainTreeManager1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new DomainTreeRepresentation1(serialiser, rootTypes), new TickManager1ForTest(), new TickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected DomainTreeManager1(final ISerialiser serialiser, final AbstractDomainTreeRepresentation dtr, final TickManager firstTick, final TickManager secondTick) {
	super(serialiser, dtr, firstTick, secondTick);
    }

    public static class TickManager1ForTest extends TickManager {
	private static final long serialVersionUID = 2154025458968872414L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public TickManager1ForTest() {
	    super();
	}

	@Override
	protected boolean isCheckedMutably(final Class<?> root, final String property) {
	    return property.endsWith("mutablyCheckedProp");
	}

	@Override
	protected void firePostCheckEvent(final IPropertyStructureChangedListener listener, final Class<?> root, final String property, final boolean check) {
	    listener.propertyStructureChanged(root, property, check ? ChangedAction.CHECKED_FIRST_TICK : ChangedAction.UNCHECKED_FIRST_TICK); // first? second?
	}
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class DomainTreeManagerForTestSerialiser extends AbstractDomainTreeManagerSerialiser<DomainTreeManager1> {
	public DomainTreeManagerForTestSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public DomainTreeManager1 read(final ByteBuffer buffer) {
	    final DomainTreeRepresentation1 dtr = readValue(buffer, DomainTreeRepresentation1.class);
	    final TickManager1ForTest firstTick = readValue(buffer, TickManager1ForTest.class);
	    final TickManager secondTick = readValue(buffer, TickManager.class);
	    return new DomainTreeManager1(kryo(), dtr, firstTick, secondTick);
	}
    }

    //	@Override
    //	protected ITickManager createFirstTick(final IDomainTreeRepresentation dtr) {
    //	    return new TickManager1ForTest(dtr, dtr.getFirstTick());
    //	}
}
