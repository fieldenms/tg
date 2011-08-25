package ua.com.fielden.platform.example.dnd;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.example.dnd.classes.ExampleRotableCanvas;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.BogieClass;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableStatus;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.WheelsetClass;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.components.bind.test.EntityModuleWithDomainValidatorsForTesting;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

import com.google.inject.Module;

import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public class DnDSupport2Demo2 extends AbstractUiApplication {

    private List<Rotable> rotables;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
	final Module module = new EntityModuleWithDomainValidatorsForTesting(true);
	rotables = setupRotables(new ApplicationInjectorFactory().add(module).getInjector().getInstance(EntityFactory.class));
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
	final JFrame mainFrame = new JFrame("Demo");
	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	mainFrame.setLayout(new MigLayout());

	mainFrame.add(createRotablesTable(rotables), "w 400:400:400, h 600:600:600");
	mainFrame.add(createCanvas(), "w 400:400:400, h 600:600:600");

	mainFrame.validate();
	mainFrame.pack();
	mainFrame.setVisible(true);
    }

    private List<Rotable> setupRotables(final EntityFactory factory) {
	final List<Rotable> rotables = new ArrayList<Rotable>();
	rotables.add(factory.newEntity(Wheelset.class, "7E1S044048", "70t Pack 740mmx58mm offset SG").setStatus(RotableStatus.R).setRotableClass(new WheelsetClass("7E1S", "desc", 10)));
	rotables.add(factory.newEntity(Wheelset.class, "R7E3S18368", "70 Ton Package 840mm 64mm offset Standard gauge Wheel set").setStatus(RotableStatus.R).setRotableClass(new WheelsetClass("R7E3S", "desc", 20)));
	rotables.add(factory.newEntity(Bogie.class, "R5B4S10561", "50T 18R A/Box 920mmx38mm offset STD Gauge").setStatus(RotableStatus.RW).setRotableClass(new BogieClass("5B4S", "desc", 90)));
	rotables.add(factory.newEntity(Bogie.class, "RGXD4069", "3 Piece (50T) Ride Control Bogie").setStatus(RotableStatus.RW).setRotableClass(new BogieClass("RGXD", "desc", 70)));
	rotables.add(factory.newEntity(Bogie.class, "RRBE343", "70T Bradken Super Service Rod thru Bogie").setStatus(RotableStatus.U).setRotableClass(new BogieClass("RRBE", "desc", 70)));
	rotables.add(factory.newEntity(Wheelset.class, "R7E3S11181", "70 Ton wheel set").setStatus(RotableStatus.U).setRotableClass(new WheelsetClass("R7E3S", "desc", 50)));
	rotables.add(factory.newEntity(Bogie.class, "5D4S12241", "50t pckge 920mm 38mm SG").setStatus(RotableStatus.S).setRotableClass(new BogieClass("5D4S", "desc", 100)));
	return rotables;
    }

    @SuppressWarnings("unchecked")
    private static JTable createRotablesTable(final List<Rotable> rotables) {
	final PropertyTableModel<Rotable> rotableTableModel = new PropertyTableModelBuilder<Rotable>(Rotable.class).addReadonly("key", "No").addReadonly("desc", "Description").addReadonly("status", "Status").addReadonly("rotableClass", "Class").build(rotables);
	final EntityGridInspector<Rotable> stRotables = new EntityGridInspector<Rotable>(rotableTableModel);
	stRotables.setName("Rotables");
	stRotables.setRowHeight(26);
	stRotables.setCellStyleProvider(DummyBuilder.csp());

	DnDSupport2.installDnDSupport(stRotables, null, new DragFromSupport() {
	    @Override
	    public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
		// drop was performed, this means user dragged some rotables from this table to other component (canvas)
		// so we have to remove dropped rotables from table
		if (action != TransferHandler.NONE) {
		    final List<Rotable> removedRotables = (List<Rotable>) object;
		    stRotables.getActualModel().removeInstances(removedRotables.toArray(new Rotable[] {}));
		}
	    }

	    @Override
	    public Object getObject4DragAt(final Point point) {
		// defining what rows are selected and returning corresponding rotables
		// System.out.println("test");
		if (stRotables.getSelectedRows().length > 0) {
		    final List<Rotable> rotables4Drag = new ArrayList<Rotable>();
		    for (int i = 0; i < stRotables.getSelectedRows().length; i++) {
			final Rotable rotable = stRotables.getActualModel().instance(stRotables.getSelectedRows()[i]);
			if(rotable != null) {
			    rotables4Drag.add(rotable);
			}
		    }
		    return rotables4Drag;
		} else {
		    return null;
		}
	    }
	}, new DragToSupport() {
	    @Override
	    public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// lets check whether lists of rotables in table and dropped rotables do not intersect
		final PropertyTableModel<Rotable> tableModel = stRotables.getActualModel();
		final List<Rotable> droppedRotables = (List<Rotable>) what;
		for (final Rotable dropped : droppedRotables) {
		    if (tableModel.instances().contains(dropped)) {
			return false;
		    }
		}
		return true;
	    }

	    @Override
	    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// just adding dropped rotable to the list of rotables in table
		final List<Rotable> droppedRotables = (List<Rotable>) what;
		stRotables.getActualModel().addInstances(droppedRotables.toArray(new Rotable[] {}));
		return true;
	    }
	});

	return stRotables;
    }

    private static PSwingCanvas createCanvas() {

	final ExampleRotableCanvas canvas = new ExampleRotableCanvas(); // canvas.setTransferHandler(new CanvasTransferHandler());

	return canvas;
    }

    public static void main(final String[] args) {
	new DnDSupport2Demo2().launch(args);
    }

}
