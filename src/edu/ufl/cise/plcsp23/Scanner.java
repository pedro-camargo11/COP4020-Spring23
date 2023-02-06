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
        ch= inputChar[pos];
    }

    @Override
    public Token next() throws LexicalException{
        scanToken();
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
                        case 0 -> {
                            return new Token(EOF,tokenStart,0,inputChars);
                        }
                        default -> {
                            throw new UnsupportedOperationException("Not implemented yet");
                        }
                        
                        //we want to check if there is any white space at the beginning
                        case ' ', '\n', '\r', '\t', '\f' -> nextchar();
                        
                        case '+' -> {
                            nextchar();
                            return new Token(PLUS, tokenStart, 1, inputChars);
                        }

                        case '*' -> {
                            nextchar();
                            return new Token(TIMES, tokenstart, 1, inputChars);
                        }

                        case '0' -> {
                            nextchar();
                            return new Token(Num_LIT,tokenStart,1,inputChars);

                        }

                        case '=' -> {
                            state = state.HAVE_EQ;
                            nextchar();
                        }

                    }
                }

                case HAVE_EQ -> {
                    if(ch == '='){
                        state = state.START;
                        nextchar();
                        return new Token(EQ, tokenstart, 2, inputchars);
                    }
                    else{
                        
                        error("expected=");

                    }
                }

                case IN_NUM_LIT -> {


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
