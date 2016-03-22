package ua.com.fielden.platform.keygen;

import static java.lang.String.format;

import org.hibernate.LockOptions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * Hibernate driven implementation of {@link IKeyNumberGenerator);
 *
 * @author TG Team
 */
@EntityType(KeyNumber.class)
public class KeyNumberDao extends CommonEntityDao<KeyNumber> implements IKeyNumber {
    
    private final EntityFactory factory;
    
    @Inject
    protected KeyNumberDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
        this.factory = factory;
    }

    /**
     * This is a convenience method for retrieval of the next number.
     */
    @Override
    @SessionRequired
    public Integer nextNumber(final String key) {
        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic UPGRADE lock
            number = (KeyNumber) getSession().load(KeyNumber.class, number.getId(), LockOptions.UPGRADE);
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = factory.newByKey(KeyNumber.class, key).setValue("0");
        }

        final Integer nextNo = Integer.parseInt(number.getValue()) + 1;
        number.setValue(nextNo.toString());
        save(number);
        return nextNo;
    }

    /**
     * This is a convenience method for retrieval of the current number.
     */
    @Override
    @SessionRequired
    public Integer currNumber(final String key) {
        final KeyNumber number = findByKey(key); // find an instance
        if (number == null) {
            throw new EntityCompanionException(format("No number associated with key [%s].", key));
        }
        return Integer.valueOf(number.getValue());
    }
}