package ua.com.fielden.platform.keygen;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Collections;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/// Db-driven implementation of [IKeyNumber].
///
@EntityType(KeyNumber.class)
public class KeyNumberDao extends CommonEntityDao<KeyNumber> implements IKeyNumber {

    public static final String ERR_NO_NUMBER_FOR_KEY = "No number associated with key [%s].";

    @Override
    @SessionRequired
    public Either<Long, KeyNumber> save(final KeyNumber entity, final Optional<fetch<KeyNumber>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    /// {@inheritDoc}
    ///
    @Override
    @SessionRequired
    public Integer nextNumber(final String key, final int radix) {
        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic write lock
            number = getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
        } else { // this would most likely never happen since the target legacy db should already have some values in table NUMBERS
            number = new_().setKey(key).setValue("0");
        }

        final int nextNo = Integer.parseInt(number.getValue(), radix) + 1;
        number.setValue(Integer.toString(nextNo, radix).toUpperCase());
        save(number, Optional.empty());
        return nextNo;
    }

    /// {@inheritDoc}
    ///
    @Override
    @SessionRequired
    public SortedSet<Integer> nextNumbers(final String key, final int count, final int radix) {
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

        final SortedSet<Integer> keys = IntStream.iterate(Integer.parseInt(number.getValue(), radix) + 1, n -> n + 1).limit(count).boxed().collect(toCollection(TreeSet::new));
        number.setValue(Integer.toString(keys.last(), radix).toUpperCase());
        save(number, Optional.empty());
        return keys;
    }

    /// {@inheritDoc}
    ///
    @Override
    @SessionRequired
    public Integer currNumber(final String key, final int radix) {
        final KeyNumber number = findByKey(key); // find an instance
        if (number == null) {
            throw new EntityCompanionException(ERR_NO_NUMBER_FOR_KEY.formatted(key));
        }
        return Integer.valueOf(number.getValue(), radix);
    }

    @Override
    @SessionRequired
    public void reset(final String key) {
        KeyNumber number = findByKey(key); // find an instance
        if (number != null) {
            // re-fetch instance with pessimistic write lock
            number = getSession().load(KeyNumber.class, number.getId(), new LockOptions(LockMode.PESSIMISTIC_WRITE));
            number.setValue("0");
        }
        else {
            number = new_().setKey(key).setValue("0");
        }
        save(number, Optional.empty());
    }

}
