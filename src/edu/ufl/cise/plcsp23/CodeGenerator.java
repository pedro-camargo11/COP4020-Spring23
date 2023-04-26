package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import java.util.Random;
import java.util.List;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;

//Do not use code.append when returning otherwise it will return the entire code
public class CodeGenerator implements ASTVisitor {

    public StringBuilder code;
    public String packages;

    Type returnType;

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
            case PIXEL -> {typeString = "int";}
            case IMAGE -> {typeString = "BufferedImage";}
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
                code.append("(");
                code.append(left.visit(this, null));
                code.append(" != 0)");
                code.append(" && ");
                code.append("(");
                code.append(right.visit(this, null));
                code.append(" != 0)");

            }

            case OR -> {
                code.append("(");
                code.append(left.visit(this, null));
                code.append(" != 0)");
                code.append(" || ");
                code.append("(");
                code.append(right.visit(this, null));
                code.append(" != 0)");
            }
        }

        code.append(" ? 1 : 0");

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
        e.visit(this, arg);

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



            condition.visit(this, arg);
            code.append(" != 0");
            code.append(") ? ");
            trueExpr.visit(this, arg);
            code.append(" : ");
            falseExpr.visit(this, arg);


        return "";
    }
    //OLD VISIT DEC
    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nameDef = declaration.getNameDef();
        nameDef.visit(this, arg);

        if(declaration.getInitializer() != null){
            code.append(" = ");
            Expr e = declaration.getInitializer();

            if (nameDef.getType() == Type.STRING && e.getType() == Type.INT)
            {
                code.append("Integer.toString(");
                e.visit(this,arg);
                code.append(")");
            }
            if (nameDef.getType() == Type.IMAGE) //cases where NameDef is a IMAGE
            {
                //case where initializer is a string
                if (e.getType() == Type.STRING)
                {
                    code.append("FileURLIO.readImage(");
                    e.visit(this,arg);
                    code.append(")");
                }
                else{
                    e.visit(this,arg);
                }
            }
            else if (nameDef.getType() == Type.PIXEL) //cases where NameDef is a PIXEL
            {
                //case where the initializer is a PIXEL
                if (e instanceof ExpandedPixelExpr)
                {
                    Expr r = ((ExpandedPixelExpr) e).getRedExpr();
                    Expr g = ((ExpandedPixelExpr) e).getGrnExpr();
                    Expr b = ((ExpandedPixelExpr) e).getBluExpr();
                    //need to extract rgb calues as ints and append to pixel ops pack
                    code.append("PixelOps.pack(");
                    code.append(r.visit(this,arg));
                    code.append(", ");
                    code.append(g.visit(this,arg));
                    code.append(", ");
                    code.append(b.visit(this,arg));
                    code.append(")");

                }

            }
            else
            {
                e.visit(this,arg);
            }
        }

        return " ";
        //throw new RuntimeException("visitDeclaration not implemented");
    }

    //VISIT DEC FOR A6
