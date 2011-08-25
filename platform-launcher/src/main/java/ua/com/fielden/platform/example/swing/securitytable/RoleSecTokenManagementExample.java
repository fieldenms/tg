package ua.com.fielden.platform.example.swing.securitytable;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.swing.treetable.SecurityTokenViewer;
import ua.com.fielden.platform.swing.treetable.SecurityTokenViewerModel;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Application that creates frame with SecurityModelViewer on it. Implemented for testing purpose only
 *
 * @author TG Team
 *
 */
public class RoleSecTokenManagementExample extends AbstractUiApplication {

    private SecurityTokenViewerModel securityModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate4userexample.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
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

	final SecurityTokenProvider tokenProvider = new SecurityTokenProvider("target/classes", "ua.com.fielden.platform.example.swing.securitytable");
	securityModel = new SecurityTokenViewerModel(injector.getInstance(ISecurityTokenController.class), tokenProvider);
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final JFrame frame = new JFrame("Token Viewer");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	final SecurityTokenViewer securityReview = new SecurityTokenViewer(securityModel);
	securityReview.setPreferredSize(new Dimension(400, 300));
	frame.add(securityReview);
	frame.pack();
	RefineryUtilities.centerFrameOnScreen(frame);
	frame.setVisible(true);
    }

    @Override
    protected void afterUiExposure(final String[] args, final SplashController splashController) throws Throwable {
    }

    public static void main(final String[] args) {
	new RoleSecTokenManagementExample().launch(args);
    }

}
