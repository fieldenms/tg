package ua.com.fielden.platform.swing.components.bind.test;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jdesktop.swingx.plaf.DatePickerAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.google.inject.Guice;

public class Test {

    static {
	// this patch need for correct usage JXDatePicker in "Nimbus" L&F. When support for "Nimbus" will be provided by default -> it should be removed
	LookAndFeelAddons.contribute(new DatePickerAddon() {
	    @Override
	    protected void addNimbusDefaults(final LookAndFeelAddons addon, final DefaultsList defaults) {
		super.addNimbusDefaults(addon, defaults);
		//TODO: don't use an image here, Nimbus uses Painters for everything
		// => e.g. reuse the com.sun.java.swing.plaf.nimbus.ComboBoxComboBoxArrowButtonPainter
		// (at the moment the OS-X icon looks most similar, it's much better
		// than no icon...)
		defaults.add("JXDatePicker.arrowIcon", LookAndFeel.makeIcon(DatePickerAddon.class, "macosx/resources/combo-osx.png"));
		// no borders, this is done by Nimbus
		defaults.add("JXDatePicker.border", null);
	    }
	});
    }

    public static void main(final String[] args) throws Exception {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	// creating and initialising entity module
	final EntityModuleWithDomainValidatorsForTesting entityModule = new CommonEntityModuleWithDomainValidatorsForTesting();
	final EntityValidatorFactory validatorFactory = new EntityValidatorFactory();

	entityModule.getDomainValidationConfig().setValidator(Entity.class, Entity.PROPERTY_STRATEGY, validatorFactory.new EntityStrategyValidator()).setValidator(Entity.class, Entity.PROPERTY_BOOL, validatorFactory.new EntityBoolValidator()).setValidator(Entity.class, Entity.PROPERTY_DEMO_ENTITY, validatorFactory.new EntityDemoEntityValidator()).setValidator(Entity.class, Entity.PROPERTY_STRING, validatorFactory.new EntityStringValidator()).setValidator(Entity.class, Entity.PROPERTY_NUMBER, validatorFactory.new EntityNumberValidator()).setValidator(Entity.class, Entity.PROPERTY_BIG_DECIMAL, validatorFactory.new EntityBigDecimalValidator()).setValidator(Entity.class, Entity.PROPERTY_MONEY, validatorFactory.new EntityMoneyValidator()).setValidator(Entity.class, Entity.PROPERTY_LIST, validatorFactory.new EntityListValidator()).setValidator(Entity.class, Entity.PROPERTY_DATE, validatorFactory.new EntityDateValidator()).setValidator(Entity.class, Entity.PROPERTY_BICYCLES, validatorFactory.new EntityBicyclesValidator());

	// launching test
	final EntityView view = new EntityView(new EntityFactory(Guice.createInjector(entityModule)));
	SimpleLauncher.show("Test", view.buildPanel());
	//		SimpleLauncher.show("Test for synchronization", null, view.buildPanel());
	//	SimpleLauncher.show("DatePickers bad performance test", null, view.buildDatePickers(view.getEntity()));
    }
}