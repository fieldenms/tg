package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;

public class EntQuerySourcesModel {
    private final IEntQuerySource main;
    private final List<EntQueryCompoundSourceModel> compounds;

    public EntQuerySourcesModel(final IEntQuerySource main, final List<EntQueryCompoundSourceModel> compounds) {
	super();
	this.main = main;
	this.compounds = compounds;
    }

    public IEntQuerySource getMain() {
        return main;
    }

    public List<EntQueryCompoundSourceModel> getCompounds() {
        return compounds;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((compounds == null) ? 0 : compounds.hashCode());
	result = prime * result + ((main == null) ? 0 : main.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntQuerySourcesModel)) {
	    return false;
	}
	final EntQuerySourcesModel other = (EntQuerySourcesModel) obj;
	if (compounds == null) {
	    if (other.compounds != null) {
		return false;
	    }
	} else if (!compounds.equals(other.compounds)) {
	    return false;
	}
	if (main == null) {
	    if (other.main != null) {
		return false;
	    }
	} else if (!main.equals(other.main)) {
	    return false;
	}
	return true;
    }
}
