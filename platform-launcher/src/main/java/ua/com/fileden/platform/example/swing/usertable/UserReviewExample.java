package ua.com.fileden.platform.example.swing.usertable;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.swing.usertable.UserReview;
import ua.com.fielden.platform.swing.usertable.UserReviewModel;
import ua.com.fielden.platform.swing.usertable.UserReviewTable;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * The example application that illustrates the usage of the {@link UserReviewTable}
 *
 * @author TG Team
 *
 */
public class UserReviewExample extends AbstractUiApplication {

    private UserReviewModel userReviewModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate4userexample.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = Guice.createInjector(hibernateModule, new EntityModule());
	final EntityFactory entityFactory = new EntityFactory(injector);
	interceptor.setFactory(entityFactory);

	System.out.println("Populating example database");
	try {
	    final Transaction tr = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
	    final Connection connection = hibernateUtil.getSessionFactory().getCurrentSession().connection();
	    final ResultSet rsTables = connection.getMetaData().getTables(null, "PUBLIC", "PERSON%", new String[] { "TABLE" });
	    if (rsTables.next()) {
		rsTables.close();
		System.out.println("Skipping db creation -- already exists");
	    } else {
		rsTables.close();
		System.out.println("Executing db creation/population script");
		connection.createStatement().execute("RUNSCRIPT FROM 'src/main/resources/userexample.ddl'");
	    }

	    tr.commit();
	    hibernateUtil.getSessionFactory().getCurrentSession().close();
	    connection.close();
	} catch (final Exception e) {
	    System.err.println("Db initialisation failed: " + e.getMessage());
	    e.printStackTrace();
	}

	final IUserController userLogic = injector.getInstance(IUserController.class);
	userReviewModel = new UserReviewModel(userLogic);
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final UserReview userReview = new UserReview(userReviewModel);
	userReview.setPreferredSize(new Dimension(400, 300));
	SimpleLauncher.show("User review example", userReview);
    }

    @Override
    protected void afterUiExposure(final String[] args, final SplashController splashController) throws Throwable {
    }

    public static void main(final String[] args) {
	new UserReviewExample().launch(args);
    }

}
