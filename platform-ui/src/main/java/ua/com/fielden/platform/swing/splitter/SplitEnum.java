package ua.com.fielden.platform.swing.splitter;

import javax.swing.JSplitPane;

/**
 * defines values for splitting and abstract method that builds the next splitting pane for the given SplitEnum value and current splitting pane
 * 
 * @author oleh
 * 
 */
public enum SplitEnum {
    /**
     * indicates the proper split action. that action also creates the split panel and according to the previous splitting action set the side of the split pane
     */
    NORTH {
	@Override
	JSplitPane splitNext(final SplitEnum currentSplit, final JSplitPane currentSplitPane) {
	    final int orientation = JSplitPane.VERTICAL_SPLIT;
	    String side = "";
	    switch (currentSplit) {
	    case NORTH:
	    case SOUTH:
		side = JSplitPane.TOP;
		break;
	    case EAST:
		side = JSplitPane.LEFT;
		break;
	    case WEST:
		side = JSplitPane.RIGHT;
		break;
	    }
	    return split(currentSplitPane, orientation, side);
	}
    },
    /**
     * indicates the proper split action. that action also creates the split panel and according to the previous splitting action set the side of the split pane
     */
    SOUTH {
	@Override
	JSplitPane splitNext(final SplitEnum currentSplit, final JSplitPane currentSplitPane) {
	    final int orientation = JSplitPane.VERTICAL_SPLIT;
	    String side = "";
	    switch (currentSplit) {
	    case NORTH:
	    case SOUTH:
		side = JSplitPane.BOTTOM;
		break;
	    case EAST:
		side = JSplitPane.LEFT;
		break;
	    case WEST:
		side = JSplitPane.RIGHT;
		break;
	    }
	    return split(currentSplitPane, orientation, side);
	}
    },
    /**
     * indicates the proper split action. that action also creates the split panel and according to the previous splitting action set the side of the split pane
     */
    WEST {
	@Override
	JSplitPane splitNext(final SplitEnum currentSplit, final JSplitPane currentSplitPane) {
	    final int orientation = JSplitPane.HORIZONTAL_SPLIT;
	    String side = "";
	    switch (currentSplit) {
	    case EAST:
	    case WEST:
		side = JSplitPane.LEFT;
		break;
	    case NORTH:
		side = JSplitPane.BOTTOM;
	    case SOUTH:
		side = JSplitPane.TOP;
		break;

	    }
	    return split(currentSplitPane, orientation, side);
	}
    },
    /**
     * indicates the proper split action. that action also creates the split panel and according to the previous splitting action set the side of the split pane
     */
    EAST {
	@Override
	JSplitPane splitNext(final SplitEnum currentSplit, final JSplitPane currentSplitPane) {
	    final int orientation = JSplitPane.HORIZONTAL_SPLIT;
	    String side = "";
	    switch (currentSplit) {
	    case EAST:
	    case WEST:
		side = JSplitPane.RIGHT;
		break;
	    case NORTH:
		side = JSplitPane.BOTTOM;
	    case SOUTH:
		side = JSplitPane.TOP;
		break;

	    }
	    return split(currentSplitPane, orientation, side);
	}
    };
    /**
     * splits current split pane in to two parts. Set as the left or right component for the given current split pane according to the current split action
     * 
     * @param currentSplit
     * @param currentSplitPane
     * @return
     */
    abstract JSplitPane splitNext(final SplitEnum currentSplit, final JSplitPane currentSplitPane);

    /**
     * creates split pane and set it as the left or right component for the given current split pane
     * 
     * @param currentSplitPane
     * @param orientation
     * @param side
     * @return
     */
    protected JSplitPane split(final JSplitPane currentSplitPane, final int orientation, final String side) {
	final JSplitPane nestedPane = new JSplitPane(orientation, null, null);
	currentSplitPane.add(nestedPane, side);
	return nestedPane;
    }
}
