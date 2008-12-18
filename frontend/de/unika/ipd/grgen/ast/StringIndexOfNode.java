/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 2.1
 * Copyright (C) 2008 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under GPL v3 (see LICENSE.txt included in the packaging of this file)
 */

/**
 * @author Moritz Kroll, Edgar Jakumeit
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ir.Expression;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.StringIndexOf;
import de.unika.ipd.grgen.parser.Coords;

public class StringIndexOfNode extends ExprNode {
	static {
		setName(StringIndexOfNode.class, "string indexOf");
	}
	
	ExprNode stringExpr, stringToSearchForExpr;
	

	public StringIndexOfNode(Coords coords, ExprNode stringExpr,
			ExprNode stringToSearchForExpr) {
		super(coords);
		
		this.stringExpr            = becomeParent(stringExpr);
		this.stringToSearchForExpr = becomeParent(stringToSearchForExpr);
	}

	public Collection<? extends BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(stringExpr);
		children.add(stringToSearchForExpr);
		return children;
	}

	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("string");
		childrenNames.add("stringToSearchFor");
		return childrenNames;
	}

	protected boolean checkLocal() {
		if(!stringExpr.getType().isEqual(BasicTypeNode.stringType)) {
			stringExpr.reportError("First argument to string indexOf expression must be of type string");
			return false;
		}
		if(!stringToSearchForExpr.getType().isEqual(BasicTypeNode.stringType)) {
			stringToSearchForExpr.reportError("Second argument (string to "
					+ "search for) to string indexOf expression must be of type string");
			return false;
		}
		return true;
	}
	
	protected IR constructIR() {
		return new StringIndexOf(stringExpr.checkIR(Expression.class),
				stringToSearchForExpr.checkIR(Expression.class));
	}
	
	public TypeNode getType() {
		return BasicTypeNode.intType;
	}
}
