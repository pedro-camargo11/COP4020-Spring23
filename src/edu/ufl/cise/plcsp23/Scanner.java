package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.Arrays;

import edu.ufl.cise.plcsp23.LexicalException;

public class Scanner implements IScanner{
    final String input;
    //array containing input chars, terminated with extra char 0
    char[] inputChars;

    int pos; //position of char
    char ch; //next char

    //define enum for internal states
    private enum State {
        START, 
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT,
        IN_OP_SEP,
        IN_STR_LIT
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
    public Token next() throws LexicalException{
        return scanToken();
        //return null;//for now
    }
    public void nextchar() throws LexicalException{
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




    private Token scanToken() throws LexicalException{

        State state = State.START;
        int tokenStart = -1;
        
        //read the characters 
        while(true){
            
            switch(state){

                case START -> {
                    
                    tokenStart = pos; //position

                    switch(ch) {
                        case 0 -> { //EOF token
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
                            return new Token(Kind.NUM_LIT,tokenStart,1,inputChars);
                        }

                        case '=' -> {
                            state = state.HAVE_EQ;
                            nextchar();
                        }

                        case '1','2','3','4','5','6','7','8','9' -> {

                            state = State.IN_NUM_LIT;
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
                        return new NumLitToken(tokenStart, length, inputChars);
                    }


                }

                case IN_STR_LIT -> {


                }

                case IN_IDENT -> {

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
