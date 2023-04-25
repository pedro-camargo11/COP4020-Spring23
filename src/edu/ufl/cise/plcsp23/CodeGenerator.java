package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import java.util.Random;
import java.util.List;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;

//Do not use code.append when returning otherwise it will return the entire code
public class CodeGenerator implements ASTVisitor {

    public StringBuilder code;
    public String packages;

    public CodeGenerator(String packageName) {
        code = new StringBuilder();
        packages = packageName;

    }

    String getTypeAsString (Type type){
        String typeString = "";
        switch (type){
            case INT -> {typeString = "int";}
            case STRING -> {typeString = "String";}
            case VOID -> {typeString = "void";}
            //case PIXEL -> {typeString = "Pixel";}
            //case IMAGE -> {typeString = "Image";}
            default -> throw new RuntimeException ("error in CodeGenerator.visitProgram, unexpected type " + type);
        }
        return typeString;
    }

    void convertBoolean (BinaryExpr binary) throws PLCException{

        Expr left = binary.getLeft();
        IToken.Kind op = binary.getOp();
        Expr right = binary.getRight();

        switch(op) {

            case LT -> {
                code.append(left.visit(this, null));
                code.append(" < ");
                code.append(right.visit(this, null));
            }
            case GT -> {
                code.append(left.visit(this, null));
                code.append(" > ");
                code.append(right.visit(this, null));
            }
            case LE -> {
                code.append(left.visit(this, null));
                code.append(" <= ");
                code.append(right.visit(this, null));
            }
            case GE -> {
                //If true append 1, else append 0
                code.append(left.visit(this, null));
                code.append(" >= ");
                code.append(right.visit(this, null));
            }
            case EQ -> {
                code.append(left.visit(this, null));
                code.append(" == ");
                code.append(right.visit(this, null));
            }

            case AND -> {
                code.append(left.visit(this, null));
                if(!(left instanceof BinaryExpr && isKind(((BinaryExpr) left).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR))){
                    code.append(" != 0");
                }
                code.append(" && ");
                code.append(right.visit(this, null));
                if(!(right instanceof BinaryExpr && isKind(((BinaryExpr) left).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR))){
                    code.append(" != 0");
                }

            }

            case OR -> {
                code.append(left.visit(this, null));
                if(!(left instanceof BinaryExpr && isKind(((BinaryExpr) left).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR))){
                    code.append(" != 0");
                }

                code.append(" || ");
                code.append(right.visit(this, null));
                if(!(right instanceof BinaryExpr && isKind(((BinaryExpr) left).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR))){
                    code.append(" != 0");
                }

            }
        }

    }

    //isKind for Binary Expression Checking in the future
    protected boolean isKind(IToken.Kind t,IToken.Kind... kinds){

        for(IToken.Kind k:kinds){

            if(k == t){
                return true;
            }
        }
        return false;
    }

    //LVALUE = EXPR
    //where LVALUE is obtained by visiting
    //LValue, and EXPR is obtained by visiting
    //Expr
    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue lv = statementAssign.getLv();
        Expr e = statementAssign.getE();
        lv.visit(this, arg);
        code.append(" = ");

