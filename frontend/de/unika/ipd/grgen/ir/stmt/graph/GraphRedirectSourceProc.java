/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

package de.unika.ipd.grgen.ir.stmt.graph;

import de.unika.ipd.grgen.ir.NeededEntities;
import de.unika.ipd.grgen.ir.executable.ProcedureBase;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.stmt.invocation.ProcedureOrBuiltinProcedureInvocationBase;

public class GraphRedirectSourceProc extends ProcedureOrBuiltinProcedureInvocationBase
{
	private Expression edge;
	private Expression newSource;
	private Expression oldSourceName; // optional

	public GraphRedirectSourceProc(Expression edge, Expression newSource, Expression oldSourceName)
	{
		super("graph redirect source procedure");
		this.edge = edge;
		this.newSource = newSource;
		this.oldSourceName = oldSourceName;
	}

	public Expression getEdge()
	{
		return edge;
	}

	public Expression getNewSource()
	{
		return newSource;
	}

	public Expression getOldSourceName()
	{
		return oldSourceName;
	}

	public ProcedureBase getProcedureBase()
	{
		return null; // dummy needed for interface, not accessed because the type of the class already defines the procedure
	}

	public void collectNeededEntities(NeededEntities needs)
	{
		needs.needsGraph();
		edge.collectNeededEntities(needs);
		newSource.collectNeededEntities(needs);
	}
}
