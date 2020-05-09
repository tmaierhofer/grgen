/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast.containers;

import de.unika.ipd.grgen.ast.*;
import de.unika.ipd.grgen.ast.exprevals.*;
import de.unika.ipd.grgen.parser.Coords;

public abstract class ContainerProcedureMethodInvocationBaseNode extends ProcedureMethodInvocationBaseNode
{
	static {
		setName(ContainerProcedureMethodInvocationBaseNode.class, "container procedure method invocation base");
	}

	protected QualIdentNode target;
	protected VarDeclNode targetVar;

	protected ContainerProcedureMethodInvocationBaseNode(Coords coords, QualIdentNode target)
	{
		super(coords);
		this.target = becomeParent(target);
	}

	protected ContainerProcedureMethodInvocationBaseNode(Coords coords, VarDeclNode targetVar)
	{
		super(coords);
		this.targetVar = becomeParent(targetVar);
	}
	
	protected TypeNode getTargetType() {
		if(target != null) {
			return target.getDecl().getDeclType();
		} else {
			return targetVar.getDeclType();
		}
	}
}
