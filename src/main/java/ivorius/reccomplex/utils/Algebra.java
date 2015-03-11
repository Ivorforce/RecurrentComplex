/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.*;

/**
 * A list of operators that is able to parse strings to an expression.
 *
 * @param <T> The Algebra's data type.
 */
public class Algebra<T>
{
    protected final Set<Operator<T>> operators = new HashSet<>();
    @Nonnull
    protected SymbolTokenizer.CharacterRules rules;
    @Nullable
    protected Logger logger;

    public Algebra()
    {
        this(new SymbolTokenizer.SimpleCharacterRules());
    }

    public Algebra(@Nonnull SymbolTokenizer.CharacterRules rules)
    {
        this.rules = rules;
    }

    @SafeVarargs
    public Algebra(Operator<T>... operators)
    {
        this(new SymbolTokenizer.SimpleCharacterRules(), operators);
    }

    @SafeVarargs
    public Algebra(@Nonnull SymbolTokenizer.CharacterRules rules, Operator<T>... operators)
    {
        this.rules = rules;
        Collections.addAll(this.operators, operators);
    }

    public boolean addOperators(Collection<Operator<T>> operators)
    {
        return this.operators.addAll(operators);
    }

    public boolean addOperator(Operator<T> operator)
    {
        return operators.add(operator);
    }

    public boolean removeOperators(Collection<Operator<T>> operators)
    {
        return this.operators.removeAll(operators);
    }

    public boolean removeOperator(Operator<T> operator)
    {
        return operators.remove(operator);
    }

    public Set<Operator<T>> operators()
    {
        return Collections.unmodifiableSet(operators);
    }

    @Nonnull
    public SymbolTokenizer.CharacterRules getRules()
    {
        return rules;
    }

    public void setRules(@Nonnull SymbolTokenizer.CharacterRules rules)
    {
        this.rules = rules;
    }

    @Nullable
    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger(@Nullable Logger logger)
    {
        this.logger = logger;
    }

    public SymbolTokenizer.TokenFactory getTokenFactory()
    {
        return new SymbolTokenizer.TokenFactory()
        {
            protected boolean hasAt(String string, String symbol, int index)
            {
                return string.regionMatches(index, symbol, 0, symbol.length());
            }

            @Nullable
            @Override
            public SymbolTokenizer.Token tryConstructSymbolTokenAt(int index, @Nonnull String string)
            {
                for (Operator<T> operator : operators)
                {
                    String[] symbols = operator.getSymbols();

                    for (int s = 0; s < symbols.length; s++)
                    {
                        String symbol = symbols[s];
                        if (hasAt(string, symbol, index))
                            return new OperatorToken<>(index, index + symbol.length(), operator, s);
                    }
                }

                return null;
            }

            @Nonnull
            @Override
            public SymbolTokenizer.Token constructStringToken(int index, @Nonnull String string)
            {
                return new ConstantToken(index, index + string.length(), string);
            }
        };
    }

