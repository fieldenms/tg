package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link TgMeterReading} retrieval.
 *
 * @author TG Team
 */

@EntityType(TgMeterReading.class)
public class TgMeterReadingDao extends CommonEntityDao<TgMeterReading> implements ITgMeterReading {

    @Inject
    protected TgMeterReadingDao(final IFilter filter) {
	super(filter);
    }
}
