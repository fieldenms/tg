package ua.com.fielden.platform.swing.timeline;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 * A utility class for generating sample datasets for the demos.
 * <p>
 * These datasets are hard-coded so that they are easily accessible for the demonstration applications. In a real application, you would create datasets dynamically by reading data
 * from a file, a database, or some other source.
 * 
 * @author David Gilbert
 */
public abstract class DemoDatasetFactory {

    /**
     * Creates and returns a {@link CategoryDataset} for the demo charts.
     * 
     * @return a sample dataset.
     */
    public static CategoryDataset createCategoryDataset() {

	final double[][] data = new double[][] { { 10.0, 4.0, 15.0, 14.0 }, { -5.0, -7.0, 14.0, -3.0 }, { 6.0, 17.0, -12.0, 7.0 }, { 7.0, 15.0, 11.0, 0.0 },
		{ -8.0, -6.0, 10.0, -9.0 }, { 9.0, 8.0, 0.0, 6.0 }, { -10.0, 9.0, 7.0, 7.0 }, { 11.0, 13.0, 9.0, 9.0 }, { -3.0, 7.0, 11.0, -10.0 } };

	return DatasetUtilities.createCategoryDataset("Series ", "Category ", data);

    }

    /**
     * Creates and returns a category dataset with JUST ONE CATEGORY for the demo charts.
     * 
     * @return a sample category dataset.
     */
    public static CategoryDataset createSingleCategoryDataset() {

	final Number[][] data = new Integer[][] { { new Integer(10) }, { new Integer(-5) }, { new Integer(6) }, { new Integer(7) }, { new Integer(-8) }, { new Integer(9) },
		{ new Integer(-10) }, { new Integer(11) }, { new Integer(-3) } };

	return DatasetUtilities.createCategoryDataset("Series ", "Category ", data);

    }

}
