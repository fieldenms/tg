package ua.com.fielden.platform.security.provider;

import java.util.SortedMap;

import ua.com.fielden.platform.migration.AbstractRetriever;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

public class UserRetriever extends AbstractRetriever<User> {

    @Inject
    public UserRetriever(final IUser dao) {
        super(dao);
    }

    @Override
    public SortedMap<String, String> resultFields() {
        return map( //
        field("key", "USER_ID") //
        );
    }

    @Override
    public String fromSql() {
        return "CRAFT";
    }
}
