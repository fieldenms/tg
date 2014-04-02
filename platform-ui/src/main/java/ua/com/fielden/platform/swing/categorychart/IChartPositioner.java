package ua.com.fielden.platform.swing.categorychart;

/**
 * The contract that supports chart positioning functionality. This contract must be used only for panel with multiple charts.
 * 
 * @author TG Team
 * 
 */
public interface IChartPositioner {

    /**
     * Moves chart from fromIndex to toIndex
     * 
     * @param fromIndex
     *            - must be greater then 0 and less then chart panel count.
     * @param toIndex
     *            - must be greater then 0 and less then chart panel count.
     */
    void positionChart(int fromIndex, int toIndex);

    /**
     * Returns the chart panel count.
     * 
     * @return
     */
    int getChartPanelCount();
}
