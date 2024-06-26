package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.SourceLocation;

public class Token implements IToken{

    Kind kind;
    int tokenStart;
    int tokenLen;
    char[] inputChars;
    int col;
    int line;

    String tokenStr;

    public Token (Kind kind, int tokenStart, int tokenLen, char[] input, int line, int col)
    {
        this.kind = kind;
        this.tokenStart = tokenStart;
        this.tokenLen = tokenLen;
        inputChars = input;
        this.col= col;
        this.line = line;
        tokenStr = this.getTokenString();
    }

    public Kind getKind()
    {
        return kind;
    }

    public String getTokenString()
    {

        String tokenStr = new String(inputChars);
        return tokenStr.substring(tokenStart,tokenStart+tokenLen);
    }

    public SourceLocation getSourceLocation ()
    {
        return new SourceLocation(line, col-tokenLen);
        //don't know if col-tokenLen logic is correct, but col is supposed to return index of first token char
    }
}