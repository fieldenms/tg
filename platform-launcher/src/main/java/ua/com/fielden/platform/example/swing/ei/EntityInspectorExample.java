package ua.com.fielden.platform.example.swing.ei;

import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.ei.CrudEntityInspectorModel;
import ua.com.fielden.platform.swing.ei.CrudEntityInspectorModel.IAfterActions;
import ua.com.fielden.platform.swing.ei.LightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This example demonstrates how a custom entity inspector can be build using
 * TG.
 *
 * @author 01es
 *
 */
public class EntityInspectorExample extends AbstractUiApplication {

    private CrudEntityInspectorModel<InspectedEntity> eiModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
	// look and feel
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	/////////////////////////////////////////////////
	// DB connectivity and Hibernate configuration //
	/////////////////////////////////////////////////
	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate4example.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = Guice.createInjector(hibernateModule, new EntityModule());
	final EntityFactory entityFactory = new EntityFactory(injector);
	interceptor.setFactory(entityFactory);

	try {
	    final Transaction trans = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
	    final Connection connection = hibernateUtil.getSessionFactory().getCurrentSession().connection();
	    System.out.println("Executing db creation/population script");
	    connection.createStatement().execute("DROP ALL OBJECTS");
	    connection.createStatement().execute("RUNSCRIPT FROM 'src/main/resources/db/example.ddl'");
	    trans.commit();
	    connection.close();
	} catch (final Exception e) {
	    System.err.println("Db initialisation failed: " + e.getMessage());
	    e.printStackTrace();
	}

	/////////////////////////////////////
	// Entity Inspector model creation //
	/////////////////////////////////////
	//final IValueMatcherFactory valueMatcherFactory = new ValueMatcherFactory(hibernateUtil.getSessionFactory());
	final IPropertyBinder propertyBinder = new LightweightPropertyBinder(injector.getInstance(IValueMatcherFactory.class), null);
	final InspectedEntityDao dao = injector.getInstance(InspectedEntityDao.class);

	final InspectedEntity entity = dao.findByKey("VALUE1") == null ? entityFactory.newEntity(InspectedEntity.class, "VALUE-X", "entity description") : dao.findByKey("VALUE1");
	setMetaPropertyProperties(entity);

	final CrudEntityInspectorModel<InspectedEntity> eiModel = new CrudEntityInspectorModel<InspectedEntity>(entity, propertyBinder, dao, new IAfterActions() {
	    @Override
	    public void afterSave() {
	    }

	    @Override
	    public void afterCancel() {
	    }

	    @Override
	    public void afterDelete() {
	    }
	});

    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
	////////////////////////////////////////////
	// Add UI controls to show the properties //
	////////////////////////////////////////////
	final JPanel propertyPanel = new JPanel(new MigLayout("fill", "[:100:][:200:]", "[c]"));
	for (final IPropertyEditor editor : eiModel.getEditors().values()) {
	    final String labelAlignment = editor.getEditor() instanceof JScrollPane ? "align left top" : "";
	    final String editorAlignment = editor.getEditor() instanceof JScrollPane ? "grow, wrap" : "growx, wrap";

	    propertyPanel.add(editor.getLabel(), labelAlignment);
	    propertyPanel.add(editor.getEditor(), editorAlignment);
	}
	///////////////////////////////
	// Add EI action UI controls //
	///////////////////////////////
	final JPanel actionPanel = new JPanel(new MigLayout("fill", "[][]push[]"));
	actionPanel.add(new JButton(eiModel.getSave()));
	actionPanel.add(new JButton(eiModel.getCancel()));
	actionPanel.add(new JButton(eiModel.getDelete()));
	////////////////////////////
	// Combine the two panels //
	////////////////////////////
	final JPanel mainPanel = new JPanel(new MigLayout("fill", "[]", "[][]"));
	mainPanel.add(propertyPanel, "grow, wrap");
	mainPanel.add(actionPanel, "growx");
	SimpleLauncher.show("Entity Inspector Example", mainPanel);
    }

    private void setMetaPropertyProperties(final InspectedEntity entity) {
	final MetaProperty key = entity.getProperty("key");
	key.setType(String.class);
	key.setTitle("Entity Key");
	key.setDesc("Key property.");

	final MetaProperty desc = entity.getProperty("desc");
	desc.setTitle("Desc");
	desc.setDesc("Entity description.");

	final MetaProperty intProperty = entity.getProperty("intProperty");
	intProperty.setTitle("Integer Property");
	intProperty.setDesc("Show off ability to edit integer properties.");

	final MetaProperty decimalProperty = entity.getProperty("decimalProperty");
	decimalProperty.setTitle("Decimal Property");
	decimalProperty.setDesc("Show off ability to edit decimal (BigDecimal) properties.");

	final MetaProperty moneyProperty = entity.getProperty("moneyProperty");
	moneyProperty.setTitle("Money Property");
	moneyProperty.setDesc("Show off ability to edit money properties.");

	final MetaProperty dateProperty = entity.getProperty("dateProperty");
	dateProperty.setTitle("Date Property");
	dateProperty.setDesc("Show off ability to edit date properties.");

	final MetaProperty booleanProperty = entity.getProperty("booleanProperty");
	booleanProperty.setTitle("Boolean Property");
	booleanProperty.setDesc("Show off ability to edit boolean properties.");

	final MetaProperty entityPropertyOne = entity.getProperty("entityPropertyOne");
	entityPropertyOne.setTitle("Entity Property One");
	entityPropertyOne.setDesc("Show off ability to edit properties of an entity type.");

	final MetaProperty entityPropertyTwo = entity.getProperty("entityPropertyTwo");
	entityPropertyTwo.setTitle("Entity Property Two");
	entityPropertyTwo.setDesc("Show off ability to edit properties of the exact entity type, but with different autocompletion logic.");

	final MetaProperty collectionalProperty = entity.getProperty("collectionalProperty");
	collectionalProperty.setTitle("Collectionl Property");
	collectionalProperty.setDesc("Editing of collectional properties is in progress.");
    }

    public static void main(final String[] args) {
	new EntityInspectorExample().launch(args);
    }

}
