package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;

/// Validator for some property to imitate concurrent changes for `tg-entity-centre-entity-editing-with-collections` web test.
///
/// @author TG Team
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
            saveParentEntity(entity);
        } else if (VALUE_FOR_CONFLICT_AND_COLLECTION_CHANGE.equals(newValue)) {
            // Imitate concurrently edited and saved situation with collection update.
            final var savedParent = saveParentEntity(entity);
            childCompanion.save(
                childCompanion.new_()
                    .setKey1(savedParent)
                    // Ensure new value every time to be unique in multiple web test runs.
                    .setKey2(new Date().getTime() + "")
            );
        }
        return successful();
    }

    /// Saves parent entity, that holds collection.
    private TgCollectionalSerialisationParent saveParentEntity(TgCollectionalSerialisationParent entity) {
        return companion.save(companion
            .findByEntityAndFetch(companion.getFetchProvider().fetchModel(), entity)
            // Ensure new value every time to be unique in multiple web test runs.
            .setDesc(new Date().getTime() + "")
        );
    }

}