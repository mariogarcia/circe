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
 * Transformer responsible for transforming a given {@link Extension#validate} call to
 * a full Helios validation call.
 *
 * @since 0.1.0
 */
@CompileStatic
class ValidateMethodCallTransformer extends AbstractExpressionTransformer<MethodCallExpression> {

    /**
     * Default constructor receiving the current {@link
     * SourceUnit}. It will be applied only to those method calls with
     * the name 'validate'
     *
     * @param sourceUnit the current {@link SourceUnit}
     * @since 0.1.0
     */
    public ValidateMethodCallTransformer(SourceUnit sourceUnit) {
        super(MethodCallExpression,
              sourceUnit,
              A.CRITERIA.and(A.CRITERIA.byExprMethodCallByName('validate'),
                             A.CRITERIA.byExprMethodCallByArgs(String, Object, Closure)))
    }

    @Override
    Expression transformExpression(MethodCallExpression source) {
        ClosureExpression dslExpression = getClosureArgument(source)
        ListExpression modExpression = transformClosureArgument(dslExpression)
        ArgumentListExpression args = A.UTIL.METHODX.getArgs(source)
        Expression validationExpression = createHeliosValidation(modExpression, args)

        return validationExpression
    }

    private ClosureExpression getClosureArgument(MethodCallExpression callX) {
        return A.UTIL.METHODX.getLastArgumentAs(A.UTIL.METHODX.getArgs(callX), ClosureExpression)
    }

    private ListExpression transformClosureArgument(ClosureExpression origin) {
        BlockStatement blockStmt = (BlockStatement) origin.code
        List<Group> checks = A.UTIL.STMT.groupStatementsByLabel(blockStmt);
        List<Map> checkMappings = mapGroupsToCheckMappings(checks)
        List<Expression> validatorList = checkMappings.collect(this.&createValidatorFromMapping.curry(origin))
        ListExpression validatorListExpr = A.EXPR.listX(validatorList as Expression[])

        return validatorListExpr
    }

    private List<Map> mapGroupsToCheckMappings(List<Group> checks) {
        return checks
            .findAll(this.&byCheckLabelName)
            .collectMany(this.&extractExpressionListByGroup)
    }

    private Boolean byCheckLabelName(Group group) {
        return group.label.name == 'check'
    }

    private List<Map<String,?>> extractExpressionListByGroup(Group group) {
        return group.statements.collect { Statement stmt ->
            [
                key: group.label.desc,
                expr: ((ExpressionStatement)stmt).expression
            ]
        } as List<Map<String,?>>
    }

    private Expression createValidatorFromMapping(ClosureExpression origin, Map<String, ?> mapping) {
        String key = "${mapping.key}"
        BooleanExpression conditionX = A.EXPR.boolX(mapping.expr as Expression)
        Statement errorsStmt = A.STMT.stmt(createErrorWithKey(key, origin.parameters.first()))
        Statement ifElseStmt = A.STMT.ifElseS(conditionX, A.STMT.stmt(emptyErrors()), errorsStmt)
        ClosureExpression closureX = A.EXPR.closureX(ifElseStmt, origin.parameters.first())
        CastExpression castToValidatorX = CastExpression.asExpression(A.NODES.clazz(helios.Validator).build(), closureX)

        return castToValidatorX
    }

    private StaticMethodCallExpression emptyErrors() {
        return A.EXPR.staticCallX(helios.ValidatorError, 'errors')
    }

    private StaticMethodCallExpression createErrorWithKey(String key, Parameter param) {
        Expression errorX = A.EXPR.staticCallX(helios.ValidatorError, 'error', A.EXPR.varX(param.name), A.EXPR.constX(key))

        return A.EXPR.staticCallX(helios.ValidatorError, 'errors', errorX)
    }

    private Expression createHeliosValidation(ListExpression validatorList, ArgumentListExpression args) {
        ConstantExpression constantX = A.UTIL.METHODX.getFirstArgumentAs(args, ConstantExpression)
        VariableExpression variableX = A.UTIL.METHODX.getArgumentByIndexAs(args, 1, VariableExpression)

        // if variable not found an error should be thrown
        if (!variableX) {
            addError('Validation payload not found at: validate(String, payload, Closure)', args)
        }

        return A.EXPR.staticCallX(Helios,
                                  'validate',
                                  constantX,
                                  A.EXPR.varX(variableX.name),
                                  validatorList)
    }
}
