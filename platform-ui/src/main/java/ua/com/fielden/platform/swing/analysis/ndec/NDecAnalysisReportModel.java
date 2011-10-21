package ua.com.fielden.platform.swing.analysis.ndec;

import java.awt.Container;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.ndec.dec.NDecView;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;

public class NDecAnalysisReportModel<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IAnalysisReportModel {

    private final NDecView multipleDecView;

    public NDecAnalysisReportModel(final NDecAnalysisReview<T, DAO> reportView, final BlockingIndefiniteProgressLayer tabPaneLayer){
	multipleDecView = new NDecView(reportView.getModel().getMultipleDecModel());
	multipleDecView.addAnalysisDoubleClickListener(createDoubleClickListener(reportView.getModel()));
	reportView.setLayout(new MigLayout("fill, insets 0", "[fill,grow]", "[fill,grow]"));
	reportView.add(multipleDecView);
    }

    private IAnalysisDoubleClickListener createDoubleClickListener(final NDecAnalysisModel<T, DAO> model) {
	return new IAnalysisDoubleClickListener() {

	    @Override
	    public void doubleClick(final AnalysisDoubleClickEvent event) {
		model.runDoubleClickAction(event);
	    }
	};
    }

    @Override
    public void restoreReportView(final Container container) throws IllegalStateException {
	throw new UnsupportedOperationException("Restoring multiple dec report view is unsupported yet.");
    }

    @Override
    public void createReportView(final Container container) throws IllegalStateException {

    }

}
