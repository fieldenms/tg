package ua.com.fielden.platform.example.swing.egi.performance;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import ua.com.fielden.platform.swing.egi.coloring.EgiColoringScheme;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class EgiWithExampleEntitySimplifiedExample extends AbstractUiApplication {

    private PropertyTableModel<ExampleEntitySimplified> exampleEntityTableModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
        com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
        LookAndFeelFactory.installJideExtension();

        final EntityModuleWithDomainValidatorsForTesting module = new EntityModuleWithDomainValidatorsForTesting();

        final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
        final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

        final List<ExampleEntitySimplified> exampleEntities = createExampleEntities(entityFactory);

        final List<ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>> columns = new ArrayList<ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>>();
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("", String.class, ExampleEntitySimplified.metaData.get("").getKey(), 80, ExampleEntitySimplified.metaData.get("").getValue(), createToolTipGetter("desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("desc", String.class, ExampleEntitySimplified.metaData.get("desc").getKey(), 120, ExampleEntitySimplified.metaData.get("desc").getValue(), createToolTipGetter("desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("initDate", Date.class, ExampleEntitySimplified.metaData.get("initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("initDate").getValue(), createToolTipGetter("initDate")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("stringProperty", String.class, ExampleEntitySimplified.metaData.get("stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("stringProperty").getValue(), createToolTipGetter("stringProperty")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("active", Boolean.class, ExampleEntitySimplified.metaData.get("active").getKey(), 80, ExampleEntitySimplified.metaData.get("active").getValue(), createToolTipGetter("active")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("numValue", Integer.class, ExampleEntitySimplified.metaData.get("numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("numValue").getValue(), createToolTipGetter("numValue")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity", String.class, ExampleEntitySimplified.metaData.get("nestedEntity").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity").getValue(), createToolTipGetter("nestedEntity.desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.desc", String.class, ExampleEntitySimplified.metaData.get("nestedEntity.desc").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.desc").getValue(), createToolTipGetter("nestedEntity.desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.initDate", Date.class, ExampleEntitySimplified.metaData.get("nestedEntity.initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.initDate").getValue(), createToolTipGetter("nestedEntity.initDate")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.stringProperty", String.class, ExampleEntitySimplified.metaData.get("nestedEntity.stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.stringProperty").getValue(), createToolTipGetter("nestedEntity.stringProperty")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.active", Boolean.class, ExampleEntitySimplified.metaData.get("nestedEntity.active").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.active").getValue(), createToolTipGetter("nestedEntity.active")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.numValue", Integer.class, ExampleEntitySimplified.metaData.get("nestedEntity.numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.numValue").getValue(), createToolTipGetter("nestedEntity.numValue")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity", String.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity.desc", String.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.desc").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.desc").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.desc")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity.initDate", Date.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.initDate").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.initDate")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity.stringProperty", String.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.stringProperty").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.stringProperty")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity.active", Boolean.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.active").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.active").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.active")));
        columns.add(new ReadonlyPropertyColumnMappingsForExample<ExampleEntitySimplified>("nestedEntity.nestedEntity.nestedEntity.numValue", Integer.class, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.numValue").getValue(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.numValue")));

        exampleEntityTableModel = new PropertyTableModel<ExampleEntitySimplified>(exampleEntities, columns, null, new EgiColoringScheme<ExampleEntitySimplified>(null, new HashMap<String, IColouringScheme<ExampleEntitySimplified>>()) {

            @Override
            public Color getBgColour(final ExampleEntitySimplified entity, final String propertyName) {
                return null;
            }

            @Override
            public Color getFgColour(final ExampleEntitySimplified entity, final String propertyName) {
                return null;
            }
        });
        //exampleEntityTableModel = new PropertyTableModelBuilder<ExampleEntitySimplified>(ExampleEntitySimplified.class)//
        //.addReadonly("", ExampleEntitySimplified.metaData.get("").getKey(), 80, ExampleEntitySimplified.metaData.get("").getKey(), createToolTipGetter("desc"), null, null, null)//
        //.addReadonly("desc", ExampleEntitySimplified.metaData.get("desc").getKey(), 120, ExampleEntitySimplified.metaData.get("desc").getKey(), createToolTipGetter("desc"), null, null, null)//
        //.addReadonly("initDate", ExampleEntitySimplified.metaData.get("initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("initDate").getKey(), createToolTipGetter("initDate"), null, null, null)//
        //.addReadonly("stringProperty", ExampleEntitySimplified.metaData.get("stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("stringProperty").getKey(), createToolTipGetter("stringProperty"), null, null, null)//
        //.addReadonly("active", ExampleEntitySimplified.metaData.get("active").getKey(), 80, ExampleEntitySimplified.metaData.get("active").getKey(), createToolTipGetter("active"), null, null, null)//
        //.addReadonly("numValue", ExampleEntitySimplified.metaData.get("numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("numValue").getKey(), createToolTipGetter("numValue"), null, null, null)//
        //.addReadonly("nestedEntity", ExampleEntitySimplified.metaData.get("nestedEntity").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity").getKey(), createToolTipGetter("nestedEntity.desc"), null, null, null)//
        //.addReadonly("nestedEntity.desc", ExampleEntitySimplified.metaData.get("nestedEntity.desc").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.desc").getKey(), createToolTipGetter("nestedEntity.desc"), null, null, null)//
        //.addReadonly("nestedEntity.initDate", ExampleEntitySimplified.metaData.get("nestedEntity.initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.initDate").getKey(), createToolTipGetter("nestedEntity.initDate"), null, null, null)//
        //.addReadonly("nestedEntity.stringProperty", ExampleEntitySimplified.metaData.get("nestedEntity.stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.stringProperty").getKey(), createToolTipGetter("nestedEntity.stringProperty"), null, null, null)//
        //.addReadonly("nestedEntity.active", ExampleEntitySimplified.metaData.get("nestedEntity.active").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.active").getKey(), createToolTipGetter("nestedEntity.active"), null, null, null)//
        //.addReadonly("nestedEntity.numValue", ExampleEntitySimplified.metaData.get("nestedEntity.numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.numValue").getKey(), createToolTipGetter("nestedEntity.numValue"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.desc"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity.desc", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.desc").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.desc").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.desc"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity.initDate", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.initDate").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.initDate").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.initDate"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity.stringProperty", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.stringProperty").getKey(), 120, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.stringProperty").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.stringProperty"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity.active", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.active").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.active").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.active"), null, null, null)//
        //.addReadonly("nestedEntity.nestedEntity.nestedEntity.numValue", ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.numValue").getKey(), 80, ExampleEntitySimplified.metaData.get("nestedEntity.nestedEntity.nestedEntity.numValue").getKey(), createToolTipGetter("nestedEntity.nestedEntity.nestedEntity.numValue"), null, null, null)//
        //.build(exampleEntities);
    }

    private ITooltipGetter<ExampleEntitySimplified> createToolTipGetter(final String propertyName) {
        return new ITooltipGetter<ExampleEntitySimplified>() {

            @Override
            public String getTooltip(final ExampleEntitySimplified entity) {
                return entity.get(propertyName).toString();
            }
        };
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final EntityGridInspector<ExampleEntitySimplified> exampleEntityGridInspector = new EntityGridInspector<ExampleEntitySimplified>(exampleEntityTableModel) {

            private static final long serialVersionUID = -4048440934194288465L;

            @Override
            protected void paintComponent(final Graphics g) {
                final Date before = new Date();
                super.paintComponent(g);
                final Date after = new Date();
                final Duration duration = new Duration(before.getTime(), after.getTime());
                System.out.println(duration);
            }
        };

        final JPanel topPanel = new JPanel(new MigLayout("fill", "[:800:]", "[]0[]"));

        topPanel.add(new JLabel("Example entity simplified perforamce example"), "growx, wrap");
        topPanel.add(new JScrollPane(exampleEntityGridInspector), "grow");
        SimpleLauncher.show("Entity Grid Inspector Performance Example With Example Entity Simplified", topPanel);
    }

    private List<ExampleEntitySimplified> createExampleEntities(final EntityFactory entityFactory) {
        final List<ExampleEntitySimplified> entities = new ArrayList<ExampleEntitySimplified>();

        for (int entityCount = 0; entityCount < 10000; entityCount++) {
            final ExampleEntitySimplified ee = entityFactory.newByKey(ExampleEntitySimplified.class, null);
            entities.add(ee);
        }
        return entities;
    }

    public static void main(final String[] args) {
        new EgiWithExampleEntitySimplifiedExample().launch(args);
    }

}
