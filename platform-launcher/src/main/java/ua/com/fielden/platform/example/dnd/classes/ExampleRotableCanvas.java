package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import ua.com.fielden.platform.events.MultipleDragEventHandler.ForcedDehighlighter;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.swing.dnd.DnDCanvas;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.StubNode;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

public class ExampleRotableCanvas extends DnDCanvas {

    private static final long serialVersionUID = -6322191731692621056L;

    private PNode transferNode = null;

    private IBasicNode highlightedNode = null;

    private List<Rotable> transferedRotables = null;

    private Color highlightColor = new Color(150, 255, 150);

    /**
     * creates new instance of ExampleRotableCanvas
     */
    public ExampleRotableCanvas() {

        super(new ForcedDehighlighter() {

            @Override
            public boolean shouldDehighlight(final IBasicNode node) {
                return true;
            }

        });
        setDragEnabled(true);
        setAutoscrollEnable(true);
        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCamera().translateView(100, 100);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                super.mouseDragged(e);
                // /System.out.println("test");
            }
        });
        setAutoscrolls(true);
    }

    @SuppressWarnings("unchecked")
    public Collection<PNode> getSelection() {
        return getSelectionHandler().getSelection();
    }

    /**
     * refreshes all the cameras those are responsible for the model viewing
     */
    public void refreshCameras() {
        getCamera().repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDnDStarted(final InputEvent dge) {
        return dge.isShiftDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void initDrag(final Point2D location, final Object what) {
        super.initDrag(location, what);
        final PLayer parentLayer = getLayer();
        if (!(what instanceof List)) {
            return;
        }
        transferedRotables = (List<Rotable>) what;
        if (transferedRotables != null) {
            if (transferNode != null) {
                transferNode.removeFromParent();
            }
            if (transferedRotables.size() == 1) {
                if (transferedRotables.get(0) instanceof Bogie) {
                    transferNode = new BogieDragModel((Bogie) transferedRotables.get(0));
                } else if (transferedRotables.get(0) instanceof Wheelset) {
                    transferNode = new WheelsetDragModel((Wheelset) transferedRotables.get(0));
                } else {
                    return;
                }
                transferNode.setTransparency(0.5f);
            } else {
                transferNode = new TransferExNode(transferedRotables, null);
            }
            parentLayer.addChild(transferNode);
        } else {
            if (transferNode != null) {
                transferNode.removeFromParent();
                transferNode = null;
            }
            return;
        }
        translateTransferNode(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateDrag(final Point2D location) {
        super.updateDrag(location);
        translateTransferNode(location);
    }

    /**
     * translates the node that represent transferable data to the specified position on the Player
     * 
     * @param point
     *            - specified point where the node must be situated
     */
    private void translateTransferNode(final Point2D point) {
        if (transferNode != null) {
            final double width = transferNode.getWidth();
            final double height = transferNode.getHeight();
            transferNode.setOffset(new Point2D.Double(point.getX() - width / 2, point.getY() - height / 2));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanUpDrag(final DropTargetEvent dte, final boolean wasDropped) {
        super.cleanUpDrag(dte, wasDropped);
        if (wasDropped) {
            transferNode.setTransparency(1.0f);
        }
        if (transferNode instanceof TransferNode) {
            transferNode.removeFromParent();
        } else {
            if (!wasDropped) {
                transferNode.removeFromParent();
            }
        }
        transferNode = null;
        transferedRotables = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isOkToDrop(final Object object, final Point2D location) {
        final boolean success = super.isOkToDrop(object, location);
        if (!success) {
            if (transferNode instanceof TransferNode) {
                ((TransferNode) transferNode).setAccept(false);
            }
            return success;
        }
        if (transferNode != null) {
            final ArrayList<PNode> intersectedNodes = new ArrayList<PNode>();
            getIntersectingNodeswith(intersectedNodes);
            if (!(transferNode instanceof TransferNode)) {

                if (getDragEventHandler().getForcedDehighlighter().shouldDehighlight(highlightedNode)) {
                    dehighlight();
                }

                boolean foundMatch = false;

                for (final PNode nextNode : intersectedNodes) {
                    if (!(nextNode instanceof StubNode) && (nextNode instanceof IContainer) && ((IContainer) nextNode).isCompatible(transferNode)) {
                        if (!nextNode.equals(highlightedNode)) {
                            dehighlight();
                        }
                        highlightedNode = (IBasicNode) nextNode;
                        highlight();
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    dehighlight();
                    if (intersectedNodes.size() == 0) {
                        return true;
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                if (intersectedNodes.size() > 0) {
                    ((TransferNode) transferNode).setAccept(false);
                    return false;
                } else {
                    ((TransferNode) transferNode).setAccept(true);
                    return true;
                }
            }
        } else {
            return success;
        }
    }

    /**
     * finds all PNodes those intersects with the transferNode and the result is returned in the cleanNodes list
     * 
     * @param cleanedNodes
     *            - the result of intersection of the transferNodes with the others in the same layer
     */
    private void getIntersectingNodeswith(final ArrayList<PNode> cleanedNodes) {
        cleanedNodes.clear();
        final ArrayList<PNode> nodes = new ArrayList<PNode>();
        // find intersecting nodes that belong to a main layer...
        getLayer().findIntersectingNodes(transferNode.getGlobalBounds(), nodes);
        nodes.removeAll(transferNode.getChildrenReference());
        nodes.remove(transferNode);
        nodes.remove(getLayer());
        // remove intersected nodes' children...
        cleanedNodes.addAll(nodes);

        for (final PNode node : nodes) {
            // handle node's children
            for (final Object childNode : node.getChildrenReference()) {
                if (!(childNode instanceof IContainer)) {
                    cleanedNodes.remove(childNode);
                } else if (nodes.contains(childNode) && !((IContainer) childNode).isCompatible(transferNode)) {
                    cleanedNodes.remove(childNode);
                }
            }
            // handle node itself
            if (!(node instanceof IContainer)) {
                cleanedNodes.remove(node);
            }

            if (node instanceof RotableContainerRetriever) {
                final RotableContainerRetriever containerretriever = (RotableContainerRetriever) node;
                if (transferedRotables.contains(containerretriever.getRotable())) {
                    cleanedNodes.remove(node);
                }
            }
        }

    }

    /**
     * highlights node when the transferNode is compatible to the highlightedNode
     */
    private void highlight() {
        if (((IContainer) highlightedNode).isCompatible(transferNode)) {
            highlightedNode.highlight(transferNode, highlightColor);
        }
    }

    /**
     * removes the selection from the highlighted node and set the highlighted node to null
     */
    private void dehighlight() {
        if (highlightedNode != null) {
            highlightedNode.dehighlight();
        }
        highlightedNode = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean importTranserred(final Object object, final Point2D location) {
        if (transferNode instanceof TransferNode) {
            final List<AbstractNode> rotables = ((TransferNode) transferNode).createRotables();
            getLayer().addChildren(rotables);
            return true;
        } else {
            final IContainer container = (IContainer) highlightedNode;
            if (container != null) {
                if (container.isCompatible(transferNode)) {
                    container.attach(null, transferNode, true);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getTransferObject(final Point2D point) {
        final Collection<PNode> nodes = getSelection();
        final List<Rotable> rotables = new ArrayList<Rotable>();
        for (final PNode node : nodes) {
            if (node instanceof RotableContainerRetriever) {
                rotables.add(((RotableContainerRetriever) node).getRotable());
            }
        }
        return rotables;
    }

    @Override
    protected void dropDone(final JComponent c, final Object transferObject, final int dropAction) {
        super.dropDone(c, transferObject, dropAction);
        if (dropAction != DnDConstants.ACTION_NONE) {
            getLayer().removeChildren(getSelection());
            getSelectionHandler().unselect(getSelection());
        }
    }

    @Override
    protected void updateCanvas(final Point2D delta) {
        super.updateCanvas(delta);
        if (transferNode != null) {
            transferNode.offset(delta.getX(), delta.getY());
        }
    }
}
