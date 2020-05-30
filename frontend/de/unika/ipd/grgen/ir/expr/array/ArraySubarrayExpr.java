/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */
package de.unika.ipd.grgen.ir.expr.array;

import de.unika.ipd.grgen.ir.NeededEntities;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.type.container.ArrayType;

public class ArraySubarrayExpr extends ArrayFunctionMethodInvocationBaseExpr
{
	private Expression startExpr;
	private Expression lengthExpr;

	public ArraySubarrayExpr(Expression targetExpr, Expression startExpr, Expression lengthExpr)
	{
		super("array subarray expr", (ArrayType)targetExpr.getType(), targetExpr);
		this.startExpr = startExpr;
		this.lengthExpr = lengthExpr;
	}

	public Expression getStartExpr()
	{
		return startExpr;
	}

	public Expression getLengthExpr()
	{
		return lengthExpr;
	}

	public void collectNeededEntities(NeededEntities needs)
	{
		needs.add(this);
		targetExpr.collectNeededEntities(needs);
		startExpr.collectNeededEntities(needs);
		lengthExpr.collectNeededEntities(needs);
	}
}
