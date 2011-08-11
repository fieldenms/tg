package ua.com.fielden.platform.swing.review;

import java.io.File;
import java.io.IOException;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.file.ExtensionFileFilter;

/**
 * Contract for anything that represents the detail view of the data on the {@link EntityReview} panel.
 * 
 * @author oleh
 * 
 */
public interface IReviewContract {

    /**
     * Returns the {@link BlockingIndefiniteProgressLayer} for this detail view component.
     * 
     * @return
     */
    BlockingIndefiniteProgressLayer getBlockingLayer();

    /**
     * Returns {@link ExtensionFileFilter} that is used for selecting file to export data into it.
     * 
     * @return
     */
    ExtensionFileFilter getExtensionFilter();

    /**
     * Returns extension for exporting files.
     * 
     * @return
     */
    String getExtension();

    /**
     * Export data in to specified file.
     * 
     * @param file
     */
    Result exportData(File file) throws IOException;

    /**
     * Returns value that indicates whether review contract can be exported into external files or not.
     * 
     * @return
     */
    boolean canExport();

    /**
     * Perform any preparation here before locking the layer on the detail view (grid or chart).
     * 
     * @return Returns value that indicates whether to continue perform the action or abort it.
     */
    boolean beforeUpdate();

    /**
     * Perform here any action in order to retrieve data from data base or where else.
     * 
     * @return returns retrieved data.
     */
    Result getData();

    /**
     * Perform here action to provide data for the detail view component.
     * 
     * @param data
     *            - data that must be provide for the detail view component
     */
    void setDataToView(Object data);

    /**
     * Enable or disable action during run action.
     * 
     * @param enable
     */
    void setActionEnabled(boolean enable);
}
