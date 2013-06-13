package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CompoundSource2;
import ua.com.fielden.platform.eql.s2.elements.ISource2;
import ua.com.fielden.platform.eql.s2.elements.Sources2;
import ua.com.fielden.platform.utils.Pair;


public class Sources1 implements IElement1<Sources2> {
    private final ISource1<? extends ISource2> main;
    private final List<CompoundSource1> compounds;

    public Sources1(final ISource1<? extends ISource2> main, final List<CompoundSource1> compounds) {
	super();
	this.main = main;
	this.compounds = compounds;
    }

    @Override
    public Sources2 transform(final TransformatorToS2 resolver) {
	final List<CompoundSource2> transformed = new ArrayList<>();
	for (final CompoundSource1 compoundSource : compounds) {
	    transformed.add(new CompoundSource2(compoundSource.getSource().transform(resolver), compoundSource.getJoinType(), //
		    compoundSource.getJoinConditions().transform(resolver)));
	}
	return new Sources2(main.transform(resolver), transformed);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(main);
        for (final CompoundSource1 compound : compounds) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public ISource1 getMain() {
        return main;
    }

    public List<CompoundSource1> getCompounds() {
        return compounds;
    }

    public List<ISource1> getAllSources() {
	final List<ISource1> result = new ArrayList<ISource1>();
	result.add(main);
	for (final CompoundSource1 compound : compounds) {
	    result.add(compound.getSource());
	}
	return result;
    }

    public List<Pair<ISource1, Boolean>> getAllSourcesAndTheirJoinType() {
	final List<Pair<ISource1, Boolean>> result = new ArrayList<Pair<ISource1, Boolean>>();
	result.add(new Pair<ISource1, Boolean>(main, false));
	for (final CompoundSource1 compound : compounds) {
	    result.add(new Pair<ISource1, Boolean>(compound.getSource(), compound.getJoinType() == JoinType.LJ));
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
	if (!(obj instanceof Sources1)) {
	    return false;
	}
	final Sources1 other = (Sources1) obj;
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