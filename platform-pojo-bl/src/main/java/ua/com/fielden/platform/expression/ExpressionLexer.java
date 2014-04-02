package ua.com.fielden.platform.expression;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;
import ua.com.fielden.platform.expression.lexer.and.AndTokenAutomata;
import ua.com.fielden.platform.expression.lexer.case_when.case_.CaseTokenAutomata;
import ua.com.fielden.platform.expression.lexer.case_when.else_.ElseTokenAutomata;
import ua.com.fielden.platform.expression.lexer.case_when.end.EndTokenAutomata;
import ua.com.fielden.platform.expression.lexer.case_when.then_.ThenTokenAutomata;
import ua.com.fielden.platform.expression.lexer.case_when.when.WhenTokenAutomata;
import ua.com.fielden.platform.expression.lexer.comma.CommaTokenAutomata;
import ua.com.fielden.platform.expression.lexer.date.DateTokenAutomata;
import ua.com.fielden.platform.expression.lexer.date_constant.DateConstantTokenAutomata;
import ua.com.fielden.platform.expression.lexer.decimal.DecimalTokenAutomata;
import ua.com.fielden.platform.expression.lexer.div.DivTokenAutomata;
import ua.com.fielden.platform.expression.lexer.eq.EqualTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.avg.AvgTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.count.CountTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.day.DayTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.day_diff.DayDiffTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.days.DaysTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.hour.HourTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.hours.HoursTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.lower.LowerTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.max.MaxTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.min.MinTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.minute.MinuteTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.minutes.MinutesTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.month.MonthTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.months.MonthsTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.now.NowTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.second.SecondTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.seconds.SecondsTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.self.SelfTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.sum.SumTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.upper.UpperTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.year.YearTokenAutomata;
import ua.com.fielden.platform.expression.lexer.function.years.YearsTokenAutomata;
import ua.com.fielden.platform.expression.lexer.greater.GreaterTokenAutomata;
import ua.com.fielden.platform.expression.lexer.greater_or_eq.GreaterOrEqualTokenAutomata;
import ua.com.fielden.platform.expression.lexer.integer.IntegerTokenAutomata;
import ua.com.fielden.platform.expression.lexer.less.LessTokenAutomata;
import ua.com.fielden.platform.expression.lexer.less_or_eq.LessOrEqualTokenAutomata;
import ua.com.fielden.platform.expression.lexer.lparen.LparenTokenAutomata;
import ua.com.fielden.platform.expression.lexer.minus.MinusTokenAutomata;
import ua.com.fielden.platform.expression.lexer.mult.MultTokenAutomata;
import ua.com.fielden.platform.expression.lexer.name.NameTokenAutomata;
import ua.com.fielden.platform.expression.lexer.not_eq.NotEqualTokenAutomata;
import ua.com.fielden.platform.expression.lexer.null_.NullTokenAutomata;
import ua.com.fielden.platform.expression.lexer.or.OrTokenAutomata;
import ua.com.fielden.platform.expression.lexer.plus.PlusTokenAutomata;
import ua.com.fielden.platform.expression.lexer.rparen.RparenTokenAutomata;
import ua.com.fielden.platform.expression.lexer.string.StringTokenAutomata;

/**
 * A lexer to scan input for expression language tokens.
 */
public class ExpressionLexer {

    public static final char EOF = (char) -1; //  represent end of file char
    public static final int EOF_TYPE = 1; //  represent EOF token type
    protected String input; // input string
    protected int curPosition = 0; // index into input of current character
    protected char currChar; // current character

