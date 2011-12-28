package ua.com.fielden.platform.example.dynamiccriteria;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.util.Properties;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.MultipleAnalysisEntityCentreConfigurationView;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

import com.jidesoft.plaf.LookAndFeelFactory;

public class EntityCentreExample extends AbstractUiApplication {

    private EntityFactory entityFactory;
    private ICriteriaGenerator criteriaGenerator;
    private IGlobalDomainTreeManager gdtm;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	//TODO please review entity_centre_example.properties and correct appropriate properties.
	final String configFileName = args.length == 1 ? args[0] : "src/main/resources/entity_centre_example.properties";
	final FileInputStream in = new FileInputStream(configFileName);
	final Properties props = IDomainDrivenTestCaseConfiguration.hbc;
	props.load(in);
	in.close();

	// override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
	props.put("hibernate.show_sql", "false");
	props.put("hibernate.format_sql", "true");
	props.put("hibernate.hbm2ddl.auto", "create");

	final IDomainDrivenTestCaseConfiguration config = new EntityCentreDataPopulationConfiguration();

	final PopulateDbForEntityCentreExample popDb = new PopulateDbForEntityCentreExample(config);
	popDb.createAndPopulate();

	entityFactory = config.getEntityFactory();
	criteriaGenerator = config.getInstance(ICriteriaGenerator.class);
	gdtm = config.getInstance(IGlobalDomainTreeManager.class);

	super.beforeUiExposure(args, splashController);
    }

    //    private void configValidation(final DomainValidationConfig dvc){
    //		dvc.setValidator(ExpressionEntity.class, "expression", new ExpressionValidator());
    //    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final CentreConfigurationModel<SimpleECEEntity> model = new CentreConfigurationModel<SimpleECEEntity>(SimpleECEEntity.class, null, gdtm, entityFactory, criteriaGenerator);
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	final MultipleAnalysisEntityCentreConfigurationView<SimpleECEEntity> centre = new MultipleAnalysisEntityCentreConfigurationView<SimpleECEEntity>(model, progressLayer);
	progressLayer.setView(centre);
	centre.open();
	centre.setPreferredSize(new Dimension(800,800));
	SimpleLauncher.show("Expression editor example", progressLayer);
    }

    public static void main(final String[] args) {
	new EntityCentreExample().launch(args);
    }

}
