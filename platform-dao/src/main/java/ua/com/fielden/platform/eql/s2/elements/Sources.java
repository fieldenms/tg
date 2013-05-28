package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.utils.Pair;


public class Sources implements IElement2 {
    private final ISource2 main;
    private final List<CompoundSource> compounds;

    public Sources(final ISource2 main, final List<CompoundSource> compounds) {
	super();
	this.main = main;
	this.compounds = compounds;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(main.getValues());
	for (final CompoundSource compSource : compounds) {
	    result.addAll(compSource.getAllValues());
	}
	return result;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final CompoundSource compSource : compounds) {
	    result.addAll(compSource.getJoinConditions().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final CompoundSource compSource : compounds) {
	    result.addAll(compSource.getJoinConditions().getLocalProps());
	}
	return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(main);
        for (final CompoundSource compound : compounds) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public ISource2 getMain() {
        return main;
    }

    public List<CompoundSource> getCompounds() {
        return compounds;
    }

    public List<ISource2> getAllSources() {
	final List<ISource2> result = new ArrayList<ISource2>();
	result.add(main);
	for (final CompoundSource compound : compounds) {
	    result.add(compound.getSource());
	}
	return result;
    }

    public List<Pair<ISource2, Boolean>> getAllSourcesAndTheirJoinType() {
	final List<Pair<ISource2, Boolean>> result = new ArrayList<Pair<ISource2, Boolean>>();
	result.add(new Pair<ISource2, Boolean>(main, false));
	for (final CompoundSource compound : compounds) {
	    result.add(new Pair<ISource2, Boolean>(compound.getSource(), compound.getJoinType() == JoinType.LJ));
	}
	return result;
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
	if (!(obj instanceof Sources)) {
	    return false;
	}
	final Sources other = (Sources) obj;
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

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}