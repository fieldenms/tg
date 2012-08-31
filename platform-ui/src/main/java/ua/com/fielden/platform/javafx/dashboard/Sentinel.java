package ua.com.fielden.platform.javafx.dashboard;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;

import javax.swing.Action;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuItemWrapper;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.swing.review.report.analysis.details.configuration.AnalysisDetailsConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.details.configuration.AnalysisDetailsConfigurationView;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class Sentinel<T extends AbstractEntity<?>> {
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator criteriaGenerator;
    private final IEntityMasterManager masterManager;
    private final DashboardView dashboardView;
    private final ISentinelDomainTreeManager sentinelManager;
    private final Class<?> menuItemType;
    private final String centreName, analysisName;

    private final Class<T> root;
    private final ICentreDomainTreeManagerAndEnhancer cdtme;
    private final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria;

    private final SimpleStringProperty sentinelTitle;

    @SuppressWarnings("unchecked")
    public Sentinel(final DashboardView dashboardView, final ICriteriaGenerator criteriaGenerator, final IGlobalDomainTreeManager gdtm, final IEntityMasterManager masterManager, final Class<?> menuItemType, final String centreName, final String analysisName) {
	this.gdtm = gdtm;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
	this.dashboardView = dashboardView;
	this.menuItemType = menuItemType;
	root = (Class<T>)AnnotationReflector.getAnnotation(EntityType.class, menuItemType).value();
	cdtme = gdtm.getEntityCentreManager(menuItemType, centreName);
	criteria = criteriaGenerator.generateCentreQueryCriteria(root, cdtme);
	this.centreName = centreName;
	this.analysisName = analysisName;

	sentinelManager = (ISentinelDomainTreeManager) gdtm.getEntityCentreManager(menuItemType, centreName).getAnalysisManager(analysisName);

        this.sentinelTitle = new SimpleStringProperty(centreName + " <= " + analysisName);
    }

    private TreeMenuItemWrapper getMenuItem() {
	final TreeMenuItem root = dashboardView.getRootNode();

	final ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>> predicate = new ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>>() {

	    @Override
	    public boolean eval(final ITreeNode<ITreeNode> node) {
		System.out.println("-----------------" + ((TreeMenuItem)node).toString() + "--------------------");
		return ((TreeMenuItem)node).toString().equals(analysisName);
	    }
	};

	final BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>> search = new BreadthFirstSearch<>();
	return (TreeMenuItemWrapper)search.search(root, predicate);
    }

//    private TreeMenuItemWrapper findItem(final TreeMenuItem item, final String analysisName) {
//	if (item instanceof TreeMenuItemWrapper && analysisName.equals(item.getTitle())) {
//	    return (TreeMenuItemWrapper) item;
//	} else {
//	    for (final Object child : item.daughters()) {
//		final TreeMenuItemWrapper found = findItem((TreeMenuItem) child, analysisName);
//		if (found != null) {
//		    return found;
//		}
//	    }
//	    return null;
//	}
//    }

    public void openAnalysis() {
//	final TreeMenuItemWrapper item = getMenuItem();
//	dashboardView.getTreeMenu().activateOrOpenItem(item);

	System.err.println("=====================countOfSelfDashboard == " + run().data().get(0).get("countOfSelfDashboard"));
	invokeDetails("GREEN");
    }

    public String getSentinelTitle() {
        return sentinelTitle.get();
    }
    public void setSentinelTitle(final String sentinelTitle) {
        this.sentinelTitle.set(sentinelTitle);
    }

    private void invokeDetails(final String status) { // "RED", "GREEN" or "YELLOW" -- the value for the only distribution property in sentinel manager.
	final String distrProperty = sentinelManager.getFirstTick().usedProperties(root).get(0);
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

		final EntityFactory entityFactory = criteria.getEntityFactory();

		final ICentreDomainTreeManagerAndEnhancer cdtme = criteria.getCentreDomainTreeManagerAndEnhnacerCopy();
		cdtme.setRunAutomatically(true);
		setValueFor(root, cdtme, choosenItem);

		final String frameTitle = createFrameTitle(choosenItem);

		final AnalysisDetailsConfigurationModel<T> detailsConfigModel = new AnalysisDetailsConfigurationModel<>(//
		root,//
		frameTitle,//
		entityFactory,//
		criteriaGenerator, masterManager, cdtme);
		final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "Loading");
		final AnalysisDetailsConfigurationView<T> detailsConfigView = new AnalysisDetailsConfigurationView<>(detailsConfigModel, progressLayer);
		progressLayer.setView(detailsConfigView);
		final DetailsFrame detailsFrame = new DetailsFrame(choosenItem.getValue(),//
		frameTitle,//
		progressLayer, new ICloseHook<DetailsFrame>() {

		    @Override
		    public void closed(final DetailsFrame frame) {
			//getOwner().removeDetailsFrame(frame);
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

		if(choosenItem.getValue() == null){
		    newCdtme.getFirstTick().setOrNull(root, choosenItem.getKey(), true);
		}

		newCdtme.getFirstTick().setValue(root, choosenItem.getKey(), value);

		if(AbstractDomainTree.isDoubleCriterion(managedType, choosenItem.getKey())){
		    newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), value);
		}

		if(EntityUtils.isBoolean(propertyType)){
		    if(choosenItem.getValue() != null){
			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), !(Boolean)value);
		    }else{
			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), null);
		    }
		}


	    }

	    private String createFrameTitle(final Pair<String, Object> choosenItems) {
		return "Details";
	    }
	};
    }

    private IPage<T> run(){
	final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
	final List<String> distributionProperties = sentinelManager.getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = sentinelManager.getSecondTick().usedProperties(root);

	final List<Pair<String, ExpressionModel>> distribution = getPropertyExpressionPair(distributionProperties, criteria);
	final List<Pair<String, ExpressionModel>> aggregation = getPropertyExpressionPair(aggregationProperties, criteria);

	final List<String> yieldProperties = new ArrayList<String>();
	yieldProperties.addAll(distributionProperties);
	yieldProperties.addAll(aggregationProperties);

	final EntityResultQueryModel<T> queryModel = DynamicQueryBuilder.createAggregationQuery((Class<T>)enhancer.getManagedType(root), criteria.createQueryProperties(), distribution, aggregation).modelAsEntity(criteria.getManagedType());

	final List<Pair<String, Ordering>> orderingProperties = new ArrayList<Pair<String,Ordering>>(sentinelManager.getSecondTick().orderedProperties(root));
	if(orderingProperties.isEmpty()){
	    for(final String groupOrder : distributionProperties){
		orderingProperties.add(new Pair<String, Ordering>(groupOrder, Ordering.ASCENDING));
	    }
	}
	final List<Pair<Object, Ordering>> orderingPairs = EntityQueryCriteriaUtils.getOrderingList(root, //
		orderingProperties, //
		criteria.getCentreDomainTreeMangerAndEnhancer().getEnhancer());
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel)
	.with(DynamicOrderingBuilder.createOrderingModel(criteria.getManagedType(), orderingPairs))//
	.with(DynamicFetchBuilder.createFetchModel(criteria.getManagedType(), new HashSet<String>(yieldProperties))).model();

	return criteria.run(resultQuery, Integer.MAX_VALUE);
    }

    /**
     * Returns the list of property name and it's expression model pairs.
     *
     * @param propertyForExpression
     * @return
     */
    private List<Pair<String, ExpressionModel>> getPropertyExpressionPair(final List<String> propertyForExpression, final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria){
        final Class<T> root = criteria.getEntityClass();
        final IDomainTreeEnhancer enhancer = criteria.getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final List<Pair<String, ExpressionModel>> propertyExpressionPair = new ArrayList<>();
        for (final String property : propertyForExpression) {
            final ExpressionModel expression = EntityQueryCriteriaUtils.getExpressionForProp(root, property, enhancer);
            propertyExpressionPair.add(new Pair<>(property, expression));
        }
        return propertyExpressionPair;
    }
}
