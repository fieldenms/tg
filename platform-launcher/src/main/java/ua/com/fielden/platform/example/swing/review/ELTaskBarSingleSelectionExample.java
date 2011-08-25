/**
 *
 */
package ua.com.fielden.platform.example.swing.review;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableClass;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.WheelsetQueryCriteria;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.EntityLocator;
import ua.com.fielden.platform.swing.review.EntityLocatorModel;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.ISelectionListener;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Example application for the EntityLocator with criteria panel placed into the task bar panel and single selection.
 *
 * @author Oleh
 */
public class ELTaskBarSingleSelectionExample {

    private static EntityLocatorModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> rotableReviewModel;

    @SuppressWarnings("deprecation")
    public static void main(final String[] args) {
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

	final IValueMatcher<Rotable> rotableValueMatcher = new HibernateValueMatcher<Rotable>(Rotable.class, "key", hibernateUtil.getSessionFactory());
	final IValueMatcher<RotableClass> rotableClassValueMatcher = new HibernateValueMatcher<RotableClass>(RotableClass.class, "key", hibernateUtil.getSessionFactory());
	final EntityQueryCriteria<Wheelset, IWheelsetDao> criteria = entityFactory.newByKey(WheelsetQueryCriteria.class, "criteria").setValueMatcher("rotables", rotableValueMatcher).setValueMatcher("rotableClasses", rotableClassValueMatcher);
	criteria.getProperty("rotables").setTitle("rotable no");
	criteria.getProperty("rotableClasses").setTitle("rotable class no");
	criteria.getProperty("hideBogies").setTitle("hide bogies");
	criteria.getProperty("hideWheelsets").setTitle("hide wheelsets");
	criteria.getProperty("compatibleOnly").setTitle("compatible only");

	final PropertyTableModelBuilder<Wheelset> rotableTableModelBuilder = new PropertyTableModelBuilder<Wheelset>(Wheelset.class).addReadonly("key", "No").addReadonly("desc", "Description").addReadonly("status", "Status").addReadonly("rotableClass", "Class");

	rotableReviewModel = new EntityLocatorModel<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>>(criteria, rotableTableModelBuilder, new ISelectionListener() {

	    @Override
	    public boolean isSelected(final AbstractEntity entityToCheck) {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public void performDeselect(final AbstractEntity selectedObject) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void performSelection(final AbstractEntity selectedObject) {
		final List<Rotable> rotables = (List<Rotable>) selectedObject;
		JOptionPane.showMessageDialog(null, rotables.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
		System.exit(0);
	    }

	    @Override
	    public void clearSelection() {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public boolean isMultiselection() {
		return false;
	    }

	    @Override
	    public boolean isSelectionEmpty() {
		return false;
	    }

	}, null);
	SwingUtilities.invokeLater(new Runnable() {

	    @SuppressWarnings("serial")
	    @Override
	    public void run() {
		final JFrame frame = new JFrame("Entity Locator Single Selection Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(640, 480));
		frame.setLayout(new MigLayout("fill, insets 0"));
		final EntityLocator<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>> rotableLocator = new EntityLocator<Wheelset, IWheelsetDao, EntityQueryCriteria<Wheelset, IWheelsetDao>>(rotableReviewModel, true);
		frame.add(rotableLocator, "grow");
		frame.pack();
		frame.setVisible(true);
	    }

	});
    }
}
