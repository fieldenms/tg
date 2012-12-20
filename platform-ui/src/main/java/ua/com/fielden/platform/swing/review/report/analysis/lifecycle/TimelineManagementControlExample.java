package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.IGroup;
import ua.com.fielden.platform.equery.lifecycle.IProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.jidesoft.plaf.LookAndFeelFactory;

public class TimelineManagementControlExample  extends AbstractUiApplication {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private LifecycleModel<TimelineEntity> chartEntryModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	chartEntryModel = createLifecycleModel();

	super.beforeUiExposure(args, splashController);
    }

    /**
     * Example {@link EntityPropertyLifecycle} with some "x" offset.
     *
     * @param x
     * @return
     */
    private EntityPropertyLifecycle<TimelineEntity> createEPL(final String key, final int x) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	// not sorted:
	intervals.add(new ValuedInterval(date(36 + x), date(42 + x), "Value 1"));
	intervals.add(new ValuedInterval(date(2 + x), date(10 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(42 + x), date(47 + x), "Value 4"));
	intervals.add(new ValuedInterval(date(10 + x), date(31 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(31 + x), date(36 + x), "Value 3"));
	intervals.add(new ValuedInterval(date(47 + x), date(52 + x), "Value 2"));
	return new EntityPropertyLifecycle<TimelineEntity>(factory.newByKey(TimelineEntity.class, key), TimelineEntity.class, "monitoring", intervals, date(15), date(55));
    }

    private DateTime date(final int minutes) {
	return new DateTime(2010, 1, 1, 0, minutes, 0, 0);
    }

    private EntityPropertyLifecycle<TimelineEntity> createUnavailableEPL(final String key) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	intervals.add(new ValuedInterval(date(15), date(55), "Value 4"));
	return new EntityPropertyLifecycle<TimelineEntity>(factory.newByKey(TimelineEntity.class, key), TimelineEntity.class, "monitoring", intervals, date(15), date(55));
    }

    /**
     * Creates testing lifecycle model.
     *
     * @return
     */
    private LifecycleModel<TimelineEntity> createLifecycleModel() {
	final List<EntityPropertyLifecycle<TimelineEntity>> ld = new ArrayList<EntityPropertyLifecycle<TimelineEntity>>();
	ld.add(createEPL("A0001", 0));
	ld.add(createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(createEPL("A0003", -2));
	ld.add(createEPL("A0004", 2));
	ld.add(createEPL("A0005", 1));
	ld.add(createEPL("A0006", -1));

	ld.add(createUnavailableEPL("A0007"));

	final LifecycleModel<TimelineEntity> lm = new LifecycleModel<TimelineEntity>(TimelineEntity.class, date(15), date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<TimelineEntity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<TimelineEntity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<TimelineEntity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<TimelineEntity> narrowedModel, final LifecycleModel<TimelineEntity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<TimelineEntity>, ? extends LifecycleModel<TimelineEntity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<TimelineEntity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }

	};
	return lm;
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final BaseFrame mainApplicationFrame = new BaseFrame("Timeline management control example", new HashMap<Class<? extends AbstractEntity<?>>, IEntityMasterCache>());
	mainApplicationFrame.setPreferredSize(new Dimension(1280, 500));
	mainApplicationFrame.add(new TimelineManagementControl<TimelineEntity>(chartEntryModel){
	    @Override
	    public void change(final TimelineEntity entity, final java.util.Date start, final java.util.Date finish) {
	    };

	    @Override
	    public boolean canBeChanged(final TimelineEntity entity, final java.util.Date start, final java.util.Date finish) {
		return true;
	    };
	}.getLocalChartPanel());
	mainApplicationFrame.pack();

	RefineryUtilities.centerFrameOnScreen(mainApplicationFrame);
	SimpleLauncher.show("Timeline management control example", mainApplicationFrame, null);
    }

    public static void main(final String[] args) {
	DOMConfigurator.configure("src/main/resources/log4j.xml");
	new TimelineManagementControlExample().launch(args);
    }
}
