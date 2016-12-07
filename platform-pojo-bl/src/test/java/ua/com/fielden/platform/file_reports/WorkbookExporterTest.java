package ua.com.fielden.platform.file_reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.ShortSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.types.Money;

public class WorkbookExporterTest {


    @Test
    public void short_collectional_property_can_be_exported() {
        final MasterEntity master1 = new MasterEntity();
        master1.setDateProp(new DateTime(2000, 1, 1, 0, 0).toDate());
        master1.setBooleanProp(true);
        master1.setMoneyProp(new Money("1.00"));
        master1.setStringProp("master1");
        master1.setIntegerProp(Integer.valueOf(1));
        final MasterEntity master2 = new MasterEntity();
        master2.setDateProp(new DateTime(2000, 1, 2, 0, 0).toDate());
        master2.setBooleanProp(true);
        master2.setMoneyProp(new Money("2.00"));
        master2.setStringProp("master2");
        master2.setIntegerProp(Integer.valueOf(2));
        final List<MasterEntity> entities = new ArrayList<MasterEntity>();
        entities.add(master1);
        entities.add(master2);
        final ShortSlaveEntity shortSlave1 = new ShortSlaveEntity();
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(master1);
        slave1.setIntegerProp(Integer.valueOf(1));
        shortSlave1.setMasterEntityProp(master1);
        shortSlave1.setKey2(slave1);
        final ShortSlaveEntity shortSlave2 = new ShortSlaveEntity();
        final SlaveEntity slave2 = new SlaveEntity();
        slave2.setMasterEntityProp(master1);
        slave2.setIntegerProp(Integer.valueOf(2));
        shortSlave2.setMasterEntityProp(master1);
        shortSlave2.setKey2(slave2);
        master1.setShortCollection(Arrays.asList(shortSlave1, shortSlave2));
        final ShortSlaveEntity shortSlave3 = new ShortSlaveEntity();
        final SlaveEntity slave3 = new SlaveEntity();
        slave3.setMasterEntityProp(master2);
        slave3.setIntegerProp(Integer.valueOf(1));
        shortSlave3.setMasterEntityProp(master2);
        shortSlave1.setKey2(slave3);
        final ShortSlaveEntity shortSlave4 = new ShortSlaveEntity();
        final SlaveEntity slave4 = new SlaveEntity();
        slave4.setMasterEntityProp(master2);
        slave4.setIntegerProp(Integer.valueOf(2));
        shortSlave4.setMasterEntityProp(master2);
        shortSlave4.setKey2(slave4);
        master2.setShortCollection(Arrays.asList(shortSlave3, shortSlave4));
    }
}
