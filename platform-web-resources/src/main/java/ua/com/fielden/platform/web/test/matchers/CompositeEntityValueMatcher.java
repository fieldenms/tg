package ua.com.fielden.platform.web.test.matchers;

import com.google.inject.Inject;
import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithContext;
import ua.com.fielden.platform.sample.domain.ITgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

public class CompositeEntityValueMatcher extends
        AbstractSearchEntityByKeyWithContext<TgPersistentEntityWithProperties, TgPersistentCompositeEntity> {

    @Inject
    public CompositeEntityValueMatcher(final ITgPersistentCompositeEntity companion) {
        super(companion);
    }
}
