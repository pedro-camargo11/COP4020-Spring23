/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.plcsp23;

import java.util.ArrayList;
import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.ast.ASTVisitor;

public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		//Add statement to return an instance of your scanner
		return new Scanner(input);

	}


//This is the method that was used to create the parser for Assignment 2
	public static IParser makeAssignment2Parser(String input) throws PLCException {
		// create scanner and parser and return the parser
		IScanner scanner = makeScanner(input);
		return new Parser(scanner);
	}

	//This is the method that will be used to create the parser
	public static IParser makeParser(String input) throws LexicalException, SyntaxException {

		// create scanner and parser and return the parser
		IScanner scanner = makeScanner(input);
		return new Parser(scanner);
	}

	public static ASTVisitor makeTypeChecker() {
		//Code to instantiate and return an ASTVisitor for type checking
		// return null for now. Can't instantiate an ASTVisitor
		return null;
	}



}
