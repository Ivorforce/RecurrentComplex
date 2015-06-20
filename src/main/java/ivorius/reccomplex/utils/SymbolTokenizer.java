/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 11.03.15.
 */
public class SymbolTokenizer
{
    @Nonnull
    protected CharacterRules characterRules;
    @Nonnull
    protected TokenFactory tokenFactory;

    public SymbolTokenizer(@Nonnull CharacterRules characterRules, @Nonnull TokenFactory tokenFactory)
    {
        this.characterRules = characterRules;
        this.tokenFactory = tokenFactory;
    }

    @Nonnull
    public CharacterRules getCharacterRules()
    {
        return characterRules;
    }

    public void setCharacterRules(@Nonnull CharacterRules characterRules)
    {
        this.characterRules = characterRules;
    }

    @Nonnull
    public TokenFactory getTokenFactory()
    {
        return tokenFactory;
    }

    public void setTokenFactory(@Nonnull TokenFactory tokenFactory)
    {
        this.tokenFactory = tokenFactory;
    }

    public List<Token> tokenize(String string) throws ParseException
    {
        Character escapeChar = characterRules.escapeChar();
        Character literalChar = characterRules.literalChar();

        int index = 0;
        int variableStart = -1;
        int literalStart = -1;
        boolean escape = false;
        TIntStack escapes = new TIntArrayStack();
        ArrayList<Token> tokens = new ArrayList<>();

        while (index < string.length())
        {
            char character = string.charAt(index);

            if (characterRules.isIllegal(character))
                throw new ParseException(String.format("Illegal character '%c'", character), index);
            else if (!escape && escapeChar != null && escapeChar == character)
            {
                escape = true;
                escapes.push(index);

                if (variableStart < 0 && literalStart < 0)
                    variableStart = index;
            }
            else if (literalStart >= 0)
            {
                if (!escape && literalChar != null && literalChar == character)
                {
                    tokens.add(constructStringToken(string, literalStart + 1, literalStart, index, escapes));
                    literalStart = -1;
                }

                escape = false;
            }
            else if (!escape && literalChar != null && literalChar == character)
            {
                if (variableStart >= 0)
                    tokens.add(constructStringToken(string, variableStart, variableStart, index, escapes));

                literalStart = index;
            }
            else if (!escape && characterRules.isWhitespace(character))
            {
                if (variableStart >= 0)
                    tokens.add(constructStringToken(string, variableStart, variableStart, index, escapes));
                variableStart = -1;
            }
            else
            {
                Token token;

                if (!escape && (token = tokenFactory.tryConstructSymbolTokenAt(index, string)) != null)
                {
                    if (variableStart >= 0)
                        tokens.add(constructStringToken(string, variableStart, variableStart, index, escapes));
                    variableStart = -1;

                    tokens.add(token);
                    index = token.endIndex;
                    continue;
                }
                else if (variableStart < 0)
                    variableStart = index;

                escape = false;
            }

            index++;
        }

        if (literalStart >= 0)
            throw new ParseException(String.format("Unclosed literal '%c'", literalChar), index);

        if (escape)
            throw new ParseException(String.format("Unclosed escape '%c'", escapeChar), index);

        if (variableStart >= 0)
            tokens.add(constructStringToken(string, variableStart, variableStart, index, escapes));

        tokens.trimToSize();
        return tokens;
    }

    public String escapeWhereNecessary(String string)
    {
        Character escapeChar = characterRules.escapeChar();

        if (escapeChar != null)
        {
            TIntStack escapes = new TIntArrayStack();
            for (int idx = 0; idx < string.length(); idx++)
            {
                if (characterRules.isWhitespace(string.charAt(idx)) || tokenFactory.tryConstructSymbolTokenAt(idx, string) != null)
                    escapes.push(idx);
            }

            if (escapes.size() > 0)
            {
                StringBuilder builder = new StringBuilder(string);

                while (escapes.size() > 0)
                    builder.insert(escapes.pop(), escapeChar);

                return builder.toString();
            }
        }

        return string;
    }

    protected Token constructStringToken(String string, int start, int varStart, int end, TIntStack escapes)
    {
        StringBuilder constant = new StringBuilder(string.substring(start, end));

        while (escapes.size() > 0) // Iterate from the right, so indexes stay the same
            constant.deleteCharAt(escapes.pop() - start);
        escapes.clear();

        return tokenFactory.constructStringToken(varStart, constant.toString());
    }

    protected boolean hasAt(String string, String symbol, int index)
    {
        return string.regionMatches(index, symbol, 0, symbol.length());
    }

    protected static abstract class Token
    {
        public int startIndex;
        public int endIndex;

        public Token(int startIndex, int endIndex)
        {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    public static interface TokenFactory
    {
        @Nullable
        Token tryConstructSymbolTokenAt(int index, @Nonnull String string);

        @Nonnull
        Token constructStringToken(int index, @Nonnull String string);
    }

    public interface CharacterRules
    {
        Character escapeChar();

        Character literalChar();

        boolean isWhitespace(char character);

        boolean isIllegal(char character);
    }

    public static class SimpleCharacterRules implements CharacterRules
    {
        @Nullable
        private Character escapeChar;
        @Nullable
        private Character literalChar;
        @Nullable
        private char[] whitespace;
        @Nullable
        private char[] illegal;

        public SimpleCharacterRules()
        {
            this('\\', '\"', null, null);
        }

        public SimpleCharacterRules(Character escapeChar, Character literalChar, char[] whitespace, char[] illegal)
        {
            this.escapeChar = escapeChar;
            this.literalChar = literalChar;
            this.whitespace = whitespace;
            this.illegal = illegal;
        }

        @Nullable
        public Character getEscapeChar()
        {
            return escapeChar;
        }

        public void setEscapeChar(@Nullable Character escapeChar)
        {
            this.escapeChar = escapeChar;
        }

        @Nullable
        public Character getLiteralChar()
        {
            return literalChar;
        }

        public void setLiteralChar(@Nullable Character literalChar)
        {
            this.literalChar = literalChar;
        }

        @Nullable
        public char[] getWhitespace()
        {
            return whitespace;
        }

        public void setWhitespace(@Nullable char[] whitespace)
        {
            this.whitespace = whitespace;
        }

        @Nullable
        public char[] getIllegal()
        {
            return illegal;
        }

        public void setIllegal(@Nullable char[] illegal)
        {
            this.illegal = illegal;
        }

        @Override
        public boolean isWhitespace(char character)
        {
            return whitespace != null
                    ? ArrayUtils.contains(whitespace, character)
                    : Character.isWhitespace(character);
        }

        @Override
        public Character escapeChar()
        {
            return escapeChar;
        }

        @Override
        public Character literalChar()
        {
            return literalChar;
        }

        @Override
        public boolean isIllegal(char character)
        {
            return illegal != null && ArrayUtils.contains(illegal, character);
        }
    }
}
