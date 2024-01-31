package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TeProductPrice;

public class OrderByPropEnhancementTest extends AbstractEqlShortcutTest {

    @Test
    public void ordering_by_composite_key_with_money_works() {
        final var query = select(TeProductPrice.class).where().prop("price").gt().val(100).model();
        final var actOrderBy = orderBy().prop("key").asc().model();
        final var expOrderBy = orderBy().prop("product").asc().prop("price").asc().model();
        assertModelResultsAreEqual(query, expOrderBy, query, actOrderBy);
    }
}
