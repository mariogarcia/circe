package circe

import asteroid.A
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import asteroid.transformer.AbstractExpressionTransformer

/**
 * @since 0.1.0
 */
@CompileStatic
class ClosureTransformer extends AbstractExpressionTransformer<MethodCallExpression> {

    static final String METHOD_CALL_NAME = 'validation'

    /**
     * @param sourceUnit
     * @since 0.1.0
     */
    public ClosureTransformer(SourceUnit sourceUnit) {
        super(MethodCallExpression, sourceUnit, A.CRITERIA.byExprMethodCallByName(METHOD_CALL_NAME))
    }

    @Override
    Expression transformExpression(MethodCallExpression source) {
        ClosureExpression dslExpression = getClosureArgument(source)
        ClosureExpression modExpression = transformClosure(dslExpression)

        return createValidationCallExpr(source, modExpression)
    }

    private ClosureExpression getClosureArgument(MethodCallExpression callX) {
        return A.UTIL.METHODX.getLastArgsAs(callX, ClosureExpression)
    }

    private ClosureExpression transformClosure(ClosureExpression origin) {
        return null
    }

    private MethodCallExpression createValidationCallExpr(MethodCallExpression origin, ClosureExpression rules) {
        return null
    }
}
