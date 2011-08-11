package ua.com.fielden.platform.swing.rowstyle;

import java.awt.Color;

import ua.com.fielden.platform.swing.egi.EntityGridInspector;

import com.jidesoft.grid.RowStripeCellStyleProvider;

/**
 * Default {@link RowStripeCellStyleProvider} for the {@link EntityGridInspector}.
 * 
 * @author oleh
 * 
 */
public class DefaultPlatformRowStyle extends RowStripeCellStyleProvider {

    /**
     * Creates new {@link DefaultPlatformRowStyle} with gray and white rows.
     */
    public DefaultPlatformRowStyle() {
	super(new Color[] { new Color(242, 242, 242), new Color(255, 255, 255) });
    }
}
