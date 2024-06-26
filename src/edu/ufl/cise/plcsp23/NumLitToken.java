package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.LexicalException;

import static java.lang.Integer.*;



public class NumLitToken extends Token implements INumLitToken { //remove abstract once sourcelocation is implemented

    int value;

    public NumLitToken(int tokenStart, int tokenLen, char[] inputChars,int line,int col){

        super(Kind.NUM_LIT, tokenStart, tokenLen, inputChars,line, col);
        value = getValue();
    }
    public int getValue(){
        int value = 0;
        // convert the token to a string. Convert string to int
        value = parseInt(this.getTokenString());
        return value;

    }


}
