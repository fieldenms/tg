package ua.com.fielden.platform.swing.review.report.analysis.grid;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.EgiPanel1;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;

public class GridAnalysisView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReview<T, CDTME, IAbstractAnalysisDomainTreeManagerAndEnhancer, IPage<T>> {

    private static final long serialVersionUID = 8538099803371092525L;

    private final EgiPanel1<T> egiPanel;

    public GridAnalysisView(final GridAnalysisModel<T, CDTME> model, final GridConfigurationView<T, CDTME> owner) {
	super(model, owner);
	this.egiPanel = new EgiPanel1<T>(getModel().getCriteria().getEntityClass(), getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer());
	getModel().getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		egiPanel.setData((IPage<T>)e.getNewPage());
	    }
	});
	getModel().getPageHolder().newPage(null);
	this.addSelectionEventListener(createGridAnalysisSelectionListener());

	//Set this analysis view for model.
	model.setAnalysisView(this);

	layoutView();
    }

    public EgiPanel1<T> getEgiPanel() {
	return egiPanel;
    }

    @Override
    public GridAnalysisModel<T, CDTME> getModel() {
	return (GridAnalysisModel<T, CDTME>) super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getModel().getCriteria().isDefaultEnabled()){
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	if(!navigate){
	    getCentre().getPaginator().setEnableActions(enable, !enable);
	}
	getCentre().getExportAction().setEnabled(enable);
	getCentre().getRunAction().setEnabled(enable);
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return null;
    }

    protected void layoutView() {
	setLayout(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	add(this.egiPanel);
    }

    /**
     * Determines the number of rows in the table those must be shown on the page using the size of the content panel as the basis.
     * If the calculated size is zero then value of 25 is returned.
     * This is done to handle cases where calculation happens prior to panel resizing takes place.
     *
     * @return
     */
    final int getPageSize() {
	double pageSize = egiPanel.getSize().getHeight() / EgiPanel1.ROW_HEIGHT;
	if (getOwner().getOwner().getCriteriaPanel() != null) {
	    pageSize += getOwner().getOwner().getCriteriaPanel().getSize().getHeight() / EgiPanel1.ROW_HEIGHT;
	}
	final int pageCapacity = (int) Math.floor(pageSize);
	return pageCapacity > 1 ? pageCapacity : 1;
    }

    //    /**
    //     * Enables or disables the paginator's actions without enabling or disabling blocking layer.
    //     *
    //     * @param enable
    //     */
    //    private void enablePaginatorActionsWithoutBlockingLayer(final boolean enable){
    //	getOwner().getPaginator().getFirst().setEnabled(enable, false);
    //	getOwner().getPaginator().getPrev().setEnabled(enable, false);
    //	getOwner().getPaginator().getNext().setEnabled(enable, false);
    //	getOwner().getPaginator().getLast().setEnabled(enable, false);
    //	if(getOwner().getPaginator().getFeedback() != null){
    //	    getOwner().getPaginator().getFeedback().enableFeedback(false);
    //	}
    //    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     *
     * @return
     */
    private ISelectionEventListener createGridAnalysisSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		//Managing the default, design and custom action changer button enablements.
		getCentre().getDefaultAction().setEnabled(getModel().getCriteria().isDefaultEnabled());
		if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
		    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if(getCentre().getCustomActionChanger() != null){
		    getCentre().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getCentre().getPaginator().setEnableActions(true, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(true);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    private AbstractEntityCentre<T, CDTME> getCentre(){
	return getOwner().getOwner();
    }
}
