package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

public class DomainTreeManagerAndEnhancer1 extends AbstractDomainTreeManagerAndEnhancer {

    /**
     * A <i>manager with enhancer</i> constructor for the first time instantiation.
     */
    public DomainTreeManagerAndEnhancer1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new DomainTreeManager1(serialiser, rootTypes), new DomainTreeEnhancer(serialiser, rootTypes));
    }

    protected DomainTreeManagerAndEnhancer1(final AbstractDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    /**
     * A specific Kryo serialiser for {@link DomainTreeManagerAndEnhancer1}.
     *
     * @author TG Team
     *
     */
    public static class DomainTreeManagerAndEnhancerForTestSerialiser extends TgSimpleSerializer<DomainTreeManagerAndEnhancer1> {
	public DomainTreeManagerAndEnhancerForTestSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public DomainTreeManagerAndEnhancer1 read(final ByteBuffer buffer) {
	    // abstract nature?
	    final DomainTreeManager1 base = readValue(buffer, DomainTreeManager1.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new DomainTreeManagerAndEnhancer1(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final DomainTreeManagerAndEnhancer1 manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }
}