package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.BasicStroke;
import java.io.Serializable;

public class DefaultStroke extends BasicStroke implements Serializable {

    public DefaultStroke() {
        super();
    }

    public DefaultStroke(final float width, final int cap, final int join, final float miterlimit, final float[] dash, final float dash_phase) {
        super(width, cap, join, miterlimit, dash, dash_phase);
    }

    public DefaultStroke(final float width, final int cap, final int join, final float miterlimit) {
        super(width, cap, join, miterlimit);
    }

    public DefaultStroke(final float width, final int cap, final int join) {
        super(width, cap, join);
    }

    public DefaultStroke(final float width) {
        super(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /**
     *
     */
    private static final long serialVersionUID = 4071948794442724141L;

}
