package circe

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import helios.Helios
import helios.Validator
import asteroid.A
import asteroid.utils.StatementUtils.Group
import asteroid.transformer.AbstractExpressionTransformer

/**
 * @since 0.1.0
 */
@CompileStatic
class ClosureTransformer extends AbstractExpressionTransformer<MethodCallExpression> {

    /**
     * @param sourceUnit
     * @since 0.1.0
     */
    public ClosureTransformer(SourceUnit sourceUnit) {
        super(MethodCallExpression,
              sourceUnit,
              A.CRITERIA.byExprMethodCallByName('validate'))
    }

    @Override
    Expression transformExpression(MethodCallExpression source) {
        ArgumentListExpression args = A.UTIL.METHODX.getArgs(source)
        ClosureExpression dslExpression = getClosureArgument(source)
        ListExpression modExpression = transformClosure(dslExpression)
        Expression validationExpression = createHeliosValidation(modExpression, args)

        return validationExpression
    }

    Expression createHeliosValidation(ListExpression validatorList, ArgumentListExpression args) {
        ConstantExpression constantX = A.UTIL.METHODX.getFirstArgumentAs(args, ConstantExpression)
        VariableExpression variableX = args.expressions[1] as VariableExpression

        return A.EXPR.staticCallX(Helios,
                                  'validate',
                                  constantX,
                                  A.EXPR.varX(variableX.name),
                                  validatorList)
    }

    private ClosureExpression getClosureArgument(MethodCallExpression callX) {
        return A.UTIL.METHODX.getLastArgumentAs(A.UTIL.METHODX.getArgs(callX), ClosureExpression)
    }

    private ListExpression transformClosure(ClosureExpression origin) {
        BlockStatement blockStmt = (BlockStatement) origin.code
        List<Group> checks = A.UTIL.STMT.groupStatementsByLabel(blockStmt);
        List<Map> checkMappings = mapGroupsToCheckMappings(checks)
        List<Expression> validatorList = checkMappings.collect(this.&createValidatorFromMapping.curry(origin))
        ListExpression validatorListExpr = A.EXPR.listX(validatorList as Expression[])

        return validatorListExpr
    }

    List<Map> mapGroupsToCheckMappings(List<Group> checks) {
        return checks.collectMany { Group group ->
            group.statements.collect { Statement stmt ->
                [key: "${group.label.desc}", expr: ((ExpressionStatement)stmt).expression]
            }
        } as List<Map>
    }

    Expression createValidatorFromMapping(ClosureExpression origin, Map m) {
        String key = "${m.k}"
        BooleanExpression conditionX = A.EXPR.boolX((Expression)m.expr)
        Statement errorsStmt = A.STMT.stmt(createErrorWithKey(key, origin.parameters.first()))
        Statement ifElseStmt = A.STMT.ifElseS(conditionX, A.STMT.stmt(emptyErrors()), errorsStmt)
        ClosureExpression closureX = A.EXPR.closureX(ifElseStmt, origin.parameters.first())
        CastExpression castToValidatorX = CastExpression.asExpression(A.NODES.clazz(helios.Validator).build(), closureX)

        return castToValidatorX
    }

    StaticMethodCallExpression emptyErrors() {
        return A.EXPR.staticCallX(helios.ValidatorError, 'errors')
    }

    StaticMethodCallExpression createErrorWithKey(String key, Parameter param) {
        Expression errorX = A.EXPR.staticCallX(helios.ValidatorError, 'error', A.EXPR.varX(param.name), A.EXPR.constX(key))

        return A.EXPR.staticCallX(helios.ValidatorError, 'errors', errorX)
    }

    private MethodCallExpression createValidationCallExpr(MethodCallExpression origin, ClosureExpression rules) {
        return null
    }
}
