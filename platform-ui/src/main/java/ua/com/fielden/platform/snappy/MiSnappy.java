package ua.com.fielden.platform.snappy;

//import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
//import ua.com.fielden.platform.swing.menu.TreeMenuItem;
//import ua.com.fielden.platform.swing.model.DefaultUiModel;
//import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * Main menu item representing Snappy rule designer.
 *
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 *
 * @author TG Team
 *
 */
public class MiSnappy {}
//extends TreeMenuItem<MiSnappy.SnappyManagerWrapper> {
//    private static final long serialVersionUID = 1L;
//
//    public MiSnappy(final TgSnappyApplicationModel snappyApplicationModel, final BlockingIndefiniteProgressPane blockingPane) {
//	super(new SnappyManagerWrapper(snappyApplicationModel, blockingPane));
//    }
//
//    /**
//     *
//     * Snappy rule designer UI wrapper to support lazy initialisation for.
//     *
//     */
//    static class SnappyManagerWrapper extends BaseNotifPanel<DefaultUiModel> {
//	private static final long serialVersionUID = 1L;
//
//	private TgSnappyApplicationPanel snappyApplicationPanel = null;
//
//	private final TgSnappyApplicationModel snappyApplicationModel;
//	private final BlockingIndefiniteProgressPane blockingPane;
//
//	public SnappyManagerWrapper(final TgSnappyApplicationModel snappyApplicationModel, final BlockingIndefiniteProgressPane blockingPane) {
//	    super("Snappy rule designer", new DefaultUiModel(true) {
//		@Override
//		public boolean canOpen() {
//		    return true;
//		}
//
//		@Override
//		public String whyCannotOpen() {
//		    return "no reason";
//		}
//
//	    });
//	    this.snappyApplicationModel = snappyApplicationModel;
//	    this.blockingPane = blockingPane;
//	    getModel().setView(this);
//	}
//
//	@Override
//	public void buildUi() {
//	    if (snappyApplicationPanel == null) {
//		snappyApplicationPanel = new TgSnappyApplicationPanel(snappyApplicationModel, blockingPane, null);
//	    }
//	    add(snappyApplicationPanel);
//	}
//
//	@Override
//	public String getInfo() {
//	    return "<html>" + "<h3>Snappy rule designer</h3>" + //
//		    "A facility to design and run rules/reports for Fleet domain entities." + "</html>";
//	}
//
//    }
//
//}
