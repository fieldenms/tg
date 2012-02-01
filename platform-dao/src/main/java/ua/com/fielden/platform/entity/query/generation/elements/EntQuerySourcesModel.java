package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.utils.Pair;


public class EntQuerySourcesModel {
    private final IEntQuerySource main;
    private final List<EntQueryCompoundSourceModel> compounds;

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append(main.sql());
	for (final EntQueryCompoundSourceModel compoundSource : compounds) {
	    sb.append(" ");
	    sb.append(compoundSource.sql());
	}

	return sb.toString();
    }

    public EntQuerySourcesModel(final IEntQuerySource main, final List<EntQueryCompoundSourceModel> compounds) {
	super();
	this.main = main;
	this.compounds = compounds;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(main);
        for (final EntQueryCompoundSourceModel compound : compounds) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public IEntQuerySource getMain() {
        return main;
    }

    public List<EntQueryCompoundSourceModel> getCompounds() {
        return compounds;
    }

    public List<IEntQuerySource> getAllSources() {
	final List<IEntQuerySource> result = new ArrayList<IEntQuerySource>();
	result.add(main);
	for (final EntQueryCompoundSourceModel compound : compounds) {
	    result.add(compound.getSource());
	}
	return result;
    }

    public List<Pair<IEntQuerySource, Boolean>> getAllSourcesAndTheirJoinType() {
	final List<Pair<IEntQuerySource, Boolean>> result = new ArrayList<Pair<IEntQuerySource, Boolean>>();
	result.add(new Pair<IEntQuerySource, Boolean>(main, false));
	for (final EntQueryCompoundSourceModel compound : compounds) {
	    result.add(new Pair<IEntQuerySource, Boolean>(compound.getSource(), compound.getJoinType() == JoinType.LJ));
	}
	return result;
    }

    public void assignSqlAliases(final int masterIndex) {
	int sourceIndex = 0;
	for (final IEntQuerySource source : getAllSources()) {
	    sourceIndex = sourceIndex + 1;
	    source.assignSqlAlias("Q" + sourceIndex + (masterIndex == 0 ? "" : ("L" + masterIndex)));
	}
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