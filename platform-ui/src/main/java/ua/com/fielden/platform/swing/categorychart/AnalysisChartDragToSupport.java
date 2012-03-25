package ua.com.fielden.platform.swing.categorychart;

import javax.swing.JList;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;

public class AnalysisChartDragToSupport<A extends AbstractEntity<?>, DAO extends IEntityDao2<A>, T, M, CT> extends AnalysisListDragToSupport {
    // TODO migrate to new API
    public AnalysisChartDragToSupport(final JList list) {
	super(list);
	// TODO Auto-generated constructor stub
    }

//    private final MultipleChartPanel<M, CT> multipleChartPanel;
//    private final CategoryChartReviewModel<A, DAO> categoryReviewModel;
//
//    public AnalysisChartDragToSupport(final CheckboxList<T> list, final MultipleChartPanel<M, CT> multipleChartPanel, final CategoryChartReviewModel<A, DAO> categoryReviewModel) {
//	super(list);
//	this.multipleChartPanel = multipleChartPanel;
//	this.categoryReviewModel = categoryReviewModel;
//    }
//
//    @Override
//    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
//	final int previousChartIndex = getSelectedOrderedProperties().indexOf(what);
//	final boolean result = super.dropTo(point, what, draggedFrom);
//	if (!result) {
//	    return false;
//	}
//	final int newChartIndex = getSelectedOrderedProperties().indexOf(what);
//	if (newChartIndex == previousChartIndex || previousChartIndex < 0 || newChartIndex < 0 || previousChartIndex >= multipleChartPanel.getChartPanelsCount()
//		|| newChartIndex >= multipleChartPanel.getChartPanelsCount()) {
//	    return true;
//	}
//	multipleChartPanel.changeChartPosition(previousChartIndex, newChartIndex);
//	multipleChartPanel.invalidate();
//	multipleChartPanel.revalidate();
//	multipleChartPanel.repaint();
//	return true;
//    }
//
//    private List<IAggregatedProperty> getSelectedOrderedProperties() {
//	final List<IAggregatedProperty> actulaProperties = categoryReviewModel.getSelectedAggregationProperties();
//	final List<IAggregatedProperty> selectedProperties = (List<IAggregatedProperty>) getList().getSelectedValuesInOrder();
//	final List<IAggregatedProperty> orderedProperties = new ArrayList<IAggregatedProperty>();
//	for (final IAggregatedProperty property : selectedProperties) {
//	    if (actulaProperties.contains(property)) {
//		orderedProperties.add(property);
//	    }
//	}
//	return orderedProperties;
//    }
//
//    @Override
//    protected CheckboxList<T> getList() {
//	return (CheckboxList<T>) super.getList();
//    }
}
