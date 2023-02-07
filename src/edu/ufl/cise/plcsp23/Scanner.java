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
        IN_OP_SEP,
        IN_STR_LIT,
        IN_COMMENT
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
    public void nextchar() throws LexicalException{

        //update the position of the line,col
        if(ch != '\n'){

            col++;
        }
        else{
            col = 0;
            line++;
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
                            return new Token(Kind.EOF,tokenStart,0,inputChars);
                        }
                        default -> {
                            throw new UnsupportedOperationException("Not implemented yet");
                        }

                        //ignore whitespace
                        case ' ', '\n', '\r', '\t', '\f' -> nextchar();

                        case '+' -> {
                            nextchar();
                            return new Token(Kind.PLUS, tokenStart, 1, inputChars);
                        }

                        case '*' -> {
                            nextchar();
                            return new Token(Kind.TIMES, tokenStart, 1, inputChars);
                        }

                        case '0' -> {
                            nextchar();
                            return new NumLitToken(tokenStart, 1, inputChars);
                        }

                        case '=' -> {
                            state = state.HAVE_EQ;
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
                        return new Token(Kind.EQ, tokenStart, 2, inputChars);
                    }
                    else{
                        //error("expected=");
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

                            NumLitToken NumLit =  new NumLitToken(tokenStart, length, inputChars);
                            NumLit.getValue(); //check val
                            return NumLit;
                        }
                        catch(NumberFormatException a){

                            throw new LexicalException("Int is too large to be parsed");
                        }
                    }


                }

                case IN_STR_LIT -> {

                }

                //when we encounter a '~" and end with newline '\n'
                case IN_COMMENT -> {

                    //check for '\r'
                    if( ch != '\n'){

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
                            return new Token(k, tokenStart, length, inputChars);
                        }
                        else {
                            //the token is not a reserved word so it is an ident
                            return new Token(Kind.IDENT, tokenStart, length, inputChars);
                        }
                    }

                }

                case IN_OP_SEP -> {


                }

                default -> {
                    throw new UnsupportedOperationException("Bugs in Scanner");
                }
            }
        }
    }
}