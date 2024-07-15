package ua.com.fielden.platform.domain.metadata;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case for {@link DomainExplorer}.
 * 
 * @author TG Team
 *
 */
public class DomainExplorerTest extends AbstractDaoTestCase {

    @Test
    public void domain_explorer_query_executes_correctly_against_an_empty_dataset() {
        assertTrue(co(DomainExplorer.class).getAllEntities(from(select(DomainExplorer.class).where().prop("dbTable").eq().val("NON-EXISTIN-TABLE").model()).model()).isEmpty());
    }

}