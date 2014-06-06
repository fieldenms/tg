package ua.com.fielden.platform.keygen;

import org.hibernate.LockMode;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Hibernate driven implementation of {@link IKeyNumberGenerator);
 *
 * @author 01es
 */
@EntityType(KeyNumber.class)
public class KeyNumberDao extends CommonEntityDao<KeyNumber> implements IKeyNumber {

    @Inject
    protected KeyNumberDao(final IFilter filter) {
        super(filter);
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
            number = (KeyNumber) getSession().get(KeyNumber.class, number.getId(), LockMode.UPGRADE);
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = new KeyNumber(key, "0");
        }

        final Integer nextNo = new Integer(Integer.parseInt(number.getValue()) + 1);
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
            throw new RuntimeException("No number associated with '" + key + "'.");
        }
        return new Integer(number.getValue());
    }
}