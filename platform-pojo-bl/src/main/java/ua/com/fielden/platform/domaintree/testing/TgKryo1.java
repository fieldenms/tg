package ua.com.fielden.platform.domaintree.testing;

import java.util.List;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1.AbstractAnalysisDomainTreeManager1Serialiser;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1.AbstractAnalysisDomainTreeManagerAndEnhancer1Serialiser;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1.AbstractAnalysisDomainTreeRepresentation1Serialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManager1.DomainTreeManagerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1.DomainTreeManagerAndEnhancerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1.DomainTreeRepresentationForTestSerialiser;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.esotericsoftware.kryo.Serializer;

public class TgKryo1 extends TgKryo {

    public TgKryo1(final EntityFactory factory, final ISerialisationClassProvider provider) {
	super(factory, provider);
    }

    @Override
    protected void registerDomainTreeTypes() {
        super.registerDomainTreeTypes();

	final List<Class<?>> types = TgKryo.typesForRegistration("../platform-pojo-bl/target/test-classes", "ua.com.fielden.platform.domaintree", AbstractDomainTree.DOMAIN_TREE_TYPES);
	types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domaintree.testing", AbstractDomainTree.DOMAIN_TREE_TYPES));
	types.remove(EntityWithoutKeyType.class);

	for (final Class<?> type : types) {
	    register(type);
	}
    }

    @Override
    public Serializer newSerializer(final Class type) {
	if (DomainTreeManager1.class.isAssignableFrom(type)) {
	    return new DomainTreeManagerForTestSerialiser(this);
	} else if (DomainTreeManagerAndEnhancer1.class.isAssignableFrom(type)) {
	    return new DomainTreeManagerAndEnhancerForTestSerialiser(this);
	} else if (DomainTreeRepresentation1.class.isAssignableFrom(type)) {
	    return new DomainTreeRepresentationForTestSerialiser(this);
	} else if (AbstractAnalysisDomainTreeManager1.class.isAssignableFrom(type)) {
	    return new AbstractAnalysisDomainTreeManager1Serialiser(this);
	} else if (AbstractAnalysisDomainTreeManagerAndEnhancer1.class.isAssignableFrom(type)) {
	    return new AbstractAnalysisDomainTreeManagerAndEnhancer1Serialiser(this);
	} else if (AbstractAnalysisDomainTreeRepresentation1.class.isAssignableFrom(type)) {
	    return new AbstractAnalysisDomainTreeRepresentation1Serialiser(this);
	} else {
	    return super.newSerializer(type);
	}
    }
}
