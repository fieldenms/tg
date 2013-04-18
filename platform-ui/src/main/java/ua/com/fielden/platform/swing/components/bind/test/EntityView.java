package ua.com.fielden.platform.swing.components.bind.test;

import static ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase.MIXED_CASE;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.VerticalLayout;
import org.joda.time.DateTime;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.Binder;
import ua.com.fielden.platform.swing.components.bind.development.BoundedJXDatePicker;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.BufferedPropertyWrapper;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.ReadOnlyLabel;
import ua.com.fielden.platform.swing.components.bind.test.Entity.Strategy;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.types.Money;

import com.jgoodies.binding.value.Trigger;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * This is the Entity view representation with 3 types of the components (used for testing and demo purposes) : CommitOnKeyTyped, CommitOnFocusLost, CommitOnTriggerCommit. Missing
 * components - have no sense - for e.g. OnKeyTypedCommit autocompleter
 *
 * @author jhou
 *
 */
public class EntityView {
    /**
     * Entity
     */
    private final Entity entity;
    private final Entity freshEntity;
    private final DemoAbstractEntity[] acceptableValues;
    private final EntityFactory factory;

    public EntityView(final EntityFactory factory) throws Result {
	this.factory = factory;

	entity = factory.newByKey(Entity.class, "key");
	freshEntity = factory.newByKey(Entity.class, "key1");

	entity.setString("Stalker");
	entity.setPassword("BlaBlaBla");
	entity.setNumber(null); // 195
	entity.setBigDecimal(new BigDecimal(1223));
	entity.setMoney(new Money(new BigDecimal(122.0)));
	entity.setDoubleProperty(new Double(1188));
	entity.setBool(true);

	entity.getProperty("strategy").setEditable(false);

	entity.setStrategy(Strategy.COMMIT);
	final ArrayList<Bicycle> bicycles = new ArrayList<Bicycle>();

	bicycles.add(factory.newEntity(Bicycle.class, "EB 2007", "Eastern Bicycles").setFrameName(null).setYear(null).setPrice(new Money("3000.00")).setDate(new DateTime("2005-10-01").toDate()).setFriendly(null).setInStock(true)); // "SC VP10"
	bicycles.add(factory.newEntity(Bicycle.class, "SBH 2008", "Specialized Big Hit Specialized Big Hit Specialized Big Hit Specialized/Big/Hit Specialized/Big/Hit Specialized Big Hit ").setFrameName("SBH").setYear(2008).setPrice(null).setFriendly(bicycles.get(0)).setInStock(true));
	bicycles.add(factory.newEntity(Bicycle.class, "XXT 2004", "Xtension Xplorer'2004 dghasdgh dwd 673gewurg 723e wqe wqrhd iuwdfuwegfuig wefg uewfd iuewufuwegfuigewuifgew weufh uweurguiwer uiewruigwe uriui ewrug uiewgh reuwir uiwegfuih iudsfi").setFrameName("XXT").setYear(2004).setPrice(null).setDate(new DateTime("2004-12-31").toDate()).setFriendly(bicycles.get(1)).setInStock(false));
	bicycles.add(factory.newEntity(Bicycle.class, "DBX 2006", "Da Bomb Accelerator").setFrameName("DBX").setYear(2006).setPrice(null).setDate(new DateTime("2004-12-12").toDate()).setFriendly(bicycles.get(1)).setInStock(true));
	bicycles.get(0).setFriendly(bicycles.get(3));

	entity.setBicycles(bicycles);
	//final DemoAbstractEntity nameXDemoEntity = new DemoEntity("NAME X", "this is a Name X demoEntity");

	freshEntity.setString(/* "Bunker" */null);
	entity.setPassword(null);
	freshEntity.setNumber(null);
	freshEntity.setBigDecimal(null);
	freshEntity.setMoney(null);
	freshEntity.setDoubleProperty(null);

	freshEntity.setBool(false);

	freshEntity.getProperty("strategy").setEditable(false);

	freshEntity.setStrategy(Strategy.REVERT_ON_INVALID);
	final ArrayList<Bicycle> freshBicycles = new ArrayList<Bicycle>();
	freshBicycles.add(bicycles.get(2));
	freshBicycles.add(bicycles.get(3));
	freshEntity.setBicycles(freshBicycles);

	//final DemoAbstractEntity nameXDemoEntity = new DemoEntity("NAME X", "this is a Name X demoEntity");
	final DemoAbstractEntity nameXDemoEntity = factory.newEntity(DemoAbstractEntity.class, "NAME X", "this is a Name X demoEntity");
	entity.setDemoEntity(nameXDemoEntity);
	entity.setDateTime(new DateTime());
	//	entity.setDate(new Date());
	entity.setDate(null);
	entity.setStringDemoEntity(nameXDemoEntity.getKey());

	final DemoAbstractEntity name1DemoEntity = factory.newEntity(DemoAbstractEntity.class, "NAME 1", "demo for name 1 demo for name 1 demo for name 1");
	final DemoAbstractEntity d2ne2DemoEntity = factory.newEntity(DemoAbstractEntity.class, "D2NE 2", "demo for name 3");
	final DemoAbstractEntity nameYDemoEntity = factory.newEntity(DemoAbstractEntity.class, "NAME Y", "this is a Name Y demoEntity");

	acceptableValues = new DemoAbstractEntity[] { nameXDemoEntity, name1DemoEntity, factory.newEntity(DemoAbstractEntity.class, "NAME 2", "demo for name 2"),
		factory.newEntity(DemoAbstractEntity.class, "NAME 3", "demo for name 3"), factory.newEntity(DemoAbstractEntity.class, "NMAE", "demo for name 2"),
		factory.newEntity(DemoAbstractEntity.class, "DONE 1", "demo for name 3"), d2ne2DemoEntity, nameYDemoEntity,
		factory.newEntity(DemoAbstractEntity.class, "DONE 3", "demo for name 3"), };
	final ArrayList<DemoAbstractEntity> list = new ArrayList<DemoAbstractEntity>(Arrays.asList(new DemoAbstractEntity[] { name1DemoEntity, d2ne2DemoEntity }));

	entity.setList(list);
	entity.setStringList(new ArrayList<String>(Arrays.asList("NMAE", "NAME 3")));

	// fresh Entity:
	System.out.println("\t\t\t&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7" + freshEntity.getDemoEntity());
	freshEntity.setDemoEntity(nameYDemoEntity);
	System.out.println("\t\t\t&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7" + freshEntity.getDemoEntity());
	freshEntity.setDemoEntity(null);
	System.out.println("\t\t\t&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7" + freshEntity.getDemoEntity());

	freshEntity.setDateTime(new DateTime());

	final Date d = new Date();
	d.setTime(d.getTime() * 2);
	freshEntity.setDate(d);

	freshEntity.setStringDemoEntity(nameYDemoEntity.getKey());
	freshEntity.setStringDemoEntity(/* nameYDemoEntity.getKey() */null);

	final ArrayList<DemoAbstractEntity> list1 = new ArrayList<DemoAbstractEntity>(Arrays.asList(new DemoAbstractEntity[] { nameYDemoEntity, d2ne2DemoEntity }));
	freshEntity.setList(list1); //null);

	freshEntity.setStringList(//new ArrayList<String>(Arrays.asList("MAENA", "NAME 3")));
		null);

	// check initial updating for validation results
	//	    entity.getMetaProperty(Entity.PROPERTY_BIG_DECIMAL).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_BOOL).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_DEMO_ENTITY).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_LIST).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_LIST_OF_STRINGS).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_NUMBER).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_STRATEGY).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_STRING).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_STRING_DEMO_ENTITY).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));
	//	    entity.getMetaProperty(Entity.PROPERTY_MONEY).setValidationResult(ValidationAnnotation.NOT_NULL,
	//		    new Result("the initial validation UI updating", new Exception("the initial validation UI updating")));

    }

