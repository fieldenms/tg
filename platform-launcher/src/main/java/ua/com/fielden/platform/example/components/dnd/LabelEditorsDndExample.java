package ua.com.fielden.platform.example.components.dnd;

import static ua.com.fielden.platform.swing.utils.SimpleLauncher.show;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.IBogieDao;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.review.CriteriaDndPanel;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class LabelEditorsDndExample extends AbstractUiApplication {

    private Injector injector;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	super.beforeUiExposure(args, splashController);
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	injector = Guice.createInjector(hibernateModule, new EntityModule());
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
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final Bogie bogie = injector.getInstance(IBogieDao.class).firstPage(1).data().get(0);

	final CriteriaDndPanel panel = new CriteriaDndPanel(new MigLayout("fill, insets 0", "[][grow][][grow]", "[30][30][30]"));

	// panel.addDraggable(new JLabel("top-left-label"), new JTextField("top-left-editor"), new CriteriaDndPanel.Position(0, 0, "", "grow"));
	// panel.addDraggable(new JLabel("bottom-left-label"), new JTextField("bottom-left-editor"), new CriteriaDndPanel.Position(0, 1, "", "grow"));
	// panel.addDraggable(new JLabel(), new JCheckBox("left-check-box"), new CriteriaDndPanel.Position(0, 2, "", "grow"));
	// panel.addDraggable(new JLabel("top-right-label"), new JTextField("top-right-editor"), new CriteriaDndPanel.Position(2, 0, "", "grow"));

	final BoundedValidationLayer<JTextField> textField = ComponentFactory.createStringTextField(bogie, "desc", true, "bogie description", EditorCase.UPPER_CASE);
	// panel.addDraggable(new JLabel("bottom-right-label"), textField, new CriteriaDndPanel.Position(2, 1, "", "grow"));
	// panel.addDraggable(new JLabel(), new JCheckBox("right-check-box"), new CriteriaDndPanel.Position(2, 2, "", "grow"));

	final TaskPanel topPanel = new TaskPanel(new MigLayout("fill"));
	topPanel.add(panel, "grow, wrap");
	topPanel.add(new JButton(panel.getChangeLayoutAction()), "growx, split 2");
	topPanel.add(new JButton(panel.getBackToNormalAction()), "growx");

	show("Components drag-n-drop example", topPanel);
    }

    public static void main(final String[] args) {
	new LabelEditorsDndExample().launch(args);
    }

}
