package ua.com.fielden.platform.example.swing.expressioneditor;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.expression.editor.ExpressionEditorModel;
import ua.com.fielden.platform.expression.editor.ExpressionEditorView;
import ua.com.fielden.platform.expression.editor.IPropertyProvider;
import ua.com.fielden.platform.expression.editor.PropertyProvider;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;
import ua.com.fielden.platform.expression.entity.validator.ExpressionValidator;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.dynamicreportstree.CriteriaTree;
import ua.com.fielden.platform.swing.dynamicreportstree.TreePanel;
import ua.com.fielden.platform.swing.ei.LightweightPropertyBinder;
import ua.com.fielden.platform.swing.review.DefaultDynamicCriteriaPropertyFilter;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class ExpressionEditorExample extends AbstractUiApplication {

    private EntityFactory entityFactory;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	//initiating entity factory
	final ProxyInterceptor interceptor = new ProxyInterceptor();
	final HibernateUtil hibernateUtil = new HibernateUtil(interceptor, new Configuration().configure("hibernate4example.cfg.xml"));
	final ExampleRmaHibernateModule hibernateModule = new ExampleRmaHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()));
	final Injector injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	entityFactory = injector.getInstance(EntityFactory.class);
	interceptor.setFactory(entityFactory);
	configValidation(injector.getInstance(DomainValidationConfig.class));
    }

    private void configValidation(final DomainValidationConfig dvc){
	dvc.setValidator(ExpressionEntity.class, "expression", new ExpressionValidator());
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]", "[][fill, grow]"));
	final ExpressionEntity expressionEntity = entityFactory.newEntity(ExpressionEntity.class, 0L);
	expressionEntity.setEntityClass(Vehicle.class);
	final IPropertyProvider propertyProvider = new PropertyProvider();
	final ExpressionEditorView expressionView = new ExpressionEditorView(new ExpressionEditorModel(expressionEntity,new LightweightPropertyBinder<ExpressionEntity>(null, null)), propertyProvider);
	final CriteriaTreeModel treeModel = createTreeModel(Vehicle.class);
	final CriteriaTree tree = new CriteriaTree(treeModel);
	tree.getSelectionModel().addTreeSelectionListener(createSelectionListener(propertyProvider, treeModel));
	final TreePanel treePanel = new TreePanel(tree);
	panel.add(expressionView,"wrap");
	panel.add(treePanel);
	panel.setPreferredSize(new Dimension(640,800));
	SimpleLauncher.show("Expression editor example", panel);
    }

    private TreeSelectionListener createSelectionListener(final IPropertyProvider propertyProvider, final CriteriaTreeModel treeModel) {
	return new TreeSelectionListener() {

	    @Override
	    public void valueChanged(final TreeSelectionEvent e) {
		propertyProvider.selectProperty(treeModel.getPropertyNameFor((DefaultMutableTreeNode)e.getPath().getLastPathComponent()));
	    }
	};
    }

    private CriteriaTreeModel createTreeModel(final Class<? extends AbstractEntity> entityClass){
	return new CriteriaTreeModel(entityClass, new DefaultDynamicCriteriaPropertyFilter(), null);
    }

    public static void main(final String[] args) {
	new ExpressionEditorExample().launch(args);
    }



}
