package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.IGroup;
import ua.com.fielden.platform.equery.lifecycle.IProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModelTest;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.plaf.LookAndFeelFactory;

public class TimelineManagementControlExample  extends AbstractUiApplication {
    private LifecycleModel<Entity> chartEntryModel;
    private TaskSeriesCollection mainDataSet = new TaskSeriesCollection();

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	chartEntryModel = createLifecycleModel();
	mainDataSet = LifecycleChartFactory.createMainDataSet(chartEntryModel);

	super.beforeUiExposure(args, splashController);
    }

    /**
     * Creates testing lifecycle model.
     *
     * @return
     */
    private LifecycleModel<Entity> createLifecycleModel() {
	final LifecycleModelTest lmt = new LifecycleModelTest();
	final List<EntityPropertyLifecycle<Entity>> ld = new ArrayList<EntityPropertyLifecycle<Entity>>();
	ld.add(lmt.createEPL("A0001", 0));
	ld.add(lmt.createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(lmt.createEPL("A0003", -2));
	ld.add(lmt.createEPL("A0004", 2));
	ld.add(lmt.createEPL("A0005", 1));
	ld.add(lmt.createEPL("A0006", -1));

	ld.add(lmt.createUnavailableEPL("A0007"));

	final LifecycleModel<Entity> lm = new LifecycleModel<Entity>(lmt.date(15), lmt.date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<Entity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<Entity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<Entity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<Entity> narrowedModel, final LifecycleModel<Entity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<Entity>, ? extends LifecycleModel<Entity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<Entity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }

	};
	return lm;
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final BaseFrame mainApplicationFrame = new BaseFrame("Timeline management control example", new HashMap<Class<? extends AbstractEntity<?>>, IEntityMasterCache>());

	final JFreeChart chart = LifecycleChartFactory.createAvailabilityChart(chartEntryModel, mainDataSet);
	final ChartPanel chartPanel = new ChartPanel(chart) {
	    @Override
	    public void mouseDragged(final MouseEvent event) {
	        super.mouseDragged(event);

	        if (event.isControlDown()) {
	            // TODO do some stuff
	        }
	    }
	};

	mainApplicationFrame.setPreferredSize(new Dimension(1280, 800));
	mainApplicationFrame.add(chartPanel);
	mainApplicationFrame.pack();

	RefineryUtilities.centerFrameOnScreen(mainApplicationFrame);
	SimpleLauncher.show("Timeline management control example", mainApplicationFrame, null);
    }

    public static void main(final String[] args) {
	DOMConfigurator.configure("src/main/resources/log4j.xml");
	new TimelineManagementControlExample().launch(args);
    }
}
