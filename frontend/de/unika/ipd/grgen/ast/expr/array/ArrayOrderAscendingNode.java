/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast.expr.array;

import de.unika.ipd.grgen.ast.expr.ExprNode;
import de.unika.ipd.grgen.ast.type.TypeNode;
import de.unika.ipd.grgen.ast.type.container.ArrayTypeNode;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.expr.array.ArrayOrderAscending;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.parser.Coords;

public class ArrayOrderAscendingNode extends ArrayFunctionMethodInvocationBaseExprNode
{
	static {
		setName(ArrayOrderAscendingNode.class, "array order ascending");
	}

	public ArrayOrderAscendingNode(Coords coords, ExprNode targetExpr)
	{
		super(coords, targetExpr);
	}

	@Override
	protected boolean checkLocal()
	{
		// target type already checked during resolving into this node
		ArrayTypeNode arrayType = getTargetType();
		if(!(arrayType.valueType.isOrderableType())) {
			targetExpr.reportError("array orderAscending only available for arrays of type "
					+ TypeNode.getOrderableTypesAsString());
		}
		return true;
	}

	@Override
	public TypeNode getType()
	{
		return getTargetType();
	}

	@Override
	protected IR constructIR()
	{
		targetExpr = targetExpr.evaluate();
		return new ArrayOrderAscending(targetExpr.checkIR(Expression.class));
	}
}