    @Nullable
    public Expression<T> tryParse(String string)
    {
        try
        {
            return parse(string);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    @Nonnull
    public Expression<T> parse(String string) throws ParseException
    {
        try
        {
            List<SymbolTokenizer.Token> tokens = new SymbolTokenizer(rules, getTokenFactory()).tokenize(string);

            implode(tokens, new TreeSet<>(PrecedenceSets.group(this.operators)), 0, tokens.size());

            return ((ExpressionToken<T>) tokens.get(0)).expression;
        }
        catch (ParseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            if (logger != null)
                logger.error("Internal error when parsing", e);

            throw new ParseException(String.format("%s", e.toString()), 0);
        }
    }

    protected void implode(List<SymbolTokenizer.Token> tokens, NavigableSet<PrecedenceSet<Operator<T>>> operators, int start, int end) throws ParseException
    {
        if (end - start < 1)
        {
            if (tokens.size() > start)
                throw new ParseException("Expected Expression", tokens.get(start).startIndex);
            else if (tokens.size() > 0)
                throw new ParseException("Expected Expression", tokens.get(tokens.size() - 1).startIndex);
            else
                throw new ParseException("Expected Expression", 0);
        }

        SymbolTokenizer.Token startToken;
        if (end - start == 1 && (startToken = tokens.get(start)) instanceof ConstantToken)
        {
            tokens.remove(start);
            tokens.add(start, new ExpressionToken<>(startToken.startIndex, startToken.endIndex,
                    new Constant<>(((ConstantToken) startToken).identifier)));
            return;
        }

        for (PrecedenceSet<Operator<T>> curOperators : operators)
        {
            Stack<BuildingExpression<T>> expressionStack = new Stack<>();
            expressionStack.push(new BuildingExpression<T>(null, tokens.get(start).startIndex, tokens.get(start).endIndex, start, -1));

            for (int t = start; t < end; t++)
            {
                SymbolTokenizer.Token token = tokens.get(t);
                if (token instanceof OperatorToken)
                {
                    OperatorToken<T> operatorToken = (OperatorToken<T>) token;
                    Operator<T> operator = operatorToken.operator;

                    if (operator.precedence < curOperators.precedence)
                        throw new ParseException("Internal Error (Operator Sorting)", operatorToken.startIndex);
                    else if (curOperators.contains(operator))
                    {
                        if (expressionStack.peek().isAtLastSymbol() && expressionStack.peek().operator.hasRightArgument() && operator.hasLeftArgument())
                        {
                            BuildingExpression<T> curExp = expressionStack.pop();
                            Operator<T> endedOperator = curExp.operator;
                            int numberOfArguments = endedOperator.getNumberOfArguments();

                            // Evaluate from left to right, so short-circuit asap
                            Integer lastTokenIndex = curExp.currentTokenIndex;
                            implode(tokens, operators.tailSet(curOperators, false), lastTokenIndex, t);

                            int difference = (t - lastTokenIndex) - 1;
                            end -= difference; // Account for imploded tokens
                            t -= difference; // Account for imploded tokens

                            finishImploding(tokens, curExp.startStringIndex, curExp.endStringIndex, t - numberOfArguments, t, endedOperator);
                            t -= numberOfArguments - 1; // Account for imploded tokens
                            end -= numberOfArguments - 1; // Account for imploded tokens
                        }

                        if (operatorToken.symbolIndex == 0 || expressionStack.peek().isNext(operatorToken.symbolIndex))
                        {
                            if (operatorToken.symbolIndex > 0 || operator.hasLeftArgument())
                            {
                                BuildingExpression curExp = expressionStack.peek();

                                Integer lastTokenIndex = curExp.currentTokenIndex;
                                implode(tokens, operators.tailSet(curOperators, false), lastTokenIndex, t);

                                int difference = (t - lastTokenIndex) - 1;
                                end -= difference; // Account for imploded tokens
                                t -= difference; // Account for imploded tokens
                            }

                            if (operatorToken.symbolIndex == 0)
                                expressionStack.push(new BuildingExpression<>(operator, operatorToken.startIndex, operatorToken.endIndex, t));
                            else
                            {
                                BuildingExpression curExp = expressionStack.peek();
                                curExp.currentStringIndex = operatorToken.startIndex;
                                curExp.endStringIndex = operatorToken.endIndex;
                                curExp.currentTokenIndex = t;
                                curExp.currentSymbolIndex = operatorToken.symbolIndex;
                            }

                            if (expressionStack.peek().isAtLastSymbol() && !operator.hasRightArgument())
                            {
                                BuildingExpression<T> curExp = expressionStack.pop();
                                int numberOfArguments = operator.getNumberOfArguments();

                                finishImploding(tokens, curExp.startStringIndex, curExp.endStringIndex, t - numberOfArguments, t, operator);

                                int difference = numberOfArguments - 1;
                                t -= difference; // Account for imploded tokens
                                end -= difference; // Account for imploded tokens
                            }

                            tokens.remove(t--); // Remove symbol
                            end--; // Account for removed symbol
                        }
                        else
                            throw new ParseException(String.format("Unexpected Token '%s'", operator.getSymbols()[operatorToken.symbolIndex]), operatorToken.startIndex);
                    }
                }
            }

            while (expressionStack.peek().isAtLastSymbol() && expressionStack.peek().operator.hasRightArgument())
            {
                BuildingExpression<T> curExp = expressionStack.pop();
                Operator<T> operator = curExp.operator;
                int numberOfArguments = operator.getNumberOfArguments();

                Integer lastTokenIndex = curExp.currentTokenIndex;
                implode(tokens, operators.tailSet(curOperators, false), lastTokenIndex, end);

                int difference = (end - lastTokenIndex) - 1;
                end -= difference; // Account for imploded tokens

                finishImploding(tokens, curExp.startStringIndex, curExp.endStringIndex, end - numberOfArguments, end, operator);
                end -= numberOfArguments - 1;
            }

            if (expressionStack.size() > 1)
            {
                BuildingExpression<T> curExp = expressionStack.peek();

                String[] symbols = curExp.operator.getSymbols();
                String expectedSymbol = symbols[curExp.expectedSymbolIndex()];
                String previousSymbol = symbols[curExp.currentSymbolIndex];

                throw new ParseException(String.format("Expected Token '%s'", expectedSymbol), curExp.currentStringIndex + previousSymbol.length());
            }
        }

        if (end - start > 1 || !(tokens.get(start) instanceof ExpressionToken))
            throw new ParseException("Expected Operator", tokens.get(start + 1).startIndex);
    }

    protected void finishImploding(List<SymbolTokenizer.Token> tokens, int startIndex, int endIndex, int start, int end, Operator<T> operator) throws ParseException
    {
        Expression<T>[] expressions = new Expression[end - start];

        if (end - start < 1)
            throw new ParseException("Internal Error (Missing Arguments)", startIndex);

        for (int i = 0; i < end - start; i++)
        {
            SymbolTokenizer.Token removed = tokens.remove(start);
            if (removed instanceof ExpressionToken)
                expressions[i] = ((ExpressionToken<T>) removed).expression;
            else
                throw new ParseException("Internal Error (Unevaluated Token)", startIndex);
        }

        tokens.add(start, new ExpressionToken<>(startIndex, endIndex, new Operation<>(operator, expressions)));
    }

    protected static class BuildingExpression<T>
    {
        public int startStringIndex;
        public int currentStringIndex;
        public int endStringIndex;
        public int currentTokenIndex;

        public Operator<T> operator;
        public int currentSymbolIndex;

        public BuildingExpression(Operator<T> operator, int startStringIndex, int endStringIndex, int currentTokenIndex)
        {
            this.operator = operator;
            this.startStringIndex = startStringIndex;
            this.currentStringIndex = startStringIndex;
            this.endStringIndex = endStringIndex;
            this.currentTokenIndex = currentTokenIndex;
            this.currentSymbolIndex = 0;
        }

        public BuildingExpression(Operator<T> operator, int startStringIndex, int endStringIndex, int currentTokenIndex, int currentSymbolIndex)
        {
            this.operator = operator;
            this.startStringIndex = startStringIndex;
            this.currentStringIndex = startStringIndex;
            this.endStringIndex = endStringIndex;
            this.currentTokenIndex = currentTokenIndex;
            this.currentSymbolIndex = currentSymbolIndex;
        }

        public int expectedSymbolIndex()
        {
            return currentSymbolIndex + 1;
        }

        public boolean isNext(int symbolIndex)
        {
            return currentSymbolIndex + 1 == symbolIndex;
        }

        public boolean isAtLastSymbol()
        {
            return operator != null && operator.getSymbols().length - 1 == currentSymbolIndex;
        }
    }

    public abstract static class Expression<T>
    {
        public abstract T evaluate(@Nullable Function<String, T> input);

        public abstract boolean walkVariables(Visitor<String> visitor);

        public abstract String toString(Function<String, String> stringMapper);
    }

    public static class Constant<T> extends Expression<T>
    {
        public String identifier;

        public Constant(String identifier)
        {
            this.identifier = identifier;
        }

        @Override
        public T evaluate(@Nullable Function<String, T> input)
        {
            if (input == null)
                throw new NullPointerException();

            return input.apply(identifier);
        }

        @Override
        public boolean walkVariables(Visitor<String> visitor)
        {
            return visitor.visit(identifier);
        }

        @Override
        public String toString(Function<String, String> stringMapper)
        {
            return stringMapper.apply(identifier);
        }
    }

    public static class Operation<T> extends Expression<T>
    {
        protected Operator<T> operator;
        protected Expression<T>[] expressions;

        @SafeVarargs
        public Operation(Operator<T> operator, Expression<T>... expressions)
        {
            this.operator = operator;
            this.expressions = expressions;
        }

        public Expression getExpression(int index)
        {
            return expressions[index];
        }

        public void setExpression(int index, Expression<T> expression)
        {
            this.expressions[index] = expression;
        }

        @Override
        public T evaluate(@Nullable Function<String, T> input)
        {
            return operator.evaluate(input, expressions);
        }

        @Override
        public boolean walkVariables(Visitor<String> visitor)
        {
            for (Expression expression : expressions)
                if (!expression.walkVariables(visitor))
                    return false;

            return true;
        }

        @Override
        public String toString(Function<String, String> stringMapper)
        {
            StringBuilder builder = new StringBuilder();

            int idx = 0;
            String[] symbols = operator.getSymbols();

            boolean hasSpaceRightOfFirst = operator.hasLeftArgument();
            boolean hasSpaceLeftOfLast = operator.hasRightArgument();

            if (operator.hasLeftArgument())
                builder.append(expressions[idx++].toString(stringMapper));

            for (int i = 0; i < symbols.length; i++)
            {
                if ((i > 0 || operator.hasLeftArgument()) && (i != symbols.length - 1 || hasSpaceLeftOfLast))
                    builder.append(' ');

                builder.append(symbols[i]);

                if ((i < symbols.length - 1 || operator.hasRightArgument()) && (i != 0 || hasSpaceRightOfFirst))
                    builder.append(' ');

                if (i < symbols.length - 1 || operator.hasRightArgument())
                    builder.append(expressions[idx++].toString(stringMapper));
            }

            return builder.toString();
        }
    }

    public abstract static class Operator<T> implements PrecedenceSet.NativeEntry
    {
        protected float precedence;

        protected boolean hasLeftArgument;
        protected boolean hasRightArgument;

        protected String[] symbols;

        public Operator(float precedence, boolean hasLeftArgument, boolean hasRightArgument, String... symbols)
        {
            this.precedence = precedence;
            this.hasLeftArgument = hasLeftArgument;
            this.hasRightArgument = hasRightArgument;
            this.symbols = symbols;
        }

        @Override
        public float getPrecedence()
        {
            return precedence;
        }

        public void setPrecedence(float precedence)
        {
            this.precedence = precedence;
        }

        public boolean hasLeftArgument()
        {
            return hasLeftArgument;
        }

        public void setHasLeftArgument(boolean hasLeftArgument)
        {
            this.hasLeftArgument = hasLeftArgument;
        }

        public boolean hasRightArgument()
        {
            return hasRightArgument;
        }

        public void setHasRightArgument(boolean hasRightArgument)
        {
            this.hasRightArgument = hasRightArgument;
        }

        public String[] getSymbols()
        {
            return symbols;
        }

        public void setSymbols(String[] symbols)
        {
            this.symbols = symbols;
        }

        public int getNumberOfArguments()
        {
            return symbols.length - 1 + (hasLeftArgument() ? 1 : 0) + (hasRightArgument() ? 1 : 0);
        }

        public abstract T evaluate(Function<String, T> variableEvaluator, Expression<T>[] expressions);
    }

    protected static class ExpressionToken<T> extends SymbolTokenizer.Token
    {
        public Expression<T> expression;

        public ExpressionToken(int startIndex, int endIndex, Expression<T> expression)
        {
            super(startIndex, endIndex);
            this.expression = expression;
        }
    }

    protected static class ConstantToken extends SymbolTokenizer.Token
    {
        public String identifier;

        public ConstantToken(int startIndex, int endIndex, String identifier)
        {
            super(startIndex, endIndex);
            this.identifier = identifier;
        }
    }

    protected static class OperatorToken<T> extends SymbolTokenizer.Token
    {
        public Operator<T> operator;
        public int symbolIndex;

        public OperatorToken(int startIndex, int endIndex, Operator<T> operator, int symbolIndex)
        {
            super(startIndex, endIndex);
            this.operator = operator;
            this.symbolIndex = symbolIndex;
        }
    }
}
