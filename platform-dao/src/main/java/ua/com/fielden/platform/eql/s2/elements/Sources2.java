package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

public class Sources2 implements IElement2 {
    private final ISource2 main;
    private final List<CompoundSource2> compounds;

    public Sources2(final ISource2 main, final List<CompoundSource2> compounds) {
        super();
        this.main = main;
        this.compounds = compounds;
    }

    public Sources2(final ISource2 main) {
        super();
        this.main = main;
        this.compounds = new ArrayList<CompoundSource2>();
    }

    @Override
    public boolean ignore() {
        return false;
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

    public ISource2 getMain() {
        return main;
    }

    public List<CompoundSource2> getCompounds() {
        return compounds;
    }

    public List<ISource2> getAllSources() {
        final List<ISource2> result = new ArrayList<ISource2>();
        result.add(main);
        for (final CompoundSource2 compound : compounds) {
            result.add(compound.getSource());
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
        if (!(obj instanceof Sources2)) {
            return false;
        }
        final Sources2 other = (Sources2) obj;
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