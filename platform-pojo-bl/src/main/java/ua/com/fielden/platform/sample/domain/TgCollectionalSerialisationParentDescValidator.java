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
    private static final String VALUE_FOR_CONFLICT = "validate + conflict";
    private static final String VALUE_FOR_CONFLICT_AND_COLLECTION_CHANGE = "validate + conflict + collection";

    @Inject
    private ITgCollectionalSerialisationParent companion;
    @Inject
    private ITgCollectionalSerialisationChild childCompanion;

    @Override
    public Result handle(MetaProperty<String> property, String newValue, Set<Annotation> mutatorAnnotations) {
        final var entity = property.<TgCollectionalSerialisationParent>getEntity();
        if (VALUE_FOR_CONFLICT.equals(newValue)) {
            // Imitate concurrently edited and saved situation.
            companion.save(companion
                .findByEntityAndFetch(companion.getFetchProvider().fetchModel(), entity)
                .setDesc(new Date().getTime() + "")
            );
        } else if (VALUE_FOR_CONFLICT_AND_COLLECTION_CHANGE.equals(newValue)) {
            // Imitate concurrently edited and saved situation with collection update.
            final var savedParent = companion.save(companion
                    .findByEntityAndFetch(companion.getFetchProvider().fetchModel(), entity)
                    .setDesc(new Date().getTime() + "")
            );
            childCompanion.save(
                childCompanion.new_()
                    .setKey1(savedParent)
                    .setKey2(new Date().getTime() + "")
            );
        }
        return successful();
    }

}