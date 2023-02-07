package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.Token;

import static java.lang.Integer.*;


public class NumLitToken extends Token implements INumLitToken { //remove abstract once sourcelocation is implemented
    public NumLitToken(int tokenStart, int tokenLen, char[] inputChars){
        super(Kind.NUM_LIT, tokenStart, tokenLen, inputChars);
    }
    public int getValue(){
        int value = 0;
        // convert the token to a string. Convert string to int
        value = parseInt(this.getTokenString());
        return value;
    }


}
