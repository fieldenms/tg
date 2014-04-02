package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1.AbstractAnalysisDomainTreeManager1Serialiser;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1.AbstractAnalysisDomainTreeRepresentation1Serialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManager1.DomainTreeManagerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1.DomainTreeManagerAndEnhancerForTestSerialiser;
import ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1.DomainTreeRepresentationForTestSerialiser;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.esotericsoftware.kryo.Serializer;

/**
 * {@link TgKryo} descendant for Domain Tree testing.
 * 
 * @author TG Team
 * 
 */
public class TgKryoForDomainTreesTestingPurposes extends TgKryo {

    public TgKryoForDomainTreesTestingPurposes(final EntityFactory factory, final ISerialisationClassProvider provider) {
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

    //    public static void main(final String[] args) throws ClassNotFoundException {
    //   	final List<Class<?>> types = TgKryo.typesForRegistration("../platform-pojo-bl/target/test-classes", "ua.com.fielden.platform.domaintree", AbstractDomainTree.DOMAIN_TREE_TYPES);
    //	types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domaintree.testing", AbstractDomainTree.DOMAIN_TREE_TYPES));
    //	types.remove(EntityWithoutKeyType.class);
    //   	final List<Class<?>> distinctTypes = new ArrayList<Class<?>>();
    //   	for(final Class<?> type : types){
    //   	    if(!distinctTypes.contains(type)){
    //   		distinctTypes.add(type);
    //   	    }
    //   	}
    //   	for (final Class<?> type : distinctTypes) {
    //   	    System.out.println("testTypes.add(findClass(\"" + type.getName() + "\"));");
    //   	}
    //   	// final String path = "../platform-pojo-bl/target/classes", packageName = "ua.com.fielden.platform.domaintree";
    //   	// try {
    //   	// 	System.out.println(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ITickManager.class));
    //   	// } catch (final Exception e) {
    //   	// 	e.printStackTrace();
    //   	// 	throw new IllegalStateException("Retrieval of the [" + ITickManager.class.getSimpleName() + "] descendants from [" + path + "; " + packageName
    //   	//	+ "] has been failed.");
    //   	// }
    //       }
}
