/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 3.6
 * Copyright (C) 2003-2013 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ir;

import java.util.HashSet;

import de.unika.ipd.grgen.ast.BaseNode;


/**
 * Represents a compound assignment changed statement in the IR.
 */
public class CompoundAssignmentChanged extends CompoundAssignment {

	/** The change assignment. */
	private Qualification changedTarget;

	/** The operation of the change assignment */
	private int changedOperation;


	public CompoundAssignmentChanged(Qualification target,
			int compoundAssignmentType, Expression expr,
			int changedAssignmentType, Qualification changedTarget) {
		super(target, compoundAssignmentType, expr);
		this.changedOperation = changedAssignmentType;
		this.changedTarget = changedTarget;
	}

	public Qualification getChangedTarget() {
		return changedTarget;
	}

	public int getChangedOperation() {
		return changedOperation;
	}

	public String toString() {
		return super.toString() + (changedOperation==UNION?" |> ":changedOperation==INTERSECTION?" &> ":" => ") + changedTarget.toString();
	}

	public void collectNeededEntities(NeededEntities needs)
	{
		super.collectNeededEntities(needs);

		Entity entity = changedTarget.getOwner();
		if(!isGlobalVariable(entity) && (entity.getContext()&BaseNode.CONTEXT_COMPUTATION)!=BaseNode.CONTEXT_COMPUTATION)
			needs.add((GraphEntity) entity);

		// Temporarily do not collect variables for changed target
		HashSet<Variable> varSet = needs.variables;
		needs.variables = null;
		changedTarget.collectNeededEntities(needs);
		needs.variables = varSet;
	}
}
