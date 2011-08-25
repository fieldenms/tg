package ua.com.fielden.platform.example.swing.ei;

import java.sql.Connection;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;

import com.google.inject.Injector;

public class InstantiationTimer {

    /**
     * @param args
     */
    public static void main(final String[] args) {
	/////////////////////////////////////////////////
	// DB connectivity and Hibernate configuration //
	/////////////////////////////////////////////////
	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate4example.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
	interceptor.setFactory(entityFactory);

	try {
	    final Transaction trans = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
	    final Connection connection = hibernateUtil.getSessionFactory().getCurrentSession().connection();
	    System.out.println("Executing db creation/population script");
	    connection.createStatement().execute("RUNSCRIPT FROM 'src/main/resources/db/example.ddl'");
	    trans.commit();
	    connection.close();
	} catch (final Exception e) {
	    System.err.println("Db initialisation failed: " + e.getMessage());
	    e.printStackTrace();
	}

	final InspectedEntityDao dao = injector.getInstance(InspectedEntityDao.class);

	final DateTime start = new DateTime();
	for (int index = 0; index < 1000; index++) {
	    // uncomment to check how long pure instrumented instantiation takes
	    //final InspectedEntity entity = entityFactory.newEntity(InspectedEntity.class, "VALUE-X", "entity description");
	    // uncomment to check how long DB retrieval in conjunction with instrumented instantiation takes
	    dao.findByKey("VALUE1");
	}
	final DateTime finish = new DateTime();
	final Duration duration = new Duration(start.getMillis(), finish.getMillis());
	System.out.println(duration.toPeriod().getMinutes() + ":" + duration.toPeriod().getSeconds() + "." + duration.toPeriod().getMillis());
    }

}
