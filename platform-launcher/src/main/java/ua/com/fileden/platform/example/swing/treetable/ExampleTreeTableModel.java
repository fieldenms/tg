package ua.com.fileden.platform.example.swing.treetable;

import ua.com.fielden.platform.swing.treetable.DynamicTreeTableModel;

public class ExampleTreeTableModel extends DynamicTreeTableModel {

    public ExampleTreeTableModel(){
	setRoot(createTreeTable());
    }

    private static ExampleTreeTableNode createTreeTable(){
	final ExampleTreeTableNode root=new ExampleTreeTableNode("root");
	for(int childrenCount=0;childrenCount<100;childrenCount++){
	    final ExampleTreeTableNode parent=new ExampleTreeTableNode("parent "+childrenCount);
	    root.add(parent);
	    insertChildren(parent,Integer.valueOf(childrenCount).toString(),3,4);
	}
	return root;
    }

    private static void insertChildren(final ExampleTreeTableNode parent, final String parentNum, final int num,final int count) {
	if(count>0){
	    for(int childrenCount=0;childrenCount<num;childrenCount++){
		final ExampleTreeTableNode child=new ExampleTreeTableNode("child "+parentNum+childrenCount);
		parent.add(child);
		insertChildren(child, parentNum+childrenCount, num, count-1);
	    }
	}
    }

    @Override
    public int getColumnCount() {
	return 3;
    }

    @Override
    public String getColumnName(final int column) {
	return "column "+column ;
    }

    @Override
    public Class<?> getColumnClass(final int column) {
	return String.class;
    }
}
