/**
 *
 */
package ua.com.fielden.platform.example.swing.egi.performance;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.joda.time.Duration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.components.bind.test.EntityModuleWithDomainValidatorsForTesting;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * EGI performance example. The entity for EGI - ExampleEntity.
 * 
 * @author TG Team
 */
public class EgiWithExampleEntityExample extends AbstractUiApplication {

    private PropertyTableModel<ExampleEntity> exampleEntityTableModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
        SwingUtilitiesEx.installNimbusLnFifPossible();
        com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
        LookAndFeelFactory.installJideExtension();

        final EntityModuleWithDomainValidatorsForTesting module = new EntityModuleWithDomainValidatorsForTesting();

        final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
        final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

        final List<ExampleEntity> exampleEntities = createExampleEntities(entityFactory);

        exampleEntityTableModel = new PropertyTableModelBuilder<ExampleEntity>(ExampleEntity.class)//
        .addReadonly("", 80)//
        .addReadonly("desc", 120)//
        .addReadonly("initDate", 120)//
        .addReadonly("stringProperty", 120)//
        .addReadonly("active", 80)//
        .addReadonly("numValue", 80)//
        .addReadonly("nestedEntity", 80)//
        .addReadonly("nestedEntity.desc", 120)//
        .addReadonly("nestedEntity.initDate", 120)//
        .addReadonly("nestedEntity.stringProperty", 120)//
        .addReadonly("nestedEntity.active", 80)//
        .addReadonly("nestedEntity.numValue", 80)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity", 80)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity.desc", 120)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity.initDate", 120)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity.stringProperty", 120)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity.active", 80)//
        .addReadonly("nestedEntity.nestedEntity.nestedEntity.numValue", 80)//
        .build(exampleEntities);
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final EntityGridInspector<ExampleEntity> exampleEntityGridInspector = new EntityGridInspector<ExampleEntity>(exampleEntityTableModel) {

            private static final long serialVersionUID = -582615854538245301L;

            @Override
            protected void paintComponent(final Graphics g) {
                final Date before = new Date();
                super.paintComponent(g);
                final Date after = new Date();
                final Duration duration = new Duration(before.getTime(), after.getTime());
                System.out.println(duration.getMillis());
            }
        };

        final JPanel topPanel = new JPanel(new MigLayout("fill", "[:800:]", "[]0[]"));

        topPanel.add(new JLabel("Example entity perforamce example"), "growx, wrap");
        topPanel.add(new JScrollPane(exampleEntityGridInspector), "grow");
        SimpleLauncher.show("Entity Grid Inspector Performance Example With Example Entity", topPanel);
    }

    private List<ExampleEntity> createExampleEntities(final EntityFactory entityFactory) {
        final List<ExampleEntity> entities = new ArrayList<ExampleEntity>();

        for (int entityCount = 0; entityCount < 10000; entityCount++) {
            final ExampleEntity ee = entityFactory.newByKey(ExampleEntity.class, "test").setActive(true).setDesc("test description").setInitDate(new Date()).//
            setNumValue(Integer.valueOf(35)).setStringProperty("string property value");
            ee.setNestedEntity(ee);
            entities.add(ee);
        }
        return entities;
    }

    public static void main(final String[] args) {
        new EgiWithExampleEntityExample().launch(args);
    }

}