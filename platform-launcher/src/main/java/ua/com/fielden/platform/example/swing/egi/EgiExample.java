/**
 *
 */
package ua.com.fielden.platform.example.swing.egi;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.swing.utils.DummyBuilder.infoMessage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.components.bind.test.EntityModuleWithDomainValidatorsForTesting;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Entity Grid Inspector example/test application
 *
 * @author Yura
 */
public class EgiExample extends AbstractUiApplication {

    private PropertyTableModel<DummyEntity> dummyEntityTableModel;

    private PropertyTableModel<DummyEntity2> dummyEntity2TableModel;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	final EntityModuleWithDomainValidatorsForTesting module = new EntityModuleWithDomainValidatorsForTesting();
	module.getDomainValidationConfig().setValidator(DummyEntity2.class, "boolField", new DummyEntity2BoolFieldValidator());
	module.getDomainValidationConfig().setValidator(DummyEntity2.class, "intField", new DummyEntity2IntFieldValidator());

	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

	final List<DummyEntity2> dummyEntities2 = createDummyEntities2(entityFactory);
	final List<DummyEntity> dummyEntities = createDummyEntities(entityFactory, dummyEntities2);

	final Action dummyEntity2ClickAction = createDummyEntity2ClickAction(), dummyEntitiesCountClickAction = createDummyEntitiesCountClickAction();

	dummyEntityTableModel = new PropertyTableModelBuilder<DummyEntity>(DummyEntity.class)//
	.addReadonly("key", (Integer) null)//
	.addEditableString("desc", null)//
	.addEditable("dummyEntity2Ref", "DummyEntity2Ref", "DummyEntity2 String reference", new PojoValueMatcher<DummyEntity2>(dummyEntities2, "key", 10))//
	.addEditable("dummyEntity2", "DummyEntity2", null, "DummyEntity2 reference", new PojoValueMatcher<DummyEntity2>(dummyEntities2, "key", 10), dummyEntity2ClickAction).addEditable("key", 30)
	//////////////  this should be failed (do not uncomment following lines) : //////////////
	//         .addEditable("dummyEntity2.key", 30)
	//         .addEditable("getKey()", 30)
	//	   .addEditable("dummyEntity2.getKey()", 30)
	//         .addEditable("", 30)
	// this one should be mapped using PropertyColumnMappingByExpression
	.addReadonly("dummyEntity2.getMoneyField()", "DE2 moneyField", null, "DummyEntity2 money field", createMoneyFieldColoring())//
	.addReadonly("dummyEntity2.boolField", "DE2 boolField", null, "DummyEntity2 boolean field", createBoolFieldColoring())//
	.addReadonly("dummyEntity2.dateField", "DE2 dateField", null, "DummyEntity2 date field")//
	.addReadonly("dummyEntity2.intField", "DE2 intField", null, "DummyEntity2 int field")//

	.setRowColoringScheme(createRowColoringScheme())//
	.build(dummyEntities);

