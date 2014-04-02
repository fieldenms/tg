package ua.com.fielden.platform.swing.egi.models.mappings;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.ConverterFactory;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;

/**
 * Read-only mapping for {@link AbstractEntity} properties.
 * <p>
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <K>
 * @param <ColumnType>
 */
@SuppressWarnings("unchecked")
public class ReadonlyPropertyColumnMapping<T extends AbstractEntity> extends AbstractPropertyColumnMapping<T> {

    private final boolean isCheckBox;

    private transient ValidationLayer layer;

    private transient JComponent view;

    private final Converter converter;

    protected ReadonlyPropertyColumnMapping() {
        this(null, null, null, null, 0, null, null, null, null, null, false);
    }

    /**
     * The principle constructor creating read-only property column mapping and initialises (if <code>initConverter</code> property is set to true) {@link #converter} property
     * using the provided property types.
     */
    private ReadonlyPropertyColumnMapping(final String propertyName, final Class propertyType, final Class collectionType, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final boolean initConverter) {
        super(propertyName, propertyType, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);

        if (isEmpty(propertyName)) {
            converter = ConverterFactory.createAbstractEntityOrListConverter(ShowingStrategy.KEY_ONLY);
        } else {
            converter = initConverter ? EntityUtils.chooseConverterBasedUponPropertyType(propertyType, collectionType, ShowingStrategy.KEY_ONLY) : null;
        }

        isCheckBox = (getColumnClass() == Boolean.class || getColumnClass() == boolean.class);
    }

    /**
     * Creates read-only property column mapping and initialises (if <code>initConverter</code> property is set to true) {@link #converter} property using {@Reflector}
     * for determining property type.
     */
    public ReadonlyPropertyColumnMapping(final String propertyName, final Class propertyType, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final boolean initConverter) {
        this(propertyName,//
        propertyType,//
        null,//
        columnName,//
        prefSize,//
        headerTooltip,//
        tooltipGetter,//
        clickAction,//
        columnTotals,//
        aggregationFunction,//
        initConverter);
    }

