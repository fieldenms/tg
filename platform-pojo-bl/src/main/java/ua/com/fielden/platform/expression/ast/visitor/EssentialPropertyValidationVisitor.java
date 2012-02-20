package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.IAstVisitor;
import ua.com.fielden.platform.expression.exception.semantic.InvalidPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.MissingPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * Ensures that a visited AST node representing a property reference is  reachable from the context property.
 *
 * @author TG Team
 *
 */
public class EssentialPropertyValidationVisitor implements IAstVisitor {


    private final Class<? extends AbstractEntity> higherOrderType;
    private final String contextProperty;

    /**
     * The <code>higherOrderType</code> argument should represent a top level type for which all referenced by the AST nodes properties should be its properties or subproperties.
     * The <code>contextProperty</code> argument specifies the relative location in the type tree against which all other AST nodes' levels should be checked for compatibility.
     * The value of the <code>contextProperty</code> argument should be null if the higher-order type represents a context.
     *
     * @param higherOrderType
     * @param contextProperty
     */
    public EssentialPropertyValidationVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	this.higherOrderType = higherOrderType;
	this.contextProperty = contextProperty;
    }

    /**
     * Convenient constructor for cases where context corresponds to high-order type.
     *
     * @param higherOrderType
     */
    public EssentialPropertyValidationVisitor(final Class<? extends AbstractEntity> higherOrderType) {
	this(higherOrderType, null);
    }

    @Override
    public void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat == EgTokenCategory.NAME) {
	    final String absolutePropertyPath = relative2Absolute(node.getToken().text);
	    final Field field;
	    try {
		field = Finder.findFieldByName(higherOrderType, absolutePropertyPath);
	    } catch (final Exception ex) {
		throw new MissingPropertyException("Could not find property " + absolutePropertyPath, ex, node.getToken());
	    }

	    if (field.isAnnotationPresent(Calculated.class)) {
		throw new InvalidPropertyException("Calculated properties cannot be used as part of expressions at this stage. Property " + absolutePropertyPath + " is calculated.", node.getToken());
	    }
	}
    }

    private String relative2Absolute(final String property) {
	return StringUtils.isEmpty(contextProperty) ? property : Reflector.fromRelative2AbsotulePath(contextProperty, property);
    }

}
