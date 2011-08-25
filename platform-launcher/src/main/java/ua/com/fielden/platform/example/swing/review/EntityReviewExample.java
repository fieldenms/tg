/**
 *
 */
package ua.com.fielden.platform.example.swing.review;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.WheelsetClass;
import ua.com.fielden.platform.example.entities.WheelsetQueryCriteria;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReview;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.utils.IconWrapper;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.utils.ResourceLoader;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Example application for the EntityReview and entityReviewModel.
 *
 * @author Yura, Oleh
 */
public class EntityReviewExample extends AbstractUiApplication {

    private EntityReviewModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> rotableReviewModel;

    private final ActionPanelBuilder actionBuilder = new ActionPanelBuilder();
    {
	actionBuilder.addButton(new Command<Void>("") {
	    {
		putValue(Action.LARGE_ICON_KEY, new IconWrapper(ResourceLoader.getIcon("images/alarmd.png")));
		putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/alarmd.png"));
		putValue(Action.SHORT_DESCRIPTION, "alarmd");
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		System.out.println("you press first button");
		return null;
	    }

	});
	actionBuilder.addSeparator();
	final List<ActionChanger<Void>> actions = new ArrayList<ActionChanger<Void>>() {
	    {
		add(new ActionChanger<Void>("wizard") {
		    {
			putValue(Action.LARGE_ICON_KEY, new IconWrapper(ResourceLoader.getIcon("images/wizard.png")));
			putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/wizard.png"));
			putValue(Action.SHORT_DESCRIPTION, "wizard");
		    }

		    @Override
		    protected Void action(final ActionEvent e) throws Exception {
			System.out.println("first button of action changer");
			return null;
		    }

		});
		add(new ActionChanger<Void>("eraser") {
		    {
			putValue(Action.LARGE_ICON_KEY, new IconWrapper(ResourceLoader.getIcon("images/eraser.png")));
			putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/eraser.png"));
			putValue(Action.SHORT_DESCRIPTION, "eraser");
		    }

		    @Override
		    protected Void action(final ActionEvent e) throws Exception {
			System.out.println("second button of action changer");
			return null;
		    }

		});
	    }
	};
	actionBuilder.addActionChangeButton(actions);
    }


    @Override
    protected void afterUiExposure(final String[] args, final SplashController splashController) throws Exception {
    }

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
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
	final Injector injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
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

	final IValueMatcher<Wheelset> rotableValueMatcher = new HibernateValueMatcher<Wheelset>(Wheelset.class, "key", hibernateUtil.getSessionFactory());
	final IValueMatcher<WheelsetClass> rotableClassValueMatcher = new HibernateValueMatcher<WheelsetClass>(WheelsetClass.class, "key", hibernateUtil.getSessionFactory());
	final EntityQueryCriteria<Wheelset, IWheelsetDao> criteria = entityFactory.newByKey(WheelsetQueryCriteria.class, "criteria").setValueMatcher("rotables", rotableValueMatcher)
	.setValueMatcher("rotableClasses", rotableClassValueMatcher);
	criteria.getProperty("rotables").setTitle("rotable no");
	criteria.getProperty("rotableClasses").setTitle("rotable class no");
	criteria.getProperty("hideBogies").setTitle("hide bogies");
	criteria.getProperty("hideWheelsets").setTitle("hide wheelsets");
	criteria.getProperty("compatibleOnly").setTitle("compatible only");

	final PropertyTableModelBuilder<Wheelset> rotableTableModelBuilder = new PropertyTableModelBuilder<Wheelset>(Wheelset.class)//
	.addReadonly("key", 100)//
	.addReadonly("desc", 240)//
	.addReadonly("status", 60)//
	.addReadonly("rotableClass.desc", 100);

	rotableReviewModel = new EntityReviewModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>>(criteria, rotableTableModelBuilder, null, null) {
	    @Override
	    public ActionPanelBuilder getActionPanelBuilder() {
		return actionBuilder;
	    }
	};
    }

    @SuppressWarnings("serial")
    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {

	final EntityReview<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> rotableReview = new EntityReview<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>>(rotableReviewModel, true) {
	    @Override
	    public String getInfo() {
		return "Wheelset review";
	    }

	};
	SimpleLauncher.show("pagination example", rotableReview);
    }

    public static void main(final String[] args) {
	new EntityReviewExample().launch(args);
    }

}
