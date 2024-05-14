package net.ajpappas.discord.boltbot.modules.counting;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.bigmath.functions.bigdecimalmath.BigDecimalMathFunctions;
import com.ezylang.evalex.bigmath.operators.bigdecimalmath.BigDecimalMathOperators;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExpressionEvaluator {

    private final ExpressionConfiguration configuration;

    @Autowired
    public ExpressionEvaluator() {
        configuration = ExpressionConfiguration.defaultConfiguration()
                .withAdditionalFunctions(BigDecimalMathFunctions.allFunctions())
                .withAdditionalOperators(BigDecimalMathOperators.allOperators());
    }

    public BigDecimal evaluate(String expression) throws EvaluationException, ParseException {
        return new Expression(expression, configuration).evaluate().getNumberValue();
    }
}
