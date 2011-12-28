package ua.com.fielden.platform.swing.review.report.analysis.view;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public abstract class AbstractAnalysisReview<T extends AbstractEntity, ADTM extends IAbstractAnalysisDomainTreeManager> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -1195915524813089236L;

    private final AbstractEntityCentre<T> owner;
    private final PageHolder pageHolder;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, ADTM> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T> owner, final PageHolder pageHolder) {
	super(model, progressLayer);
	this.owner = owner;
	this.pageHolder = pageHolder;
	pageHolder.newPage(null);
    }

    public void loadData(){
	//TODO implement data loading
    }

    public void exportData(){
	//TODO implement data exporting to external file.
    }

    //    public PageHolder<AbstractEntity> getPageHolder(){
    //	return pageHolder;
    //    }

}
