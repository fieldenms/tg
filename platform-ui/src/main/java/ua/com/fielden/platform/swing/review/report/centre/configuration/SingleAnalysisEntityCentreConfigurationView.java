package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityCentre;

public class SingleAnalysisEntityCentreConfigurationView<T extends AbstractEntity<?>> extends CentreConfigurationView<T, SingleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -3749891053466125465L;

    /**
     * Holds the details for the entity centre with single analysis.
     */
    private final Map<Object, DetailsFrame> detailsCache;

    public SingleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
        super(model, progressLayer);
        this.detailsCache = new HashMap<>();
    }

    /**
     * Returns the cache of details.
     * 
     * @return
     */
    public Map<Object, DetailsFrame> getDetailsCache() {
        return detailsCache;
    }

    @Override
    public ICloseGuard canClose() {
        for (final DetailsFrame frame : detailsCache.values()) {
            final ICloseGuard unableToClose = frame.canClose();
            if (unableToClose != null) {
                return unableToClose;
            }
        }
        return super.canClose();
    }

    @Override
    public void close() {
        super.close();
        for (final DetailsFrame frame : detailsCache.values()) {
            frame.close();
        }
        detailsCache.clear();
    }

    @Override
    protected SingleAnalysisEntityCentre<T> createConfigurableView() {
        return new SingleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), this);
    }
}
