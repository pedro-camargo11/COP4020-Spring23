package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.Arrays;
import java.util.HashMap;



public class Scanner implements IScanner{
    final String input;
    //array containing input chars, terminated with extra char 0
    char[] inputChars;

    int pos; //position of char
    char ch; //next char

    //if the character is a newline increment line, set col to 0
    int line = 1;
    int col = 1;

    //set including all the keywords
    private static HashMap<String, Kind> reservedWords;
    static{
        reservedWords = new HashMap<String, Kind>();
        reservedWords.put("image", Kind.RES_image);
        reservedWords.put("pixel", Kind.RES_pixel);
        reservedWords.put("int", Kind.RES_int);
        reservedWords.put("string", Kind.RES_string);
        reservedWords.put("void", Kind.RES_void);
        reservedWords.put("nil", Kind.RES_nil);
        reservedWords.put("load", Kind.RES_load);
        reservedWords.put("display", Kind.RES_display);
        reservedWords.put("write", Kind.RES_write);
        reservedWords.put("x", Kind.RES_x);
        reservedWords.put("y", Kind.RES_y);
        reservedWords.put("a", Kind.RES_a);
        reservedWords.put("X", Kind.RES_X);
        reservedWords.put("Y", Kind.RES_Y);
        reservedWords.put("Z", Kind.RES_Z);
        reservedWords.put("r",Kind.RES_r);
        reservedWords.put("x_cart", Kind.RES_x_cart);
        reservedWords.put("y_cart", Kind.RES_y_cart);
        reservedWords.put("a_polar", Kind.RES_a_polar);
        reservedWords.put("r_polar", Kind.RES_r_polar);
        reservedWords.put("rand", Kind.RES_rand);
        reservedWords.put("sin", Kind.RES_sin);
        reservedWords.put("cos", Kind.RES_cos);
        reservedWords.put("atan", Kind.RES_atan);
        reservedWords.put("if", Kind.RES_if);
        reservedWords.put("while", Kind.RES_while);
    }


    //define enum for internal states
    private enum State {
        START,
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT,
        IN_ESCAPE,
        IN_STR_LIT,
        IN_COMMENT,
        HAVE_GE,
        HAVE_LE,
        HAVE_EXCHANGE,
        HAVE_OR,
        HAVE_AND,
        HAVE_EXP
    }

    //constructor
    public Scanner (String input)
    {
        this.input = input;
        //creates an array of chars from input string with an extra 0 on the end.
        inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        pos = 0;
        ch = inputChars[pos];

    }

    @Override
    public IToken next() throws LexicalException{
        return scanToken();

    }
    //miscounted pos
    public void nextchar(){

        //update the position of the line,col
        if(ch == '\n'){
            col = 1;
            line++;
        }
        else{
            col++;
        }

        pos++;
        ch = inputChars[pos];
    }

    //is the variable a digit
    private boolean isDigit(int ch){

        return '0' <= ch && ch <= '9';
    }

    //is the variable a Letter
    private boolean isLetter(int ch){

        return ('a' <= ch && ch <= 'z') || ('A' <= ch &&  ch <= 'Z');
    }

    //this will be the new lexical exception throw
    private void error(String mes) throws LexicalException{

        throw new LexicalException(mes);
    }