        //if e is an instance of BinaryExpr, then we need to append a number to it
        if(e instanceof BinaryExpr && isKind(((BinaryExpr) e).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR)){
            e.visit(this,arg);
            code.append("? 1 : 0");
        }
        else{
            e.visit(this, arg);
        }
        return " \n";
        //throw new RuntimeException("visitAssignmentStatement not implemented");
    }

    //0 -> False
    //1 -> True
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {

        code.append("(");
        Expr left = binaryExpr.getLeft();
        IToken.Kind op = binaryExpr.getOp();
        Expr right = binaryExpr.getRight();

        switch(op){

            case PLUS -> {
                code.append(left.visit(this, arg));
                code.append(" + ");
                code.append(right.visit(this, arg));
            }

            case MINUS -> {
                code.append(left.visit(this, arg));
                code.append(" - ");
                code.append(right.visit(this, arg));
            }

            case TIMES -> {
                code.append(left.visit(this, arg));
                code.append(" * ");
                code.append(right.visit(this, arg));
            }

            case DIV -> {
                code.append(left.visit(this, arg));
                code.append(" / ");
                code.append(right.visit(this, arg));
            }

            case MOD -> {
                code.append(left.visit(this, arg));
                code.append(" % ");
                code.append(right.visit(this, arg));
            }

            case EXP -> {
                code.insert(0, "import java.lang.Math; \n");
                code.append("(int) Math.pow(");
                code.append(left.visit(this, arg));
                code.append(", ");
                code.append(right.visit(this, arg));
                code.append(")");
            }

            //Need to deal with the boolean values and how that will be returned via docs.
            case LT,GT, LE, GE, EQ, AND, OR-> {
                convertBoolean(binaryExpr);
            }
        }

        //throw new RuntimeException("visitBinaryExpr not implemented");

        code.append(")");
        return "";
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        //visit children
        code.append(") { \n");

        List<Declaration> decs = block.getDecList();
        for (Declaration dec : decs) {
            dec.visit(this, arg);
            code.append("; \n");
        }
        List<Statement> statements = block.getStatementList();
        for (Statement statement : statements) {
            statement.visit(this, arg);
            //if statement is not a while statement, add semicolon I THINK?
            code.append("; \n");
        }

        code.append("} \n");


        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Expr condition = conditionalExpr.getGuard();
        Expr trueExpr = conditionalExpr.getTrueCase();
        Expr falseExpr = conditionalExpr.getFalseCase();

        code.append("(");  //needs to do something here to return an integer

        //check to see if it is not equal to 0 -> same logic for while statement
        if(condition instanceof BinaryExpr){

            condition.visit(this, arg);
            code.append(") ? ");
            trueExpr.visit(this, arg);
            code.append(" : ");
            falseExpr.visit(this, arg);
        }
        else{

            condition.visit(this, arg);
            code.append(" != 0");
            code.append(") ? ");
            trueExpr.visit(this, arg);
            code.append(" : ");
            falseExpr.visit(this, arg);

        }
        return "";
    }

    //all declarations have to be unique. implement a check for that with a set
    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nameDef = declaration.getNameDef();
        nameDef.visit(this, arg);

        if(declaration.getInitializer() != null){
            code.append(" = ");
            Expr e = declaration.getInitializer();

            //if e is an instance of BinaryExpr, then we need to append a number to it
            if(e instanceof BinaryExpr && isKind(((BinaryExpr) e).getOp(), IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.LE, IToken.Kind.GE, IToken.Kind.EQ, IToken.Kind.AND, IToken.Kind.OR)){
                e.visit(this,arg);
                code.append("? 1 : 0");
            }
            else{
                e.visit(this, arg);
            }
        }

        return " ";
        //throw new RuntimeException("visitDeclaration not implemented");
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
       throw new RuntimeException("visitDimension not implemented");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        throw new RuntimeException("visitExpandedPixelExpr not implemented");
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        throw new RuntimeException("visitIdent not implemented");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        code.append(identExpr.getJavaName());
        return " ";
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        String ident = lValue.getIdent().getJavaName();
        code.append(ident);
        return " "; //append semicolon inside
        //later consider the case with a pixel selector and channel selector
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Type type = nameDef.getType();
        String typeString = getTypeAsString(type);
        String name = nameDef.getIdent().getJavaName();

        code.append(typeString);
        code.append(" ");
        code.append(name);
        return " "; //append semicolon inside

        //throw new RuntimeException("visitNameDef not implemented");
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        code.append(numLitExpr.getValue()); //return the value
        return " "; //append semicolon inside
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        throw new RuntimeException("visitPixelFuncExpr not implemented");
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        throw new RuntimeException("visitPixelSelector not implemented");
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        throw new RuntimeException("visitPredeclaredVarExpr not implemented");
    }

    // Generate and return a string containing a valid java class.
    // public class NAME {
    //     public static TYPE apply(PARAMS) {
    //         BLOCK
    //     }
    // where: NAME is from the Ident, TYPE is the Java Type corresponding to Type.
    // PARAMS are from NameDef* and separated with a comma
    // Block contains the declarations and the statements in Block
    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        String name = program.getIdent().getName();
        code.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO; \n");
        code.append("public class ");
        code.append(name);
        code.append(" { \n");
        code.append("public static ");

        String type = getTypeAsString(program.getType());
        code.append(type);
        code.append(" apply(");

        List<NameDef> params = program.getParamList();
        for (int i = 0; i < params.size(); i++){
            NameDef param = params.get(i);
            //get type from NameDef as String
            String paramType = getTypeAsString(param.getType());

            //get name from NameDef
            String paramName = param.getIdent().getJavaName();

            //append to paramString with a, if not the first param
            if (i < params.size() - 1){
                code.append(paramType);
                code.append(" ");
                code.append(paramName);
                code.append(", ");
            }
            else {
                code.append(paramType);
                code.append(" ");
                code.append(paramName);
            }
        }

        Block block = program.getBlock();
        visitBlock(block, arg);

        code.append("}");

        return code.toString();
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        double random = Math.floor(Math.random() * 256);
        return code.append(random);
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        code.append("return ");
        code.append(returnStatement.getE().visit(this, arg));
        return code;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        code.append("\"");
        code.append(stringLitExpr.getValue()); //append the stringLit value
        code.append("\"");
        return " "; //return the stringLit value
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitUnaryExpr not yet implemented");
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitUnaryExprPostFix not yet implemented");
    }

    //If your input program has redeclared an
    //identifier in the inner scope, a straightforward
    //translation into Java will not work. To get full
    //credit, you will need to handle this case. One
    //easy way to do it is to give each variable a
    //unique name in the generated java code.
    //You may find it easiest to do this in the type
    //checking pass
    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        code.append("while(");
        Expr condition = whileStatement.getGuard();

        if(condition instanceof BinaryExpr){

            condition.visit(this, arg);
            Block block = whileStatement.getBlock();
            block.visit(this, arg);
        }
        else{

            condition.visit(this, arg);
            code.append(" != 0");
            Block block = whileStatement.getBlock();
            block.visit(this, arg);
        }

        //code.append("}");
        return " \n";
    }

    //Fixed: Note that if you have a semicolon on an empty line, it will be ignored.
    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {

        //code.insert(0,"import edu.ufl.cise.plcsp23.runtime.ConsoleIO; \n");

        Expr e = statementWrite.getE();
        code.append("ConsoleIO.write(");
        code.append(e.visit(this, arg));
        code.append(")");

        return "";
        //throw new RuntimeException("CodeGenerator.visitWriteStatement not yet implemented");
    }


    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return code.append(zExpr.getValue()); //return value 255
    }
}
