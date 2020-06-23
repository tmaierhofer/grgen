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

import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.type.basic.BooleanType;

public class ArrayEmptyExpr extends ArrayFunctionMethodInvocationBaseExpr
{
	public ArrayEmptyExpr(Expression targetExpr)
	{
		super("array empty expression", BooleanType.getType(), targetExpr);
	}
}
