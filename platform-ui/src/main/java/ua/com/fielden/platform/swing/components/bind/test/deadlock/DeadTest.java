package ua.com.fielden.platform.swing.components.bind.test.deadlock;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.xml.DOMConfigurator;
import org.jdesktop.swingx.plaf.DatePickerAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.components.bind.test.EntityModuleWithDomainValidatorsForTesting;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.google.inject.Injector;

public class DeadTest {

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
        // configure Log4j
        DOMConfigurator.configure("src/main/resources/log4j.xml");

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
        final EntityModuleWithDomainValidatorsForTesting entityModule = new EntityModuleWithDomainValidatorsForTesting();

        entityModule.getDomainValidationConfig().setValidator(DeadEntity.class, DeadEntity.PROPERTY_VEHICLE, new HappyValidator()).setValidator(DeadEntity.class, DeadEntity.PROPERTY_ODOMETER, new HappyValidator()).setValidator(DeadEntity.class, DeadEntity.PROPERTY_ACT_ST, new HappyValidator());

        //	entityModule.getDomainMetaPropertyConfig()
        //		.setDefiner(DeadEntity.class, DeadEntity.PROPERTY_VEHICLE, new IMetaPropertyDefiner() {
        //
        //		    @Override
        //		    public void define(final MetaProperty property, final Object entityPropertyValue) {
        //			try {
        //			    Thread.sleep(10000);
        //			} catch (final InterruptedException e) {
        //			    e.printStackTrace();
        //			}
        //			((DeadEntity)property.getEntity()).setOdometerReading(23);
        //		    }
        //		});

        // launching test
        final Injector injector = new ApplicationInjectorFactory().add(entityModule).getInjector();
        final DeadEntityView view = new DeadEntityView(injector.getInstance(EntityFactory.class));
        SimpleLauncher.show("Test", view.buildPanel());
        //		SimpleLauncher.show("Test for synchronization", null, view.buildPanel());
        //	SimpleLauncher.show("DatePickers bad performance test", null, view.buildDatePickers(view.getEntity()));
    }
}