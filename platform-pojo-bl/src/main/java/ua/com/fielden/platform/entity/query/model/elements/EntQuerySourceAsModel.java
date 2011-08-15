package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;

public class EntQuerySourceAsModel implements IEntQuerySource {
    private final List<EntQuery> models;
    private final String alias;

    public EntQuerySourceAsModel(final String alias, final EntQuery... models) {
	super();
	this.alias = alias;
	this.models = Arrays.asList(models);
    }

    public String getAlias() {
        return alias;
    }
}
