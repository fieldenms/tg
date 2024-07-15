package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public interface ITokensBuilder {

    void add(TokenCategory cat, Object value);

    boolean isClosing();

    void finaliseChild();

    Pair<TokenCategory, Object> getResult();

}
