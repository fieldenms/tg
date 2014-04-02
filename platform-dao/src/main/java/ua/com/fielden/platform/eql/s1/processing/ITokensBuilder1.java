package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public interface ITokensBuilder1 {

    void add(TokenCategory cat, Object value);

    boolean isClosing();

    boolean canBeClosed();

    void finaliseChild();

    Pair<TokenCategory, Object> getResult();

}
