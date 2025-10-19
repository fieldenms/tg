package ua.com.fielden.platform.keygen;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Db-driven implementation of [IKeyNumber].
///
@EntityType(KeyNumber.class)
public class KeyNumberDao extends CommonEntityDao<KeyNumber> implements IKeyNumber {

    public static final String ERR_NO_NUMBER_FOR_KEY = "No number associated with key [%s].";

    /// Retrieves the next number for a `key`.
    ///
    @Override
    @SessionRequired
    public Integer nextNumber(final String key) {
        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic write lock
            number = getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = new_().setKey(key).setValue("0");
        }

        final Integer nextNo = Integer.parseInt(number.getValue()) + 1;
        number.setValue(nextNo.toString());
        save(number);
        return nextNo;
    }

    /// Retrieves the next `count` numbers for a `key`.
    ///
    @Override
    @SessionRequired
    public SortedSet<Integer> nextNumbers(final String key, final int count) {
        if (count < 1) {
            return Collections.emptySortedSet();
        }

        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic write lock
            number = getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = new_().setKey(key).setValue("0");
        }

        final SortedSet<Integer> keys = IntStream.iterate(Integer.parseInt(number.getValue()) + 1, n -> n + 1).limit(count).boxed().collect(Collectors.toCollection(() -> new TreeSet<Integer>()));
        number.setValue(keys.last().toString());
        save(number);
        return keys;
    }

    /// Retrieves the current number for a `key`.
    ///
    @Override
    @SessionRequired
    public Integer currNumber(final String key) {
        final KeyNumber number = findByKey(key); // find an instance
        if (number == null) {
            throw new EntityCompanionException(ERR_NO_NUMBER_FOR_KEY.formatted(key));
        }
        return Integer.valueOf(number.getValue());
    }

}
