package ua.com.fielden.platform.example.unionentity;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.example.entities.FWorkOrder;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.factory.DefaultEntityReviewFactory;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class UnionEntityExample extends AbstractUiApplication {

    private DynamicCriteriaModelBuilder builderModel;
    private IEntityDao dao;
    private IValueMatcherFactory vmf;
    private IEntityReviewFactory entityReviewModelFactory;

    public UnionEntityExample() {

    }

    @Override
    @SuppressWarnings("unchecked")
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
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = Guice.createInjector(hibernateModule, new EntityModule());
	final EntityFactory entityFactory = new EntityFactory(injector);
	interceptor.setFactory(entityFactory);

	System.out.println("Populating example database");
	try {
	    hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
	    final Connection connection = hibernateUtil.getSessionFactory().getCurrentSession().connection();
	    final ResultSet rsTables = connection.getMetaData().getTables(null, "PUBLIC", "RMA%", new String[] { "TABLE" });
	    if (rsTables.next()) {
		rsTables.close();
		System.out.println("Skipping db creation -- already exists");
	    } else {
		rsTables.close();
		System.out.println("Executing db creation/population script");
		connection.createStatement().execute("RUNSCRIPT FROM 'src/main/resources/script.sql'");
	    }

	    connection.close();
	} catch (final Exception e) {
	    System.err.println("Db initialisation failed: " + e.getMessage());
	    e.printStackTrace();
	} finally {
	    if (hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive()) {
		hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	    }
	}

	final Class entityClass = FWorkOrder.class;
	dao = injector.getInstance(IDaoFactory.class).newDao(entityClass);
	vmf = injector.getInstance(IValueMatcherFactory.class);
	entityReviewModelFactory = new DefaultEntityReviewFactory(null, injector.getInstance(ILocatorConfigurationController.class));
	builderModel = new DynamicCriteriaModelBuilder(entityFactory, vmf, injector.getInstance(IDaoFactory.class), dao, injector.getInstance(IEntityAggregatesDao.class), entityClass, "resources/criterias/testUnion.dcf", injector.getInstance(ICenterConfigurationController.class), null, entityReviewModelFactory);

    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final BaseFrame frame = new BaseFrame("test");
	final ActionChangerBuilder actionChangerBuilder = new ActionChangerBuilder();
	actionChangerBuilder.setAction(builderModel.createSaveAction());
	builderModel.init(frame.getContentPane(), actionChangerBuilder, null, false, true);
	SimpleLauncher.show("Dynamic Criteria Example", frame, null);
    }

    public static void main(final String args[]) {
	new UnionEntityExample().launch(args);
    }

}
