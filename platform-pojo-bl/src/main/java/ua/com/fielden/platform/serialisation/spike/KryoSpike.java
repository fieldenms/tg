package ua.com.fielden.platform.serialisation.spike;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
// import ua.com.fielden.platform.serialisation.api.impl.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;

public class KryoSpike {

    public static void main(final String[] args) throws Exception {
        System.out.println("kryo");

        final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
        final EntityFactory factory = injector.getInstance(EntityFactory.class);

        final ProvidedSerialisationClassProvider provider = new ProvidedSerialisationClassProvider(DomainType1.class);
        final ISerialiserEngine kryoWriter = new Serialiser(factory, provider).getEngine(SerialiserEngines.KRYO);
        final ISerialiserEngine kryoReader = new Serialiser(factory, provider).getEngine(SerialiserEngines.KRYO);

        System.out.print("Creating objects...");
        final List<DomainType1> list = new ArrayList<DomainType1>(1000);
        for (int index = 0; index < 1000; index++) {
            final DomainType1 entity = factory.newByKey(DomainType1.class, "key");
            entity.setDesc("description");
            entity.setMoney(new Money("20.00"));
            entity.setBigDecimal(new BigDecimal("1000.23"));
            entity.setInteger(136);
            entity.setDate(new Date());
            final DomainType1 entity1 = factory.newByKey(DomainType1.class, "key1");
            entity1.setDesc("description");
            entity1.setMoney(new Money("20.00"));
            entity1.setBigDecimal(new BigDecimal("1000.23"));
            entity1.setInteger(136);
            entity1.setDate(new Date());
            //entity1.setItself(entity);
            entity.setItself(entity1);
            list.add(entity);
        }
        System.out.println("finished");

        System.out.println("starting marshalling...");
        final DateTime start = new DateTime();

        final List restoredList = kryoReader.deserialise(kryoWriter.serialise(list), ArrayList.class);

        final DateTime finish = new DateTime();
        final Period duration = new Period(start, finish);
        System.out.println("murshaling time: " + duration.getMinutes() + ":" + duration.getSeconds() + "." + duration.getMillis());

    }
}
