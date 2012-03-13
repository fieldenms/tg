package ua.com.fielden.platform.dao2;

import java.lang.reflect.Field;

import ua.com.fielden.platform.reflection.Finder;

public class UsernameSetterMixin2 {

    public static final void setUsername(final String username, final Object usernameObtainer, final Field obtainerUsernameField) {
	if (obtainerUsernameField.getType() != String.class) {
	    throw new IllegalArgumentException("Field for holding username must be of time String.");
	}

	final String prevUsername;
	final boolean accessFlag = obtainerUsernameField.isAccessible();
	try {
	    obtainerUsernameField.setAccessible(true);
	    prevUsername = (String) obtainerUsernameField.get(usernameObtainer);
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	} finally {
	    obtainerUsernameField.setAccessible(accessFlag);
	}

	if (username == null) {
	    throw new IllegalArgumentException("Username of value null is not an acceptable value.");
	} else if (prevUsername != null && !prevUsername.equals(username)) {
	    throw new IllegalStateException("Username should be assigned only once during DAO instance life cycle.");
	} else if (prevUsername == null) {
	    final boolean thisFlag = obtainerUsernameField.isAccessible();
	    try {
		obtainerUsernameField.setAccessible(true);
		obtainerUsernameField.set(usernameObtainer, username);
	    } catch (final Exception e) {
		throw new IllegalStateException(e);
	    } finally {
		obtainerUsernameField.setAccessible(thisFlag);
	    }
	    // assigned username to all other aggregated DAO instances
	    for (final Field field : Finder.getFieldsOfSpecifiedType(usernameObtainer.getClass(), IEntityDao2.class)) {
		final boolean flag = field.isAccessible();
		try {
		    field.setAccessible(true);
		    final IEntityDao2<?> dao = (IEntityDao2<?>) field.get(usernameObtainer);
		    if (dao != null) {
			dao.setUsername(username);
		    }
		} catch (final Exception e) {
		    throw new IllegalStateException(e);
		} finally {
		    field.setAccessible(flag);
		}
	    }
	    // assigned username to all other aggregated DAO instances
	    for (final Field field : Finder.getFieldsOfSpecifiedType(usernameObtainer.getClass(), IEntityAggregatesDao2.class)) {
		final boolean flag = field.isAccessible();
		try {
		    field.setAccessible(true);
		    final IEntityAggregatesDao2 dao = (IEntityAggregatesDao2) field.get(usernameObtainer);
		    if (dao != null) {
			dao.setUsername(username);
		    }
		} catch (final Exception e) {
		    throw new IllegalStateException(e);
		} finally {
		    field.setAccessible(flag);
		}
	    }
	}
    }

}