    private IToken scanToken() throws LexicalException{

        State state = State.START;
        int tokenStart = -1;

        //read the characters
        while(true){

            switch(state){

                case START -> {

                    tokenStart = pos; //position


                    switch(ch) {
                        case 0 -> {
                            return new Token(Kind.EOF,tokenStart,0,inputChars,line,col);
                        }

                        //ignore whitespace
                        case ' ', '\n', '\r', '\t', '\f' -> nextchar();

                        case '+' -> {
                            nextchar();
                            return new Token(Kind.PLUS, tokenStart, 1, inputChars,line,col);
                        }

                        case '*' -> {
                            state = State.HAVE_EXP;
                            nextchar();
                        }

                        case '-' -> {
                            nextchar();
                            return new Token(Kind.MINUS, tokenStart, 1, inputChars,line,col);
                        }

                        case '=' -> {
                            state = State.HAVE_EQ;
                            nextchar();
                        }

                        case '/' -> {
                            nextchar();
                            return new Token(Kind.DIV,tokenStart,1,inputChars,line,col);
                        }

                        case '%' -> {
                            nextchar();
                            return new Token(Kind.MOD,tokenStart,1,inputChars,line,col);
                        }

                        case '0' -> {
                            nextchar();
                            return new NumLitToken(tokenStart, 1, inputChars,line,col);
                        }

                        case '!' -> {

                            nextchar();
                            return new Token(Kind.BANG, tokenStart,1,inputChars,line,col);
                        }

                        case '?' -> {

                            nextchar();
                            return new Token(Kind.QUESTION,tokenStart,1,inputChars,line,col);
                        }

                        case '.' -> {

                            nextchar();
                            return new Token(Kind.DOT,tokenStart,1,inputChars,line,col);
                        }

                        case ':' -> {

                            nextchar();
                            return new Token(Kind.COLON, tokenStart, 1,inputChars,line,col);
                        }

                        case ',' -> {
                            nextchar();
                            return new Token(Kind.COMMA, tokenStart,1,inputChars,line,col);
                        }

                        case '(' -> {

                            nextchar();
                            return new Token(Kind.LPAREN, tokenStart, 1, inputChars, line, col);
                        }

                        case ')' -> {

                            nextchar();
                            return new Token(Kind.RPAREN, tokenStart, 1, inputChars, line, col);
                        }

                        case '{' -> {

                            nextchar();
                            return new Token(Kind.LCURLY, tokenStart, 1, inputChars, line, col);
                        }

                        case '}' -> {

                            nextchar();
                            return new Token(Kind.RCURLY, tokenStart, 1, inputChars, line, col);
                        }

                        case '[' -> {

                            nextchar();
                            return new Token(Kind.LSQUARE, tokenStart, 1, inputChars, line, col);
                        }

                        case ']' -> {

                            nextchar();
                            return new Token(Kind.RSQUARE, tokenStart, 1, inputChars, line, col);
                        }

                        case '&' -> {

                            state = State.HAVE_AND;
                            nextchar();
                        }

                        case '|' -> {

                            state = State.HAVE_OR;
                            nextchar();
                        }

                        case '>' -> {
                            state = State.HAVE_GE;
                            nextchar();
                        }

                        case '<' -> {

                            state = State.HAVE_LE;
                            nextchar();

                        }

                        case '"' -> {
                            state = State.IN_STR_LIT;
                            nextchar();
                        }

                        case '1','2','3','4','5','6','7','8','9' -> {

                            state = State.IN_NUM_LIT;
                            nextchar();
                        }

                        //all cases of In_DENT have been merged into one case
                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_' -> {

                            state = State.IN_IDENT;
                            nextchar();
                        }

                        case '~' ->{

                            state = State.IN_COMMENT;
                            nextchar();
                        }

                        default -> error("Not implemented yet at position: " + pos);

                    }
                }

                case HAVE_EQ -> {
                    if(ch == '='){
                        state = State.START;
                        nextchar();
                        return new Token(Kind.EQ, tokenStart, 2, inputChars,line, col);
                    }
                    //if it is followed by anything else, then it is now an assigned token
                    else{
                        state = State.START;
                        return new Token(Kind.ASSIGN,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_GE -> {

                    if(ch == '=' ){
                        state = State.START;
                        nextchar();
                        return new Token(Kind.GE,tokenStart,2,inputChars,line,col);

                    }
                    //return >
                    else{

                        state = State.START;
                        return new Token(Kind.GT,tokenStart,1,inputChars,line,col);

                    }
                }

                case HAVE_LE -> {

                    if(ch == '='){
                        state = State.START;
                        nextchar();
                        return new Token(Kind.LE,tokenStart,2,inputChars,line,col);

                    }
                    //check to see if it could be part of an exchange token <->
                    else if(ch == '-'){

                        state = State.HAVE_EXCHANGE;
                        nextchar();

                    }
                    //return <
                    else{

                        System.out.println(pos);
                        state = State.START;
                        return new Token(Kind.LT,tokenStart,1,inputChars,line,col);

                    }

                }

                case HAVE_EXCHANGE -> {

                    if(ch == '>')
                    {

                        state = State.START;
                        nextchar();
                        return new Token(Kind.EXCHANGE,tokenStart,3,inputChars,line,col);
                    }
                    else
                    {
                        error("exchange does not work at position" + pos);
                    }
                }

                case HAVE_AND -> {

                    if (ch == '&')
                    {

                        state = State.START;
                        nextchar();
                        return new Token(Kind.AND,tokenStart,2,inputChars,line,col);
                    }
                    else
                    {

                        state = State.START;
                        return new Token(Kind.BITAND,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_OR -> {

                    if (ch == '|')
                    {

                        state = State.START;
                        nextchar();
                        return new Token(Kind.OR,tokenStart,2,inputChars,line,col);
                    }
                    else
                    {

                        state = State.START;
                        return new Token(Kind.BITOR,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_EXP -> {

                    if (ch == '*')
                    {

                        state = State.START;
                        nextchar();
                        return new Token(Kind.EXP,tokenStart,2,inputChars,line,col);
                    }
                    else
                    {

                        state = State.START;
                        return new Token(Kind.TIMES,tokenStart,1,inputChars,line,col);
                    }
                }

                case IN_NUM_LIT -> {

                    if(isDigit(ch))
                    {

                        nextchar();
                    }
                    else
                    {

                        try
                        {
                            int length = pos - tokenStart;
                            NumLitToken NumLit =  new NumLitToken(tokenStart, length, inputChars,line,col);
                            NumLit.getValue(); //check val
                            return NumLit;
                        }
                        catch(NumberFormatException a)
                        {

                            error("Int is too large to be parsed at position " + pos);
                        }
                    }


                }

                case IN_STR_LIT -> {

                    //check to see if the character is a valid input character
                    if (ch == '"') {
                        nextchar();
                        int length = pos - tokenStart;
                        return new StringLitToken(tokenStart, length, inputChars,line,col);
                    }
                    else if (ch == '\n' || ch == '\r')
                    {
                        error("Invalid input sequence at position " + pos);
                    }
                    else if(ch == '\\')
                    {
                        state = State.IN_ESCAPE;
                        nextchar();
                    }
                    else
                    {
                        nextchar();
                    }
                }

                case IN_ESCAPE -> {

                    //go back to string lit and get the next character
                    if(ch == 't' || ch == 'b' || ch == '"' || ch == '\\' || ch == 'n' || ch == 'r'){

                        state = State.IN_STR_LIT;
                        nextchar();
                    }
                    else{

                        error("Illegal escape sequence at position " + pos);
                    }

                }

                //when we encounter a '~" and end with newline '\n'
                case IN_COMMENT -> {

                    if( ch != '\r' && ch !='\n')
                    {
                        //EOF case
                        if(ch == 0){

                            return new Token(Kind.EOF,tokenStart,0,inputChars,line,col);
                        }
                        else{

                            nextchar();
                        }
                    }
                    else
                    {

                        state = State.START;
                    }
                }

                case IN_IDENT -> {

                    if(isDigit(ch) || isLetter((ch)) || ch == '_')
                    {
                        nextchar();
                    }
                    else
                    {
                        int length = pos - tokenStart;
                        String tokenString = new String (inputChars);
                        tokenString = tokenString.substring(tokenStart, tokenStart+length);
                        //check if token is a reserved word
                        if (reservedWords.containsKey(tokenString))
                        {
                            //gets the kind of the reserved word from the map
                            Kind k = reservedWords.get(tokenString);
                            return new Token(k, tokenStart, length, inputChars,line,col);
                        }
                        else
                        {
                            //the token is not a reserved word so it is an ident
                            return new Token(Kind.IDENT, tokenStart, length, inputChars,line,col);
                        }
                    }

                }

                default -> throw new UnsupportedOperationException("Bugs in Scanner");
            }
        }
    }
}