    /**
     * Trigger to assign to TriggeredComponents. Used in Save/Flush Buttons to commit/flush assigned properties.
     *
     * @see ComponentFactory.createTriggered () factory methods.
     */
    private Trigger saveButtonTrigger;
    /**
     * PropertyWrappers to get buffered properties, that construct components
     */
    private BufferedPropertyWrapper enumPropertyWrapper;

    private JPanel panel;
    private BoundedValidationLayer<EntityGridInspector<Bicycle>> egi;

    /**
     * Creates Entity. Sets its properties. Creates trigger. Creates components from ComponentFactory. Creates 2 autocompleters with OnFocusLost commiting strategy and 2 with
     * OnSaveButtonClick strategy.
     *
     * <strong>NOTE: </strong>components can be created from simple Entity and PropertyName, but it will commit any changes on keyTyped/focusLost. If you want to delay commits
     * (e.g. Save button) - use triggers and ComponentFactory.createTriggered*() methods
     *
     * @param factory
     * @throws Exception
     */
    private void initComponents() {
	try {

	    com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	    LookAndFeelFactory.installJideExtension();

	    saveButtonTrigger = new Trigger();

	    panel = new JPanel(new GridLayout(28, 4));
	    panel.setPreferredSize(new Dimension(700, 600));
	    panel.add(createPropertyPanel(new JLabel("On Key Typed:"), new JLabel("On Focus Lost:"), new JLabel("On Trigger Commit:"), new JLabel("Bounded Label:")));
	    panel.add(createPropertyPanel(null, new JLabel("String fields/areas :"), null, null));

	    // string fields
	    final BoundedValidationLayer<JTextField> stringFieldOKT = ComponentFactory.createStringTextField(entity, Entity.PROPERTY_STRING, false, "string text field OKT", MIXED_CASE);
	    stringFieldOKT.setPreferredSize(new Dimension(250, 25));
	    // this property commits on focus lost
	    final BoundedValidationLayer<JTextField> stringFieldOFL = ComponentFactory.createStringTextField(entity, Entity.PROPERTY_STRING, true, "string text field OFL", MIXED_CASE);
	    stringFieldOFL.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<JTextField> stringFieldOTC = ComponentFactory.createTriggeredStringTextField(entity, Entity.PROPERTY_STRING, saveButtonTrigger, true, "string text field OTC");
	    stringFieldOTC.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<ReadOnlyLabel> label1 = ComponentFactory.createLabel(entity, Entity.PROPERTY_STRING, "PROPERTY_STRING");
	    panel.add(createPropertyPanel(stringFieldOKT, stringFieldOFL, stringFieldOTC, label1));

	    // string fields
	    final BoundedValidationLayer<JPasswordField> passwordFieldOKT = ComponentFactory.createPasswordField(entity, Entity.PROPERTY_STRING, false, "password text field OKT", '*');
	    passwordFieldOKT.setPreferredSize(new Dimension(250, 25));
	    // this property commits on focus lost
	    final BoundedValidationLayer<JPasswordField> passwordFieldOFL = ComponentFactory.createPasswordField(entity, Entity.PROPERTY_STRING, true, "password text field OFL", 'X');
	    passwordFieldOFL.setPreferredSize(new Dimension(250, 25));
	    panel.add(createPropertyPanel(passwordFieldOKT, passwordFieldOFL, null, null));

	    // string areas
	    final BoundedValidationLayer<JTextArea> stringTextAreaOKT = ComponentFactory.createStringTextArea(entity, Entity.PROPERTY_STRING, false, true, "string text area OKT", new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for string text area"));
	    final BoundedValidationLayer<JTextArea> stringTextAreaOFL = ComponentFactory.createStringTextArea(entity, Entity.PROPERTY_STRING, true, true, "string text area OFL", new SimpleOnCommitSysoutMessageAction("onFocusLost OnCommitAction for string text area"));
	    final BoundedValidationLayer<JTextArea> stringTextAreaOTC = ComponentFactory.createTriggeredStringTextArea(entity, Entity.PROPERTY_STRING, saveButtonTrigger, "string text area OTC", new SimpleOnCommitSysoutMessageAction("onTriggerCommit OnCommitAction for string text area"));
	    final BoundedValidationLayer<ReadOnlyLabel> label7 = ComponentFactory.createLabel(entity, Entity.PROPERTY_STRING, "PROPERTY_STRING");
	    panel.add(createPropertyPanel(stringTextAreaOKT, stringTextAreaOFL, stringTextAreaOTC, label7));

	    panel.add(createPropertyPanel(null, new JLabel("Formatted fields :"), null, null));
	    // integer formatted field
	    final BoundedValidationLayer<JFormattedTextField> numberFieldOKT = ComponentFactory.createIntegerTextField(entity, Entity.PROPERTY_NUMBER, false, "integer text field OKT");
	    numberFieldOKT.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<JFormattedTextField> numberFieldOFL = ComponentFactory.createIntegerTextField(entity, Entity.PROPERTY_NUMBER, true, "integer text field OFL");
	    numberFieldOFL.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<JFormattedTextField> numberFieldOTC = ComponentFactory.createTriggeredIntegerTextField(entity, Entity.PROPERTY_NUMBER, saveButtonTrigger, "integer text field OTC");
	    numberFieldOTC.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<ReadOnlyLabel> label2 = ComponentFactory.createLabel(entity, Entity.PROPERTY_NUMBER, "PROPERTY_NUMBER");
	    panel.add(createPropertyPanel(numberFieldOKT, numberFieldOFL, numberFieldOTC, label2));

	    // bigDecimal formatted field
	    final BoundedValidationLayer<JFormattedTextField> bigDecimalFormattedFieldOKT = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_BIG_DECIMAL, false, "big decimal field");
	    final BoundedValidationLayer<JFormattedTextField> bigDecimalFormattedFieldOFL = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_BIG_DECIMAL, true, "big decimal field");
	    final BoundedValidationLayer<JFormattedTextField> bigDecimalFormattedFieldOTC = ComponentFactory.createTriggeredBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_BIG_DECIMAL, saveButtonTrigger, "triggered big decimal text field");
	    final BoundedValidationLayer<ReadOnlyLabel> label38 = ComponentFactory.createLabel(entity, Entity.PROPERTY_BIG_DECIMAL, "PROPERTY_BIG_DECIMAL");
	    panel.add(createPropertyPanel(bigDecimalFormattedFieldOKT, bigDecimalFormattedFieldOFL, bigDecimalFormattedFieldOTC, label38));

