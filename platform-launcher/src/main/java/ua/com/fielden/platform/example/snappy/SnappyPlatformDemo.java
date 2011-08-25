package ua.com.fielden.platform.example.snappy;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.snappy.TgSnappyApplicationModel;
import ua.com.fielden.platform.snappy.TgSnappyApplicationPanel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.snappy.SnappyDbEnhancer;
import ua.com.fielden.snappy.storing.ApplicationModel;

import com.google.inject.Injector;

public class SnappyPlatformDemo {
    private static final long serialVersionUID = -7939058735965835834L;

    public static void main(final String[] args) {
	// 1. create hibernate configuration :
	final Configuration config = new Configuration().configure("hibernate.cfg.xml");

	// 2. enhance hibernate configuration :
	final SnappyDbEnhancer snappyDbEnhancer = new SnappyDbEnhancer();
	snappyDbEnhancer.enhanceConfiguration(config, "jdbc:h2:src/main/resources/db/snappy-db");

	// 3. initiate injectors, session factory etc :
	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, config);
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil
		.getConfiguration()));
	final Injector injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	final SessionFactory sessionFactory = hibernateUtil.getSessionFactory();
	interceptor.setFactory(injector.getInstance(EntityFactory.class));

	// 4. Initiate database:
	try {
	    final Transaction trans = sessionFactory.getCurrentSession().beginTransaction();
	    final Connection connection = sessionFactory.getCurrentSession().connection();
	    final ResultSet rsTables = connection.getMetaData().getTables(null, "PUBLIC", "RMA%", new String[] { "TABLE" });
	    if (rsTables.next()) {
		rsTables.close();
		System.out.print("Skipping db creation -- already exists...");
	    } else {
		rsTables.close();
		System.out.print("Clear db...");
		connection.createStatement().execute("DROP ALL OBJECTS");
		System.out.println("success");
		System.out.print("Executing db creation/population script...");
		connection.createStatement().execute("RUNSCRIPT FROM 'src/main/resources/script.sql'");
		System.out.println("success");
	    }
	    trans.commit();
	    connection.close();
	} catch (final Exception e) {
	    System.err.println("Db initialisation failed: " + e.getMessage());
	    e.printStackTrace();
	}

	// 5. initiate SingletonEntity table :
	snappyDbEnhancer.initSingletonEntityTable(sessionFactory);

	final TgSnappyApplicationModel applicationModel = new TgSnappyApplicationModel("target/classes",
		"ua.com.fielden.platform.example", "RMA module(platf.example)", injector);

	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(ApplicationModel.MAIN_CAPTION + " - "+  applicationModel.getDomainName());
		frame.add(new TgSnappyApplicationPanel(applicationModel, new BlockingIndefiniteProgressPane("", frame), frame));
		frame.pack();
		frame.setSize(1200, 768);
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }
	});
    }
}
