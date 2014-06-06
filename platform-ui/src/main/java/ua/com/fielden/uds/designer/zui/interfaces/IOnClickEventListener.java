package ua.com.fielden.uds.designer.zui.interfaces;

import java.io.Serializable;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * An interface to be implemented for on_click behaviour.
 * 
 * @author 01es
 * 
 */
public interface IOnClickEventListener extends Serializable {
    public void click(PInputEvent event);
}