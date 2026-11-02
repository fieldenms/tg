package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgEntityWithIsPropertyOverriddenId;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// Retrieval of an entity type that redeclares field `id` ([TgEntityWithIsPropertyOverriddenId]), as some entity types do (e.g., synthetic ones).
///
/// Retrieved instances must have the redeclared field assigned, as it is the one read by the overridden `getId()`.
/// Both kinds of instantiation are covered: plain instances for lightweight retrieval, and instances created by the injector for instrumented retrieval.
///
public class EntityWithRedeclaredIdRetrievalTest extends AbstractDaoTestCase {

    private static final String KEY = "ENTITY1";

    @Test
    public void entity_with_redeclared_id_is_retrieved_with_its_id_in_the_lightweight_mode() {
        final var savedId = persistedId();

        final var entities = co(TgEntityWithIsPropertyOverriddenId.class).getAllEntities(
                from(select(TgEntityWithIsPropertyOverriddenId.class).model()).with(fetchKeyAndDescOnly(TgEntityWithIsPropertyOverriddenId.class)).lightweight().model());

        assertThat(entities).singleElement().satisfies(entity -> {
            assertThat(entity.isInstrumented()).isFalse();
            assertThat(entity.getId()).isEqualTo(savedId);
        });
    }

    @Test
    public void entity_with_redeclared_id_is_retrieved_with_its_id_when_instrumented() {
        final var savedId = persistedId();

        final var entity = co$(TgEntityWithIsPropertyOverriddenId.class).findByKey(KEY);

        assertThat(entity.isInstrumented()).isTrue();
        assertThat(entity.getId()).isEqualTo(savedId);
    }

    /// The persisted ID, retrieved as an aggregate, which is instantiated independently of the entity type.
    ///
    private Long persistedId() {
        final var query = select(TgEntityWithIsPropertyOverriddenId.class).where().prop("key").eq().val(KEY).yield().prop("id").as("id").modelAsAggregate();
        final Long id = ((Number) co(EntityAggregates.class).getAllEntities(from(query).model()).getFirst().get("id")).longValue();
        assertThat(id).isNotNull();
        return id;
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        save(new_(TgEntityWithIsPropertyOverriddenId.class, KEY));
    }

}
