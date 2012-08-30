package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.MultipleAnalysisEntityCentre;

public class MultipleAnalysisEntityCentreConfigurationView<T extends AbstractEntity<?>> extends CentreConfigurationView<T, MultipleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -6434256458143463705L;

    private final Map<String, Map<Object, DetailsFrame>> detailsCache;

    public MultipleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.detailsCache = new HashMap<>();
    }

    @Override
    public void close() {
        super.close();
        for(final Map<Object, DetailsFrame> detailsFrames : detailsCache.values()){
            for(final DetailsFrame frame : detailsFrames.values()){
        	frame.close();
            }
            detailsFrames.clear();
        }
        detailsCache.clear();
    }

    /**
     * Returns the cache for details frames, for specific analysis name
     *
     * @param name
     * @return
     */
    public Map<Object, DetailsFrame> getDetailsCache(final String name){
	Map<Object, DetailsFrame> detailsFrames = detailsCache.get(name);
	if(detailsFrames == null){
	    detailsFrames = new HashMap<>();
	    detailsCache.put(name, detailsFrames);
	}
	return detailsFrames;
    }

    @Override
    protected MultipleAnalysisEntityCentre<T> createConfigurableView() {
	return new MultipleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), this);
    }

}
