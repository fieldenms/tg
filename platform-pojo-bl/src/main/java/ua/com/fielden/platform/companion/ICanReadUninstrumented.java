package ua.com.fielden.platform.companion;

/**
 * A contract that should be used to indicate if a companion implementing it can read uninstrumented entity instances.
 * <p>
 * This contract should be viewed more as an implementation detail to support <code>co()</code> and <code>co$()</code> API, and should be implemented by concrete classes rather than extended by interfaces.
 * It is used as part of the instantiation of companion objects to instruct them reading instrumented or uninstrumented entity instances.
 * <p>
 * The application developers should not have any need to interact directly with methods of this contract.
 * 
 * @author TG Team
 *
 */
public interface ICanReadUninstrumented {
    
    /**
     * Instructs the companion to read entities as uninstrumented.
     */
    void readUninstrumented();
}
