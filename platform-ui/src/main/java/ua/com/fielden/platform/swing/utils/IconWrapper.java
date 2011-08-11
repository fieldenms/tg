package ua.com.fielden.platform.swing.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

import org.jvnet.flamingo.common.icon.ResizableIcon;

/**
 * IconWraper wraps the Icon instance
 * 
 * @author oleh
 * 
 */
public class IconWrapper implements ResizableIcon {
    /**
     * wrapped icon
     */
    protected Icon delegate;

    /**
     * creates new instance of the IconWraper for the given Icon
     * 
     * @param delegate
     *            - specified Icon
     */
    public IconWrapper(Icon delegate) {
	this.delegate = delegate;
    }

    /**
     * returns the height of the icon
     */
    @Override
    public int getIconHeight() {
	return delegate.getIconHeight();
    }

    /**
     * returns the width of the icon
     */
    @Override
    public int getIconWidth() {
	return delegate.getIconHeight();
    }

    /**
     * draw the icon on the component
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
	delegate.paintIcon(c, g, x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jvnet.flamingo.common.ResizableIcon#revertToOriginalDimension()
     */
    public void revertToOriginalDimension() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jvnet.flamingo.common.ResizableIcon#setDimension(java.awt.Dimension)
     */
    public void setDimension(Dimension dim) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jvnet.flamingo.common.ResizableIcon#setHeight(int)
     */
    public void setHeight(int height) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jvnet.flamingo.common.ResizableIcon#setWidth(int)
     */
    public void setWidth(int width) {
    }

}
