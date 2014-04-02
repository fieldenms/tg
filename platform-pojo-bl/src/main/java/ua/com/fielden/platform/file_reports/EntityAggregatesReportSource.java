package ua.com.fielden.platform.file_reports;

import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.EntityUtils;

public class EntityAggregatesReportSource implements JRRewindableDataSource {
    private int index = -1;

    private final EntityAggregates[] data;
    private final Map<String, Class> props;

    public EntityAggregatesReportSource(final EntityAggregates[] data, final Map<String, Class> props) {
        this.data = data;
        this.props = props;
    }

    @Override
    public void moveFirst() throws JRException {
        index = 0;
    }

    @Override
    public boolean next() throws JRException {
        index++;
        boolean returnVal = true;
        if (index >= data.length) {
            returnVal = false;
        }
        return returnVal;
    }

    @Override
    public Object getFieldValue(final JRField jrField) throws JRException {
        if (!props.containsKey(jrField.getName())) {
            throw new JRException("Invalid field: " + jrField.getName());
        }

        return EntityUtils.toString(data[index].get(jrField.getName()), props.get(jrField.getName()));
    }
}