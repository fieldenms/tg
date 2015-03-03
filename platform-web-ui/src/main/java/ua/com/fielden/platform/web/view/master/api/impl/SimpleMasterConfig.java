package ua.com.fielden.platform.web.view.master.api.impl;

import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
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
    @SuppressWarnings("rawtypes")
    private final Map<String, Class<IValueMatcher>> valueMatcherForProps;

    @SuppressWarnings("rawtypes")
    public SimpleMasterConfig(
            final IRenderable renderableRepresentation,
            final Map<String, Class<IValueMatcher>> valueMatcherForProps) {
        this.renderableRepresentation = renderableRepresentation;
        this.valueMatcherForProps = valueMatcherForProps;
    }

    @Override
    public IRenderable render() {
        return renderableRepresentation;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<IValueMatcher> matcherTypeFor(final String propName) {
        return valueMatcherForProps.get(propName);
    }

}
