package ua.com.fielden.platform.web.view.master.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;

/**
 *
 * This is the default implementation for contract {@link ISimpleMasterConfig}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class SimpleMasterConfig<T extends AbstractEntity<?>> implements ISimpleMasterConfig<T> {

    private final IRenderable renderableRepresentation;

    // TODO More arguments to be added
    public SimpleMasterConfig(final IRenderable renderableRepresentation) {
        this.renderableRepresentation = renderableRepresentation;
    }

    @Override
    public IRenderable render() {
        return renderableRepresentation;
    }

}
