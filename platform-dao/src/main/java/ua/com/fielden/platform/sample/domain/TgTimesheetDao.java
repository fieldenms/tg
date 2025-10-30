package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;

import java.util.stream.Stream;

/// DAO for retrieving TgTimesheets.
///
@EntityType(TgTimesheet.class)
public class TgTimesheetDao extends CommonEntityDao<TgTimesheet> implements ITgTimesheet {

    @Override
    @SessionRequired
    public int batchInsert(Stream<TgTimesheet> newEntities, int batchSize) {
        return defaultBatchInsert(newEntities, batchSize);
    }

}
