package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.SourceLocation;

public class Token implements IToken{

    Kind kind;
    int tokenStart;
    int tokenLen;
    char[] inputChars;

    public Token (Kind kind, int tokenStart, int tokenLen, char[] input)
    {
        this.kind = kind;
        this.tokenStart = tokenStart;
        this.tokenLen = tokenLen;
        inputChars = input;
    }
    
    public Kind getKind()
    {
        return kind;
    }

    public String getTokenString()
    {
        String tokenStr= inputChars.toString();
        return tokenStr.substring(tokenStart, tokenLen);
    }

    public SourceLocation getSourceLocation ()
    {
        return null;//for now
        //return SourceLocation()
    }
}
