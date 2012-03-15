package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link TgMeterReading} retrieval.
 *
 * @author TG Team
 */

@EntityType(TgMeterReading.class)
public class TgMeterReadingDao2 extends CommonEntityDao2<TgMeterReading> implements ITgMeterReading2 {

    @Inject
    protected TgMeterReadingDao2(final IFilter filter) {
	super(filter);
    }
}
