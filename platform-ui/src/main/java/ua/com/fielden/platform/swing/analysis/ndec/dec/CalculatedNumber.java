package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.reportquery.ChartModelChangedEvent;
import ua.com.fielden.platform.reportquery.ChartModelChangedListener;
import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;

public abstract class CalculatedNumber {

    private final String caption;

    private final EventListenerList listenerList;

    private final NumberFormat numberFormat;

    private Number number;

    public CalculatedNumber(final String caption, final ICategoryChartEntryModel chartModel, final CalculatedNumber... calculatedNumbers){
	this.caption = caption;
	this.number = 0;

	this.numberFormat = new DecimalFormat("#,##0.00");
	numberFormat.setRoundingMode(RoundingMode.HALF_UP);
	this.listenerList = new EventListenerList();

	if(chartModel != null){
	    chartModel.addChartModelChangedListener(createChartModelChangedListener());
	}

	for(final CalculatedNumber number : calculatedNumbers){
	    number.addPropertyChangeListener(createNumberPropertyChangeListener());
	}
    }

    public CalculatedNumber(final String caption, final CalculatedNumber... calculatedNumbers){
	this(caption, null, calculatedNumbers);
    }

    private PropertyChangeListener createNumberPropertyChangeListener() {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		setNumber(calculate());
	    }
	};
    }

    private ChartModelChangedListener createChartModelChangedListener() {
	return new ChartModelChangedListener() {

	    @Override
	    public void cahrtModelChanged(final ChartModelChangedEvent event) {
		setNumber(calculate());
	    }
	};
    }

    abstract protected Number calculate();

    public final void setNumber(final Number number) {
	final Number oldNumber = this.number;
	this.number = number;
	notifyNumberChanged(new PropertyChangeEvent(this, "number", oldNumber, number));
    }

    public Number getNumber() {
	return number;
    }

    public String getCaption() {
	return caption;
    }

    public String getConvertedNumber(){
	return numberFormat.format(number);
    }

    @Override
    public String toString() {
	return caption + ": " + numberFormat.format(number);
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
	listenerList.add(PropertyChangeListener.class, l);
    }

    public void removePropertyChangeListener(final PropertyChangeListener l){
	listenerList.remove(PropertyChangeListener.class, l);
    }

    private void notifyNumberChanged(final PropertyChangeEvent event){
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==PropertyChangeListener.class) {
		((PropertyChangeListener)listeners[i+1]).propertyChange(event);
	    }
	}

    }

}
