/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ir.stmt.array;

import de.unika.ipd.grgen.ir.*;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.expr.Qualification;
import de.unika.ipd.grgen.ir.stmt.ContainerQualProcedureMethodInvocationBase;

public class ArrayAddItem extends ContainerQualProcedureMethodInvocationBase
{
	Expression valueExpr;
	Expression indexExpr;

	public ArrayAddItem(Qualification target, Expression valueExpr, Expression indexExpr)
	{
		super("array add item", target);
		this.valueExpr = valueExpr;
		this.indexExpr = indexExpr;
	}

	public Expression getValueExpr()
	{
		return valueExpr;
	}

	public Expression getIndexExpr()
	{
		return indexExpr;
	}

	@Override
	public void collectNeededEntities(NeededEntities needs)
	{
		super.collectNeededEntities(needs);
		
		valueExpr.collectNeededEntities(needs);

		if(indexExpr != null)
			indexExpr.collectNeededEntities(needs);
	}
}
