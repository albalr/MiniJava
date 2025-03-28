package dk.dtu.compute.course02324.mini_java.semantics;

import dk.dtu.compute.course02324.mini_java.model.*;

import java.util.HashMap;
import java.util.Map;

public class ProgramSerializerVisitor extends ProgramVisitor  {

    private StringBuilder result = new StringBuilder();

    private Map<Statement, String> statementRepresentations = new HashMap<>();

    private Map<Expression, String> expessionRepresentations = new HashMap<>();

    @Override
    public void visit(Sequence sequence) {
        result.setLength(0);
        for (Statement statement: sequence.statements) {
            result.append(statementRepresentations.get(statement)).append(System.lineSeparator());
        }
        statementRepresentations.put(sequence, result.toString());
    }

    @Override
    public void visit(Declaration declaration) {
        result.setLength(0);
        result.append(declaration.type.getName()).append(" ").append(declaration.variable.name);
        if (declaration.expression == null) {
            result.append(";");
        } else {
            result.append(" = ").append(expessionRepresentations.get(declaration.expression)).append(";");
        }
        statementRepresentations.put(declaration, result.toString());
    }

    @Override
    public void visit(Assignment assignment) {
        result.setLength(0);
        result.append(assignment.variable.name).append(" = ").append(expessionRepresentations.get(assignment.expression));
        statementRepresentations.put(assignment, result.toString());
        expessionRepresentations.put(assignment, result.toString());
    }

    @Override
    public void visit(Literal literal) {
        result.setLength(0);
        if (literal instanceof IntLiteral) {
            result.append(((IntLiteral) literal).literal);
        } else if (literal instanceof FloatLiteral) {
            result.append(((FloatLiteral) literal).literal).append("f");
        } else {
            assert false;
        }
        expessionRepresentations.put(literal, result.toString());
    }

    @Override
    public void visit(Var var) {
        result.setLength(0);
        result.append(var.name);
        expessionRepresentations.put(var, result.toString());
    }

    @Override
    public void visit(OperatorExpression operatorExpression) {
        result.setLength(0);
        if (operatorExpression.operands.size() == 0) {
            result.append(operatorExpression.operator.getName()).append("()");
        } else if (operatorExpression.operands.size() == 1) {
            result.append(operatorExpression.operator.getName()).append(" ")
                    .append(expessionRepresentations.get(operatorExpression.operands.getFirst()));
        } else if (operatorExpression.operands.size() == 2) {
            result.append(operandToString(operatorExpression.operator, operatorExpression.operands.getFirst(),0)).append(" ")
                    .append(operatorExpression.operator.getName()).append(" ")
                    .append(operandToString(operatorExpression.operator, operatorExpression.operands.getLast(), 1));
        } else {
            result.append(operatorExpression.operator.getName()).append("(");
            boolean first = true;
            for (Expression operand : operatorExpression.operands) {
                if (!first) {
                    result.append(", ");
                } else {
                    first = false;
                }
                result.append(expessionRepresentations.get(operand));
            }
            result.append(")");
        }
        expessionRepresentations.put(operatorExpression, result.toString());
    }

    private String operandToString(Operator operator, Expression expression, int number) {
        String result = expessionRepresentations.get(expression);
        if (expression instanceof OperatorExpression) {
            OperatorExpression operatorExpression = (OperatorExpression) expression;
            if (operatorExpression.operator.precedence > operator.precedence ||
                    (operatorExpression.operator.precedence == operator.precedence &&
                            ((operator.associativity == Associativity.LtR && number == 0) ||
                                    (operator.associativity == Associativity.RtL && number == 1)))) {
                return result;
            } else {
                return "( " + result + " )";
            }
        } else if (expression instanceof Assignment) {
            return "( " + result + " )";
        }

        return result;
    }

    public String result() {
        return result.toString();
    }

    @Override
    public void visit(PrintStatement printStatement) {
        // Visit the expression and serialize it
        printStatement.expression.accept(this);
        String exprString = result.toString();

        // Reset result and build the println statement
        result.setLength(0);
        result.append("println(\"")
                .append(printStatement.text)
                .append("\", ")
                .append(exprString)
                .append(");");

        // 💡 Add this line to store the result:
        statementRepresentations.put(printStatement, result.toString());
    }


}