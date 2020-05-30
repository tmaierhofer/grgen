/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Moritz Kroll, Edgar Jakumeit
 */

package de.unika.ipd.grgen.ir.expr;

import de.unika.ipd.grgen.ir.NeededEntities;
import de.unika.ipd.grgen.ir.type.Type;

public class IndexedAccessExpr extends ContainerFunctionMethodInvocationBaseExpr
{
	Expression keyExpr;

	public IndexedAccessExpr(Expression targetExpr, Expression keyExpr, Type type)
	{
		super("indexed access expression", type, targetExpr);
		this.keyExpr = keyExpr;
	}

	public void collectNeededEntities(NeededEntities needs)
	{
		needs.add(this);
		keyExpr.collectNeededEntities(needs);
		targetExpr.collectNeededEntities(needs);
	}

	public Expression getKeyExpr()
	{
		return keyExpr;
	}
}
