package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.InvalidPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.MissingPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.Finder;

/**
 * Ensures that a visited AST node representing a property reference is  reachable from the context property.
 *
 * @author TG Team
 *
 */
public class EssentialPropertyValidationVisitor extends AbstractAstVisitor {

    /**
     * {@inheritDoc}
     */
    public EssentialPropertyValidationVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	super(higherOrderType, contextProperty);
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
		field = Finder.findFieldByName(getHigherOrderType(), absolutePropertyPath);
	    } catch (final Exception ex) {
		throw new MissingPropertyException("Could not find property " + absolutePropertyPath, ex, node.getToken());
	    }

	    if (field.isAnnotationPresent(Calculated.class)) {
		throw new InvalidPropertyException("Calculated properties cannot be used as part of expressions at this stage. Property " + absolutePropertyPath + " is calculated.", node.getToken());
	    }
	}
    }

}
