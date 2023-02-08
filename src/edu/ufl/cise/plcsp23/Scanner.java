package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.Arrays;
import java.util.HashMap;

import edu.ufl.cise.plcsp23.LexicalException;

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
        ch= inputChars[pos];

    }

    @Override
    public IToken next() throws LexicalException{
        return scanToken();
        //return null;//for now
    }
    //miscounted pos
    public void nextchar() throws LexicalException{

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

    //helper function for determining whether a character is a string character. (used for StringLit)
    private boolean isStringCharacter (int ch){
        if (ch != '"' && ch != 92){ //valid string character cannot be " or \ (\ is 92 in ascii)
            return true;
        }
        //if \ is followed by b, t, ", or \ it is a legal string character.
        if (pos != inputChars.length-1){ //makes sure that pos doesn't point to last index in inputChars
            int next = inputChars[pos+1];
            if (next == 'b' || next == '"' || next == 't' || next == 92) //92 corresponds to \ in ascii
            {
                return true;
            }

        }
        return false;
    }



    private IToken scanToken() throws LexicalException{

        State state = State.START;
        int tokenStart = -1;

        //read the characters
        while(true){
            //col++;
            switch(state){

                case START -> {

                    tokenStart = pos; //position


                    switch(ch) {
                        case 0 -> {
                            return new Token(Kind.EOF,tokenStart,0,inputChars,line,col);
                        }
                        default -> {
                            throw new LexicalException("Not implemented yet");
                        }

                        //ignore whitespace
                        case ' ', '\n', '\r', '\t', '\f' -> nextchar();

                        case '+' -> {
                            nextchar();
                            return new Token(Kind.PLUS, tokenStart, 1, inputChars,line,col);
                        }

                        case '*' -> {
                            state = state.HAVE_EXP;
                            nextchar();
                        }

                        case '-' -> {
                            nextchar();
                            return new Token(Kind.MINUS, tokenStart, 1, inputChars,line,col);
                        }

                        case '=' -> {
                            state = state.HAVE_EQ;
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

                            state = state.HAVE_AND;
                            nextchar();
                        }

                        case '|' -> {

                            state = state.HAVE_OR;
                            nextchar();
                        }

                        case '>' -> {
                            state = state.HAVE_GE;
                            nextchar();
                        }

                        case '<' -> {

                            state = state.HAVE_LE;
                            nextchar();

                        }

                        case '"' -> {
                            state = state.IN_STR_LIT;
                            nextchar();
                        }



                        case '1','2','3','4','5','6','7','8','9' -> {

                            state = State.IN_NUM_LIT;
                            nextchar();
                        }

                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' -> {

                            state = State.IN_IDENT;
                            nextchar();
                        }

                        case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z' -> {

                            state = State.IN_IDENT;
                            nextchar();
                        }

                        case '_' -> {

                            state = State.IN_IDENT;
                            nextchar();
                        }

                        case '~' ->{

                            state = State.IN_COMMENT;
                            nextchar();
                        }



                    }
                }

                case HAVE_EQ -> {
                    if(ch == '='){
                        state = state.START;
                        nextchar();
                        return new Token(Kind.EQ, tokenStart, 2, inputChars,line, col);
                    }
                    //if it is followed by anything else, then it is now an assigned token
                    else{
                        state = state.START;
                        return new Token(Kind.ASSIGN,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_GE -> {

                    if(ch == '=' ){
                        state = state.START;
                        nextchar();
                        return new Token(Kind.GE,tokenStart,2,inputChars,line,col);

                    }
                    //return >
                    else{

                        state = state.START;
                        return new Token(Kind.GT,tokenStart,1,inputChars,line,col);

                    }
                }

                case HAVE_LE -> {

                    if(ch == '='){
                        state = state.START;
                        nextchar();
                        return new Token(Kind.LE,tokenStart,2,inputChars,line,col);

                    }
                    //check to see if it could be part of an exchange token <->
                    else if(ch == '-'){

                        state = state.HAVE_EXCHANGE;
                        nextchar();

                    }
                    //return <
                    else{

                        System.out.println(pos);
                        state = state.START;
                        return new Token(Kind.LT,tokenStart,1,inputChars,line,col);

                    }

                }

                case HAVE_EXCHANGE -> {

                    if(ch == '>'){

                        state = state.START;
                        nextchar();
                        return new Token(Kind.EXCHANGE,tokenStart,3,inputChars,line,col);
                    }
                    else{

                        throw new LexicalException("exchange does not work");
                    }
                }

                case HAVE_AND -> {

                    if (ch == '&'){

                        state = state.START;
                        nextchar();
                        return new Token(Kind.AND,tokenStart,2,inputChars,line,col);
                    }
                    else{

                        state = state.START;
                        return new Token(Kind.BITAND,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_OR -> {

                    if (ch == '|'){

                        state = state.START;
                        nextchar();
                        return new Token(Kind.OR,tokenStart,2,inputChars,line,col);
                    }
                    else{

                        state = state.START;
                        return new Token(Kind.BITOR,tokenStart,1,inputChars,line,col);
                    }
                }

                case HAVE_EXP -> {

                    if (ch == '*'){

                        state = state.START;
                        nextchar();
                        return new Token(Kind.EXP,tokenStart,2,inputChars,line,col);
                    }
                    else{

                        state = state.START;
                        return new Token(Kind.TIMES,tokenStart,1,inputChars,line,col);
                    }
                }

                case IN_NUM_LIT -> {

                    if(isDigit(ch)){

                        nextchar();
                    }
                    else{

                        int length = pos - tokenStart;
                        //return new Token(Kind.NUM_LIT,tokenStart,length,inputChars);

                        try {

                            NumLitToken NumLit =  new NumLitToken(tokenStart, length, inputChars,line,col);
                            NumLit.getValue(); //check val
                            return NumLit;
                        }
                        catch(NumberFormatException a){

                            throw new LexicalException("Int is too large to be parsed");
                        }
                    }


                }

                case IN_STR_LIT -> {
                    //check to see if the character is a valid input character
                    if (isStringCharacter(ch)){
                        nextchar();
                    }
                    else {
                        nextchar(); //increment pos and ch to skip to include the closing quotation
                        int length = pos - tokenStart;
                        StringLitToken StringLit = new StringLitToken(tokenStart, length, inputChars,line,col);
                        return StringLit;
                    }
                }

                //when we encounter a '~" and end with newline '\n'
                case IN_COMMENT -> {

                    if( ch != '\r' && ch !='\n'){

                          nextchar();

                    }
                    else{

                        state = State.START;
                    }
                }

                case IN_IDENT -> {
                    if(isDigit(ch) || isLetter((ch)) || ch == '_'){
                        nextchar();
                    }
                    else{
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
                        else {
                            //the token is not a reserved word so it is an ident
                            return new Token(Kind.IDENT, tokenStart, length, inputChars,line,col);
                        }
                    }

                }

        
                default -> {
                    throw new UnsupportedOperationException("Bugs in Scanner");
                }
            }
        }
    }
}
