/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

package de.unika.ipd.grgen.ir;

import de.unika.ipd.grgen.ir.expr.Expression;

/**
 * Class for initializing a single attribute of a type, or the name
 */
public class NameOrAttributeInitialization extends IR
{
	public GraphEntity owner;
	public Entity attribute;
	public Expression expr;

	public NameOrAttributeInitialization()
	{
		super("name or attribute init");
	}

	public void collectNeededEntities(NeededEntities needs)
	{
		expr.collectNeededEntities(needs);
	}
}
