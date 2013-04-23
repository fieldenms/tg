package ua.com.fielden.platform.example.swing.egi.performance;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TableModelForTesting extends AbstractTableModel{

    private static final long serialVersionUID = 2592033209407785463L;

    private final List<String> propertyNames = new ArrayList<String>();

    private final List<ExampleEntitySimplified> data = new ArrayList<ExampleEntitySimplified>();

    public TableModelForTesting(final List<ExampleEntitySimplified> data, final List<String> propertyNames){
	this.propertyNames.addAll(propertyNames);
	this.data.addAll(data);
    }

    @Override
    public int getRowCount() {
	return data.size();
    }

    @Override
    public int getColumnCount() {
	return propertyNames.size();
    }

    @Override
    public String getColumnName(final int columnIndex) {
	return ExampleEntitySimplified.metaData.get(propertyNames.get(columnIndex)).getKey();
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
	return ExampleEntitySimplified.types.get(propertyNames.get(columnIndex));
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
	return false;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
	return data.get(rowIndex).get(propertyNames.get(columnIndex));
    }
}
