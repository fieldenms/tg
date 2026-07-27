package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DIRTY_CANNOT_BE_DELETED;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_ONLY_PERSISTED_CAN_BE_DELETED;

public class EntityDeletionTest extends AbstractDaoTestCase {

    @Test
    public void new_entities_cannot_be_deleted() {
        final var cat = new_(TgCategory.class, "CAT1");
        assertThatThrownBy(() -> co$(TgCategory.class).delete(cat))
                .hasMessage(ERR_ONLY_PERSISTED_CAN_BE_DELETED);
    }

    @Test
    public void dirty_entities_cannot_be_deleted() {
        final var cat = save(new_(TgCategory.class, "CAT1"))
                .setDesc("Broad category");
        assertThatThrownBy(() -> co$(TgCategory.class).delete(cat))
                .hasMessage(ERR_DIRTY_CANNOT_BE_DELETED);
    }

}
