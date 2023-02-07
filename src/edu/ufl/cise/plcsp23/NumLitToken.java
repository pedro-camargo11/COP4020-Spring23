package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.Token;

import static java.lang.Integer.*;


public abstract class NumLitToken implements INumLitToken{ //remove abstract once sourcelocation is implemented
    public int getValue(){
        int value = 0;
        // convert the token to a string. Convert string to int
        value = parseInt(getTokenString());
        return value;
    }

}
