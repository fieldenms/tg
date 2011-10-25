package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.reportquery.ChartModelChangedEvent;
import ua.com.fielden.platform.reportquery.ChartModelChangedListener;
import ua.com.fielden.platform.swing.analysis.ndec.DecChartPanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

public class DecView{

    private static final long serialVersionUID = -4925754026655660289L;

    private final DecModel model;

    private final JPanel calculatedNumberPanel;
    private final DecChartPanel chartPanel;

    public DecView(final DecModel model) {
	this.model = model;

	if(model.getCalcValuesNumber() > 0){
	    String rowConstraints= "";
	    for(int calcNumberIndex = 0; calcNumberIndex < model.getCalcValuesNumber()-1; calcNumberIndex++){
		rowConstraints += "[t]";
	    }
	    calculatedNumberPanel = new JPanel(new MigLayout("fill, insets 5", "[l][r]", rowConstraints + (model.getCalcValuesNumber() == 1 ? "[t]":"[t, grow]")));
	    for(final CalculatedNumber number : model.getCalcNumbers()){
		calculatedNumberPanel.add(DummyBuilder.label(number.getCaption()+": ", new Color(23, 92, 154)));
		final JLabel label = new JLabel(number.getConvertedNumber());
		number.addPropertyChangeListener(createNumberChangeListener(label));
		calculatedNumberPanel.add(label, "wrap");

	    }
	}else{
	    calculatedNumberPanel = null;
	}
	chartPanel = new DecChartPanel(model.createChart());
	model.getChartModel().addChartModelChangedListener(createChartModelChangedListener(chartPanel));
    }

    public JPanel getCalculatedNumberPanel(){
	return calculatedNumberPanel;
    }

    public DecChartPanel getChartPanel() {
	return chartPanel;
    }

    private ChartModelChangedListener createChartModelChangedListener(final DecChartPanel chartPanel) {
	return new ChartModelChangedListener() {

	    @Override
	    public void cahrtModelChanged(final ChartModelChangedEvent event) {
		chartPanel.setChart(getModel().createChart());
	    }
	};
    }

    private PropertyChangeListener createNumberChangeListener(final JLabel label) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		label.setText(((CalculatedNumber)evt.getSource()).getConvertedNumber());
	    }
	};
    }

    public DecModel getModel() {
	return model;
    }

}
