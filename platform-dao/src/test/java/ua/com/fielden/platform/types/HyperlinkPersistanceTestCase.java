package ua.com.fielden.platform.types;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class HyperlinkPersistanceTestCase extends AbstractDaoTestCase {

    @Test
    public void hyperlinks_can_be_persisted() {
        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null).setWebpage(new Hyperlink("http://www.amazon.com/date")));
        
        assertEquals(new Hyperlink("http://www.amazon.com/date"), date.getWebpage());
    }

    @Test
    public void entities_can_be_queried_with_like_operation_and_ordered_by_hyperlinks() {
        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        TgAuthor shcherbyna = save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych").setWebpage(new Hyperlink("http://www.amazon.com/shchernyna")));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null).setWebpage(new Hyperlink("http://www.amazon.com/date")));


        final EntityResultQueryModel<TgAuthor> query = select(TgAuthor.class).where().prop("webpage").like().val("%amazon%").model();
        final OrderingModel orderBy = orderBy().prop("webpage").asc().model();
        final QueryExecutionModel<TgAuthor, EntityResultQueryModel<TgAuthor>> qem = from(query).with(fetchAll(TgAuthor.class)).with(orderBy).model();

        final List<TgAuthor> authors = co(TgAuthor.class).getAllEntities(qem);
        assertEquals(2, authors.size());
        assertEquals(date, authors.get(0));
        assertEquals(shcherbyna, authors.get(1));
    }

    @Test
    public void entities_can_be_queried_with_eq_operation_against_hyperlink_value() {
        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych").setWebpage(new Hyperlink("http://www.amazon.com/shchernyna")));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null).setWebpage(new Hyperlink("http://www.amazon.com/date")));


        final EntityResultQueryModel<TgAuthor> query = select(TgAuthor.class).where().prop("webpage").eq().val(new Hyperlink("http://www.amazon.com/date")).model();
        final OrderingModel orderBy = orderBy().prop("webpage").asc().model();
        final QueryExecutionModel<TgAuthor, EntityResultQueryModel<TgAuthor>> qem = from(query).with(fetchAll(TgAuthor.class)).with(orderBy).model();

        final List<TgAuthor> authors = co(TgAuthor.class).getAllEntities(qem);
        assertEquals(1, authors.size());
        assertEquals(date, authors.get(0));
    }

    @Test
    public void entities_can_be_queried_with_eq_operation_against_hyperlinks_as_string() {
        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych").setWebpage(new Hyperlink("http://www.amazon.com/shchernyna")));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null).setWebpage(new Hyperlink("http://www.amazon.com/date")));


        final EntityResultQueryModel<TgAuthor> query = select(TgAuthor.class).where().prop("webpage").eq().val("http://www.amazon.com/date").model();
        final OrderingModel orderBy = orderBy().prop("webpage").asc().model();
        final QueryExecutionModel<TgAuthor, EntityResultQueryModel<TgAuthor>> qem = from(query).with(fetchAll(TgAuthor.class)).with(orderBy).model();

        final List<TgAuthor> authors = co(TgAuthor.class).getAllEntities(qem);
        assertEquals(1, authors.size());
        assertEquals(date, authors.get(0));
    }
}
