package ua.com.fielden.platform.web.test.server.config;

/**
 * Provides an API to consistently compose Web UI layouts for both entity centres and masters.
 */
public class LayoutComposer {
    public final static String MARGIN_PIX = "20px";

    public final static String CENTRE_LAYOUT_SPECIFICATION = "'horizontal', 'center', 'start-justified',";
    public final static String COMPONENT = "['flex']";
    public final static String COMPONENT_WITH_PADDING = "['flex', 'margin-right: " + MARGIN_PIX + "']";

    public final static String MASTER_LAYOUT_SPECIFICATION = "'horizontal','justified',";
    public final static String MASTER_ACTION_LAYOUT_SPECIFICATION = "'horizontal', 'padding: 20px 20px 0 20px', 'wrap', 'justify-content: center',";
    public final static String MASTER_ACTION_SPECIFICATION = "'margin: 10px', 'width: 110px'";

    /**
     * @param layout
     *            provides layout which will be modified by adding row of additional columns.
     * @param specification
     *            provides layout specification.
     * @param colNumber
     *            number of columns that should be added.
     */
    private static void appendRow(final StringBuilder layout, final String specification, final int colNumber) {
        layout.append("[").append(specification);
        for (int col = 0; col < colNumber - 1; col++) {
            layout.append(COMPONENT_WITH_PADDING).append(",");
        }
        layout.append(COMPONENT).append("],");
    }

    /**
     * Produces consistent rectangular layout for centre with rowNumber by columnNumber dimension.
     *
     * @param rowNumber
     *            specifies number of rows in the layout that would be produced.
     * @param colNumber
     *            specifies number of columns in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkGridForCentre(3, 2) will produce [[[],[]], [[],[]], [[],[]],]
     */

    public static String mkGridForCentre(final int rowNumber, final int colNumber) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['vertical',");
        for (int row = 0; row < rowNumber; row++) {
            appendRow(layout, CENTRE_LAYOUT_SPECIFICATION, colNumber);
        }
        layout.append("]");
        return layout.toString();
    }


    /**
     * Produces an inconsistent rectangular layout for centre with different number of columns in rows.
     *
     * @param numColsInFirstRow
     *            specifies number of columns in the first row. Prevents incorrect usage of API without specifying number of columns and providing empty array.
     * @param colsPerSecondRowOnwards
     *            array that specifies number of columns in each rows (starting from the second row) in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkVarGridForCentre(3, 2) will produce two rows with 3 components in the first row and 2 components in the second row [[[],[], []], [[],[]],]
     */

    public static String mkVarGridForCentre(final int numColsInFirstRow, final int... colsPerSecondRowOnwards) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['vertical',");
        //processing the first row
        appendRow(layout, CENTRE_LAYOUT_SPECIFICATION, numColsInFirstRow);
        //processing the array
        for (final int colsInRow : colsPerSecondRowOnwards) {
            appendRow(layout, CENTRE_LAYOUT_SPECIFICATION, colsInRow);
        }
        layout.append("]");
        return layout.toString();
    }

    /**
     * Produces a consistent rectangular layout for simple master with rowNumber by columnNum dimension.
     *
     * @param width
     *            specifies the width of the layout that would be produced.
     * @param rowNumber
     *            specifies number of rows in the layout that would be produced.
     * @param colNumber
     *            specifies number of columns in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkGridForMaster(500, 3, 2) will produce [[[],[]], [[],[]], [[],[]]]
     * @return
     */
    public static String mkGridForMaster(final int width, final int rowNumber, final int colNumber) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['padding:20px', 'width:").append(width).append("px',");
        for (int row = 0; row < rowNumber; row++) {
            appendRow(layout, MASTER_LAYOUT_SPECIFICATION, colNumber);
        }
        layout.deleteCharAt(layout.length() - 1);
        layout.append("]");
        return layout.toString();
    }

    /**
     * Produces an inconsistent rectangular layout for simple master with different number of columns in rows.
     *
     * @param width
     *            specifies the width of the layout that would be produced.
     * @param numColsInFirstRow
     *            specifies number of columns in the first row. Prevents incorrect usage of API without specifying number of columns and providing empty array.
     * @param colsPerSecondRowOnwards
     *            array that specifies number of columns in each rows in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkVarGridForMaster(500, 3, 1, 2 ) will produce three rows with 3 components in the first row, 1 component in the second row and 2 components in the
     *         third row [[[],[],[]], [[]], [[],[]],]
     */
    public static String mkVarGridForMaster(final int width, final int numColsInFirstRow, final int... colsPerSecondRowOnwards) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['padding:20px', 'width:").append(width).append("px',");
        //processing the first row
        appendRow(layout, MASTER_LAYOUT_SPECIFICATION, numColsInFirstRow);
        //processing the array
        for (final int colsInRow : colsPerSecondRowOnwards) {
            appendRow(layout, MASTER_LAYOUT_SPECIFICATION, colsInRow);
        }
        layout.deleteCharAt(layout.length() - 1);
        layout.append("]");
        return layout.toString();
    }

    /**
     * Produces a consistent rectangular layout for embedded master with rowNumber by columnNum dimension.
     *
     * @param rowNumber
     *            specifies number of rows in the layout that would be produced.
     * @param colNumber
     *            specifies number of columns in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkGridForEmbeddedMaster(3, 2) will produce [[[],[]], [[],[]], [[],[]]]
     * @return
     */
    public static String mkGridForEmbeddedMaster(final int rowNumber, final int colNumber) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['padding:20px',");
        for (int row = 0; row < rowNumber; row++) {
            appendRow(layout, MASTER_LAYOUT_SPECIFICATION, colNumber);
        }
        layout.deleteCharAt(layout.length() - 1);
        layout.append("]");
        return layout.toString();
    }

    /**
     * Produces an inconsistent rectangular layout for embedded master with different number of columns in rows.
     *
     * @param numColsInFirstRow
     *            specifies number of columns in the first row. Prevents incorrect usage of API without specifying number of columns and providing empty array.
     * @param colsPerSecondRowOnwards
     *            array that specifies number of columns in each rows in the layout that would be produced.
     * @return String representation of the layout.
     *         <p>
     *         For example, mkVarGridForEmbeddedMaster(3, 1, 2 ) will produce three rows with 3 components in the first row, 1 component in the second row and 2 components in the
     *         third row [[[],[],[]], [[]], [[],[]],]
     */
    public static String mkVarGridForEmbeddedMaster(final int numColsInFirstRow, final int... colsPerSecondRowOnwards) {
        final StringBuilder layout = new StringBuilder();
        layout.append("['padding:20px',");
        //processing the first row
        appendRow(layout, MASTER_LAYOUT_SPECIFICATION, numColsInFirstRow);
        //processing the array
        for (final int colsInRow : colsPerSecondRowOnwards) {
            appendRow(layout, MASTER_LAYOUT_SPECIFICATION, colsInRow);
        }
        layout.deleteCharAt(layout.length() - 1);
        layout.append("]");
        return layout.toString();
    }

    public static String mkActionLayoutForMaster() {
        final StringBuilder layout = new StringBuilder();
        layout.append("[").append(MASTER_ACTION_LAYOUT_SPECIFICATION).append(",[").append(MASTER_ACTION_SPECIFICATION).append("],[").append(MASTER_ACTION_SPECIFICATION).append("]]");
        return layout.toString();
    }
}

