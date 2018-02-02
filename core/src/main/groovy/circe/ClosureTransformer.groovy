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
        List<Expression> validatorList = checkMappings.collect(this.&createValidatorFromMapping)
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

    Expression createValidatorFromMapping(Map m) {
        String key = "${m.k}"
        BooleanExpression condition = A.EXPR.boolX((Expression)m.expr)
        // TODO error('') doesnt exist, this method needs to receive the expression of the closure error(s, '')
        ClosureExpression closureX = A.EXPR.closureX(A.STMT.ifElseS(condition, A.STMT.stmt(emptyErrors()), A.STMT.stmt(createErrorWithKey(key))),
                                                     // TODO the same as above need the closure param
                                                     A.NODES.param('s').type(A.NODES.clazz(String).build()).build())
        CastExpression castToValidatorX = CastExpression.asExpression(A.NODES.clazz(helios.Validator).build(),
                                                                      closureX)

        return castToValidatorX
    }

    StaticMethodCallExpression emptyErrors() {
        return A.EXPR.staticCallX(Helios, 'errors')
    }

    StaticMethodCallExpression createErrorWithKey(String key) {
        return A.EXPR.staticCallX(Helios, 'errors', A.EXPR.staticCallX(Helios, 'error', A.EXPR.constX(key)))
    }

    private MethodCallExpression createValidationCallExpr(MethodCallExpression origin, ClosureExpression rules) {
        return null
    }
}
