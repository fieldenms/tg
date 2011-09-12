package ua.com.fielden.platform.domain.tree;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementSet;

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
    protected DomainTreeManager1(final ISerialiser serialiser, final IDomainTreeRepresentation dtr, final TickManager firstTick, final TickManager secondTick) {
	super(serialiser, dtr, firstTick, secondTick);
    }

    public static class TickManager1ForTest extends TickManager {
	private static final long serialVersionUID = 2154025458968872414L;
	private final EnhancementSet touchedProperties;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public TickManager1ForTest() {
	    super();
	    touchedProperties = createSet();
	}

	@Override
	protected boolean isCheckedMutably(final Class<?> root, final String property) {
	    return property.endsWith("mutablyCheckedProp");
	}

//	@Override
//	public boolean isChecked(final Class<?> root, final String property) {
//	    if (!touchedProperties.contains(key(root, property))) {
//		return isCheckedMutably(root, property) || //
//			super.isChecked(root, property);
//	    } else {
//		return super.isChecked(root, property);
//	    }
//	};

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    super.check(root, property, check);
	    touchedProperties.add(key(root, property));
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((touchedProperties == null) ? 0 : touchedProperties.hashCode());
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
	    final TickManager1ForTest other = (TickManager1ForTest) obj;
	    if (touchedProperties == null) {
		if (other.touchedProperties != null)
		    return false;
	    } else if (!touchedProperties.equals(other.touchedProperties))
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
