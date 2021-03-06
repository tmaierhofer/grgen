/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

package de.unika.ipd.grgen.ast.expr;

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ast.*;
import de.unika.ipd.grgen.ast.type.MatchTypeNode;
import de.unika.ipd.grgen.ast.type.TypeNode;
import de.unika.ipd.grgen.ast.type.basic.BasicTypeNode;
import de.unika.ipd.grgen.ast.type.basic.GraphTypeNode;
import de.unika.ipd.grgen.ast.type.container.ContainerTypeNode;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.expr.CopyExpr;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.parser.Coords;

/**
 * A node yielding the copy of a subgraph, or a match, or a container.
 */
public class CopyExprNode extends BuiltinFunctionInvocationBaseNode
{
	static {
		setName(CopyExprNode.class, "copy expr");
	}

	private ExprNode sourceExpr;

	public CopyExprNode(Coords coords, ExprNode sourceExpr)
	{
		super(coords);
		this.sourceExpr = sourceExpr;
		becomeParent(this.sourceExpr);
	}

	/** returns children of this node */
	@Override
	public Collection<BaseNode> getChildren()
	{
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(sourceExpr);
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	@Override
	public Collection<String> getChildrenNames()
	{
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("source expression");
		return childrenNames;
	}

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolveLocal() */
	@Override
	protected boolean resolveLocal()
	{
		return true;
	}

	/** @see de.unika.ipd.grgen.ast.BaseNode#checkLocal() */
	@Override
	protected boolean checkLocal()
	{
		if(!(sourceExpr.getType() instanceof GraphTypeNode)
				&& !(sourceExpr.getType() instanceof MatchTypeNode)
				&& !(sourceExpr.getType() instanceof ContainerTypeNode)) {
			sourceExpr.reportError("graph or match or container expected as argument to copy");
			return false;
		}
		return true;
	}

	@Override
	protected IR constructIR()
	{
		sourceExpr = sourceExpr.evaluate();
		return new CopyExpr(sourceExpr.checkIR(Expression.class), getType().getType());
	}

	@Override
	public TypeNode getType()
	{
		if(sourceExpr.getType() instanceof MatchTypeNode
				|| sourceExpr.getType() instanceof ContainerTypeNode)
			return sourceExpr.getType();
		else
			return BasicTypeNode.graphType;
	}
}