    private final BaseNonDeterministicAutomata[] tokenLexers = {//
    new LparenTokenAutomata(), new RparenTokenAutomata(), new CommaTokenAutomata(), //
            new PlusTokenAutomata(), new MinusTokenAutomata(), new MultTokenAutomata(), new DivTokenAutomata(), //
            ///////////////////// functions ////////////////////
            new AvgTokenAutomata(), new SumTokenAutomata(), new MinTokenAutomata(), new MaxTokenAutomata(),//
            new DaysTokenAutomata(), new MonthsTokenAutomata(), new YearsTokenAutomata(), //
            new CountTokenAutomata(), new DayTokenAutomata(), new MonthTokenAutomata(), new YearTokenAutomata(), //
            new HoursTokenAutomata(), new MinutesTokenAutomata(), new SecondsTokenAutomata(), //
            new HourTokenAutomata(), new MinuteTokenAutomata(), new SecondTokenAutomata(), //
            new UpperTokenAutomata(), new LowerTokenAutomata(), new DayDiffTokenAutomata(), //
            ////////////////////////////////////////////////////
            new CaseTokenAutomata(), // this block of lexers should go begore the name lexer
            new WhenTokenAutomata(), new ThenTokenAutomata(), new ElseTokenAutomata(), new EndTokenAutomata(),
            ////////////////////////////////////////////////////
            new NullTokenAutomata(), // should go before the name token
            new NowTokenAutomata(), // should go before the name token
            new SelfTokenAutomata(), // should go before the name token
            new NameTokenAutomata(),//
            new StringTokenAutomata(),//
            new DateConstantTokenAutomata(), //
            new DecimalTokenAutomata(), //
            new IntegerTokenAutomata(),//
            new DateTokenAutomata(),//
            /////////////////////////////////////////////////////
            new NotEqualTokenAutomata(), // should go before the less token
            new EqualTokenAutomata(),//
            new LessOrEqualTokenAutomata(),// should go before the less token
            new GreaterOrEqualTokenAutomata(), // should go before the greater token
            new LessTokenAutomata(), //
            new GreaterTokenAutomata(),//
            //////////////////////////////////////////////////////
            new AndTokenAutomata(), //
            new OrTokenAutomata() //
    };

    public ExpressionLexer(final String input) {
        if (StringUtils.isEmpty(input)) {
            throw new IllegalArgumentException("Empty string is an illegal input.");
        }
        this.input = input;
    }

    /**
     * Produces the next in the input text token.
     * 
     * @return
     * @throws SequenceRecognitionFailed
     */
    public Token nextToken() throws SequenceRecognitionFailed {
        while (currChar != EOF) {
            return predict(input.substring(curPosition));
        }
        return new Token(EgTokenCategory.EOF, "<EOF>", input.length(), input.length());
    }

    /**
     * Tokenizes the input into tokens (lexemes). Does not include the EOF token indicating the end of the input.
     * 
     * @return
     * @throws SequenceRecognitionFailed
     */
    public Token[] tokenize() throws SequenceRecognitionFailed {
        final List<Token> tokens = new ArrayList<Token>();
        Token token = nextToken();
        while (token.category.getIndex() != EgTokenCategory.EOF.index) {
            tokens.add(token);
            token = nextToken();
        }
        return tokens.toArray(new Token[] {});
    }

    private Token predict(final String substring) throws SequenceRecognitionFailed {
        SequenceRecognitionFailed error = null;
        String lastPretendant = "";

        BaseNonDeterministicAutomata tokenLexer = null;
        int index = 0;
        while (index < tokenLexers.length) {
            try {
                tokenLexer = tokenLexers[index];
                final String tokenText = tokenLexer.recognisePartiallyFromStart(substring, curPosition);
                final int prevPosition = curPosition;
                curPosition += tokenLexer.getCharsRecognised();
                if (curPosition >= input.length()) {
                    currChar = EOF;
                }
                return new Token(tokenLexer.lexemeCat, tokenText, prevPosition, curPosition); // prevPosition + tokenLexer.getCharsRecognised()
            } catch (final SequenceRecognitionFailed e) {
                if (tokenLexer.getPretendantSequence().toString().length() > lastPretendant.length()) {
                    lastPretendant = tokenLexer.getPretendantSequence().toString();
                    error = e;
                }
            } finally {
                index++;
            }
        }
        throw error != null ? error : new SequenceRecognitionFailed("Unrecognisable symbol at position " + curPosition, curPosition);
    }
}