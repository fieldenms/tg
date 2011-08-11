package ua.com.fielden.platform.events;

/**
 * That interface is responsible for the decorating and undecorating nodes during the selection class that extends PNode class or any other class that extend PNode may implement
 * that interface if it wants to change it's properties like stroke or the paint when it's being selected or unselected
 * 
 * @author oleh
 * 
 */
public interface IDecorable {

    /**
     * decorates node when it was selected
     */
    public void Decorate();

    /**
     * undecorates the node when it was unselected
     */
    public void Undecorate();
}
