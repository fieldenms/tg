package ua.com.fielden.platform.expression.ast;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * A convenient base implementation for AST visitors where method {@link #postVisit(AstNode)} does nothing.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAstVisitor implements IAstVisitor {

    private final Class<? extends AbstractEntity> higherOrderType;
    private final String contextProperty;

    /**
     * The <code>higherOrderType</code> argument should represent a top level type for which all referenced by the AST nodes properties should be its properties or subproperties.
     * The <code>contextProperty</code> argument specifies the relative location in the type tree against which all other AST nodes' should be checked for compatibility.
     * The value of the <code>contextProperty</code> argument should be null if the higher-order type represents a context.
     *
     * @param higherOrderType
     * @param contextProperty
     */
    protected AbstractAstVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	this.higherOrderType = higherOrderType;
	this.contextProperty = contextProperty;

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

    public Class<? extends AbstractEntity> getHigherOrderType() {
        return higherOrderType;
    }

    public String getContextProperty() {
        return contextProperty;
    }

}
