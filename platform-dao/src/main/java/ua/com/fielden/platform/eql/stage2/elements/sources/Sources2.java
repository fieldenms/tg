package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.emptyList;

import java.util.List;

import com.google.common.base.Objects;

public class Sources2 {
    public final IQrySource2 main;
    private final List<CompoundSource2> compounds;

    public Sources2(final IQrySource2 main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public Sources2(final IQrySource2 main) {
        this(main, emptyList());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(main);
        for (final CompoundSource2 compound : compounds) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public List<CompoundSource2> getCompounds() {
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

        if (!(obj instanceof Sources2)) {
            return false;
        }
        
        final Sources2 other = (Sources2) obj;
        
        return Objects.equal(main, other.main) &&
                Objects.equal(compounds, other.compounds);
    }
}