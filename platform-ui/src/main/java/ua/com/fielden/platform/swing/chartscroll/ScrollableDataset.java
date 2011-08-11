package ua.com.fielden.platform.swing.chartscroll;

import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetGroup;

public class ScrollableDataset extends DefaultCategoryDataset {

    private static final long serialVersionUID = 8826359376056500515L;

    private final CategoryDataset originDataset;

    private int startIndex = 0;
    private int pageSize = 0;

    public ScrollableDataset(final CategoryDataset originDataset) {
	this.originDataset = originDataset;
	pageSize = originDataset.getColumnCount();
    }

    @Override
    public int getColumnIndex(final Comparable key) {
	return originDataset.getColumnIndex(key) - startIndex;
    }

    @Override
    public Comparable getColumnKey(final int column) {
	return originDataset.getColumnKey(column + startIndex);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List getColumnKeys() {
	return originDataset.getColumnKeys().subList(startIndex, pageSize);
    }

    @Override
    public int getRowIndex(final Comparable key) {
	return originDataset.getRowIndex(key);
    }

    @Override
    public Comparable getRowKey(final int row) {
	return originDataset.getRowKey(row);
    }

    @Override
    public List getRowKeys() {
	return originDataset.getRowKeys();
    }

    @Override
    public Number getValue(final Comparable rowKey, final Comparable columnKey) {
	return originDataset.getValue(rowKey, columnKey);
    }

    @Override
    public int getColumnCount() {
	return pageSize;
    }

    @Override
    public int getRowCount() {
	return originDataset.getRowCount();
    }

    @Override
    public Number getValue(final int row, final int column) {
	return originDataset.getValue(row, column + startIndex);
    }

    @Override
    public DatasetGroup getGroup() {
	return originDataset.getGroup();
    }

    @Override
    public void setGroup(final DatasetGroup group) {
	originDataset.setGroup(group);
    }

    public int getPageSize() {
	return pageSize;
    }

    public int getStartIndex() {
	return startIndex;
    }

    public void setPageSize(final int pageSize) {
	if (this.pageSize == pageSize) {
	    return;
	}
	setScrollProperties(pageSize, startIndex);
    }

    public void setStartIndex(final int startIndex) {
	if (this.startIndex == startIndex) {
	    return;
	}
	setScrollProperties(pageSize, startIndex);
    }

    public void setScrollProperties(final int size, final int index) {
	if (size <= 0 || index < 0) {
	    return;
	}
	if (size + index > originDataset.getColumnCount()) {
	    startIndex = originDataset.getColumnCount() - size;
	    if (startIndex < 0) {
		this.pageSize = originDataset.getColumnCount();
		this.startIndex = 0;
	    } else {
		this.pageSize = size;
	    }
	} else {
	    this.pageSize = size;
	    this.startIndex = index;
	}
	fireDatasetChanged();
    }

    public CategoryDataset getOriginDataset() {
	return originDataset;
    }
}