//    @Override
//    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
//        NameDef nameDef = declaration.getNameDef();
//        nameDef.visit(this, arg);
//
//        if(declaration.getInitializer() != null){
//
//            code.append(" = ");
//            Expr e = declaration.getInitializer();
//
//            if (nameDef.getType() == Type.STRING && e.getType() == Type.INT)
//            {
//                code.append("Integer.toString(");
//                e.visit(this,arg);
//                code.append(")");
//            }
//            else if (nameDef.getType() == Type.IMAGE) // type is image
//            {
//                //if dimension is null, size is determined from the initializer
//                if (nameDef.getDimension() == null)
//                {
//                    //if Initializer is a String, it is the url of a file name
//                    //use FileURLIO.readimage (like cg20)
//                    if (e.getType() == Type.STRING)
//                    {
//                        code.append("FileURLIO.readImage(");
//                        e.visit(this,arg);
//                        code.append(")");
//                    }
//                    //if Initializer has a type image, use ImageOps.cloneImage
//                    else if (e.getType() == Type.IMAGE)
//                    {
//                        code.append("ImageOps.cloneImage(");
//                        e.visit(this,arg);
//                        code.append(")");
//                    }
//                }
//                else // (nameDef.getDimension() != null) create image with the specified dimensions
//                {
//                    //default pixel values are ff000000
//                    code.append("new Image("); // IDK if this is right
//                    nameDef.getDimension().visit(this,arg);
//                    code.append(")");
//                }
//            }
//            else if (e.getType() == Type.STRING) // if initializer is image use readImage overload with size paramaters
//            {
//
//            }
//            else if (e.getType() == Type.IMAGE) // if initializer is image use copyAndResize
//            {
//
//            }
//            else //regular
//            {
//                e.visit(this,arg);
//            }
//        }
//        else // (declaration.getInitializer() == null)
//        {
//            //if no initializer, use ImageOps.makeImage
//            //makeImage(int width, int height)
//            code.append("ImageOps.makeImage(");
//            nameDef.getDimension().visit(this,arg); // may have to change where this is
//            code.append(")");
//        }
//
//        return " ";
//        //throw new RuntimeException("visitDeclaration not implemented");
//    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        //Generate comma separated code to evaluate the two expressions
        Expr width = dimension.getWidth();
        Expr height = dimension.getHeight();

        width.visit(this, arg);
        code.append(", ");
        height.visit(this, arg);

        return " ";
       //throw new RuntimeException("visitDimension not implemented");
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
        returnType = program.getType();

        String name = program.getIdent().getName();
        code.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO; \n");
        code.append("import edu.ufl.cise.plcsp23.runtime.FileURLIO; \n");
        code.append("import edu.ufl.cise.plcsp23.runtime.ImageOps; \n");
        code.append("import edu.ufl.cise.plcsp23.runtime.PixelOps; \n");
        code.append("import edu.ufl.cise.plcsp23.runtime.PLCRuntimeException; \n");
        code.append("import java.awt.image.BufferedImage;");
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

        if (returnStatement.getE().getType() == Type.INT && returnType == Type.STRING)
        {
            code.append("String.valueOf(");
            code.append(returnStatement.getE().visit(this, arg));
            code.append(")");
        }
        else
        {
            code.append(returnStatement.getE().visit(this, arg));
        }
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
        Expr primaryExpr = unaryExprPostfix.getPrimary();

        if (primaryExpr.getType() == Type.IMAGE)
        {
            // case with pixel selector only
            if (unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() == null)
            {
                //PrimayExpr PixelSelector ε
                // Use ImageOps.getRGB
                //Example
                // a[x,y]
                // ImageOps.getRGB(a,x,y)
                code.append("ImageOps.getRGB(");
                Expr pixelX = unaryExprPostfix.getPixel().getX();
                Expr pixelY = unaryExprPostfix.getPixel().getY();
                code.append(primaryExpr.visit(this, arg));
                code.append(", ");
                code.append(pixelX.visit(this, arg));
                code.append(", ");
                code.append(pixelY.visit(this, arg));
                code.append(")");
            }
            //case with pixel selector and color channel
            else if (unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() != null)
            {
                //Use PixelOps method to get color from pixel
                //and ImageOps.getRGB
                //Example:
                // a[x,y]:red
                // PixelOps.red(ImageOps.getRGB(a,x,y)
                //see test cg6_1
                Expr pixelX = unaryExprPostfix.getPixel().getX();
                Expr pixelY = unaryExprPostfix.getPixel().getY();
                ColorChannel color = unaryExprPostfix.getColor();

                code.append("PixelOps.");
                code.append(color.name()); //not sure if this is correct
                code.append("(ImageOps.getRGB(");
                code.append(primaryExpr.visit(this, arg));
                code.append(", ");
                code.append(pixelX.visit(this, arg));
                code.append(", ");
                code.append(pixelY.visit(this, arg));
                code.append("))");

            }
            //case with color channel only
            else if (unaryExprPostfix.getPixel() == null && unaryExprPostfix.getColor() != null)
            {
                // Use ImageOps extract routine
                //Example:
                // a:red
                // ImageOps.extractRed(a)

                ColorChannel color = unaryExprPostfix.getColor();
                code.append("ImageOps.extract");
                if (color.name().equals("red"))
                {
                    code.append("Red(");
                }
                else if (color.name().equals("green"))
                {
                    code.append("Grn(");
                }
                else
                {
                    code.append("Blu(");
                }
                code.append(primaryExpr.visit(this, arg));
                code.append(")");

            }
            else{
                throw new RuntimeException("CodeGenerator.visitUnaryExprPostFix error");
            }
        }
        else if (primaryExpr.getType() == Type.PIXEL)
        {
            //PrimaryExpr ChannelSelector
            // Use PixelOps red,grn, or blu
            //Example:
            // a:red
            // PixelOps.red(a)

            ColorChannel color = unaryExprPostfix.getColor();
            code.append("PixelOps.");
            code.append(color.name());
            code.append("(");
            code.append(primaryExpr.visit(this, arg));
            code.append(")");
        }
        else {
            throw new RuntimeException("CodeGenerator.visitUnaryExprPostFix error");
        }

    return " ";
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



            condition.visit(this, arg);
            code.append(" != 0");
            Block block = whileStatement.getBlock();
            block.visit(this, arg);


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
