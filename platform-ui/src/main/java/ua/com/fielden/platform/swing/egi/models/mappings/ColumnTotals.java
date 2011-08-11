package ua.com.fielden.platform.swing.egi.models.mappings;

import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

/**
 * Enumeration, defining policy for displaying totals for separate column.
 * 
 * @author yura
 * 
 */
public enum ColumnTotals {
    /**
     * No totals to be displayed for column.
     */
    NO_TOTALS {
	@Override
	public boolean hasGrandTotals() {
	    return false;
	}

	@Override
	public boolean hasGroupTotals() {
	    return false;
	}
    },
    /**
     * Only group totals to be displayed for column.
     */
    GROUP_TOTALS {
	@Override
	public boolean hasGrandTotals() {
	    return false;
	}

	@Override
	public boolean hasGroupTotals() {
	    return true;
	}
    },
    /**
     * Only grand totals to be displayed for column.
     */
    GRAND_TOTALS {
	@Override
	public boolean hasGrandTotals() {
	    return true;
	}

	@Override
	public boolean hasGroupTotals() {
	    return false;
	}
    },
    /**
     * Only grand totals displayed as separate footer below {@link EntityGridInspector} when using {@link EgiPanel}.
     */
    GRAND_TOTALS_SEPARATE_FOOTER {
	@Override
	public boolean hasGrandTotals() {
	    return false;
	}

	@Override
	public boolean hasGroupTotals() {
	    return false;
	}
    },
    /**
     * Both group and grand totals to be displayed for column.
     */
    GROUP_GRAND_TOTALS {
	@Override
	public boolean hasGrandTotals() {
	    return true;
	}

	@Override
	public boolean hasGroupTotals() {
	    return true;
	}
    };

    /**
     * Returns true if this column totals includes group totals. False otherwise.
     * 
     * @return
     */
    public abstract boolean hasGroupTotals();

    /**
     * Returns true if this column totals includes grand totals. False otherwise.
     * 
     * @return
     */
    public abstract boolean hasGrandTotals();
}
