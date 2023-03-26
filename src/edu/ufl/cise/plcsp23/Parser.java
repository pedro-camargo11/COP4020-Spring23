package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.ast.Dimension;



import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{

    IScanner scanner;
    IToken t;
    private int current = 0; //keeps track of position


    //constructor -> passes in only one time
//    public Parser(ArrayList<IToken> tokenList) throws PLCException {
//        this.tokenList = tokenList;
//        t = tokenList.get(current);
//
//    }


    //overloaded constructor of Parser that takes in the scanner instead of a tokenList
    public Parser (IScanner scanner) throws LexicalException {

        this.scanner = scanner;
        t = scanner.next();
    }

    //consume the token and move on to the next.
    void consume() throws LexicalException{
        t = scanner.next();
    }

    void error(String mes) throws SyntaxException{
        throw new SyntaxException(mes);
    }

    //Singular token.
    protected boolean isKind(IToken.Kind kind){
        return (t.getKind() == kind);
    }

    //multiple kinds of isKind tokens
    protected boolean isKind(IToken.Kind... kinds){

        for(IToken.Kind k:kinds){

            if(k == t.getKind()){
                return true;
            }
        }
        return false;
    }

    //Match the token and move on to the next.
    protected void match(IToken.Kind... kinds) throws PLCException{

        for(IToken.Kind k:kinds){

            if(k == t.getKind()){
                consume();
                return;
            }

        }

        error("Syntax error: expected " + kinds + " but got " + t.getKind() + " instead.");
    }

    //no error given anymore, go with implementing grammar functions.
    public AST parse() throws PLCException{

        AST program = Program();

        //check for EOF
        if(t.getKind() != IToken.Kind.EOF){
            error("Syntax error: expected EOF but got " + t.getKind() + " instead.");
        }
        return program;

    }

    //Program -> Type IDENT LPAREN ParamList RPAREN Block (entry point)
    Program Program() throws PLCException{

        Type type = Type(); //get the type
        IToken identToken = t;
        match(IToken.Kind.IDENT);
        Ident ident = new Ident(identToken);
        match(IToken.Kind.LPAREN);
        List<NameDef> paramList = ParamList();
        match(IToken.Kind.RPAREN);
        Block block = Block();

        return new Program(t, type, ident, paramList, block);

    }

    //Block Syntax: LCURLY DeclarationList StatementList RCURLY
    Block Block() throws PLCException{

        match(IToken.Kind.LCURLY);
        List<Declaration> declarationList = DecList();
        List<Statement> statementList = StatementList();
        match(IToken.Kind.RCURLY);
        return new Block(t, declarationList, statementList);
    }

    List<Declaration> DecList() throws PLCException{


        List<Declaration> decList = new ArrayList<>();

        //when there is an empty string (epsilon) -> return empty decList
        if(isKind(IToken.Kind.RCURLY, IToken.Kind.IDENT, IToken.Kind.RES_write, IToken.Kind.RES_while)){
            return decList;
        }

        //while the next token is a DOT, keep adding to the list
        while(isKind(IToken.Kind.RES_int, IToken.Kind.RES_string, IToken.Kind.RES_image, IToken.Kind.RES_pixel, IToken.Kind.RES_void)){
            Declaration dec = Declaration();
            decList.add(dec);

            //check to see if it's a DOT, if not then throw an error
            if(isKind(IToken.Kind.DOT)){
                match(IToken.Kind.DOT);
            }
        }

        return decList;
    }

    List<Statement> StatementList() throws PLCException {

        List<Statement> statementList = new ArrayList<>();

        //when there is an empty string (epsilon) -> return empty statementList
        if(isKind(IToken.Kind.RCURLY)){
            return statementList;
        }

        while (!(isKind(IToken.Kind.RCURLY))) {
            Statement statement = Statement();

            //error check
            if (!isKind(IToken.Kind.DOT)) {
                error("Syntax error: expected a DOT but got " + t.getKind() + " instead.");
            }

            match(IToken.Kind.DOT);
            statementList.add(statement);
        }

        return statementList;
    }

    List<NameDef> ParamList() throws PLCException{

        List<NameDef> paramList = new ArrayList<>();
        //when there is an empty string (epsilon) -> empty paramList
        if(isKind(IToken.Kind.RPAREN)){
            return paramList;
        }

        //when there is a non-empty string -> NameDef(,NameDef)*
        paramList.add(NameDef());
        while(isKind(IToken.Kind.COMMA)){
            consume();
            paramList.add(NameDef());
        }
        return paramList;
    }

    //NameDef Syntax: type IDENT | Type Dimension IDENT
    NameDef NameDef() throws PLCException{

        IToken identToken;
        Type type = Type();
        if(isKind(IToken.Kind.LSQUARE)){
            Dimension dimension = Dimension();
            identToken = t;
            match(IToken.Kind.IDENT);
            Ident ident = new Ident(identToken);
            return new NameDef(t, type, dimension, ident);
        }
        else{
            identToken = t;
            match(IToken.Kind.IDENT);
            Ident ident = new Ident(identToken);
            return new NameDef(t, type,null ,ident);
        }


    }

    //Dimension Syntax: [Expr, Expr]
    Dimension Dimension() throws PLCException{

        match(IToken.Kind.LSQUARE);
        Expr expr = Expression();
        match(IToken.Kind.COMMA);
        Expr expr2 = Expression();
        match(IToken.Kind.RSQUARE);
        return new Dimension(t, expr, expr2);

    }

    //get the type and consume the token: image, pixel, int, string, void
    Type Type() throws PLCException{
        if (isKind(IToken.Kind.RES_image, IToken.Kind.RES_pixel, IToken.Kind.RES_int, IToken.Kind.RES_string, IToken.Kind.RES_void)) {
            Type type = Type.getType(t);
            consume();
            return type;
        }
        else {
            error("Syntax error: expected a type but got " + t.getKind() + " instead.");
            return null;
        }
    }

    //Declaration Syntax: NameDef | NameDef ASSIGN Expr
    Declaration Declaration() throws PLCException{

        NameDef namedef = NameDef();

        if(isKind(IToken.Kind.ASSIGN)){
            match(IToken.Kind.ASSIGN);
            Expr expr = Expression();
            return new Declaration(t, namedef, expr);
        }
        else{
            consume();
            return new Declaration(t, namedef, null);
        }


    }

    //Expression
    Expr Expression() throws PLCException {

        //conditional_expr
        if(isKind(IToken.Kind.RES_if)){
            return ConditionalExpression();
        }
        //Or_expr
        else{

            return OrExpression();
        }
    }

    //Using match to error check
    Expr ConditionalExpression() throws PLCException{

        match(IToken.Kind.RES_if);//match if
        Expr guard = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr trueCase = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr falseCase = Expression();
        return new ConditionalExpr(t, guard, trueCase, falseCase);
    }

    //LogicalOrExpression
    Expr OrExpression() throws PLCException {

        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = AndExpression();

        while(isKind(IToken.Kind.BITOR, IToken.Kind.OR)){
            IToken.Kind op = t.getKind();
            consume();
            right = AndExpression();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    //LogicalAndExpression
    Expr AndExpression() throws PLCException {
        Expr left = null;
        Expr right = null;
        IToken firstToken = t;
        left =  CompareExpr();

        while(isKind(IToken.Kind.BITAND,IToken.Kind.AND)){
            IToken.Kind op = t.getKind();
            consume();
            right = CompareExpr();
            left = new BinaryExpr (firstToken, left, op, right);
        }
        return left;

    }

    //Comparison Expression
    Expr CompareExpr() throws PLCException {

        Expr left = null;
        Expr right = null;
        IToken firstToken = t;
        left = PowExpr();

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GE)){
            IToken.Kind op = t.getKind();
            consume();
            right = PowExpr();
            left = new BinaryExpr (firstToken, left, op, right);
        }
        return left;
    }

    //Power Expression
    Expr PowExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        IToken firstToken = t;
        left = AdditiveExpr();

        while(isKind(IToken.Kind.EXP)){
            IToken.Kind op = t.getKind();
            consume();
            right = PowExpr();
            left = new BinaryExpr (firstToken, left, op, right);
        }
        return left;
    }

    //Additive Expression
    Expr AdditiveExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        IToken firstToken = t;
        left = MultExpr();

        while(isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){
            IToken.Kind op = t.getKind();
            consume();
            right = MultExpr();
            left = new BinaryExpr (firstToken, left, op, right);
        }
        return left;
    }

    //Multiplicative Expression
    Expr MultExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        IToken firstToken = t;
        left = UnaryExpression();

        while(isKind(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD)){
            IToken.Kind op = t.getKind();
            consume();
            right = UnaryExpression();
            left = new BinaryExpr (firstToken, left, op, right);
        }
        return left;
    }

    //Unary Expressions
    Expr UnaryExpression() throws PLCException{

        Expr right = null;
        IToken firstToken = t;
        if(isKind(IToken.Kind.BANG, IToken.Kind.RES_atan, IToken.Kind.MINUS, IToken.Kind.RES_sin,IToken.Kind.RES_cos)){
            IToken unaryToken = t; // reassign
            consume();
            right = UnaryExpression();
            return new UnaryExpr(firstToken,unaryToken.getKind(),right);
        }

        return UnaryExpressionPostfix();

    }

    //UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
    Expr UnaryExpressionPostfix() throws PLCException {
        IToken firstToken = t;
        Expr primary = PrimaryExpr();
        if (isKind(IToken.Kind.LSQUARE)) {

            PixelSelector pixelSelector = selector();

            if(!(isKind(IToken.Kind.DOT))){
                ColorChannel channelSelector = channel();
                return new UnaryExprPostfix(firstToken, primary, pixelSelector, channelSelector);
            }
            else{
                return new UnaryExprPostfix(firstToken, primary, pixelSelector, null);
            }

        }

        //(ChannelSelector | ε )
        else if(isKind(IToken.Kind.COLON)){

            ColorChannel channelSelector = channel();
            return new UnaryExprPostfix(firstToken, primary, null, channelSelector);

        }

        return primary;
    }

    //Primary Expressions
    Expr PrimaryExpr() throws PLCException {


        switch (t.getKind()) {

            case NUM_LIT -> {
                IToken num_lit = t;
                consume();
                return new NumLitExpr(num_lit);
            }
            case STRING_LIT -> {
                IToken string_lit = t;
                consume();
                return new StringLitExpr(string_lit);
            }

            case IDENT ->{
                IToken ident = t;
                consume();
                return new IdentExpr(ident);
            }

            case RES_Z -> {
                IToken z_token = t;
                consume();
                return new ZExpr(z_token);
            }

            case RES_rand -> {
                IToken rand_token = t;
                consume();
                return new RandomExpr(rand_token);
            }

            //Parenthesis ()
            case LPAREN -> {

               match(IToken.Kind.LPAREN);
               Expr e = Expression();
               match(IToken.Kind.RPAREN);
               return e;

            }

            //Predeclared Variables
            case RES_x,RES_y,RES_a,RES_r -> {
                IToken predeclared = t;
                consume();
                return new PredeclaredVarExpr(predeclared);
            }

            //Expanded Pixel
            case LSQUARE -> {

                IToken firstToken = t;
                match(IToken.Kind.LSQUARE);
                Expr expr1 = Expression();
                match(IToken.Kind.COMMA);
                Expr expr2 = Expression();
                match(IToken.Kind.COMMA);
                Expr expr3 = Expression();
                match(IToken.Kind.RSQUARE);

                //return t because of match() function
                return new ExpandedPixelExpr(firstToken, expr1, expr2, expr3);

            }

            //Pixel Function Expression
            case RES_x_cart, RES_y_cart, RES_a_polar, RES_r_polar -> {

                IToken firstToken = t;
                IToken.Kind kind = t.getKind();

                consume();

                PixelSelector selector = selector();

                //return t because of match() function
                return new PixelFuncExpr(firstToken, kind, selector);
            }

            default -> error("Invalid Primary Expression");
        }
        return null;
    }

    //Channel Selector
    ColorChannel channel() throws PLCException{

        match(IToken.Kind.COLON);
        ColorChannel color = ColorChannel.getColor(t);
        consume();

        return color;

    }

    //Pixel Selector function
    PixelSelector selector() throws PLCException{
        //maybe we have to save first token here to pass to pixel selector constructor !!!!!
        IToken firstToken = t;
        match (IToken.Kind.LSQUARE);
        Expr expr1 = Expression();
        match(IToken.Kind.COMMA);
        Expr expr2 = Expression();
        match(IToken.Kind.RSQUARE);

        return new PixelSelector(firstToken,expr1, expr2);

    }

    //LValue ::= IDENT (PixelSelector | ε ) (ChannelSelector | ε )
    LValue LValue() throws PLCException {
        IToken firstToken = t;
        Ident ident = new Ident(t);
        consume();
        PixelSelector pixelSelector = null;
        ColorChannel channelSelector = null;
        //if token is a '[' we have a pixel selector
        if (isKind(Token.Kind.LSQUARE)) {
            pixelSelector = selector();
        }
        //if token is ":" it is a channel selector
        if (isKind(IToken.Kind.COLON)){
            match(IToken.Kind.COLON);
            channelSelector = channel();
        }
        return new LValue (firstToken, ident, pixelSelector, channelSelector);
    }

    //Statement::= LValue = Expr | write Expr | while Expr Block
    Statement Statement() throws PLCException {
        IToken firstToken = t;
        //case for an assignment statement
        if (isKind(Token.Kind.IDENT)) {
            LValue lValue = LValue();
            match(IToken.Kind.ASSIGN);
            Expr expr = Expression();
            return new AssignmentStatement(firstToken, lValue, expr);
        }
        //case for a write statement
        else if (isKind(Token.Kind.RES_write)){
            match(IToken.Kind.RES_write);
            Expr expr = Expression();
            return new WriteStatement(firstToken, expr);
        }
        //case for a while statement
        else if (isKind(Token.Kind.RES_while)){
            match(IToken.Kind.RES_while);
            Expr expr = Expression();
            Block block = Block();
            return new WhileStatement(firstToken, expr, block);
        }
        error("Invalid statement");
        return null;
    }




}
