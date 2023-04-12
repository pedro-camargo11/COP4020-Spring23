package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import java.util.Random;
import java.util.List;

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

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        throw new RuntimeException("visitAssignmentStatement not implemented");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        throw new RuntimeException("visitBinaryExpr not implemented");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        //visit children
        code.append(") { \n");

        List<Declaration> decs = block.getDecList();
        for (Declaration dec : decs) {
            dec.visit(this, arg);
        }
        List<Statement> statements = block.getStatementList();
        for (Statement statement : statements) {
            statement.visit(this, arg);
        }

        code.append("} \n");


        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        throw new RuntimeException("visitConditionalExpr not implemented");
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        throw new RuntimeException("visitDeclaration not implemented");
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
        return identExpr.getName(); //return the name
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        throw new RuntimeException("visitLValue not implemented");
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        throw new RuntimeException("visitNameDef not implemented");
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return numLitExpr.getValue(); //return the value
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
            String paramName = param.getIdent().getName();

            //append to paramString with a , if not the first param
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
        code.append(";");
        return code;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        return stringLitExpr.getValue(); //return the stringLit value
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitUnaryExpr not yet implemented");
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitUnaryExprPostFix not yet implemented");
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitWhileStatement not yet implemented");
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        throw new RuntimeException("CodeGenerator.visitWriteStatement not yet implemented");
    }


    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return code.append(zExpr.getValue()); //return value 255
    }
}
