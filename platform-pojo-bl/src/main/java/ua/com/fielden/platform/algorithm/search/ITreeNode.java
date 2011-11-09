package ua.com.fielden.platform.algorithm.search;

import java.util.List;

/**
 * An contract, which should be supported by every type that needs to be serve a role of the tree node.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ITreeNode<T> {

    List<? extends ITreeNode<T>> daughters();
    T state();

}
