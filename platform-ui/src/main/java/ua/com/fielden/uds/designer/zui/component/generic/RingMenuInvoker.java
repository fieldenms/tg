/**
 * 
 */
package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.Timer;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class implements an input event handler for displaying a ring menu associated with a node in its right top corner. It uses a timer to control menu's visibility: a mouse
 * should be held still for 300 milliseconds before menu appears.
 * 
 * @author 01es
 * 
 */
public class RingMenuInvoker extends PBasicInputEventHandler implements Serializable {
    private static final long serialVersionUID = 4523350088034984134L;
    private ActionListener taskPerformer = new Action();

    private AbstractNode node;

    public RingMenuInvoker(AbstractNode node) {
        this.node = node;
    }

    private class Action implements ActionListener, Serializable {
        private static final long serialVersionUID = 845725509648065834L;

        public void actionPerformed(ActionEvent evt) {
            int centreX = (int) (node.getGlobalBounds().getX() + node.getWidth());
            int centreY = (int) node.getGlobalBounds().getY();
            if (node.getRingMenu() != null) { // just in case
                node.getRingMenu().show(centreX, centreY);
            }
        }
    };

    private Timer timer = new Timer(300, taskPerformer);

    public void mouseExited(PInputEvent event) {
        timer.stop();
    }

    public void mouseMoved(PInputEvent event) {
        timer.restart();
    }
}