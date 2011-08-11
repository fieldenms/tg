package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;
import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.IAstVisitor;
import ua.com.fielden.platform.expression.exception.semantic.InvalidPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.MissingPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.Finder;

/**
 * Ensures that a visited AST node representing a property reference is correct, non-collectional, is not an entity itself and is reachable from the context property.
 *
 * @author TG Team
 *
 */
public class EssentialPropertyValidationVisitor implements IAstVisitor {

    private final Class<? extends AbstractEntity> context;

    /**
     * Context is a top level type for which all referenced by AST nodes properties should be its properties or subproperties.
     *
     * @param context
     */
    public EssentialPropertyValidationVisitor(final Class<? extends AbstractEntity> context) {
	this.context = context;
    }

    @Override
    public void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat == EgTokenCategory.NAME) {
	    final Field field;
	    try {
		field = Finder.findFieldByName(context, node.getToken().text);
	    } catch (final Exception ex) {
		throw new MissingPropertyException("Could not find property " + node.getToken().text, ex, node.getToken());
	    }
	    if (Collection.class.isAssignableFrom(field.getType())) {
		throw new InvalidPropertyException("Property " + node.getToken().text + " is collectional and cannot be used in the expression.\n" +
				"Please consider using one of its sub properties.", node.getToken());
	    }
	    if (AbstractEntity.class.isAssignableFrom(field.getType())) {
		throw new InvalidPropertyException("Property " + node.getToken().text + " is of entity type and cannot be used in the expression.\n" +
		"Please consider using one of its sub properties.", node.getToken());
	    }
	}
    }

}
