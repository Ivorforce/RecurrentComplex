/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.tools.Pairs;
import ivorius.ivtoolkit.tools.Ranges;
import ivorius.ivtoolkit.tools.Visitor;
import ivorius.reccomplex.utils.PrecedenceSet;
import ivorius.reccomplex.utils.PrecedenceSets;
import ivorius.reccomplex.utils.SymbolTokenizer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A list of operators that is able to parse strings to an expression.
 *
 * @param <T> The Algebra's data type.
 */
public class Algebra<T>
{
    protected final Set<Operator<T>> operators = new HashSet<>();
    @Nonnull
    protected SymbolTokenizer.CharacterRules characterRules;

    public Algebra()
    {
        this(new SymbolTokenizer.SimpleCharacterRules());
    }

    public Algebra(@Nonnull SymbolTokenizer.CharacterRules characterRules)
    {
        this.characterRules = characterRules;
    }

    @SafeVarargs
    public Algebra(Operator<T>... operators)
    {
        this(new SymbolTokenizer.SimpleCharacterRules(), operators);
    }

    @SafeVarargs
    public Algebra(@Nonnull SymbolTokenizer.CharacterRules characterRules, Operator<T>... operators)
    {
        this.characterRules = characterRules;
        Collections.addAll(this.operators, operators);
    }

    protected static <T> ExpressionToken<T> reduceExpressions(List<SymbolTokenizer.Token> tokens, NavigableSet<PrecedenceSet<Operator<T>>> operators, int stringIndex) throws ParseException
    {
        if (tokens.size() == 0)
            throw new ParseException("Expected Expression", stringIndex);

        SymbolTokenizer.Token startToken;
        if (tokens.size() == 1 && (startToken = tokens.get(0)) instanceof ConstantToken)
        {
            tokens.remove(0);
            ExpressionToken<T> exp = new ExpressionToken<>(startToken.startIndex, startToken.endIndex,
                    new Constant<>(((ConstantToken) startToken).identifier));
            tokens.add(exp);
            return exp;
        }

        for (PrecedenceSet<Operator<T>> curOperators : operators)
        {
            Stack<BuildingExpression<T>> expressionStack = new Stack<>();
            expressionStack.push(new BuildingExpression<>(null, stringIndex, tokens.get(tokens.size() - 1).endIndex, 0, -1));

            for (int t = 0; t < tokens.size(); t++)
            {
                SymbolTokenizer.Token token = tokens.get(t);

                if (token instanceof AmbiguousOperatorToken)
                    throw new IllegalArgumentException("Ambiguous operators are not supported at this point"); // TODO
                else if (token instanceof OperatorToken)
                {
                    OperatorToken<T> operatorToken = (OperatorToken<T>) token;
                    Operator<T> operator = operatorToken.operator;

                    if (operator.precedence < curOperators.getPrecedence())
                        throw new ParseException("Internal Error (Operator Sorting)", operatorToken.startIndex);
                    else if (curOperators.contains(operator))
                    {
                        if (expressionStack.peek().isAtLastSymbol() && expressionStack.peek().operator.hasRightArgument() && operator.hasLeftArgument())
                            t -= reduceBuildingExpression(expressionStack.pop(), tokens, operators.tailSet(curOperators, false), t); // Account for reduced tokens

                        if (operatorToken.symbolIndex == 0 || expressionStack.peek().isNext(operatorToken.symbolIndex))
                        {
                            if (operatorToken.symbolIndex > 0 || operator.hasLeftArgument())
                            {
                                Integer lastTokenIndex = expressionStack.peek().currentTokenIndex;
                                reduceExpressions(tokens.subList(lastTokenIndex, t), operators.tailSet(curOperators, false), operatorToken.startIndex);
                                t -= (t - lastTokenIndex) - 1; // Account for reduced tokens
                            }

                            if (operatorToken.symbolIndex == 0)
                                expressionStack.push(new BuildingExpression<>(operator, operatorToken.startIndex, operatorToken.endIndex, t));
                            else
                                expressionStack.peek().advance(operatorToken.startIndex, operatorToken.endIndex, t, operatorToken.symbolIndex);

                            if (expressionStack.peek().isAtLastSymbol() && !operator.hasRightArgument())
                            {
                                BuildingExpression<T> curExp = expressionStack.pop();
                                int numberOfArguments = operator.getNumberOfArguments();

                                reduceOperator(tokens.subList(t - numberOfArguments, t), curExp.startStringIndex, curExp.endStringIndex, operator);
                                t -= numberOfArguments - 1; // Account for reduced tokens
                            }

                            tokens.remove(t--); // Remove symbol
                        }
                        else
                            throw new ParseException(String.format("Unexpected Token '%s'", operator.getSymbols()[operatorToken.symbolIndex]), operatorToken.startIndex);
                    }
                }
            }

            while (expressionStack.peek().isAtLastSymbol() && expressionStack.peek().operator.hasRightArgument())
                reduceBuildingExpression(expressionStack.pop(), tokens, operators.tailSet(curOperators, false), tokens.size());

            if (expressionStack.size() > 1)
            {
                BuildingExpression<T> curExp = expressionStack.peek();
                String[] symbols = curExp.operator.getSymbols();

                throw new ParseException(String.format("Expected Token '%s'", symbols[curExp.expectedSymbolIndex()]),
                        curExp.currentStringIndex + symbols[curExp.currentSymbolIndex].length());
            }
        }

        if (tokens.size() > 1 || !(tokens.get(0) instanceof ExpressionToken))
            throw new ParseException("Expected Operator", tokens.get(1).startIndex);

        return (ExpressionToken) tokens.get(0);
    }

