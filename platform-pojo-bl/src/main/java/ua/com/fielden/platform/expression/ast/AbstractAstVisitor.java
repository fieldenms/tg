package ua.com.fielden.platform.expression.ast;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * A convenient base implementation for AST visitors where method {@link #postVisit(AstNode)} does nothing.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAstVisitor implements IAstVisitor {

    private final Class<? extends AbstractEntity<?>> higherOrderType;
    private final String contextProperty;
    private final Class<? extends AbstractEntity<?>> contextPropertyType;

    /**
     * The <code>higherOrderType</code> argument should represent a top level type for which all referenced by the AST nodes properties should be its properties or subproperties.
     * The <code>contextProperty</code> argument specifies the relative location in the type tree against which all other AST nodes' should be checked for compatibility. The value
     * of the <code>contextProperty</code> argument should be null if the higher-order type represents a context.
     *
     * @param higherOrderType
     * @param contextProperty
     */
    protected AbstractAstVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	this.higherOrderType = (Class<? extends AbstractEntity<?>>) higherOrderType;
	this.contextProperty = contextProperty;
	final Class<?> contextType = StringUtils.isEmpty(contextProperty) ? higherOrderType : PropertyTypeDeterminator.determinePropertyType(higherOrderType, contextProperty);
	if (contextType != null) {
	    if (AbstractEntity.class.isAssignableFrom(contextType)) {
		contextPropertyType = (Class<? extends AbstractEntity<?>>) contextType;
	    } else {
		throw new IllegalArgumentException("Context property '" + contextProperty + "' in type " + higherOrderType + " is not an enitity.");
	    }
	} else {
	    contextPropertyType = null;
	}
    }

    @Override
    public void postVisit(final AstNode rootNode) throws SemanticException {
    }

    protected String relative2Absolute(final String property) {
	return StringUtils.isEmpty(contextProperty) ? property : Reflector.fromRelative2AbsotulePath(contextProperty, property);
    }

    protected String absolute2Relative(final String property) {
	return StringUtils.isEmpty(contextProperty) ? property : Reflector.fromAbsotule2RelativePath(contextProperty, property);
    }

    /**
     * Unlike relative2Absolute this method inverts the path making the context property to be the first node in the path. This is mainly required to correctly build a
     * computational model, which can link properties downwards from the context property.
     *
     * @param property
     * @return
     */
    protected String relative2AbsoluteInverted(final String property) {
	final String absolutePath = Reflector.relative2AbsoluteInverted(higherOrderType, contextProperty, property);
	if (absolutePath.contains("←")) {
	    throw new IllegalStateException("Relative property path '" + property + "' cannot be converted for context '" + contextProperty + "' in type "
		    + higherOrderType.getName());
	}
	return absolutePath;
    }

    public Class<? extends AbstractEntity> getHigherOrderType() {
	return higherOrderType;
    }

    public String getContextProperty() {
	return contextProperty;
    }

    public Class<? extends AbstractEntity<?>> getContextPropertyType() {
	return contextPropertyType;
    }

}
