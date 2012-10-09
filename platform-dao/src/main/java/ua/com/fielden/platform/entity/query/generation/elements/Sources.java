package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.utils.Pair;


public class Sources implements IPropertyCollector {
    private final ISource main;
    private final List<CompoundSource> compounds;

    public Sources(final ISource main, final List<CompoundSource> compounds) {
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

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append(main.sql());
	for (final CompoundSource compoundSource : compounds) {
	    sb.append(" ");
	    sb.append(compoundSource.sql());
	}

	return sb.toString();
    }

    public void reorderSources() {
	boolean needOneMoreIteration = true;

	while (needOneMoreIteration) {
	    final List<ISource> availableSources = new ArrayList<>();
	    availableSources.add(main);

	    int index = 0;
	    needOneMoreIteration = false;
	    for (final CompoundSource compoundSource : compounds) {

		boolean allSourcesAvailable = true;
		for (final ISource involvedSource : compoundSource.getJoinConditions().getInvolvedSources()) {
		    if (!(compoundSource.getSource() == involvedSource || availableSources.contains(involvedSource))) {
			allSourcesAvailable = false;
			break;
		    }
		}

		if (allSourcesAvailable) {
		    availableSources.add(compoundSource.getSource());
		    index = index + 1;
		} else {
		    // swap
		    if (compounds.size() > index + 1) {
			final CompoundSource curr = compounds.get(index);
			final CompoundSource next = compounds.get(index + 1);
			compounds.set(index, next);
			compounds.set(index + 1, curr);
			needOneMoreIteration = true;
		    }

		    break;
		}
	    }
	}
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

    public ISource getMain() {
        return main;
    }

    public List<CompoundSource> getCompounds() {
        return compounds;
    }

    public List<ISource> getAllSources() {
	final List<ISource> result = new ArrayList<ISource>();
	result.add(main);
	for (final CompoundSource compound : compounds) {
	    result.add(compound.getSource());
	}
	return result;
    }

    public List<Pair<ISource, Boolean>> getAllSourcesAndTheirJoinType() {
	final List<Pair<ISource, Boolean>> result = new ArrayList<Pair<ISource, Boolean>>();
	result.add(new Pair<ISource, Boolean>(main, false));
	for (final CompoundSource compound : compounds) {
	    result.add(new Pair<ISource, Boolean>(compound.getSource(), compound.getJoinType() == JoinType.LJ));
	}
	return result;
    }

    public void assignSqlAliases(final int masterIndex) {
	int sourceIndex = 0;
	for (final ISource source : getAllSources()) {
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
}