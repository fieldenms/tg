package ua.com.fielden.platform.serialisation.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.ClientEntityConverter;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;
import com.thoughtworks.xstream.XStream;

public class XStreamSpike {

    public static void main(final String[] args) {
	System.out.println("xstream");

	// for serialisation timing
	final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
	final EntityFactory factory = injector.getInstance(EntityFactory.class);

	final XStream ser = new XStream();
	ser.registerConverter(new ClientEntityConverter(factory));

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
	    entity1.setItself(entity);
	    entity.setItself(entity1);
	    list.add(entity);
	}

	final DateTime start = new DateTime();

	final String xml = ser.toXML(list);
	System.out.println(xml.length() / 1024);
	ser.fromXML(xml);

	final DateTime finish = new DateTime();
	final Period duration = new Period(start, finish);
	System.out.println("murshaling time: " + duration.getMinutes() + ":" + duration.getSeconds() + "." + duration.getMillis());
    }

}