	dummyEntity2TableModel = new PropertyTableModelBuilder<DummyEntity2>(DummyEntity2.class)//
	.addReadonly("key", "Number", null, "DummyEntity2 number")//
	.addEditableString("desc", "Description", null, "DummyEntity2 description")//
	.addEditable("moneyField", "Money field", "Money field")//
	.addEditable("boolField", "Bool field", "Boolean field")//
	.addEditable("dateField", "Date field", null, "Date field")//
	.addEditable("intField", "Int field", null, "Integer field")//
	.addReadonly("getDummyEntitiesCount()", "Dummy Entities Count", null, "Dummy Entities count", dummyEntitiesCountClickAction)//
	//.addReadonly("dummyEntities.size()", "Dummy Entities Count()", null, "Dummy Entities count")// FIXME This causes failure...
	.build(dummyEntities2);
    }

    private IColouringScheme<DummyEntity> createRowColoringScheme() {
	return new IColouringScheme<DummyEntity>() {
	    @Override
	    public Color getColor(final DummyEntity entity) {
		// colouring rows with empty 'dummyEntity2Ref' property with light-violet colour
		return isEmpty(entity.getDummyEntity2Ref()) ? new Color(255, 235, 255) : null;
	    }
	};
    }

    private IColouringScheme<DummyEntity> createBoolFieldColoring() {
	return new IColouringScheme<DummyEntity>() {
	    @Override
	    public Color getColor(final DummyEntity entity) {
		return entity.getDummyEntity2() != null && entity.getDummyEntity2().isBoolField() ? new Color(200, 255, 200) : null;
	    }
	};
    }

    private IColouringScheme<DummyEntity> createMoneyFieldColoring() {
	return new IColouringScheme<DummyEntity>() {
	    @Override
	    public Color getColor(final DummyEntity entity) {
		// highlighting properties with null moneyField with violet colour
		return entity.getDummyEntity2() != null && entity.getDummyEntity2().getMoneyField() == null || entity.getDummyEntity2() == null ? new Color(255, 200, 255) : null;
	    }
	};
    }

    private Action createDummyEntitiesCountClickAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = -5979626161116220473L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final DummyEntity2 entity = (DummyEntity2) e.getSource();

		infoMessage("Property getDummyEntitiesCount() of dummyEntity2 " + entity.getKey() + " was double-clicked", "DummyEntity2 getDummyEntitiesCount() double-click action").setVisible(true);
	    }
	};
    }

    private Action createDummyEntity2ClickAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = -5979626161116220473L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final DummyEntity entity = (DummyEntity) e.getSource();

		infoMessage("Property dummyEntity2 of dummyEntity " + entity.getKey() + " was double-clicked", "DummyEntity dummyEntity2 double-click action").setVisible(true);
	    }
	};
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
	final EntityGridInspector<DummyEntity> dummyEntityGridInspector = new EntityGridInspector<DummyEntity>(dummyEntityTableModel);
	dummyEntityGridInspector.setRowHeight(26);
	final EntityGridInspector<DummyEntity2> dummyEntity2GridInspector = new EntityGridInspector<DummyEntity2>(dummyEntity2TableModel);
	dummyEntity2GridInspector.setRowHeight(26);

	final JPanel topPanel = new JPanel(new MigLayout("fill", "[:800:]", "[:300:]0[]0[][][:300:]"));
	addTotalsFooterTo(dummyEntityGridInspector, topPanel);

	//	final EgiPanel<DummyEntity> dummyEntityEgiPanel = new EgiPanel<DummyEntity>(dummyEntityTableModel, true);
	//	topPanel.add(dummyEntityEgiPanel, "grow, wrap");

	topPanel.add(new JLabel("Double-click \"DummyEntity2\" column of the above table or \"Dummy Entities Count\" column of the below one."), "growx, wrap");
	topPanel.add(new JScrollPane(dummyEntity2GridInspector), "grow");
	SimpleLauncher.show("Entity Grid Inspector Demo 2", topPanel);
    }

    private void addTotalsFooterTo(final EntityGridInspector egi, final JPanel topPanel) {
	//	final JPanel panel = new JPanel(new MigLayout("insets 0", "[]", "[]0[]push[]"));
	//	panel.add(egi.getTableHeader(), "grow, wrap");
	//	panel.add(egi, "grow, wrap");

	final JScrollPane scrollPane = new JScrollPane(egi);
	topPanel.add(scrollPane, "grow, wrap");

	final JPanel footer = new JPanel(new MigLayout("nogrid, insets 0"));
	//	footer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	final List<JComponent> totalsComponents = new ArrayList<JComponent>();
	for (int i = 0; i < egi.getColumnCount(); i++) {
	    final TableColumn column = egi.getColumnModel().getColumn(i);
	    final JComponent totalsComponent = i % 2 == 0 ? new JTextField("totals " + i) : new JLabel();
	    totalsComponent.setPreferredSize(new Dimension(column.getPreferredWidth(), 30));

	    footer.add(totalsComponent, "grow");
	    totalsComponents.add(totalsComponent);
	}

	final JScrollPane footerPane = new JScrollPane(footer, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	topPanel.add(footerPane, "grow, wrap, h 40::");
	topPanel.add(scrollPane.getHorizontalScrollBar(), "grow, wrap");

	scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
	    @Override
	    public void adjustmentValueChanged(final AdjustmentEvent e) {
		footerPane.getViewport().setViewPosition(new Point(e.getValue(), 0));
	    }
	});

	egi.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {
		final TableColumn column = egi.getTableHeader().getResizingColumn();
		if (column != null) {
		    final JComponent totalsComponent = totalsComponents.get(egi.convertColumnIndexToView(column.getModelIndex()));
		    totalsComponent.setPreferredSize(new Dimension(column.getWidth(), totalsComponent.getHeight()));
		    footer.revalidate();
		}
	    }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		final JComponent fromComponent = totalsComponents.get(e.getFromIndex());
		totalsComponents.set(e.getFromIndex(), totalsComponents.get(e.getToIndex()));
		totalsComponents.set(e.getToIndex(), fromComponent);

		footer.removeAll();
		for (int i = 0; i < egi.getColumnCount(); i++) {
		    footer.add(totalsComponents.get(i), "grow, gap 0 0 0 0");
		}
		footer.revalidate();
	    }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {
	    }
	});
	//
	//	return panel;
    }

    private List<DummyEntity> createDummyEntities(final EntityFactory entityFactory, final List<DummyEntity2> entities2) {
	final List<DummyEntity> entities = new ArrayList<DummyEntity>();

	entities.add(entityFactory.newEntity(DummyEntity.class, "ENTITY1", "entity1 desc").setDummyEntity2Ref("10001").setDummyEntity2(entities2.get(0)));
	entities.add(entityFactory.newEntity(DummyEntity.class, "ENTITY2", "entity2 desc").setDummyEntity2Ref("10002").setDummyEntity2(entities2.get(1)));
	entities.add(entityFactory.newEntity(DummyEntity.class, "ENTITY3", "entity3 desc").setDummyEntity2Ref(null).setDummyEntity2(null));
	entities.add(entityFactory.newEntity(DummyEntity.class, "ENTITY4", "entity4 desc").setDummyEntity2Ref("10004").setDummyEntity2(entities2.get(3)));

	entities2.get(0).getDummyEntities().addAll(entities);
	entities2.get(2).getDummyEntities().addAll(entities);

	return entities;
    }

    private List<DummyEntity2> createDummyEntities2(final EntityFactory entityFactory) {
	final List<DummyEntity2> entities = new ArrayList<DummyEntity2>();

	entities.add(entityFactory.newEntity(DummyEntity2.class, 10001L, "10001 desc").setBoolField(false).setDateField(new Date()).setIntField(5).setMoneyField(new Money("1000")));
	entities.add(entityFactory.newEntity(DummyEntity2.class, 10002L, "10002 desc").setBoolField(true).setDateField(null).setIntField(20).setMoneyField(null));
	entities.add(entityFactory.newEntity(DummyEntity2.class, 10003L, "10003 desc").setBoolField(false).setDateField(new Date()).setIntField(0).setMoneyField(new Money("2000")));
	entities.add(entityFactory.newEntity(DummyEntity2.class, 10004L, "10004 desc").setBoolField(false).setDateField(new Date()).setIntField(12).setMoneyField(new Money("10000")));

	return entities;
    }

    public static void main(final String[] args) {
	new EgiExample().launch(args);
    }

}