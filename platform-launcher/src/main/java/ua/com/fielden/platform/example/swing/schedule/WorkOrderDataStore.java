package ua.com.fielden.platform.example.swing.schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;

public class WorkOrderDataStore {

    public static final List<AbstractEntity<?>> dataAll = new ArrayList<AbstractEntity<?>>();

    static {
	final List<WorkOrderEntity> data = new ArrayList<WorkOrderEntity>();
	final WorkRequest workRequest = new WorkRequest()//
		.setKey("Work reques")//
		.setDesc("Work request")//
		.setRequestStart(new DateTime(2013, 11, 11, 0, 0).toDate())//
		.setRequestFinish(new DateTime(2013, 11, 28, 0, 0).toDate());//
	data.add(new WorkOrderEntity()
		.setKey("wo1")//
		.setDesc("wo1 desc")//
		.setEarlyStart(new DateTime(2013, 11, 11, 0, 0).toDate())//
		.setEarlyFinish(new DateTime(2013, 11, 18, 0, 0).toDate())//
		.setActualStart(new DateTime(2013, 11, 13, 0, 0).toDate())//
		.setActualFinish(new DateTime(2013, 11, 19, 0, 0).toDate())//
		.setWorkRequest(workRequest)//
		.setJobNo("job1"));
	data.add(new WorkOrderEntity()
		.setKey("wo2")//
		.setDesc("wo2 desc")//
		.setEarlyStart(null)//
		.setEarlyFinish(new DateTime(2013, 11, 18, 0, 0).toDate())//
		.setActualStart(new DateTime(2013, 11, 13, 0, 0).toDate())//
		.setActualFinish(new DateTime(2013, 11, 20, 0, 0).toDate())//
		.setWorkRequest(workRequest)//
		.setJobNo("job2"));
	data.add(new WorkOrderEntity()
		.setKey("wo3")//
		.setDesc("wo3 desc")//
		.setEarlyStart(new DateTime(2013, 11, 11, 0, 0).toDate())//
		.setEarlyFinish(new DateTime(2013, 11, 18, 0, 0).toDate())//
		.setActualStart(null)//
		.setActualFinish(new DateTime(2013, 11, 21, 0, 0).toDate())//
		.setWorkRequest(workRequest)//
		.setJobNo("job3"));
	data.add(new WorkOrderEntity()
		.setKey("wo4")//
		.setDesc("wo4 desc")//
		.setEarlyStart(null)//
		.setEarlyFinish(new DateTime(2013, 11, 18, 0, 0).toDate())//
		.setActualStart(null)//
		.setActualFinish(new DateTime(2013, 11, 21, 0, 0).toDate())//
		.setWorkRequest(workRequest)//
		.setJobNo("job4"));
	data.add(new WorkOrderEntity()
		.setKey("wo5")//
		.setDesc("wo5 desc")//
		.setEarlyStart(new DateTime(2013, 11, 11, 0, 0).toDate())//
		.setEarlyFinish(new DateTime(2013, 11, 18, 0, 0).toDate())//
		.setActualStart(new DateTime(2013, 11, 13, 0, 0).toDate())//
		.setActualFinish(null)//
		.setWorkRequest(workRequest)//
		.setJobNo("job5"));
	data.add(new WorkOrderEntity()
		.setKey("wo6")//
		.setDesc("wo6 desc")//
		.setEarlyStart(new DateTime(2013, 11, 11, 0, 0).toDate())//
		.setEarlyFinish(null)//
		.setActualStart(new DateTime(2013, 11, 13, 0, 0).toDate())//
		.setActualFinish(new DateTime(2013, 11, 19, 0, 0).toDate())//
		.setWorkRequest(workRequest)//
		.setJobNo("job6"));
	workRequest.setWorkOrders(new HashSet<WorkOrderEntity>(data));
	dataAll.addAll(data);
	dataAll.add(workRequest);
    }
}
