package ua.com.fielden.platform.keygen;

import static java.lang.String.format;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * Hibernate driven implementation of {@link IKeyNumberGenerator);
 *
 * @author TG Team
 */
@EntityType(KeyNumber.class)
public class KeyNumberDao extends CommonEntityDao<KeyNumber> implements IKeyNumber {
    
    private final EntityFactory factory;
    
    @Inject
    protected KeyNumberDao(final EntityFactory factory) {
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
            // re-fetch instance with pessimistic write lock
            number = (KeyNumber) getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = factory.newByKey(KeyNumber.class, key).setValue("0");
        }

        final Integer nextNo = Integer.parseInt(number.getValue()) + 1;
        number.setValue(nextNo.toString());
        save(number);
        return nextNo;
    }

    @Override
    @SessionRequired
    public SortedSet<Integer> nextNumbers(final String key, final int count) {
        if (count < 1) {
            return Collections.emptySortedSet();
        }

        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic write lock
            number = (KeyNumber) getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = factory.newByKey(KeyNumber.class, key).setValue("0");
        }

        final SortedSet<Integer> keys = IntStream.iterate(Integer.parseInt(number.getValue()) + 1, n -> n + 1).limit(count).boxed().collect(Collectors.toCollection(() -> new TreeSet<Integer>()));
        number.setValue(keys.last().toString());
        save(number);
        return keys;
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
