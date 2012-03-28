package ua.com.fielden.uds.designer.zui.util;

import java.awt.Graphics2D;

import javax.swing.JFrame;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

public class GlobalObjects {
    public static PCanvas canvas;
    public static PCamera secondaryCamera;
    public static PLayer nodeLayer;
    public static PLayer linkLayer;
    public static JFrame frame;
    public static Graphics2D graphics2D;

    public static boolean isInitialised() {
	return canvas != null && linkLayer != null && frame != null;
    }
}
