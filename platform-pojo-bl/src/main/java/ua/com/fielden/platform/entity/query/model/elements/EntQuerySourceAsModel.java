package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;

public class EntQuerySourceAsModel implements IEntQuerySource {
    private final List<EntQuery> models;

    public EntQuerySourceAsModel(final EntQuery... models) {
	super();
	this.models = Arrays.asList(models);
    }
}
