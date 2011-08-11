/**
 *
 */
package ua.com.fielden.platform.application;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.Pair;

/**
 * Base class for applications with one main frame and any number of "master" frames for particular {@link AbstractEntity} modification or creation. Please refer to
 * {@link #showMasterFor(AbstractEntity, JPanel, String)} method documentation also.
 * 
 * @author Yura
 */
@SuppressWarnings("unchecked")
public abstract class SingleFrameApplication extends AbstractUiApplication {

    private final Map<Long, BaseFrame> entityMasters = new HashMap<Long, BaseFrame>();

    /**
     * Usage of this method ensures that only one instance of master per {@link AbstractEntity} is used. When master for some entity is needed, this method either creates new
     * master (if no masters are displayed) or brings to front existing one.<br>
     * <br>
     * <b>Note</b> : it is advised to use only this method for masters showing and closing, because otherwise there is no guarantee that there will be only one master per entity
     * 
     * @param entity
     *            - one for which master should be shown
     * @param entityMasterFactory
     *            - factory, which should create master's title and panel. Will be used only when master is not visible and only during invocation of this method.
     */
    public void showMasterFor(final AbstractEntity entity, final IEntityMasterFactory entityMasterFactory) {
	if (entityMasters.get(entity.getId()) == null) {
	    final ICloseHook<BaseFrame> entityMastersHook = new ICloseHook<BaseFrame>() {
		@Override
		public void closed(BaseFrame frame) {
		    entityMasters.remove(entity.getId());
		}
	    };

	    final Pair<String, JPanel> titleAndPanel = entityMasterFactory.createEntityMaster();
	    final BaseFrame newEntityMaster = new BaseFrame(titleAndPanel.getKey(), entityMastersHook);
	    newEntityMaster.add(titleAndPanel.getValue());
	    newEntityMaster.pack();
	    RefineryUtilities.centerFrameOnScreen(newEntityMaster);
	    entityMasters.put(entity.getId(), newEntityMaster);
	}
	entityMasters.get(entity.getId()).setVisible(true);
    }

    /**
     * Factory for creating title and main panel for entity master.
     * 
     * @author Yura
     */
    public static interface IEntityMasterFactory {

	/**
	 * Should return master's title and master's main panel
	 * 
	 * @return
	 */
	public Pair<String, JPanel> createEntityMaster();

    }

}
