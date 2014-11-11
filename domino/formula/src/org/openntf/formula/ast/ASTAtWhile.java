/* Generated By:JJTree: Do not edit this line. ASTAtWhile.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openntf.formula.ast;

import java.util.Set;

import org.openntf.formula.FormulaContext;
import org.openntf.formula.FormulaReturnException;
import org.openntf.formula.ValueHolder;
import org.openntf.formula.parse.AtFormulaParserImpl;

public class ASTAtWhile extends SimpleNode {

	public ASTAtWhile(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	/**
	 * AtDoWhile returns always TRUE, or an Error-ValueHolder, if an error occurs in the last parameter.
	 */
	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws FormulaReturnException {

		ValueHolder ret = null;
		if (children != null) {
			while (children[0].evaluate(ctx).isTrue(ctx)) {
				for (int i = 1; i < children.length; ++i) {
					children[i].evaluate(ctx);
				}
			}
		}
		return ValueHolder.valueOf(1); // returns always TRUE

	}

	@Override
	protected void analyzeThis(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {
		functions.add("@while");
	}

}
/* JavaCC - OriginalChecksum=82a89b5b4119700eb90e21e98b430148 (do not edit this line) */