package ua.com.fielden.platform.javafx.dashboard;

import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.GREEN;
import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.RED;
import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.YELLOW;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

import javax.swing.Action;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.ChartAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuItemWrapper;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaView;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a row in a dashboard table holds and manages a sentinel information.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DashboardRow<T extends AbstractEntity<?>> {
    private final TreeMenuWithTabs<?> treeMenu;

    private final ICriteriaGenerator criteriaGenerator;
    private final IGlobalDomainTreeManager gdtm;
    private final IEntityMasterManager masterManager;

    private final Class<?> menuItemType;
    private final String centreName;
    private final String analysisName;

    private final Class<T> rootType;
    private final SimpleStringProperty sentinelTitle;
    private final SimpleIntegerProperty countOfBad;
    private final TrafficLightsModel model;
    private IPage<T> resultPage;

    /**
     * Creates a dashboard row that holds and manages a sentinel information.
     *
     * @param treeMenu
     * @param criteriaGenerator
     * @param gdtm
     * @param masterManager
     * @param menuItemType
     * @param centreName
     * @param analysisName
     */
    public DashboardRow(final TreeMenuWithTabs<?> treeMenu, final ICriteriaGenerator criteriaGenerator, final IGlobalDomainTreeManager gdtm, final IEntityMasterManager masterManager, final Class<?> menuItemType, final String centreName, final String analysisName) {
	this.treeMenu = treeMenu;
	this.gdtm = gdtm;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
	this.menuItemType = menuItemType;
	this.centreName = centreName;
	this.analysisName = analysisName;

	this.rootType = (Class<T>) AnnotationReflector.getAnnotation(this.menuItemType, EntityType.class).value();
        this.sentinelTitle = new SimpleStringProperty(centreName + " " + TitlesDescsGetter.LEFT_ARROW + " " + analysisName);
        this.countOfBad = new SimpleIntegerProperty();
        model = new TrafficLightsModel();
    }

    private ISentinelDomainTreeManager sentinelManager() {
	return (ISentinelDomainTreeManager) gdtm.getEntityCentreManager(menuItemType, centreName).getAnalysisManager(analysisName);
    }

    private TreeMenuItemWrapper getMenuItem() {
	final TreeMenuItem root = (TreeMenuItem<?>) treeMenu.getModel().getRoot();
	final ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>> predicate = new ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>>() {
	    @Override
	    public boolean eval(final ITreeNode<ITreeNode> node) {
		return ((TreeMenuItem)node).toString().equals(analysisName);
	    }
	};
	final BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>> search = new BreadthFirstSearch<>();
	return (TreeMenuItemWrapper)search.search(root, predicate);
    }

    /**
     * Activates or opens appropriate analysis for the sentinel that is represented by this {@link DashboardRow}.
     */
    public void openAnalysis() {
	treeMenu.activateOrOpenItem(getMenuItem());
    }

    /**
     * Runs query and updates UI by fresh results.
     */
    public void run() {
	resultPage = runQuery();
    }

    /**
     * Runs query and updates UI by fresh results.
     */
    public void update() {
	if (resultPage == null) {
	    model.getRedLightingModel().setCount(0);
	    model.getYellowLightingModel().setCount(0);
	    model.getGreenLightingModel().setCount(0);
	}
	final String distrProperty = sentinelManager().getFirstTick().usedProperties(rootType).get(0);
	countOfBad.set(0);

	for (final T entity : resultPage.data()) {
	    final Integer count = (Integer) entity.get(SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
	    final String distrPropValue = (String) entity.get(distrProperty);
	    if (RED.equalsIgnoreCase(distrPropValue)) {
		model.getRedLightingModel().setCount(count);
		countOfBad.set(countOfBad.get() + count * 1000);
	    } else if (YELLOW.equalsIgnoreCase(distrPropValue)) {
		model.getYellowLightingModel().setCount(count);
		countOfBad.set(countOfBad.get() + count);
	    } else if (GREEN.equalsIgnoreCase(distrPropValue)) {
		model.getGreenLightingModel().setCount(count);
	    }
	}
	if (model.getRedLightingModel().isLighting() || model.getYellowLightingModel().isLighting()) {
	    model.getGreenLightingModel().setCount(0);
	}
    }

    /**
     * Returns a colour for concrete string value of sentinel dashboard property.
     *
     * @param status
     * @return
     */
    public static Color getColour(final String status) {
	if (RED.equalsIgnoreCase(status)) {
	    return TrafficLights.RED_COLOUR;
	} else if (YELLOW.equalsIgnoreCase(status)) {
	    return TrafficLights.GOLD_COLOUR;
	} else if (GREEN.equalsIgnoreCase(status)) {
	    return TrafficLights.GREEN_COLOUR;
	} else {
	    throw new IllegalArgumentException("Unsupported value [" + status + "].");
	}
    }

    public void invokeDetails(final String status) { // "RED", "GREEN" or "YELLOW" -- the value for the only distribution property in sentinel manager.
	final String distrProperty = sentinelManager().getFirstTick().usedProperties(rootType).get(0);
	createDoubleClickAction(new Pair<String, Object>(distrProperty, status)).actionPerformed(null);
    }

    private final Action createDoubleClickAction(final Pair<String, Object> choosenItem) {
	return new Command<Void>("Details") {
	    private static final long serialVersionUID = 1986658954874008023L;

	    @Override
	    protected final Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria = criteriaGenerator.generateCentreQueryCriteria(rootType, gdtm.getEntityCentreManager(menuItemType, centreName));
		final EntityFactory entityFactory = criteria.getEntityFactory();
		final ICentreDomainTreeManagerAndEnhancer cdtme = criteria.getCentreDomainTreeManagerAndEnhnacerCopy();
		cdtme.setRunAutomatically(true);
		setValueFor(rootType, cdtme, choosenItem);
		final String frameTitle = createFrameTitle(choosenItem);
		final CentreConfigurationWithoutCriteriaModel<T> detailsConfigModel = new CentreConfigurationWithoutCriteriaModel<>(rootType, frameTitle, entityFactory, criteriaGenerator, masterManager, cdtme);
		final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "Loading");
		final CentreConfigurationWithoutCriteriaView<T> detailsConfigView = new CentreConfigurationWithoutCriteriaView<>(detailsConfigModel, progressLayer);
		progressLayer.setView(detailsConfigView);
		final DetailsFrame detailsFrame = new DetailsFrame(choosenItem.getValue(), frameTitle, progressLayer, new ICloseHook<DetailsFrame>() {
		    @Override
		    public void closed(final DetailsFrame frame) {
			// getOwner().removeDetailsFrame(frame);
		    }
		});
		detailsConfigView.open();
		detailsFrame.setVisible(true);
	    }

	    @SuppressWarnings("unchecked")
	    private void setValueFor(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer newCdtme, final Pair<String, Object> choosenItem){
		final Class<T> managedType = (Class<T>)newCdtme.getEnhancer().getManagedType(root);
		final boolean isEntityItself = "".equals(choosenItem.getKey()); // empty property means "entity itself"
		final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, choosenItem.getKey());
		final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, choosenItem.getKey());
		final boolean isEntity = EntityUtils.isEntityType(propertyType);
		final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
		final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (EntityUtils.isBoolean(propertyType) ? Boolean.class : propertyType);

		Object value = choosenItem.getValue();
		if (List.class.isAssignableFrom(newPropertyType) && choosenItem.getValue() != null) {
		    final List<Object> values = new ArrayList<>();
		    values.add(choosenItem.getValue().toString());
		    value = values;
		}

		newCdtme.getFirstTick().check(root, choosenItem.getKey(), true);
		if (choosenItem.getValue() == null) {
		    newCdtme.getFirstTick().setOrNull(root, choosenItem.getKey(), true);
		}
		newCdtme.getFirstTick().setValue(root, choosenItem.getKey(), value);

		if (AbstractDomainTree.isDoubleCriterion(managedType, choosenItem.getKey())) {
		    newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), value);
		}

		if (EntityUtils.isBoolean(propertyType)) {
		    if (choosenItem.getValue() != null) {
			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), !(Boolean)value);
		    } else {
			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), null);
		    }
		}
	    }

	    private String createFrameTitle(final Pair<String, Object> choosenItems) {
		return "Details";
	    }
	};
    }

    private IPage<T> runQuery() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = gdtm.getEntityCentreManager(menuItemType, centreName);
	final IReportQueryGenerator<T> analysisQueryGenerator = new ChartAnalysisQueryGenerator<>(rootType, cdtme, sentinelManager());
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria = criteriaGenerator.generateCentreQueryCriteria(rootType, cdtme);
	final AnalysisResultClassBundle<T> classBundle = analysisQueryGenerator.generateQueryModel();
	return criteria.run(classBundle.getQueries().get(0).composeQuery(), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation(), Integer.MAX_VALUE);
    }

    public TrafficLightsModel getModel() {
	return model;
    }

    public String getSentinelTitle() {
	return sentinelTitle.get();
    }

    public void setSentinelTitle(final String sentinelTitle) {
	this.sentinelTitle.set(sentinelTitle);
    }

    public Integer getCountOfBad() {
	return countOfBad.get();
    }

    public void setCountOfBad(final Integer countOfBad) {
	this.countOfBad.set(countOfBad);
    }
}