    protected static <T> void reduceOperator(final List<SymbolTokenizer.Token> tokens, final int startIndex, int endIndex, Operator<T> operator) throws ParseException
    {
        if (tokens.size() < 1)
            throw new ParseException("Internal Error (Missing Arguments)", startIndex);

        Expression<T>[] expressions = new Expression[tokens.size()];
        for (int i = 0; i < tokens.size(); i++)
        {
            SymbolTokenizer.Token token = tokens.get(i);
            if (token instanceof ExpressionToken)
                expressions[i] = ((ExpressionToken<T>) token).expression;
            else
                throw new ParseException("Internal Error (Unevaluated Token)", startIndex);
        }
        tokens.clear();

        tokens.add(new ExpressionToken<>(startIndex, endIndex, new Operation<>(operator, expressions)));
    }

    protected static <T> int reduceBuildingExpression(BuildingExpression<T> buildingExpression, List<SymbolTokenizer.Token> tokens, NavigableSet<PrecedenceSet<Operator<T>>> followingOperators, int currentTokenIndex) throws ParseException
    {
        Operator<T> endedOperator = buildingExpression.operator;
        int numberOfArguments = endedOperator.getNumberOfArguments();

        // Evaluate from left to right, so short-circuit asap
        Integer lastTokenIndex = buildingExpression.currentTokenIndex;
        reduceExpressions(tokens.subList(lastTokenIndex, currentTokenIndex), followingOperators, buildingExpression.endStringIndex);

        int difference = (currentTokenIndex - lastTokenIndex) - 1;
        currentTokenIndex -= difference; // Account for reduced tokens

        reduceOperator(tokens.subList(currentTokenIndex - numberOfArguments, currentTokenIndex), buildingExpression.startStringIndex, buildingExpression.endStringIndex, endedOperator);
        return difference + numberOfArguments - 1; // Count reduced tokens
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
    public SymbolTokenizer.CharacterRules getCharacterRules()
    {
        return characterRules;
    }

    public void setCharacterRules(@Nonnull SymbolTokenizer.CharacterRules characterRules)
    {
        this.characterRules = characterRules;
    }

    public SymbolTokenizer.TokenFactory getTokenFactory()
    {
        return new SymbolTokenizer.TokenFactory()
        {
            protected boolean hasAt(String string, int index, String symbol)
            {
                return string.regionMatches(index, symbol, 0, symbol.length());
            }

            @Nullable
            @Override
            public SymbolTokenizer.Token tryConstructSymbolTokenAt(int index, @Nonnull String string)
            {
                SortedSet<List<Pair<Operator<T>, Integer>>> sortedSymbols = sortedSymbols();

                // Try each symbol string
                for (List<Pair<Operator<T>, Integer>> symbolPairList : sortedSymbols)
                {
                    String symbol = getStringSymbol(symbolPairList.get(0));
                    if (hasAt(string, index, symbol))
                    {
                        return symbolPairList.size() > 1
                                ? new AmbiguousOperatorToken<>(index, index + symbol.length(), symbolPairList)
                                : new OperatorToken<>(index, index + symbol.length(), symbolPairList.get(0).getLeft(), symbolPairList.get(0).getRight());
                    }
                }

                return null;
            }

            @Nonnull
            protected TreeSet<List<Pair<Operator<T>, Integer>>> sortedSymbols()
            {
                // Extract individual symbols
                ArrayList<Pair<Operator<T>, Integer>> symbols = Lists.newArrayList(Iterables.concat(operators.stream().map(operator -> Pairs.pairLeft(operator, Ranges.toIterable(operator.symbols.length))).collect(Collectors.toList())));

                // Sort by length
                TreeSet<List<Pair<Operator<T>, Integer>>> sortedSymbols = new TreeSet<>((o1, o2) -> getStringSymbol(o2.get(0)).compareTo(getStringSymbol(o1.get(0))));

                // Group by symbol string
                sortedSymbols.addAll(group(symbols, this::getStringSymbol).stream().map(Lists::newArrayList).collect(Collectors.toList()));

                return sortedSymbols;
            }

            private String getStringSymbol(Pair<Operator<T>, Integer> symbol)
            {
                return symbol.getLeft().getSymbols()[symbol.getRight()];
            }

            @Nonnull
            @Override
            public SymbolTokenizer.Token constructStringToken(int index, @Nonnull String string)
            {
                return new ConstantToken(index, index + string.length(), string);
            }
        };
    }

    public static <T> Collection<Collection<T>> group(List<T> list, Function<T, Object> group)
    {
        Multimap<Object, T> groupedSymbols = HashMultimap.create();
        list.forEach(o -> groupedSymbols.put(group.apply(o), o));
        return groupedSymbols.asMap().values();
    }

    @Nonnull
    public Expression<T> parse(String string) throws ParseException
    {
        return constructExpression(new SymbolTokenizer(characterRules, getTokenFactory()).tokenize(string));
    }

    public Expression<T> constructExpression(List<SymbolTokenizer.Token> tokens) throws ParseException
    {
        return reduceExpressions(Lists.newArrayList(tokens), new TreeSet<>(PrecedenceSets.group(this.operators)), 0).expression;
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

        public void advance(int stringStartIndex, int stringEndIndex, int tokenIndex, int symbolIndex)
        {
            currentStringIndex = stringStartIndex;
            endStringIndex = stringEndIndex;
            currentTokenIndex = tokenIndex;
            currentSymbolIndex = symbolIndex;
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
            Preconditions.checkNotNull(input);

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

    public static class Value<T> extends Expression<T>
    {
        public T value;
        public String representation;

        public Value(T value, String representation)
        {
            this.value = value;
            this.representation = representation;
        }

        @Override
        public T evaluate(@Nullable Function<String, T> input)
        {
            return value;
        }

        @Override
        public boolean walkVariables(Visitor<String> visitor)
        {
            return true;
        }

        @Override
        public String toString(Function<String, String> stringMapper)
        {
            return representation;
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

    protected static class AmbiguousOperatorToken<T> extends SymbolTokenizer.Token
    {
        final List<Pair<Operator<T>, Integer>> operators;

        public AmbiguousOperatorToken(int startIndex, int endIndex, List<Pair<Operator<T>, Integer>> operators)
        {
            super(startIndex, endIndex);
            this.operators = Collections.unmodifiableList(operators);
        }

        public OperatorToken<T> toOperatorToken(Pair<Operator<T>, Integer> pair)
        {
            return new OperatorToken<>(startIndex, endIndex, pair.getKey(), pair.getValue());
        }
    }

    protected static class OperatorToken<T> extends SymbolTokenizer.Token
    {
        public final Operator<T> operator;
        public final int symbolIndex;

        public OperatorToken(int startIndex, int endIndex, Operator<T> operator, int symbolIndex)
        {
            super(startIndex, endIndex);
            this.operator = operator;
            this.symbolIndex = symbolIndex;
        }
    }
}
