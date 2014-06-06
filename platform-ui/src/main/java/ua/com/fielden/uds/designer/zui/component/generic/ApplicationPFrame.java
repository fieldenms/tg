package ua.com.fielden.uds.designer.zui.component.generic;

import edu.umd.cs.piccolox.PFrame;

/**
 * This one is needed to block undesired behaviour of PFrame of displaying frame before it is necessary.
 * 
 * @author 01es
 * 
 */
public class ApplicationPFrame extends PFrame {

    private static final long serialVersionUID = 3653566445206200088L;

    /**
     * The default implementation in PFrame is setting frame to become visible, which is an undesired affect. This overridden method does not currently handle the full screen mode,
     * however this functionality can be added if necessary.
     */
    public void setFullScreenMode(boolean fullScreenMode) {
    }
}
