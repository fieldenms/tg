package ua.com.fielden.platform.swing.components.blocking;

/**
 * This interface was made because {@link AdviceReview} and {@link WagonReview} classes use blocking panes, which are set after their construction. Thus it is not possible to use
 * this blocking pane in the constructor. This interface provides a way to obtain a reference to that blocking pane.
 * 
 * @author Yura
 */
public interface IContainerWithBlockingPane {
    public BlockingIndefiniteProgressPane getBlockingPane();
}