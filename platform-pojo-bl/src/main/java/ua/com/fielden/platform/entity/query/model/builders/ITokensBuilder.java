package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public interface ITokensBuilder {

    void add(TokenCategory cat, Object value);
    boolean isClosing();
    boolean canBeClosed();
    void finaliseChild();
    Pair<TokenCategory, Object> getResult();

}
