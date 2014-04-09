/* Generated By:JJTree: Do not edit this line. ASTAtConCat.java Version 4.3 */
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
 * 
 */
package org.openntf.domino.formula.ast;

import java.util.Set;

import org.openntf.domino.formula.EvaluateException;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.FormulaReturnException;
import org.openntf.domino.formula.ValueHolder;
import org.openntf.domino.formula.ValueHolder.DataType;
import org.openntf.domino.formula.impl.OpenNTF;
import org.openntf.domino.formula.parse.AtFormulaParserImpl;

public class ASTAtConCat extends SimpleNode {

	public ASTAtConCat(final AtFormulaParserImpl p, final int id) {
		super(p, id);
	}

	/**
	 * Returns a äquivalent formula
	 */
	@OpenNTF
	public void toFormula(final StringBuilder sb) {
		if (children == null)
			return;
		sb.append("(");
		children[0].toFormula(sb);
		for (int i = 1; i < children.length; i++) {
			sb.append(" + ");
			children[i].toFormula(sb);
		}
		sb.append(")");

	}

	/**
	 * 
	 */
	@Override
	public ValueHolder evaluate(final FormulaContext ctx) throws FormulaReturnException {
		if (children == null)
			return ValueHolder.valueDefault();

		if (children.length == 1)
			return children[0].evaluate(ctx);

		// check for errors
		ValueHolder vh;
		ValueHolder[] res = new ValueHolder[children.length];
		int size = 0;
		for (int i = 0; i < children.length; i++) {
			vh = children[i].evaluate(ctx);
			if (vh != null && vh.dataType == DataType.ERROR)
				return vh;
			res[i] = vh;
			size = Math.max(size, vh.size);
		}

		int entry = 0;
		vh = res[0].newInstance(size);
		try {
			switch (res[0].dataType) {
			case DOUBLE:
			case INTEGER:
				for (int i = 0; i < size; i++) {
					double sum = 0;
					for (entry = 0; entry < res.length; entry++) {
						sum += res[entry].getDouble(i);
					}
					vh.add(sum);
				}
				break;

			case STRING:
				for (int i = 0; i < size; i++) {

					StringBuffer sb = new StringBuffer();
					for (entry = 0; entry < res.length; entry++) {
						sb.append(res[entry].getString(i));
					}
					vh.add(sb.toString());
				}
				break;
			default:
				throw new UnsupportedOperationException("Concat on " + res[0].dataType + " is not supported");

			}
		} catch (RuntimeException cause) {
			if (entry < res.length) {
				// find the entry where the error occured
				SimpleNode child = (SimpleNode) children[entry];
				return ValueHolder.valueOf(new EvaluateException(child.codeLine, child.codeColumn, cause));
			}
			return ValueHolder.valueOf(new EvaluateException(codeLine, codeColumn, cause));
		}
		return vh;
	}

	/**
	 * This function uses no fields/functions
	 */
	@Override
	protected void analyzeThis(final Set<String> readFields, final Set<String> modifiedFields, final Set<String> variables,
			final Set<String> functions) {

	}

}
/* JavaCC - OriginalChecksum=2b095cf813979405364ecca27c4a6b83 (do not edit this line) */
