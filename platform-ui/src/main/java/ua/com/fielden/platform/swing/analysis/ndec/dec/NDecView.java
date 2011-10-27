package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.PlotEntity;

import ua.com.fielden.platform.swing.analysis.ndec.DecChartPanel;
import ua.com.fielden.platform.swing.analysis.ndec.IAnalysisDoubleClickListener;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.view.BasePanel;

public class NDecView extends BasePanel{

    private static final long serialVersionUID = -4573644376854599911L;

    private static final int DEFAULT_MIN_HEIGHT = 225;
    private static final int DEFAULT_MIN_WIDTH = 400;

    private final NDecModel model;

    private final JComponent[][] components;

    private int minHeight = DEFAULT_MIN_HEIGHT;
    private int minWidth = DEFAULT_MIN_WIDTH;

    private boolean zoom =false;

    public NDecView(final NDecModel model, final int minHeight){
	this(model);
	this.minHeight = minHeight;
    }

    public NDecView(final NDecModel model){
	super(new MigLayout("fill, insets 0"));
	this.model = model;
	components = new JComponent[model.getDecCount()*2][];
	final JPanel decPanel = new JPanel(new MigLayout("fill, debug, insets 0","[l][c]","[t]"));
	for(int decIndex = 0; decIndex < model.getDecCount(); decIndex++){
	    final DecView decView = new DecView(model.getDec(decIndex));
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

    private IAnalysisDoubleClickListener createZoomListener(final JPanel decPanel, final int decViewIndex) {
	return new IAnalysisDoubleClickListener() {

	    @Override
	    public void doubleClick(final AnalysisDoubleClickEvent event) {
		final ChartMouseEvent chartEvent = (ChartMouseEvent) event.getSourceMouseEvent();
		if(chartEvent.getEntity() instanceof PlotEntity){
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

    private ComponentListener createComponentResizedAdapter(final JScrollPane scrollPane){
	return new ComponentAdapter() {
	    @Override
	    public void componentResized(final ComponentEvent e) {
		changeComponentSize(scrollPane);
	    }
	};
    }

    private void changeComponentSize(final JScrollPane scrollPane){
	int height = scrollPane.getHeight();
	int width = scrollPane.getWidth();

	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    height -= components[componentIndex * 2][1].getHeight();
	}
	width -= getMaximumCalculatedPanelWidth();

	height = height / (components.length / 2);
	if(height < minHeight){
	    height = minHeight;
	}
	if(width < minWidth){
	    width = minWidth;
	}
	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    final int titleHeight = components[componentIndex * 2][1].getPreferredSize().height;
	    final int titleWidth = getMaximumCalculatedPanelWidth();
	    components[componentIndex * 2][0].setPreferredSize(new Dimension(titleWidth, titleHeight));
	    //components[componentIndex * 2][1].setPreferredSize(new Dimension(width, titleHeight));
	    components[componentIndex * 2 + 1][0].setPreferredSize(new Dimension(titleWidth, height));
	    components[componentIndex * 2 + 1][1].setPreferredSize(new Dimension(width, height));
	}
	scrollPane.invalidate();
	scrollPane.revalidate();
    }

    private int getMaximumCalculatedPanelWidth(){
	int maxWidth = 0;
	for(int decIndex = 0; decIndex < model.getDecCount(); decIndex++){
	    maxWidth = maxWidth < components[decIndex * 2 + 1][0].getWidth() ? components[decIndex * 2 + 1][0].getWidth() : maxWidth;
	}
	return maxWidth;
    }

    public void setMinChartHeight(final int minHeight){
	this.minHeight = minHeight;
    }

    public void setMinChartWidth(final int minWidth){
	this.minWidth = minWidth;
    }


    private JLabel stubLablel(){
	return new JLabel("");
    }

    private JLabel getChartTitle(final DecModel decModel){
	final JLabel label = new JLabel(decModel.getChartName());
	label.setFont(new Font("SansSerif", Font.BOLD, 18));
	label.setHorizontalTextPosition(SwingConstants.CENTER);
	return label;
    }


    @Override
    public String getInfo() {
	return "Multiple dec view";
    }

    public void addAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    ((DecChartPanel)components[componentIndex * 2 + 1][1]).addAnalysisDoubleClickListener(l);
	}
    }

    public void removeAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	for(int componentIndex = 0; componentIndex < (components.length / 2); componentIndex++){
	    ((DecChartPanel)components[componentIndex * 2 + 1][1]).removeAnalysisDoubleClickListener(l);
	}
    }

}
