package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1.AbstractAnalysisDomainTreeManager1Serialiser;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1.AbstractAnalysisDomainTreeRepresentation1Serialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManager1.DomainTreeManagerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1.DomainTreeManagerAndEnhancerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1.DomainTreeRepresentationForTestSerialiser;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo0;

import com.esotericsoftware.kryo.Serializer;

/**
 * WARNING: this is an OLD version!
 * 
 * @author TG Team
 * 
 */
@Deprecated
public class TgKryo0ForDomainTreesTestingPurposes extends TgKryo0 {

    public TgKryo0ForDomainTreesTestingPurposes(final EntityFactory factory, final ISerialisationClassProvider provider) {
        super(factory, provider);
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
        } else if (AbstractAnalysisDomainTreeRepresentation1.class.isAssignableFrom(type)) {
            return new AbstractAnalysisDomainTreeRepresentation1Serialiser(this);
        } else {
            return super.newSerializer(type);
        }
    }

}