	    // money formatted field
	    final BoundedValidationLayer<JFormattedTextField> moneyFormattedFieldOKT = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_MONEY, false, "money field");
	    final BoundedValidationLayer<JFormattedTextField> moneyFormattedFieldOFL = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_MONEY, true, "money field");
	    final BoundedValidationLayer<JFormattedTextField> moneyFormattedFieldOTC = ComponentFactory.createTriggeredBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_MONEY, saveButtonTrigger, "triggered money field");
	    final BoundedValidationLayer<ReadOnlyLabel> label28 = ComponentFactory.createLabel(entity, Entity.PROPERTY_MONEY, "PROPERTY_MONEY");
	    panel.add(createPropertyPanel(moneyFormattedFieldOKT, moneyFormattedFieldOFL, moneyFormattedFieldOTC, label28));

	    // double formatted field
	    final BoundedValidationLayer<JFormattedTextField> doubleFormattedFieldOKT = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_DOUBLE, false, "double field");
	    final BoundedValidationLayer<JFormattedTextField> doubleFormattedFieldOFL = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_DOUBLE, true, "double field");
	    final BoundedValidationLayer<JFormattedTextField> doubleFormattedFieldOTC = ComponentFactory.createTriggeredBigDecimalOrMoneyOrDoubleField(entity, Entity.PROPERTY_DOUBLE, saveButtonTrigger, "triggered double text field");
	    final BoundedValidationLayer<ReadOnlyLabel> label39 = ComponentFactory.createLabel(entity, Entity.PROPERTY_DOUBLE, "PROPERTY_DOUBLE");
	    panel.add(createPropertyPanel(doubleFormattedFieldOKT, doubleFormattedFieldOFL, doubleFormattedFieldOTC, label39));

	    // checkboxes
	    final BoundedValidationLayer<JCheckBox> checkBoxOKT = ComponentFactory.createCheckBox(entity, Entity.PROPERTY_BOOL, "check box OKT", "check box OKT");
	    final BoundedValidationLayer<JCheckBox> checkBoxOTC = ComponentFactory.createTriggeredCheckBox(entity, Entity.PROPERTY_BOOL, "check box OTC", "check box OTC", saveButtonTrigger);
	    final BoundedValidationLayer<JCheckBox> readOnlyCheckBox = ComponentFactory.createReadOnlyCheckBox(entity, Entity.PROPERTY_BOOL, "read-only check box", "read-only check box");
	    panel.add(createPropertyPanel(checkBoxOKT, null, checkBoxOTC, readOnlyCheckBox));

	    // ======================================== spinners :
	    panel.add(createPropertyPanel(null, new JLabel("Spinners :"), null, null));
	    // integer spinner
	    final BoundedValidationLayer<JSpinner> intSpinnerOKT = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_NUMBER, false, "integer spinner OKT", 2);
	    final BoundedValidationLayer<JSpinner> intSpinnerOFL = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_NUMBER, true, "integer spinner OFL", 1);
	    panel.add(createPropertyPanel(intSpinnerOKT, intSpinnerOFL, null, null));

	    // big decimal spinner
	    final BoundedValidationLayer<JSpinner> bigDecimalSpinnerOKT = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_BIG_DECIMAL, false, "big decimal spinner OKT", 0.5d);
	    final BoundedValidationLayer<JSpinner> bigDecimalSpinnerOFL = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_BIG_DECIMAL, true, "big decimal spinner OFL", 1.0d);
	    panel.add(createPropertyPanel(bigDecimalSpinnerOKT, bigDecimalSpinnerOFL, null, null));

	    // money spinner
	    final BoundedValidationLayer<JSpinner> moneySpinnerOKT = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_MONEY, false, "money spinner OKT", 0.5d);
	    final BoundedValidationLayer<JSpinner> moneySpinnerOFL = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_MONEY, true, "money spinner OFL", 1.0d);
	    panel.add(createPropertyPanel(moneySpinnerOKT, moneySpinnerOFL, null, null));

	    // double spinner
	    final BoundedValidationLayer<JSpinner> doubleSpinnerOKT = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_DOUBLE, false, "double spinner OKT", 0.5d);
	    final BoundedValidationLayer<JSpinner> doubleSpinnerOFL = ComponentFactory.createNumberSpinner(entity, Entity.PROPERTY_DOUBLE, true, "double spinner OFL", 1.0d);
	    panel.add(createPropertyPanel(doubleSpinnerOKT, doubleSpinnerOFL, null, null));

	    //=================================

	    // onKeyTyped radiobuttons
	    final BoundedValidationLayer<JRadioButton> revertRadioButtonOKT = ComponentFactory.createRadioButton(entity, Entity.PROPERTY_STRATEGY, Strategy.REVERT, Strategy.REVERT.toString(), "revert radiobutton", new SimpleOnCommitSysoutMessageAction("REVERT radioButton onKeyTyped OnCommitAction"));
	    final BoundedValidationLayer<JRadioButton> commitRadioButtonOKT = ComponentFactory.createRadioButton(entity, Entity.PROPERTY_STRATEGY, Strategy.COMMIT, Strategy.COMMIT.toString(), "commit radiobutton", new SimpleOnCommitSysoutMessageAction("COMMIT radioButton onKeyTyped OnCommitAction"));
	    final BoundedValidationLayer<JRadioButton> revertOnInvalidRadioButtonOKT = ComponentFactory.createRadioButton(entity, Entity.PROPERTY_STRATEGY, Strategy.REVERT_ON_INVALID, Strategy.REVERT_ON_INVALID.toString(), "revertOnInvalid radiobutton", new SimpleOnCommitSysoutMessageAction("REVERT_ON_INVALID radioButton onKeyTyped OnCommitAction"));
	    final BoundedValidationLayer<JRadioButton> commitOnValidRadioButtonOKT = ComponentFactory.createRadioButton(entity, Entity.PROPERTY_STRATEGY, Strategy.COMMIT_ON_VALID, Strategy.COMMIT_ON_VALID.toString(), "commitOnValid radiobutton", new SimpleOnCommitSysoutMessageAction("COMMIT_ON_VALID radioButton onKeyTyped OnCommitAction"));

	    // onTriggerCommit radioButtons
	    enumPropertyWrapper = ComponentFactory.createPropertyWrapper(entity, Entity.PROPERTY_STRATEGY, saveButtonTrigger, new SimpleOnCommitSysoutMessageAction("AnyRadiobutton onTriggerCommit OnCommitAction"));
	    final BoundedValidationLayer<JRadioButton> revertRadioButtonOTC = ComponentFactory.createTriggeredRadioButton(enumPropertyWrapper, Strategy.REVERT, "revert radiobutton");
	    final BoundedValidationLayer<JRadioButton> commitRadioButtonOTC = ComponentFactory.createTriggeredRadioButton(enumPropertyWrapper, Strategy.COMMIT, "commit radiobutton");
	    final BoundedValidationLayer<JRadioButton> revertOnInvalidRadioButtonOTC = ComponentFactory.createTriggeredRadioButton(enumPropertyWrapper, Strategy.REVERT_ON_INVALID, "revertOnInvalid radiobutton");
	    final BoundedValidationLayer<JRadioButton> commitOnValidRadioButtonOTC = ComponentFactory.createTriggeredRadioButton(enumPropertyWrapper, Strategy.COMMIT_ON_VALID, "commitOnValid radiobutton");
	    final BoundedValidationLayer<ReadOnlyLabel> label3a = ComponentFactory.createLabel(entity, Entity.PROPERTY_STRATEGY, "PROPERTY_STRATEGY");
	    panel.add(createPropertyPanel(revertRadioButtonOKT, null, revertRadioButtonOTC, new JLabel("")));
	    panel.add(createPropertyPanel(commitRadioButtonOKT, null, commitRadioButtonOTC, label3a));
	    panel.add(createPropertyPanel(revertOnInvalidRadioButtonOKT, null, revertOnInvalidRadioButtonOTC, new JLabel("")));
	    panel.add(createPropertyPanel(commitOnValidRadioButtonOKT, null, commitOnValidRadioButtonOTC, new JLabel("")));

	    final String keyExpression = "key";
	    final IValueMatcher<DemoAbstractEntity> matcher = new PojoValueMatcher<DemoAbstractEntity>(Arrays.asList(acceptableValues), keyExpression, 10) {
		@Override
		public List<DemoAbstractEntity> findMatches(final String value) {
		    try {
			Thread.sleep(100);
		    } catch (final InterruptedException e) {
		    }
		    return super.findMatches(value);
		}
	    };

	    panel.add(createPropertyPanel(null, new JLabel("Entity autocompleters :"), null, null));
	    final BoundedValidationLayer<AutocompleterTextFieldLayer> singleValueAutocompleterOnFocusLost = ComponentFactory.createOnFocusLostAutocompleter(entity, Entity.PROPERTY_DEMO_ENTITY, "enter demoEntity name...", DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, null, matcher, "singleValueAutocompleterOnFocusLost", false);
	    final BoundedValidationLayer<AutocompleterTextFieldLayer<DemoAbstractEntity>> singleValueAutocompleterOnSaveButton = ComponentFactory.createTriggeredAutocompleter(entity, Entity.PROPERTY_DEMO_ENTITY, "enter demoEntity name...", saveButtonTrigger, DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, null, matcher, "singleValueAutocompleterOnSaveButton", false, new SimpleOnCommitSysoutMessageAction("**************************************************************this postCommitAction invokes after OnSaveButtonCommit"), new SimpleOnCommitSysoutMessageAction("--------------------------------------------------------------this postCommitAction invokes after OnSaveButtonCommit"));
	    singleValueAutocompleterOnSaveButton.addOnCommitAction(new SimpleOnCommitSysoutMessageAction("==============================================================this postCommitAction invokes after OnSaveButtonCommit"));
	    final BoundedValidationLayer<ReadOnlyLabel> label5 = ComponentFactory.createLabel(entity, Entity.PROPERTY_DEMO_ENTITY, "PROPERTY_DEMO_ENTITY");
	    panel.add(createPropertyPanel(null, singleValueAutocompleterOnFocusLost, singleValueAutocompleterOnSaveButton, label5));

	    final BoundedValidationLayer<AutocompleterTextFieldLayer> multValueAutocompleterOnFocusLost = ComponentFactory.createOnFocusLostAutocompleter(entity, Entity.PROPERTY_LIST, "enter demoEntity name...", DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, ",", matcher, "multValueAutocompleterOnFocusLost", false);
	    final BoundedValidationLayer<AutocompleterTextFieldLayer<DemoAbstractEntity>> multValueAutocompleterOnSaveButton = ComponentFactory.createTriggeredAutocompleter(entity, Entity.PROPERTY_LIST, "enter demoEntity name...", saveButtonTrigger, DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, ",", matcher, "multValueAutocompleterOnSaveButton", false);
	    final BoundedValidationLayer<ReadOnlyLabel> label6 = ComponentFactory.createLabel(entity, Entity.PROPERTY_LIST, "PROPERTY_LIST");
	    panel.add(createPropertyPanel(null, multValueAutocompleterOnFocusLost, multValueAutocompleterOnSaveButton, label6));

	    final JButton commitFocusOwnerButton = new JButton(new CommitFocusOwnerAction());
	    commitFocusOwnerButton.setFocusable(false);
	    commitFocusOwnerButton.setMnemonic('c');
	    panel.add(createPropertyPanel(new JButton(new CommitBufferAction()), //
		    new JButton(new FlushBufferAction()), new JButton(new ShowValueHolderValueAction()), commitFocusOwnerButton));

	    // stringBinding autocompleters :
	    panel.add(createPropertyPanel(null, new JLabel("String autocompleters :"), null, null));
	    final BoundedValidationLayer<AutocompleterTextFieldLayer> stringValueAutocompleterOnFocusLost = ComponentFactory.createOnFocusLostAutocompleter(entity, Entity.PROPERTY_STRING_DEMO_ENTITY, "enter demoEntity name...", DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, null, matcher, "stringValueAutocompleterOnFocusLost", true);

	    final BoundedValidationLayer<AutocompleterTextFieldLayer<DemoAbstractEntity>> stringValueAutocompleterOnSaveButton = ComponentFactory.createTriggeredAutocompleter(entity, Entity.PROPERTY_STRING_DEMO_ENTITY, "enter demoEntity name...", saveButtonTrigger, DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, null, matcher, "stringValueAutocompleterOnSaveButton", true);
	    final BoundedValidationLayer<ReadOnlyLabel> label10 = ComponentFactory.createLabel(entity, Entity.PROPERTY_STRING_DEMO_ENTITY, "PROPERTY_STRING_DEMO_ENTITY");
	    panel.add(createPropertyPanel(null, stringValueAutocompleterOnFocusLost, stringValueAutocompleterOnSaveButton, label10));

	    // mult valued:
	    final BoundedValidationLayer<AutocompleterTextFieldLayer> multStringValueAutocompleterOnFocusLost = ComponentFactory.createOnFocusLostAutocompleter(entity, Entity.PROPERTY_LIST_OF_STRINGS, "enter demoEntity name...", DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, ",", matcher, "multValueAutocompleterOnFocusLost", true);
	    final BoundedValidationLayer<AutocompleterTextFieldLayer<DemoAbstractEntity>> multStringValueAutocompleterOnSaveButton = ComponentFactory.createTriggeredAutocompleter(entity, Entity.PROPERTY_LIST_OF_STRINGS, "enter demoEntity name...", saveButtonTrigger, DemoAbstractEntity.class, keyExpression, new String[] {"desc"}, ",", matcher, "multValueAutocompleterOnSaveButton", true);
	    final BoundedValidationLayer<ReadOnlyLabel> label11 = ComponentFactory.createLabel(entity, Entity.PROPERTY_LIST_OF_STRINGS, "PROPERTY_LIST_OF_STRINGS");
	    panel.add(createPropertyPanel(null, multStringValueAutocompleterOnFocusLost, multStringValueAutocompleterOnSaveButton, label11));

	    // date pickers (swing-x)
	    final BoundedValidationLayer<BoundedJXDatePicker> dateJXDatePickerOKT = ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE, "vaidation layer for BoundedJXDatePicker", true, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()); // , new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for date jxdatePicker")
	    dateJXDatePickerOKT.setPreferredSize(new Dimension(250, 25));
	    final BoundedValidationLayer<ReadOnlyLabel> label8 = ComponentFactory.createLabel(entity, Entity.PROPERTY_DATE, "PROPERTY_DATE");

	    // date picker layer
	    final BoundedValidationLayer<DatePickerLayer> datePickerLayerOKT = ComponentFactory.createDatePickerLayer(entity, Entity.PROPERTY_DATE, "enter date (toolTip)", "enter date (caption)", true, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()); // , new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for date date picker layer")
	    datePickerLayerOKT.setPreferredSize(new Dimension(250, 25));

	    //	    final BoundedValidationLayer<BoundedJXDatePicker> dateJXDatePickerOKTcopy = ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE,
	    //		    "vaidation layer for BoundedJXDatePicker", false, new SimpleOnCommitSysoutMessageAction("onKeyTyped OnCommitAction for date jxdatePicker"));
	    //	    dateJXDatePickerOKTcopy.setPreferredSize(new Dimension(250, 25));
	    //	    final BoundedValidationLayer<ReadOnlyLabel> label9 = ComponentFactory.createLabel(entity, Entity.PROPERTY_DATE, "PROPERTY_DATE");
	    //	    panel.add(createPropertyPanel(dateJXDatePickerOKTcopy, null, null, label9));

	    final PropertyTableModelBuilder<Bicycle> propertyTableModelBuilder = new PropertyTableModelBuilder<Bicycle>(Bicycle.class).addReadonly("key", "Number", null, "Bicycle number").addEditableString("desc", "Description", null, "Bicycle description").addEditable("year", "Year", null, "Year").addEditable("price", "Price", null, "Price").addReadonly("date", "Date", null, "Date").addReadonly("friendly", "Friendly", null, "Friendly bicycle").addEditable("inStock", "In Stock", null, "In Stock");
	    egi = ComponentFactory.createEGI(entity, Entity.PROPERTY_BICYCLES, propertyTableModelBuilder);//, actions)
	    egi.getView().setRowHeight(26);

	    final JButton reconnect = new JButton("reconnect");
	    reconnect.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
		    stringFieldOKT.rebindTo(freshEntity);
		    stringFieldOFL.rebindTo(freshEntity);
		    stringFieldOTC.rebindTo(freshEntity);

		    numberFieldOKT.rebindTo(freshEntity);
		    numberFieldOFL.rebindTo(freshEntity);
		    numberFieldOTC.rebindTo(freshEntity);

		    stringTextAreaOKT.rebindTo(freshEntity);
		    stringTextAreaOFL.rebindTo(freshEntity);
		    stringTextAreaOTC.rebindTo(freshEntity);

		    bigDecimalFormattedFieldOKT.rebindTo(freshEntity);
		    bigDecimalFormattedFieldOFL.rebindTo(freshEntity);
		    bigDecimalFormattedFieldOTC.rebindTo(freshEntity);

		    doubleFormattedFieldOKT.rebindTo(freshEntity);
		    doubleFormattedFieldOFL.rebindTo(freshEntity);
		    doubleFormattedFieldOTC.rebindTo(freshEntity);

		    moneyFormattedFieldOKT.rebindTo(freshEntity);
		    moneyFormattedFieldOFL.rebindTo(freshEntity);
		    moneyFormattedFieldOTC.rebindTo(freshEntity);

		    dateJXDatePickerOKT.rebindTo(freshEntity);
		    //		    dateJXDatePickerOKTcopy.rebindTo(freshEntity);

		    singleValueAutocompleterOnFocusLost.rebindTo(freshEntity);
		    singleValueAutocompleterOnSaveButton.rebindTo(freshEntity);

		    stringValueAutocompleterOnFocusLost.rebindTo(freshEntity);
		    stringValueAutocompleterOnSaveButton.rebindTo(freshEntity);

		    multValueAutocompleterOnFocusLost.rebindTo(freshEntity);
		    multValueAutocompleterOnSaveButton.rebindTo(freshEntity);

		    multStringValueAutocompleterOnFocusLost.rebindTo(freshEntity);
		    multStringValueAutocompleterOnSaveButton.rebindTo(freshEntity);

		    label1.rebindTo(freshEntity);
		    label10.rebindTo(freshEntity);
		    label11.rebindTo(freshEntity);
		    label2.rebindTo(freshEntity);
		    label28.rebindTo(freshEntity);
		    readOnlyCheckBox.rebindTo(freshEntity);
		    label38.rebindTo(freshEntity);
		    label39.rebindTo(freshEntity);
		    label3a.rebindTo(freshEntity);
		    label5.rebindTo(freshEntity);
		    label6.rebindTo(freshEntity);
		    label7.rebindTo(freshEntity);
		    label8.rebindTo(freshEntity);
		    //		    label9.rebindTo(freshEntity);

		    checkBoxOKT.rebindTo(freshEntity);
		    checkBoxOTC.rebindTo(freshEntity);

		    revertRadioButtonOKT.rebindTo(freshEntity);
		    commitRadioButtonOKT.rebindTo(freshEntity);
		    commitOnValidRadioButtonOKT.rebindTo(freshEntity);
		    revertOnInvalidRadioButtonOKT.rebindTo(freshEntity);

		    egi.rebindTo(freshEntity);

		    // Very Important!!! :  to rebind OnTriggerCommit radiobuttons,
		    // you have to use rebindTo() method only once for ALL radiobuttons!!!
		    revertRadioButtonOTC.rebindTo(freshEntity);
		}

	    });
	    panel.add(createPropertyPanel(dateJXDatePickerOKT, datePickerLayerOKT, reconnect, label8));
	    //revertRequirementForAllProperties();
	} catch (final Exception e1) {
	    e1.printStackTrace();
	}
    }

    /**
     * creates simple horizontal panel for three types of the bounded components
     *
     * @param commitOnKeyTypedComponent
     * @param commitOnFocusLostComponent
     * @param commitOnTriggerCommitComponent
     * @return
     */
    private static final JPanel createPropertyPanel(final JComponent commitOnKeyTypedComponent, final JComponent commitOnFocusLostComponent, final JComponent commitOnTriggerCommitComponent, final JComponent boundedLabel) {

	final JPanel panel = new JPanel(new GridLayout(1, 4));
	if (commitOnKeyTypedComponent == null) {
	    panel.add(new JLabel(""));
	} else {
	    panel.add(commitOnKeyTypedComponent);
	}
	if (commitOnFocusLostComponent == null) {
	    panel.add(new JLabel(""));
	} else {
	    panel.add(commitOnFocusLostComponent);
	}
	if (commitOnTriggerCommitComponent == null) {
	    panel.add(new JLabel(""));
	} else {
	    panel.add(commitOnTriggerCommitComponent);
	}
	if (boundedLabel == null) {
	    panel.add(new JLabel(""));
	} else {
	    panel.add(boundedLabel);
	}
	return panel;
    }

    /**
     * builds the panel from existing components
     *
     * @param factory
     * @return
     * @throws Exception
     */
    public JComponent buildPanel() {
	initComponents();
	final JPanel mainPanel = new JPanel(new VerticalLayout());
	final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(mainPanel, "");
	mainPanel.setPreferredSize(new Dimension(750, 765));
	mainPanel.add(panel);
	mainPanel.add(new JScrollPane(egi));
	final JButton removeAllButton = new JButton("removeAll");
	removeAllButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		entity.setBicycles(new ArrayList<Bicycle>());
		System.out.println(egi.getView().getActualModel().instances());
	    }
	});
	final JButton removeButton = new JButton("remove");
	removeButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (entity.getProperty(Entity.PROPERTY_BICYCLES).isValid() && !entity.getBicycles().isEmpty()) {
		    entity.removeFromBicycles(entity.getBicycles().get(0));
		    System.out.println(egi.getView().getActualModel().instances());
		}
	    }
	});
	final JButton addButton = new JButton("add");
	addButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final Bicycle bicycle = factory.newByKey(Bicycle.class, generateBicycleKey(entity.getBicycles())).setFrameName(null).setYear(null).setPrice(new Money("3000.00")).setDate(new DateTime("2005-10-01").toDate()).setFriendly(null).setInStock(true);
		entity.addToBicycles(bicycle);
		System.out.println(egi.getView().getActualModel().instances());
	    }
	});
	final JButton revertRequirementButton = new JButton("revert requirement");
	revertRequirementButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		revertRequirementForAllProperties();
	    }
	});
	mainPanel.add(createPropertyPanel(removeAllButton, removeButton, addButton, revertRequirementButton));
	mainPanel.validate();
	return blockingLayer;
    }

    public static JPanel buildDatePickers(final Entity entity) {
	final JPanel mainPanel = new JPanel(new MigLayout("", "[]", "[][]"));
	final JButton switchButton = new JButton("remove");
	final JPanel panel1 = new JPanel(new VerticalLayout());
	for (int i = 0; i < 30; i++) {
	    panel1.add(createPropertyPanel(//
		    //		    new JXDatePicker(), new JXDatePicker(), new JXDatePicker(), new JXDatePicker()

		    ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE, "vaidation layer for BoundedJXDatePicker", false, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()),
		    ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE, "vaidation layer for BoundedJXDatePicker", false, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()),
		    ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE, "vaidation layer for BoundedJXDatePicker", false, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()),
		    ComponentFactory.createBoundedJXDatePicker(entity, Entity.PROPERTY_DATE, "vaidation layer for BoundedJXDatePicker", false, DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()) //
		    //
		    ));
	}

	switchButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (panel1.getParent() == null) {
		    //		    if (j < UIManager.getInstalledLookAndFeels().length) {
		    //			final LookAndFeelInfo laf = UIManager.getInstalledLookAndFeels()[j];
		    //			j++;
		    //			System.out.println("laf = " + laf);
		    //			try {
		    //			    UIManager.setLookAndFeel(laf.getClassName());
		    //			} catch (final Exception e1) {
		    //			    e1.printStackTrace();
		    //			}
		    //		    }

		    System.out.println("add");
		    mainPanel.add(panel1);
		} else {
		    System.out.println("remove");
		    mainPanel.remove(panel1);
		}
		mainPanel.repaint();
	    }
	});
	mainPanel.add(switchButton, "wrap");
	mainPanel.add(panel1);
	return mainPanel;
    }

    private void revertRequirementForAllProperties() {
	for (final String propertyName : entity.getProperties().keySet()) {
	    revertRequirement(propertyName);
	}
    }

    private void revertRequirement(final String propertyName) {
	entity.getProperty(propertyName).setRequired(!entity.getProperty(propertyName).isRequired());
    }

    /**
     * Generates new unique(among passed instances) key for new {@link Bicycle} instance.
     *
     * @param bicycles
     * @return
     */
    private static String generateBicycleKey(final List<Bicycle> bicycles) {
	final String key = "BICYCLE";
	for (int i = 0; i < Integer.MAX_VALUE; i++) {
	    boolean keyExists = false;
	    for (final Bicycle bicycle : bicycles) {
		final String newKey = key + i;
		if (newKey.equals(bicycle.getKey())) {
		    keyExists = true;
		    break;
		}
	    }
	    if (!keyExists) {
		return key + i;
	    }
	}
	throw new IllegalStateException("Cannot generate unique key for entity");
    }

    /**
     * Commits buffer
     *
     * @author jhou
     *
     */
    private class CommitBufferAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public CommitBufferAction() {
	    super("Commit Buffer");
	}

	public void actionPerformed(final ActionEvent event) {
	    System.out.println("commiting...");
	    saveButtonTrigger.triggerCommit();
	}
    }

    /**
     * Flushes buffer
     *
     * @author jhou
     *
     */
    private class FlushBufferAction extends AbstractAction {
	private static final long serialVersionUID = 5535229394441192112L;

	public FlushBufferAction() {
	    super("Flush Buffer");
	}

	public void actionPerformed(final ActionEvent event) {
	    System.out.println("flushing...");
	    saveButtonTrigger.triggerFlush();
	}
    }

    /**
     * Shows stored property values
     *
     * @author jhou
     *
     */
    private class CommitFocusOwnerAction extends AbstractAction {
	private static final long serialVersionUID = -3306444239370368759L;

	public CommitFocusOwnerAction() {
	    super("Commit focus owner");
	}

	public void actionPerformed(final ActionEvent event) {
	    Binder.commitFocusOwner();
	}
    }

    /**
     * Shows stored property values
     *
     * @author jhou
     *
     */
    private class ShowValueHolderValueAction extends AbstractAction {
	private static final long serialVersionUID = -3306444239370368759L;

	public ShowValueHolderValueAction() {
	    super("Show Value");
	}

	public void actionPerformed(final ActionEvent event) {
	    final StringBuffer message = new StringBuffer();
	    message.append("<html>");
	    message.append("<br><b>Number:</b> ");
	    message.append(entity.getNumber());
	    message.append("<br><b>String:</b> ");
	    message.append(entity.getString());
	    message.append("<br><b>BigDecimal:</b> ");
	    message.append(entity.getBigDecimal());
	    message.append("<br><b>Double:</b> ");
	    message.append(entity.getDoubleProperty());
	    message.append("<br><b>Bool:</b> ");
	    message.append(entity.getBool());
	    message.append("<br><b>Strategy enum:</b> ");
	    message.append(entity.getStrategy());
	    message.append("<br><b>List :</b> ");
	    message.append(entity.getList());
	    message.append("<br><b>DemoEntity :</b> ");
	    message.append(entity.getDemoEntity());
	    message.append("</html>");

	    JOptionPane.showMessageDialog(null, message.toString());
	}
    }

    /**
     * sysout-styled OnCommitAction for testing purposes
     *
     * @author jhou
     *
     */
    private class SimpleOnCommitSysoutMessageAction implements IOnCommitAction {

	private final String message;

	public SimpleOnCommitSysoutMessageAction(final String message) {
	    this.message = message;
	}

	@Override
	public void postCommitAction() {
	    System.out.println("\t\t\tpost CommitAction : " + message);
	}

	@Override
	public void postNotSuccessfulCommitAction() {
	    System.out.println("\t\t\tpost Not Successful CommitAction");
	}

	@Override
	public void postSuccessfulCommitAction() {
	    System.out.println("\t\t\tpost Successful CommitAction");
	}
    }

    public Entity getEntity() {
	return entity;
    }

}
