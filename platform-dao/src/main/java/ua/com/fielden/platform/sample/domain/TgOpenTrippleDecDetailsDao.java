package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/**
 * DAO implementation for companion object {@link ITgGeneratedEntity}.
 *
 * @author TG Team
 *
 */
@EntityType(TgOpenTrippleDecDetails.class)
public class TgOpenTrippleDecDetailsDao extends CommonEntityDao<TgOpenTrippleDecDetails> implements ITgOpenTrippleDecDetails {

    @Inject
    public TgOpenTrippleDecDetailsDao(final IFilter filter) {
        super(filter);
    }
}