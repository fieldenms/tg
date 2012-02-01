package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class OrderedQueryModel extends QueryModel {

    OrderedQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

}
