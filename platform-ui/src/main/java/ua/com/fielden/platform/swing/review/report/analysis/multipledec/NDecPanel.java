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
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.view.BasePanel;

public class NDecPanel<T extends AbstractEntity<?>> extends BasePanel{

    private static final long serialVersionUID = -4573644376854599911L;

    private final NDecPanelModel<T> model;

    private final JComponent[][] components;

    private boolean zoom =false;

    public NDecPanel(final NDecPanelModel<T> model){
	super(new MigLayout("fill, insets 0"));
	this.model = model;
	components = new JComponent[model.getDecCount()*2][];
	final JPanel decPanel = new JPanel(new MigLayout("fill, insets 0","[l][c]","[t]"));
	for(int decIndex = 0; decIndex < model.getDecCount(); decIndex++){
	    final DecView<T> decView = new DecView<T>(model.getDec(decIndex));
	    final JLabel chartTitle = getChartTitle(model.getDec(decIndex));
	    final JLabel stubLabel = stubLablel();
	    final JPanel calculatedNumberPanel = decView.getCalculatedNumberPanel();
	    final DecChartPanel chartPanel = decView.getChartPanel();
	    chartPanel.addAnalysisDoubleClickListener(createZoomListener(decPanel, decIndex));

	    components[2 * decIndex] = new JComponent[2];
	    components[2 * decIndex + 1] = new JComponent[2];
	    components[2 * decIndex][0] = stubLabel;
	    components[2 * decIndex][1] = chartTitle;
	    components[2 * decIndex + 1][0] = calculatedNumberPanel;
	    components[2 * decIndex + 1][1] = chartPanel;

	    decPanel.add(stubLabel);
	    decPanel.add(chartTitle, "wrap");
	    decPanel.add(decView.getCalculatedNumberPanel());
	    decPanel.add(decView.getChartPanel(), (decIndex < model.getDecCount() - 1) ? "wrap, grow, push":"grow, push");
	}
	final JScrollPane scrollPane = new JScrollPane(decPanel);
	//scrollPane.addComponentListener(createComponentResizedAdapter(scrollPane));
	add(scrollPane, "grow");
    }

    private IMultipleDecDoubleClickListener createZoomListener(final JPanel decPanel, final int decViewIndex) {
	return new IMultipleDecDoubleClickListener() {

	    @Override
	    public void doubleClick(final AnalysisDataEvent<ChartMouseEvent> event) {
		if(event.getData().getEntity() instanceof PlotEntity){
		    toggleZoom();
		}
	    }

	    private void toggleZoom() {
		decPanel.removeAll();
		final int startIndex = zoom ? 0 : decViewIndex;
		final int endIndex = zoom ? model.getDecCount()-1 : decViewIndex;
		for(int decIndex = startIndex; decIndex <= endIndex; decIndex++){
		    decPanel.add(components[2 * decIndex][0]);
		    decPanel.add(components[2 * decIndex][1], "wrap");
		    decPanel.add(components[2 * decIndex + 1][0]);
		    decPanel.add(components[2 * decIndex + 1][1], (decIndex < endIndex) ? "wrap, grow, push":"grow, push");
		}
		zoom = !zoom;
		decPanel.invalidate();
		decPanel.revalidate();
		decPanel.repaint();
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

}
