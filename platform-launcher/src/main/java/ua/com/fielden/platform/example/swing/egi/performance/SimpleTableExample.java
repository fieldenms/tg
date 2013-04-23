package ua.com.fielden.platform.example.swing.egi.performance;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import org.joda.time.Duration;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

public class SimpleTableExample extends AbstractUiApplication {

    private TableModelForTesting exampleTableModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	final List<ExampleEntitySimplified> exampleEntities = createExampleEntities();

	final List<String> columns = new ArrayList<String>();
	columns.add("");
	columns.add("desc");
	columns.add("initDate");
	columns.add("stringProperty");
	columns.add("active");
	columns.add("numValue");
	columns.add("nestedEntity");
	columns.add("nestedEntity.desc");
	columns.add("nestedEntity.initDate");
	columns.add("nestedEntity.stringProperty");
	columns.add("nestedEntity.active");
	columns.add("nestedEntity.numValue");
	columns.add("nestedEntity.nestedEntity.nestedEntity");
	columns.add("nestedEntity.nestedEntity.nestedEntity.desc");
	columns.add("nestedEntity.nestedEntity.nestedEntity.initDate");
	columns.add("nestedEntity.nestedEntity.nestedEntity.stringProperty");
	columns.add("nestedEntity.nestedEntity.nestedEntity.active");
	columns.add("nestedEntity.nestedEntity.nestedEntity.numValue");

	exampleTableModel = new TableModelForTesting(exampleEntities, columns);
    }


    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
	final JTable exampleEntityGridInspector = new JTable(exampleTableModel){

	    private static final long serialVersionUID = 8797128620455415545L;

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

	topPanel.add(new JLabel("Simple table perforamce example"), "growx, wrap");
	topPanel.add(new JScrollPane(exampleEntityGridInspector), "grow");
	SimpleLauncher.show("Simple table performance example.", topPanel);
    }

    private List<ExampleEntitySimplified> createExampleEntities() {
	final List<ExampleEntitySimplified> entities = new ArrayList<ExampleEntitySimplified>();

	for(int entityCount = 0; entityCount < 10000; entityCount++){
	    final ExampleEntitySimplified ee = new ExampleEntitySimplified();
	    entities.add(ee);
	}
	return entities;
    }

    public static void main(final String[] args) {
	new SimpleTableExample().launch(args);
    }
}
