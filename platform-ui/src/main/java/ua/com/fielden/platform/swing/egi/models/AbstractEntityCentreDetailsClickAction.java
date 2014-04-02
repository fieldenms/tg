package ua.com.fielden.platform.swing.egi.models;

/**
 * A base class to be used for implementing double click action to view details of some calculated values represented as part of an entity centre EGI.
 * 
 * @author TG Team
 * 
 */

// TODO fix
public abstract class AbstractEntityCentreDetailsClickAction {
    //	<MASTER_TYPE extends AbstractEntity,
    //	 DETAILS_TYPE extends AbstractEntity,
    //	 MASTER_VIEW extends UvEntityCentre<MASTER_TYPE, ? extends IEntityDao2<MASTER_TYPE>, ?, ?>> extends BlockingLayerCommand<MASTER_TYPE> {
    //    private static final long serialVersionUID = 1L;
    //
    //    protected final String propertyName;
    //    protected final MASTER_VIEW view;
    //    protected final Map<String, DetailsFrame> detailsCache = new HashMap<String, DetailsFrame>();
    //
    //    public AbstractEntityCentreDetailsClickAction(//
    //	    final String propertyName, // calculated property for which the details need to be displayed
    //	    final MASTER_VIEW view) { // the view from which the details are invoked
    //	super("Details action", new BlockingProvider<MASTER_TYPE, DETAILS_TYPE, MASTER_VIEW>(view));
    //	this.propertyName = propertyName;
    //	this.view = view;
    //    }
    //
    //    @Override
    //    protected final MASTER_TYPE action(final ActionEvent e) throws Exception {
    //	return view.getEntityGridInspector().getActualModel().getSelectedEntity();
    //    }
    //
    //    @Override
    //    protected final void postAction(final MASTER_TYPE value) {
    //	final String frameTitle =  frameTitle(value);
    //	// make the key base on the report configuration title and the frame title in order to separate details frames for different configurations of the same report type
    //	final String detailsFrameKey = view.getSelectedTabTitle() + frameTitle;
    //	DetailsFrame detailsFrame = detailsCache.get(detailsFrameKey);
    //	if (detailsFrame == null) {
    //	    detailsFrame = new DetailsFrame(detailsFrameKey, frameTitle, getDetailsEntityReview(value), new ICloseHook<DetailsFrame>() {
    //		@Override
    //		public void closed(final DetailsFrame frame) {
    //		    detailsCache.remove(frame.getAssociatedEntity());
    //		}
    //
    //	    });
    //	    detailsCache.put(detailsFrameKey, detailsFrame);
    //	}
    //	detailsFrame.setVisible(true);
    //	super.postAction(value);
    //    }
    //
    //
    //    protected abstract String frameTitle(final MASTER_TYPE value);
    //
    //    protected abstract DetailsDynamicEntityReview<DETAILS_TYPE, ? extends IEntityDao2<DETAILS_TYPE>> getDetailsEntityReview(final MASTER_TYPE value);
    //
    //    protected abstract PropertyTableModelBuilder<DETAILS_TYPE> createPropertyTableModelBuilder();
    //
    //
    //    /** Wrapper class. */
    //    private static class BlockingProvider<MASTER_TYPE extends AbstractEntity,
    //	 DETAILS_TYPE extends AbstractEntity,
    //	 MASTER_VIEW extends UvEntityCentre<MASTER_TYPE, ? extends IEntityDao2<MASTER_TYPE>, ?, ?>> implements IBlockingLayerProvider {
    //
    //	private final MASTER_VIEW view;
    //
    //	public BlockingProvider(final MASTER_VIEW view) {
    //	    this.view = view;
    //	}
    //
    //	@Override
    //	public BlockingIndefiniteProgressLayer getBlockingLayer() {
    //	    return view != null ? view.getReviewContract().getBlockingLayer() : null;
    //	}
    //
    //    }
}
