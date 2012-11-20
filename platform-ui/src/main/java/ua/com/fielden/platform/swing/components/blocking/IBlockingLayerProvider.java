package ua.com.fielden.platform.swing.components.blocking;

/**
 * A contract for providing blocking layer in a lazy manner.
 * <p>
 * Useful in cases where UI model needs access to blocking layer that belong to a corresponding view, which gets set only after model is instantiated.
 *
 * @author TG Team
 *
 */
public interface IBlockingLayerProvider {
    BlockingIndefiniteProgressLayer getBlockingLayer();
}
