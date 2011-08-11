package ua.com.fielden.platform.basic;

/**
 * Interface for builder classes used in Builder pattern.
 * 
 * @author Yura
 * 
 * @param <T>
 *            - class, instances of which are built by this builder
 */
public interface IBuilder<T> {

    /**
     * Builds instance of type T
     */
    T build();

}
