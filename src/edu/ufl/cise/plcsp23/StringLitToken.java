package edu.ufl.cise.plcsp23;

import java.util.ArrayList;

public class StringLitToken extends Token implements IStringLitToken{
    public StringLitToken(int tokenStart, int tokenLen, char[] inputChars,int line,int col){
        super(Kind.STRING_LIT, tokenStart, tokenLen, inputChars,line, col);
    }

    //Easier to read and understand - could be made to return stringBuilder or char?
    private void EscapeSeq(char next, StringBuilder result){

        if (next == 'b' || next == 't' || next == '"' || next == 92 || next == 'n' || next == 'r')
        {
            switch(next){

                case 'b' -> result.append('\b');
                case 't' -> result.append('\t');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
            }
        }

    }

    public String getValue(){
        String tokenStr = getTokenString();
        tokenStr= tokenStr.substring(1,tokenLen-1);//remove quotations enclosing StringLit
        StringBuilder result = new StringBuilder(); //append everything to the stringbuilder to return
        //now handle escape sequences;
        for (int i = 0; i < tokenStr.length() ; i++)
        {
            if (tokenStr.charAt(i) == 92) //92 is \ in ascii
            {
                i++;
                //check to see if the next char is part of a legal escape sequence
                if (i <= tokenStr.length()){
                    char next = tokenStr.charAt(i);
                    EscapeSeq(next,result);
                }
            }
            else {
                char c = tokenStr.charAt(i);
                result.append(c);
            }
        }

        return result.toString();
    }
}
