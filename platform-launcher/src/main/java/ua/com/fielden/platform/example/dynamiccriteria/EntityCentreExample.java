package ua.com.fielden.platform.example.dynamiccriteria;

import java.awt.Dimension;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.example.ioc.ExampleRmaHibernateModule;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;
import ua.com.fielden.platform.expression.entity.validator.ExpressionValidator;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class EntityCentreExample extends AbstractUiApplication {

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

	super.beforeUiExposure(args, splashController);
    }

    private void configValidation(final DomainValidationConfig dvc){
	dvc.setValidator(ExpressionEntity.class, "expression", new ExpressionValidator());
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final CentreConfigurationModel<Vehicle> model = new CentreConfigurationModel<Vehicle>(Vehicle.class, null, entityFactory);
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	final CentreConfigurationView<Vehicle> centre = new CentreConfigurationView<Vehicle>(model, progressLayer);
	progressLayer.setView(centre);
	centre.open();
	centre.setPreferredSize(new Dimension(640,800));
	SimpleLauncher.show("Expression editor example", progressLayer);
    }

    public static void main(final String[] args) {
	new EntityCentreExample().launch(args);
    }

}
