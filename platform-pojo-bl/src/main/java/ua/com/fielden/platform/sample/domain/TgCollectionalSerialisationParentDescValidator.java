package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;

public class TgCollectionalSerialisationParentDescValidator implements IBeforeChangeEventHandler<String> {

    @Inject
    private ITgCollectionalSerialisationParent companion;

    @Override
    public Result handle(MetaProperty<String> property, String newValue, Set<Annotation> mutatorAnnotations) {
        if ("validate + conflict".equals(newValue)) {
            final var entity = property.<TgCollectionalSerialisationParent>getEntity();
            // Imitate concurrently edited and saved situation.
            companion.save(companion
                .findByEntityAndFetch(companion.getFetchProvider().fetchModel(), entity)
                .setDesc(new Date().getTime() + "")
            );
        }
        return successful();
    }

}