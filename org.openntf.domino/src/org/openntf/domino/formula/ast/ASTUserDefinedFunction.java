/* Generated By:JJTree: Do not edit this line. ASTUserDefinedFunction.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openntf.domino.formula.ast;

import java.util.Set;

import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.FormulaReturnException;
import org.openntf.domino.formula.ValueHolder;
import org.openntf.domino.formula.impl.UserDefinedFunction;
import org.openntf.domino.formula.parse.AtFormulaParserImpl;

/**
 * @author Roland Praml, Foconis AG
 * 
 */
public class ASTUserDefinedFunction extends SimpleNode {

	public ASTUserDefinedFunction(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	public void toFormula(final StringBuilder sb) {
		// does not generate any output
	}

	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws FormulaReturnException {
		return ValueHolder.valueOf("");
	}

	@Override
	protected void analyzeThis(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {
		// TODO
	}

	/**
	 * A extended function needs not to inspect it's children. If the function is never invoked, nothing is needed
	 */
	@Override
	public void inspect(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {
	}

	public void init() {
		int functionVariables = 0;
		ASTUserDefinedFunctionDef def = (ASTUserDefinedFunctionDef) children[0];

		UserDefinedFunction function = def.getFunction();

		for (int i = 1; i < children.length; i++) {
			if (children[i] instanceof ASTUserDefinedFunctionVariable) {
				functionVariables++;
			} else {
				function.setFunction(children[i]);
			}
		}

		ASTUserDefinedFunctionVariable[] var = new ASTUserDefinedFunctionVariable[functionVariables];
		for (int i = 0; i < functionVariables; i++) {
			var[i] = (ASTUserDefinedFunctionVariable) children[i + 1];
		}

		function.setVariables(var);

	}
}
/* JavaCC - OriginalChecksum=3108394236ef426155ab016a1734bf8a (do not edit this line) */