    /**
     * Creates read-only property column mapping and initialises (if <code>initConverter</code> property is set to true) {@link #converter} property using {@Reflector}
     * for determining property type.
     */
    public ReadonlyPropertyColumnMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final boolean initConverter) {
        this(propertyName,//
        PropertyTypeDeterminator.determinePropertyType(entityClass, propertyName),//
        initConverter ? AnnotationReflector.getAnnotation(Finder.findFieldByName(entityClass, propertyName), IsProperty.class).value() : null,//
        columnName,//
        prefSize,//
        headerTooltip,//
        tooltipGetter,//
        clickAction,//
        columnTotals,//
        aggregationFunction,//
        initConverter);

    }

    /**
     * Creates {@link RendererValidationLayer} instance with no view set
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @return
     */
    protected final RendererValidationLayer createValidationLayer() {
        return new RendererValidationLayer(null);
    }

    /**
     * Creates view for {@link ValidationLayer} (either {@link RendererLabel} or {@link RendererCheckBox})
     * 
     * @param isCheckBox
     *            - determines whether {@link JCheckBox} or {@link JLabel} should be created
     * @param valueClass
     *            - class of values to be passed to be rendered using this view
     * @return
     */
    protected final JComponent createView(final boolean isCheckBox, final Class<?> valueClass) {
        return isCheckBox ? new RendererCheckBox() : new RendererLabel(valueClass);
    }

    @Override
    public JComponent getCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column) {
        if (!shouldUseMetaProperty(entity)) {
            return getEntityCellRendererComponent(entity);
        } else {
            return getAbstractEntityCellRendererComponent(entity, value, isSelected, hasFocus, table, row, column);
        }
    }

    /**
     * 
     * 
     * @param entity
     * @return
     */
    private boolean shouldUseMetaProperty(final T entity) {
        return !(entity instanceof EntityAggregates || isEmpty(getPropertyName()) || !entity.isEnhanced());
    }

    /**
     * Returns cell renderer component in case when entity is not instance of {@link EntityAggregates}.
     * 
     * @param entity
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param table
     * @param row
     * @param column
     * @return
     */
    private JComponent getAbstractEntityCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column) {
        // determining if property this renderer is bounded to is valid in entity
        // meta properties determining failed for some dot notated properties, then
        final List<MetaProperty> metaProperties = Finder.findMetaProperties(entity, getPropertyName());
        // next variable should contain either valid last property (if all previous properties, represented via dot-notation, are correct) or first property, that is invalid or
        // warning
        final MetaProperty firstFailedMetaProperty = EntityUtils.findFirstFailedMetaProperty(metaProperties);
        // next variable determines whether firstFailedMetaProperty variable holds reference to the last property in dot-notated list
        final boolean lastMetaPropertyIsReturned = firstFailedMetaProperty.equals(metaProperties.get(metaProperties.size() - 1));
        // setting result if incorrect or warning
        getLayer().setResult(!firstFailedMetaProperty.isValid() ? firstFailedMetaProperty.getFirstFailure()
                : (firstFailedMetaProperty.hasWarnings() ? firstFailedMetaProperty.getFirstWarning() : null));

        // setting value
        if (isCheckBox) {
            final boolean selectedValue;
            if (firstFailedMetaProperty.isValid() && lastMetaPropertyIsReturned) {
                final Boolean propValue = (Boolean) entity.get(getPropertyName());
                selectedValue = propValue != null ? propValue.booleanValue() : false;
            } else if (lastMetaPropertyIsReturned) {
                selectedValue = firstFailedMetaProperty.getLastInvalidValue() != null ? (Boolean) firstFailedMetaProperty.getLastInvalidValue() : false;
            } else {
                selectedValue = false;
            }
            ((JCheckBox) getView()).setSelected(selectedValue);
        } else {
            ((JLabel) getView()).setText(EntityUtils.getLabelText(firstFailedMetaProperty, !lastMetaPropertyIsReturned, converter));
        }

        if (getLayer().getResult() == null || getLayer().getResult().isSuccessfulWithoutWarning()) {
            return getView();
        } else {
            getLayer().setView(getView());
            return getLayer();
        }
    }

    /**
     * Returns cell renderer component for rendering {@link AbstractEntity}s or {@link EntityAggregates}.
     * 
     * @param entity
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param table
     * @param row
     * @param column
     * @return
     */
    private JComponent getEntityCellRendererComponent(final AbstractEntity entity) {
        final Object value = isEmpty(getPropertyName()) ? entity : entity.get(getPropertyName());

        if (isCheckBox) {
            ((JCheckBox) getView()).setSelected((Boolean) value);
        } else {
            ((JLabel) getView()).setText(EntityUtils.getLabelText(value, converter));
        }

        return getView();
    }

    @Override
    public boolean isPropertyEditable(final T entity) {
        return false;
    }

    @Override
    public boolean isNavigableTo(final T entity) {
        return false;
    }

    /**
     * Not used in this class, because {@link #isPropertyEditable(AbstractEntity)} always returns false
     */
    @Override
    public EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> createBoundedEditorFor(final T entity) {
        return null;
    }

    protected boolean isCheckBox() {
        return isCheckBox;
    }

    protected ValidationLayer getLayer() {
        // emulating transient-final behaviour using absence of setter and default initialisation during first access
        if (layer == null) {
            layer = createValidationLayer();
        }
        return layer;
    }

    protected JComponent getView() {
        // emulating transient-final behaviour using absence of setter and default initialisation during first access
        if (view == null) {
            view = createView(isCheckBox, getColumnClass());
        }
        return view;
    }

    /**
     * Optimised {@link ValidationLayer} for rendering.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @author Yura
     * 
     * @param <T>
     */
    public static class RendererValidationLayer<T extends JComponent> extends ValidationLayer<T> {

        private static final long serialVersionUID = 5200090084973968059L;

        public RendererValidationLayer(final T view) {
            super(view);

            setOpaque(true);
            setName("Table.cellRenderer");
        }

        /**
         * Following implementation is taken from DefaultTableCellRenderer class - done for optimisation purposes
         */
        @Override
        public boolean isOpaque() {
            final Color back = getBackground();
            Component p = getParent();
            if (p != null) {
                p = p.getParent();
            }

            // p should now be the JTable.
            final boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
            return !colorMatch && super.isOpaque();
        }

        @Override
        public void repaint(final long tm, final int x, final int y, final int width, final int height) {
        }

        @Override
        public void repaint(final Rectangle r) {
        }

        @Override
        public void repaint() {
        }

        @Override
        protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
            if ("text".equals(propertyName)
                    || "labelFor".equals(propertyName)
                    || "displayedMnemonic".equals(propertyName)
                    || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        @Override
        public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        }
    }

    /**
     * Optimised {@link JLabel} for rendering
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @author Yura
     */
    public static class RendererLabel extends JLabel {

        private static final long serialVersionUID = 2661482742947149211L;

        /**
         * If <code>valueClass</code> represents number, then horizontal alignment is set to right
         * 
         * @param valueClass
         */
        public RendererLabel(final Class<?> valueClass) {
            super();

            setOpaque(true);
            setName("Table.cellRenderer");

            if (Number.class.isAssignableFrom(valueClass) || Money.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == double.class) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }

        /**
         * Following implementation is taken from DefaultTableCellRenderer class - done for optimisation purposes
         */
        @Override
        public boolean isOpaque() {
            final Color back = getBackground();
            Component p = getParent();
            if (p != null) {
                p = p.getParent();
            }

            // p should now be the JTable.
            final boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
            return !colorMatch && super.isOpaque();
        }

        @Override
        public void invalidate() {
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        public void repaint(final long tm, final int x, final int y, final int width, final int height) {
        }

        @Override
        public void repaint(final Rectangle r) {
        }

        @Override
        public void repaint() {
        }

        @Override
        protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
            if ("text".equals(propertyName)
                    || "labelFor".equals(propertyName)
                    || "displayedMnemonic".equals(propertyName)
                    || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        @Override
        public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        }
    }

    /**
     * Optimised {@link JCheckBox} for rendering
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @author TG Team
     */
    public static class RendererCheckBox extends JCheckBox {

        private static final long serialVersionUID = -3255018680797456148L;

        public RendererCheckBox() {
            setOpaque(true);
            setName("Table.cellRenderer");

            setHorizontalAlignment(SwingConstants.CENTER);
            setBorderPainted(true);
        }

        /**
         * Following implementation is taken from DefaultTableCellRenderer class - done for optimisation purposes
         */
        @Override
        public boolean isOpaque() {
            final Color back = getBackground();
            Component p = getParent();
            if (p != null) {
                p = p.getParent();
            }

            // p should now be the JTable.
            final boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
            return !colorMatch && super.isOpaque();
        }

        @Override
        public void invalidate() {
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        public void repaint(final long tm, final int x, final int y, final int width, final int height) {
        }

        @Override
        public void repaint(final Rectangle r) {
        }

        @Override
        public void repaint() {
        }

        @Override
        protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
            if ("text".equals(propertyName)
                    || "labelFor".equals(propertyName)
                    || "displayedMnemonic".equals(propertyName)
                    || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        @Override
        public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        }
    }

}
