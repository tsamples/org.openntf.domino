/* Generated By:JJTree: Do not edit this line. ASTFunction.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
/*
 * © Copyright FOCONIS AG, 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.domino.formula.ast;

import java.util.Set;

import org.openntf.domino.formula.Function;
import org.openntf.domino.formula.EvaluateException;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.FormulaReturnException;
import org.openntf.domino.formula.ValueHolder;
import org.openntf.domino.formula.ValueHolder.DataType;
import org.openntf.domino.formula.impl.UserDefinedFunction;
import org.openntf.domino.formula.parse.AtFormulaParserImpl;
import org.openntf.domino.formula.parse.ParseException;

public class ASTFunction extends SimpleNode {
	protected Function function;

	public ASTFunction(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	public void init(final String string) throws ParseException {
		function = parser.getFunctionLC(string.toLowerCase());
		if (function == null) {
			throw new IllegalArgumentException("'" + string + "' is not a function");
		}
		if (!function.checkParamCount(jjtGetNumChildren())) {
			throw new ParseException(parser, "parameter count mismatch");
		}
	}

	/* (non-Javadoc)
	 * @see org.openntf.domino.tests.rpr.formula.SimpleNode#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ": " + function;
	}

	/**
	 * function.evaluate(ctx,params) may throw any runtime exception. Error-Valueholders are not passed to the function. Thats why we need
	 * an AST-AtText
	 */
	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws FormulaReturnException {
		ValueHolder params[] = new ValueHolder[children == null ? 0 : children.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = children[i].evaluate(ctx);
			if (params[i].dataType == DataType.ERROR)
				return params[i];
		}
		try {
			return function.evaluate(ctx, params);
		} catch (RuntimeException cause) {
			return ValueHolder.valueOf(new EvaluateException(codeLine, codeColumn, cause));
		}
	}

	@Override
	public void toFormula(final StringBuilder sb) {
		sb.append(function.getImage());
		appendParams(sb);
	}

	@Override
	protected void analyzeThis(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {
		functions.add(function.getImage().toLowerCase());
		if (function instanceof UserDefinedFunction) {
			((UserDefinedFunction) function).inspect(readFields, modifiedFields, variables, functions);
		}
	}
}
/* JavaCC - OriginalChecksum=ccaad8d7c28a4b42a02e6b2cb416c0b9 (do not edit this line) */
