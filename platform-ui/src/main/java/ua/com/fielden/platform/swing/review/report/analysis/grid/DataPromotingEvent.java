package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.EventObject;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

public class DataPromotingEvent<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends EventObject {

    private static final long serialVersionUID = -5440584892642376390L;

    private final IPage<T> page;

    public DataPromotingEvent(final GridAnalysisView<T, CDTME> analysis, final IPage<T> page) {
        super(analysis);
        this.page = page;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridAnalysisView<T, CDTME> getSource() {
        return (GridAnalysisView<T, CDTME>) super.getSource();
    }

    public IPage<T> getPage() {
        return page;
    }
}
