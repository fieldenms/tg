package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.PlotEntity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.IChartPositioner;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.view.BasePanel;

public class NDecPanel<T extends AbstractEntity<?>> extends BasePanel implements IChartPositioner{

    private static final long serialVersionUID = -4573644376854599911L;

    private final NDecPanelModel<T> model;

    private final JPanel decPanel;

    private final JComponent[][] components;

    private boolean zoom =false;

    public NDecPanel(final NDecPanelModel<T> model){
	super(new MigLayout("fill, insets 0"));
	this.model = model;
	this.components = new JComponent[model.getDecCount() * 2][];
	this.decPanel = new JPanel(new MigLayout("fill, insets 0","[l][c]","[t]"));
	for(int decIndex = 0; decIndex < model.getDecCount(); decIndex++){
	    final DecView<T> decView = new DecView<T>(model.getDec(decIndex));
	    final JLabel chartTitle = getChartTitle(model.getDec(decIndex));
	    final JLabel stubLabel = stubLablel();
	    final JPanel calculatedNumberPanel = decView.getCalculatedNumberPanel();
	    final DecChartPanel chartPanel = decView.getChartPanel();
	    chartPanel.addAnalysisDoubleClickListener(createZoomListener(chartPanel));

	    components[2 * decIndex] = new JComponent[2];
	    components[2 * decIndex + 1] = new JComponent[2];
	    components[2 * decIndex][0] = stubLabel;
	    components[2 * decIndex][1] = chartTitle;
	    components[2 * decIndex + 1][0] = calculatedNumberPanel;
	    components[2 * decIndex + 1][1] = chartPanel;
	}
	final JScrollPane scrollPane = new JScrollPane(decPanel);
	//scrollPane.addComponentListener(createComponentResizedAdapter(scrollPane));
	add(scrollPane, "grow");
	layoutCharts(0, getChartPanelCount() - 1);
    }

    private IMultipleDecDoubleClickListener createZoomListener(final DecChartPanel chartPanel) {
	return new IMultipleDecDoubleClickListener() {

	    @Override
	    public void doubleClick(final AnalysisDataEvent<ChartMouseEvent> event) {
		if(event.getData().getEntity() instanceof PlotEntity){
		    toggleZoom();
		}
	    }

	    private void toggleZoom() {
		final int decViewIndex = indexOfChart(chartPanel);
		if(decViewIndex < 0){
		    return;
		}
		final int startIndex = zoom ? 0 : decViewIndex;
		final int endIndex = zoom ? getChartPanelCount() - 1 : decViewIndex;
		layoutCharts(startIndex, endIndex);
		zoom = !zoom;
	    }

	    private int indexOfChart(final DecChartPanel chartPanel) {
		for(int chartIndex = 0; chartIndex < getChartPanelCount(); chartIndex++){
		    if(components[2 * chartIndex + 1][1] == chartPanel){
			return chartIndex;
		    }
		}
		return -1;
	    }
	};
    }

    private JLabel stubLablel(){
	return new JLabel("");
    }

    private JLabel getChartTitle(final DecModel<T> decModel){
	final JLabel label = new JLabel(decModel.getChartName());
	label.setFont(new Font("SansSerif", Font.BOLD, 18));
	label.setHorizontalTextPosition(SwingConstants.CENTER);
	return label;
    }


    @Override
    public String getInfo() {
	return "Multiple dec view";
    }

    public void addAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l){
	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    ((DecChartPanel)components[componentIndex * 2 + 1][1]).addAnalysisDoubleClickListener(l);
	}
    }

    public void removeAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l){
	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    ((DecChartPanel)components[componentIndex * 2 + 1][1]).removeAnalysisDoubleClickListener(l);
	}
    }

    @Override
    public void positionChart(final int fromIndex, final int toIndex) {
	final JComponent[] tempComponents = saveComponentsIntoTemp(fromIndex);
	final int incrementCoef = (toIndex - fromIndex) < 0 ? -1 : 1;
	for (int index = fromIndex; incrementCoef * index < incrementCoef * toIndex; index += incrementCoef) {
	    moveChart(index, index + incrementCoef);
	}
	restoreFromTemp(tempComponents, toIndex);
	layoutCharts(0, getChartPanelCount() - 1);
    }

    private void restoreFromTemp(final JComponent[] temp, final int toIndex){
	components[2 * toIndex][0] = temp[0];
	components[2 * toIndex][1] = temp[1];
	components[2 * toIndex + 1][0] = temp[2];
	components[2 * toIndex + 1][1] = temp[3];
    }

    private void moveChart(final int from, final int to) {
	components[2 * from][0] = components[2 * to][0];
	components[2 * from][1] = components[2 * to][1];
	components[2 * from + 1][0] = components[2 * to + 1][0];
	components[2 * from + 1][1] = components[2 * to + 1][1];
    }

    private JComponent[] saveComponentsIntoTemp(final int index){
	final JComponent[] tempComponents = new JComponent[4];
	tempComponents[0] = components[2 * index][0];
	tempComponents[1] = components[2 * index][1];
	tempComponents[2] = components[2 * index + 1][0];
	tempComponents[3] = components[2 * index + 1][1];
	return tempComponents;
    }

    @Override
    public int getChartPanelCount() {
	return model.getDecCount();
    }

    private void layoutCharts(final int fromIndex, final int toIndex){
	decPanel.removeAll();
	for(int decIndex = fromIndex; decIndex <= toIndex; decIndex++){
	    decPanel.add(components[2 * decIndex][0]);
	    decPanel.add(components[2 * decIndex][1], "wrap");
	    decPanel.add(components[2 * decIndex + 1][0]);
	    decPanel.add(components[2 * decIndex + 1][1], (decIndex < toIndex) ? "wrap, grow, push":"grow, push");
	}
	invalidate();
	revalidate();
	repaint();
    }

}
