package ua.com.fielden.platform.swing.components.blocking;

/**
 * This is a provider, which supports setting of a blocking layer instance at any time it is necessary.
 * 
 * @author TG Team
 * 
 */
public class AdhocBlockingLayerProvider implements IBlockingLayerProvider {

    private BlockingIndefiniteProgressLayer blockingLayer;

    @Override
    public BlockingIndefiniteProgressLayer getBlockingLayer() {
        return blockingLayer;
    }

    public void setBlockingLayer(final BlockingIndefiniteProgressLayer blockingLayer) {
        this.blockingLayer = blockingLayer;
    }

}
