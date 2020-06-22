/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author shack
 */

package de.unika.ipd.grgen.ast.decl.executable;

import de.unika.ipd.grgen.ast.BaseNode;
import de.unika.ipd.grgen.ast.CallActionNode;
import de.unika.ipd.grgen.ast.ExecNode;
import de.unika.ipd.grgen.ast.IdentNode;
import de.unika.ipd.grgen.ast.decl.DeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.AlternativeCaseDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.AlternativeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.ConstraintDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.EdgeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.EdgeTypeChangeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.IteratedDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.NodeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.NodeTypeChangeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.RhsDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.VarDeclNode;
import de.unika.ipd.grgen.ast.expr.ExprNode;
import de.unika.ipd.grgen.ast.expr.array.ArrayAccumulationMethodNode;
import de.unika.ipd.grgen.ast.pattern.ConnectionCharacter;
import de.unika.ipd.grgen.ast.pattern.ConnectionNode;
import de.unika.ipd.grgen.ast.pattern.EdgeCharacter;
import de.unika.ipd.grgen.ast.pattern.ImplicitNegComputer;
import de.unika.ipd.grgen.ast.pattern.ImplicitNegComputerInduced;
import de.unika.ipd.grgen.ast.pattern.NodeCharacter;
import de.unika.ipd.grgen.ast.pattern.PatternGraphLhsNode;
import de.unika.ipd.grgen.ast.type.TypeNode;
import de.unika.ipd.grgen.ir.Entity;
import de.unika.ipd.grgen.ir.executable.MatchingAction;
import de.unika.ipd.grgen.ir.executable.Rule;
import de.unika.ipd.grgen.ir.pattern.Alternative;
import de.unika.ipd.grgen.ir.pattern.Node;
import de.unika.ipd.grgen.ir.pattern.PatternGraphLhs;
import de.unika.ipd.grgen.ir.pattern.PatternGraphRhs;
import de.unika.ipd.grgen.ir.pattern.Variable;
import de.unika.ipd.grgen.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

/**
 * Base class for pattern matching related ast nodes
 */
public abstract class MatcherDeclNode extends DeclNode
{
	public PatternGraphLhsNode pattern;

	public MatcherDeclNode(IdentNode id, TypeNode type, PatternGraphLhsNode left)
	{
		super(id, type);

		this.pattern = left;
		becomeParent(this.pattern);
	}

	/**
	 * Get the IR object for this matcher decl node.
	 * The IR object is instance of Rule.
	 * @return The IR object.
	 */
	public Rule getMatcher()
	{
		return checkIR(Rule.class);
	}

	protected static boolean resolveFilters(ArrayList<FilterAutoDeclNode> filters)
	{
		boolean filtersOk = true;
		for(FilterAutoDeclNode filter : filters) {
			if(filter instanceof FilterAutoSuppliedDeclNode) {
				filtersOk &= ((FilterAutoSuppliedDeclNode)filter).resolve();
			} else { //if(filter instanceof FilterAutoGeneratedNode)
				filtersOk &= ((FilterAutoGeneratedDeclNode)filter).resolve();
			}
		}
		return filtersOk;
	}

	protected boolean checkFilters(PatternGraphLhsNode pattern, ArrayList<FilterAutoDeclNode> filters)
	{
		boolean filtersOk = true;
		for(FilterAutoDeclNode filter : filters) {
			if(filter instanceof FilterAutoSuppliedDeclNode) {
				filtersOk &= ((FilterAutoSuppliedDeclNode)filter).check();
			} else { //if(filter instanceof FilterAutoGeneratedNode)
				filtersOk &= ((FilterAutoGeneratedDeclNode)filter).check();
			}
		}
		boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
		HashSet<String> alreadySeenFilters = new HashSet<String>();
		for(FilterAutoDeclNode fa : filters) {
			if(fa instanceof FilterAutoGeneratedDeclNode) {
				FilterAutoGeneratedDeclNode filter = (FilterAutoGeneratedDeclNode)fa;
				String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
				if(alreadySeenFilters.contains(filterNameWithEntitySuffix)) {
					reportError(filterNameWithEntitySuffix + " was already declared, only one declaration admissible.");
					allFilterEntitiesExistAndAreOfAdmissibleType = false;
				} else {
					alreadySeenFilters.add(filterNameWithEntitySuffix);
				}
				allFilterEntitiesExistAndAreOfAdmissibleType &= checkAutoGeneratedFilter(filter);
			}
		}
		return filtersOk & allFilterEntitiesExistAndAreOfAdmissibleType;
	}

	protected boolean checkAutoGeneratedFilter(FilterAutoGeneratedDeclNode filter)
	{
		String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
		switch(filter.name) {
		case "orderAscendingBy":
		case "orderDescendingBy":
		{
			boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
			for(String filterEntity : filter.entities) {
				allFilterEntitiesExistAndAreOfAdmissibleType &= pattern.checkFilterVariable(getIdentNode(),
						filterNameWithEntitySuffix, filterEntity);
			}
			return allFilterEntitiesExistAndAreOfAdmissibleType;
		}
		case "keepOneForEach":
		case "groupBy":
		case "keepSameAsFirst":
		case "keepSameAsLast":
		{
			boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
			for(String filterEntity : filter.entities) {
				allFilterEntitiesExistAndAreOfAdmissibleType &= pattern.checkFilterEntity(getIdentNode(),
						filterNameWithEntitySuffix, filterEntity);
			}
			if(filter.entities.size() != 1) {
				reportError(filterNameWithEntitySuffix
						+ " must be declared with exactly one variable, but is declared with " + filter.entities.size()
						+ " variables");
				allFilterEntitiesExistAndAreOfAdmissibleType = false;
			}
			return allFilterEntitiesExistAndAreOfAdmissibleType;
		}
		case "keepOneForEachAccumulateBy":
			if(filter.entities.size() != 3) {
				getIdentNode().reportError(filterNameWithEntitySuffix
						+ " must be declared with exactly one variable, one accumulation variable,"
						+ " and one accumulation method");
				return false;
			} else {
				if(filter.entities.get(0).equals(filter.entities.get(1))) {
					getIdentNode().reportError("The accumulation variable " + filter.entities.get(1)
							+ " must be different from the variable.");
					return false;
				}
				boolean filterEntityExistsAndIsOfAdmissibleType = pattern.checkFilterEntity(getIdentNode(),
						filterNameWithEntitySuffix, filter.entities.get(0));
				if(!filterEntityExistsAndIsOfAdmissibleType)
					return false;
				ArrayAccumulationMethodNode accumulationMethod = 
						ArrayAccumulationMethodNode.getArrayMethodNode(filter.entities.get(2));
				if(accumulationMethod == null) {
					getIdentNode().reportError("The array accumulation method "
							+ filter.entities.get(2) + " is not known.");
					return false;
				}
				VarDeclNode filterAccumulationVariable = pattern.tryGetVar(filter.entities.get(1));
				if(filterAccumulationVariable == null) {
					getIdentNode().reportError(filterNameWithEntitySuffix
							+ ": unknown accumulation variable " + filter.entities.get(1));
					return false;
				}
				TypeNode filterAccumulationVariableType = filterAccumulationVariable.getDeclType();
				if(!accumulationMethod.isValidTargetTypeOfAccumulation(filterAccumulationVariableType)) {
					getIdentNode().reportError("The array accumulation method " + filter.entities.get(2)
							+ " is not applicable to the type " + filterAccumulationVariableType
							+ " of the accumulation variable " + filter.entities.get(1)
							+ " / its result cannot be assigned to the accumulation variable."
							+ " (Allowed are: " + accumulationMethod.getValidTargetTypesOfAccumulation() + ")");
					return false;
				}
				return true;
			}
		case "auto":
			return true; // skip
		default:
			assert(false);
			return false;
		}
	}

	protected boolean checkNonAction(RhsDeclNode right)
	{
		boolean leftHandGraphsOk = checkLeft();

		boolean rightHandGraphsOk = true;
		if(right != null)
			rightHandGraphsOk = right.checkAgainstLhsPattern(pattern);

		boolean noReturnInPattern = true;
		if(pattern.returns.size() > 0) {
			error.error(getCoords(), "No return statement in (pattern parts of) " + getConstructName() + " allowed");
			noReturnInPattern = false;
		}

		boolean noReturnInNestedReplacement = true;
		if(right != null) {
			if(right.patternGraph.returns.size() > 0) {
				error.error(getCoords(), "No return statement in " + getConstructName() + " allowed");
				noReturnInNestedReplacement = false;
			}
		}

		boolean rhsReuseOk = true;
		boolean execParamsNotDeleted = true;
		boolean sameNumberOfRewriteParts = sameNumberOfRewriteParts(right, getConstructName());
		boolean noNestedRewriteParameters = true;
		if(right != null) {
			rhsReuseOk = checkRhsReuse(right);
			execParamsNotDeleted = checkExecParamsNotDeleted(right);
			noNestedRewriteParameters = noNestedRewriteParameters(right, getConstructName());
		}

		return leftHandGraphsOk
				& rightHandGraphsOk
				& sameNumberOfRewriteParts
				& noNestedRewriteParameters
				& rhsReuseOk
				& noReturnInPattern
				& noReturnInNestedReplacement
				& execParamsNotDeleted;
	}

	protected abstract String getConstructName();

	protected boolean checkLeft()
	{
		// check if reused names of edges connect the same nodes in the same direction with the same edge kind for each usage
		boolean isLhsEdgeReuseOk = true;

		// get the negative and independent graphs and the pattern of this ActionDeclNode
		// NOTE: the order affect the error coords
		Collection<PatternGraphLhsNode> leftHandGraphs = new LinkedList<PatternGraphLhsNode>();
		leftHandGraphs.add(pattern);
		for(PatternGraphLhsNode neg : pattern.negs.getChildren()) {
			leftHandGraphs.add(neg);
		}
		for(PatternGraphLhsNode idpt : pattern.idpts.getChildren()) {
			leftHandGraphs.add(idpt);
		}

		PatternGraphLhsNode[] graphs = leftHandGraphs.toArray(new PatternGraphLhsNode[0]);
		Collection<EdgeCharacter> alreadyReported = new HashSet<EdgeCharacter>();

		for(int i = 0; i < graphs.length; i++) {
			for(ConnectionCharacter iConnectionCharacter : graphs[i].getConnections()) {
				if(!(iConnectionCharacter instanceof ConnectionNode)) {
					continue;
				}
				ConnectionNode iConnection = (ConnectionNode)iConnectionCharacter;

				for(int j = i + 1; j < graphs.length; j++) {
					for(ConnectionCharacter jConnectionCharacter : graphs[j].getConnections()) {
						if(!(jConnectionCharacter instanceof ConnectionNode)) {
							continue;
						}
						ConnectionNode jConnection = (ConnectionNode)jConnectionCharacter;

						if(iConnection.getEdge().equals(jConnection.getEdge()) && !alreadyReported.contains(iConnection.getEdge())) {
							isLhsEdgeReuseOk &= isLhsEdgeReuseOk(alreadyReported, iConnection, jConnection);
						}
					}
				}
			}
		}

		return isLhsEdgeReuseOk;
	}

	private static boolean isLhsEdgeReuseOk(Collection<EdgeCharacter> alreadyReported,
			ConnectionNode iConnection, ConnectionNode jConnection)
	{
		boolean edgeReuse = true;

		NodeCharacter iSrc = iConnection.getSrc();
		NodeCharacter iTgt = iConnection.getTgt();
		NodeCharacter jSrc = jConnection.getSrc();
		NodeCharacter jTgt = jConnection.getTgt();

		assert !(iSrc instanceof NodeTypeChangeDeclNode) : "no type changes in test actions";
		assert !(iTgt instanceof NodeTypeChangeDeclNode) : "no type changes in test actions";
		assert !(jSrc instanceof NodeTypeChangeDeclNode) : "no type changes in test actions";
		assert !(jTgt instanceof NodeTypeChangeDeclNode) : "no type changes in test actions";

		//check only if there's no dangling edge
		if(!( (iSrc instanceof NodeDeclNode) && ((NodeDeclNode)iSrc).isDummy() )
			&& !( (jSrc instanceof NodeDeclNode) && ((NodeDeclNode)jSrc).isDummy() )
			&& iSrc != jSrc) {
			alreadyReported.add(iConnection.getEdge());
			iConnection.reportError("Reused edge does not connect the same nodes");
			edgeReuse = false;
		}

		//check only if there's no dangling edge
		if(!( (iTgt instanceof NodeDeclNode) && ((NodeDeclNode)iTgt).isDummy() )
			&& !( (jTgt instanceof NodeDeclNode) && ((NodeDeclNode)jTgt).isDummy() )
			&& iTgt != jTgt
			&& !alreadyReported.contains(iConnection.getEdge())) {
			alreadyReported.add(iConnection.getEdge());
			iConnection.reportError("Reused edge does not connect the same nodes");
			edgeReuse = false;
		}

		if(iConnection.getConnectionKind() != jConnection.getConnectionKind()) {
			alreadyReported.add(iConnection.getEdge());
			iConnection.reportError("Reused edge does not have the same connection kind");
			edgeReuse = false;
		}

		return edgeReuse;
	}

	/** Checks, whether the reused nodes and edges of the RHS are consistent with the LHS.
	 * If consistent, replace the dummy nodes with the nodes the pattern edge is
	 * incident to (if these aren't dummy nodes themselves, of course). */
	protected boolean checkRhsReuse(RhsDeclNode right)
	{
		boolean res = true;

		HashMap<EdgeDeclNode, NodeDeclNode> redirectedFrom = new HashMap<EdgeDeclNode, NodeDeclNode>();
		HashMap<EdgeDeclNode, NodeDeclNode> redirectedTo = new HashMap<EdgeDeclNode, NodeDeclNode>();

		Collection<EdgeDeclNode> alreadyReported = new HashSet<EdgeDeclNode>();
		for(ConnectionNode rightConnection : right.getConnectionsToReuse(pattern)) {
			EdgeDeclNode rightEdge = rightConnection.getEdge();

			if(rightEdge instanceof EdgeTypeChangeDeclNode) {
				rightEdge = ((EdgeTypeChangeDeclNode)rightEdge).getOldEdge();
			}

			for(ConnectionCharacter leftConnectionCharacter : pattern.getConnections()) {
				if(!(leftConnectionCharacter instanceof ConnectionNode)) {
					continue;
				}

				ConnectionNode leftConnection = (ConnectionNode)leftConnectionCharacter;

				EdgeDeclNode leftEdge = leftConnection.getEdge();

				if(!leftEdge.equals(rightEdge)) {
					continue;
				}

				if(leftConnection.getConnectionKind() != rightConnection.getConnectionKind()) {
					res = false;
					rightConnection.reportError("Reused edge does not have the same connection kind");
					// if you don't add to alreadyReported erroneous errors can occur,
					// e.g. lhs=x-e->y, rhs=y-e-x
					alreadyReported.add(rightEdge);
				}

				res &= isLhsRhsReuseOk(alreadyReported, redirectedFrom, redirectedTo,
						right, leftConnection, rightConnection);
			}
		}

		return res;
	}

	private boolean isLhsRhsReuseOk(Collection<EdgeDeclNode> alreadyReported,
			HashMap<EdgeDeclNode, NodeDeclNode> redirectedFrom, HashMap<EdgeDeclNode, NodeDeclNode> redirectedTo,
			RhsDeclNode right, ConnectionNode leftConnection, ConnectionNode rightConnection)
	{
		NodeDeclNode rSrc = rightConnection.getSrc();
		NodeDeclNode rTgt = rightConnection.getTgt();

		HashSet<BaseNode> rhsNodes = new HashSet<BaseNode>();
		rhsNodes.addAll(right.getNodesToReuse(pattern));

		if(rSrc instanceof NodeTypeChangeDeclNode) {
			rSrc = ((NodeTypeChangeDeclNode)rSrc).getOldNode();
			rhsNodes.add(rSrc);
		}
		if(rTgt instanceof NodeTypeChangeDeclNode) {
			rTgt = ((NodeTypeChangeDeclNode)rTgt).getOldNode();
			rhsNodes.add(rTgt);
		}

		boolean res = true;

		res &= isLhsRhsSourceReuseOk(alreadyReported, redirectedFrom, leftConnection, rightConnection, rSrc, rhsNodes);

		res &= isLhsRhsTargetReuseOk(alreadyReported, redirectedTo, leftConnection, rightConnection, rTgt, rhsNodes);

		return res;
	}

	private static boolean isLhsRhsSourceReuseOk(Collection<EdgeDeclNode> alreadyReported,
			HashMap<EdgeDeclNode, NodeDeclNode> redirectedFrom,
			ConnectionNode leftConnection, ConnectionNode rightConnection, 
			NodeDeclNode rSrc, HashSet<BaseNode> rhsNodes)
	{
		boolean res = true;

		EdgeDeclNode le = leftConnection.getEdge();
		EdgeDeclNode re = rightConnection.getEdge();

		NodeDeclNode lSrc = leftConnection.getSrc();

		if(!lSrc.isDummy()) {
			if(rSrc.isDummy()) {
				if(rhsNodes.contains(lSrc)) {
					//replace the dummy src node by the src node of the pattern connection
					rightConnection.setSrc(lSrc);
				} else if(!alreadyReported.contains(re)) {
					res = false;
					rightConnection.reportError("The source node of reused edge \"" + le + "\" must be reused, too");
					alreadyReported.add(re);
				}
			} else if(lSrc != rSrc
					&& (rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE) != ConnectionNode.REDIRECT_SOURCE
					&& !alreadyReported.contains(re)) {
				res = false;
				rightConnection.reportError("Reused edge \"" + le
						+ "\" does not connect the same nodes (and is not declared to redirect source)");
				alreadyReported.add(re);
			}
		}

		if((rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE) == ConnectionNode.REDIRECT_SOURCE) {
			if(rSrc.isDummy()) {
				res = false;
				rightConnection.reportError("An edge with source redirection must be given a source node.");
			}

			if(lSrc.equals(rSrc)) {
				rightConnection.reportWarning("Redirecting edge to same source again.");
			}

			if(redirectedFrom.containsKey(le)) {
				res = false;
				rightConnection.reportError("Can't redirect edge source more than once.");
			}
			redirectedFrom.put(le, rSrc);
		}

		//check, whether RHS "adds" a node to a dangling end of a edge
		if(!alreadyReported.contains(re)) {
			if(lSrc.isDummy() && !rSrc.isDummy()
					&& (rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE) != ConnectionNode.REDIRECT_SOURCE) {
				res = false;
				rightConnection.reportError("Reused edge dangles on LHS, but has a source node on RHS");
				alreadyReported.add(re);
			}
		}

		return res;
	}

	private static boolean isLhsRhsTargetReuseOk(Collection<EdgeDeclNode> alreadyReported,
			HashMap<EdgeDeclNode, NodeDeclNode> redirectedTo,
			ConnectionNode leftConnection, ConnectionNode rightConnection, 
			NodeDeclNode rTgt, HashSet<BaseNode> rhsNodes)
	{
		boolean res = true;
	
		EdgeDeclNode le = leftConnection.getEdge();
		EdgeDeclNode re = rightConnection.getEdge();

		NodeDeclNode lTgt = leftConnection.getTgt();

		if(!lTgt.isDummy()) {
			if(rTgt.isDummy()) {
				if(rhsNodes.contains(lTgt)) {
					//replace the dummy tgt node by the tgt node of the pattern connection
					rightConnection.setTgt(lTgt);
				} else if(!alreadyReported.contains(re)) {
					res = false;
					rightConnection.reportError("The target node of reused edge \"" + le + "\" must be reused, too");
					alreadyReported.add(re);
				}
			} else if(lTgt != rTgt
					&& (rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET) != ConnectionNode.REDIRECT_TARGET
					&& !alreadyReported.contains(re)) {
				res = false;
				rightConnection.reportError("Reused edge \"" + le
						+ "\" does not connect the same nodes (and is not declared to redirect target)");
				alreadyReported.add(re);
			}
		}

		if((rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET) == ConnectionNode.REDIRECT_TARGET) {
			if(rTgt.isDummy()) {
				res = false;
				rightConnection.reportError("An edge with target redirection must be given a target node.");
			}

			if(lTgt.equals(rTgt)) {
				rightConnection.reportWarning("Redirecting edge to same target again.");
			}

			if(redirectedTo.containsKey(le)) {
				res = false;
				rightConnection.reportError("Can't redirect edge target more than once.");
			}
			redirectedTo.put(le, rTgt);
		}

		//check, whether RHS "adds" a node to a dangling end of a edge
		if(!alreadyReported.contains(re)) {
			if(lTgt.isDummy() && !rTgt.isDummy()
					&& (rightConnection.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET) != ConnectionNode.REDIRECT_TARGET) {
				res = false;
				rightConnection.reportError("Reused edge dangles on LHS, but has a target node on RHS");
				alreadyReported.add(re);
			}
		}

		return res;
	}

	/**
	 * Check that exec parameters are not deleted.
	 *
	 * The check consider the case that parameters are deleted due to
	 * homomorphic matching.
	 */
	protected boolean checkExecParamsNotDeleted(RhsDeclNode right)
	{
		assert isResolved();

		boolean valid = true;

		Set<ConstraintDeclNode> deletedElements = right.getElementsToDelete(pattern);
		Set<ConstraintDeclNode> maybeDeletedElements = right.getMaybeDeletedElements(pattern);

		for(BaseNode imperativeStatement : right.patternGraph.imperativeStmts.getChildren()) {
			if(!(imperativeStatement instanceof ExecNode))
				continue;

			ExecNode exec = (ExecNode)imperativeStatement;
			for(CallActionNode callAction : exec.callActions.getChildren()) {
				for(ExprNode arg : callAction.params.getChildren()) {
					HashSet<ConstraintDeclNode> potentiallyResultingElements = new HashSet<ConstraintDeclNode>();
					arg.getPotentiallyResultingElements(potentiallyResultingElements);
					for(ConstraintDeclNode potentiallyResultingElement : potentiallyResultingElements) {
						valid &= checkExecParamNotDeleted(potentiallyResultingElement, deletedElements, maybeDeletedElements);
					}
				}
			}
		}

		return valid;
	}

	private static boolean checkExecParamNotDeleted(ConstraintDeclNode declNode,
			Set<ConstraintDeclNode> deletedElements, Set<ConstraintDeclNode> maybeDeletedElements)
	{
		if(deletedElements.contains(declNode)) {
			declNode.reportError("The deleted " + declNode.getKind() + " \"" + declNode.ident
					+ "\" must not be passed to an exec statement");
			return false;
		} else if(maybeDeletedElements.contains(declNode)) {
			declNode.maybeDeleted = true;

			if(!declNode.getIdentNode().getAnnotations().isFlagSet("maybeDeleted")) {
				String errorMessage = "Parameter \"" + declNode.ident + "\" of exec statement may be deleted"
						+ ", possibly it's homomorphic with a deleted " + declNode.getKind();
				errorMessage += " (use a [maybeDeleted] annotation if you think that this does not cause problems)";

				if(declNode instanceof EdgeDeclNode) {
					errorMessage += " or \"" + declNode.ident + "\" is a dangling " + declNode.getKind()
							+ " and a deleted node exists";
				}
				declNode.reportError(errorMessage);
				
				return false;
			}
		}

		return true;
	}

	protected boolean sameNumberOfRewriteParts(RhsDeclNode right, String actionKind)
	{
		boolean res = true;

		for(AlternativeDeclNode alt : pattern.alts.getChildren()) {
			for(AlternativeCaseDeclNode altCase : alt.getChildren()) {
				if((right == null) != (altCase.right == null)) {
					error.error(getCoords(), "Different number of replacement patterns/rewrite parts in " + actionKind
							+ " " + ident.toString() + " and nested alternative case " + altCase.ident.toString());
					res = false;
				}
			}
		}

		for(IteratedDeclNode iter : pattern.iters.getChildren()) {
			if((right == null) != (iter.right == null)) {
				error.error(getCoords(), "Different number of replacement patterns/rewrite parts in " + actionKind + " "
						+ ident.toString() + " and nested iterated/multiple/optional " + iter.ident.toString());
				res = false;
			}
		}

		return res;
	}

	protected boolean noNestedRewriteParameters(RhsDeclNode right, String actionKind)
	{
		boolean res = true;

		for(AlternativeDeclNode alt : pattern.alts.getChildren()) {
			for(AlternativeCaseDeclNode altCase : alt.getChildren()) {
				if(altCase.right == null)
					continue;

				Vector<DeclNode> parametersInNestedAlternativeCase = altCase.right.patternGraph.getParamDecls();

				if(parametersInNestedAlternativeCase.size() != 0) {
					error.error(altCase.getCoords(),
							"No replacement parameters allowed in nested alternative cases; given in "
									+ altCase.ident.toString());
					res = false;
				}
			}
		}

		for(IteratedDeclNode iter : pattern.iters.getChildren()) {
			if(iter.right == null)
				continue;

			Vector<DeclNode> parametersInNestedIterated = iter.right.patternGraph.getParamDecls();

			if(parametersInNestedIterated.size() != 0) {
				error.error(iter.getCoords(),
						"No replacement parameters allowed in nested iterated/multiple/optional; given in "
								+ iter.ident.toString());
				res = false;
			}
		}

		return res;
	}
	
	protected boolean noAmbiguousRetypes(RhsDeclNode right)
	{
		if(right == null)
			return false;
		boolean result = true;
		for(NodeDeclNode node : pattern.getNodes()) {
			if(node.directlyNestingLHSGraph == pattern)
				result &= noAmbiguousRetypes(right, node);
		}
		for(EdgeDeclNode edge : pattern.getEdges()) {
			if(edge.directlyNestingLHSGraph == pattern)
				result &= noAmbiguousRetypes(right, edge);
		}
		for(AlternativeDeclNode alternative : pattern.alts.getChildren()) {
			for(AlternativeCaseDeclNode alternativeCase : alternative.getChildren()) {
				result &= alternativeCase.noAmbiguousRetypes(alternativeCase.right);
			}
		}
		for(IteratedDeclNode iterated : pattern.iters.getChildren()) {
			result &= iterated.noAmbiguousRetypes(iterated.right);
		}
		return result;
	}
	
	protected boolean noAmbiguousRetypes(RhsDeclNode right, NodeDeclNode node)
	{
		boolean noAmbiguousRetypes = true;
		NodeTypeChangeDeclNode retypeOfNode = null;
		Pair<Boolean, NodeTypeChangeDeclNode> result = right.getRhsGraph().noAmbiguousRetypes(node, retypeOfNode);
		noAmbiguousRetypes &= result.first.booleanValue();
		retypeOfNode = result.second;
		for(AlternativeDeclNode alternative : pattern.alts.getChildren()) {
			NodeTypeChangeDeclNode tempRetype = null;
			for(AlternativeCaseDeclNode alternativeCase : alternative.getChildren()) {
				result = alternativeCase.noAmbiguousRetypes(alternativeCase.right, node, retypeOfNode);
				noAmbiguousRetypes &= result.first.booleanValue();
				if(tempRetype == null)
					tempRetype = result.second;
			}
			if(retypeOfNode == null)
				retypeOfNode = tempRetype;
		}
		for(IteratedDeclNode iterated : pattern.iters.getChildren()) {
			result = iterated.noAmbiguousRetypes(iterated.right, node, retypeOfNode);
			noAmbiguousRetypes &= result.first.booleanValue();
			if(retypeOfNode == null)
				retypeOfNode = result.second;
		}
		return noAmbiguousRetypes;
	}

	protected boolean noAmbiguousRetypes(RhsDeclNode right, EdgeDeclNode edge)
	{
		boolean noAmbiguousRetypes = true;
		EdgeTypeChangeDeclNode retypeOfEdge = null;
		Pair<Boolean, EdgeTypeChangeDeclNode> result = right.getRhsGraph().noAmbiguousRetypes(edge, retypeOfEdge);
		noAmbiguousRetypes &= result.first.booleanValue();
		retypeOfEdge = result.second;
		for(AlternativeDeclNode alternative : pattern.alts.getChildren()) {
			EdgeTypeChangeDeclNode tempRetype = null;
			for(AlternativeCaseDeclNode alternativeCase : alternative.getChildren()) {
				result = alternativeCase.noAmbiguousRetypes(alternativeCase.right, edge, retypeOfEdge);
				noAmbiguousRetypes &= result.first.booleanValue();
				if(tempRetype == null)
					tempRetype = result.second;
			}
			if(retypeOfEdge == null)
				retypeOfEdge = tempRetype;
		}
		for(IteratedDeclNode iterated : pattern.iters.getChildren()) {
			result = iterated.noAmbiguousRetypes(iterated.right, edge, retypeOfEdge);
			noAmbiguousRetypes &= result.first.booleanValue();
			if(retypeOfEdge == null)
				retypeOfEdge = result.second;
		}
		return noAmbiguousRetypes;
	}

	protected Pair<Boolean, NodeTypeChangeDeclNode> noAmbiguousRetypes(RhsDeclNode right, NodeDeclNode node, NodeTypeChangeDeclNode retypeOfNode)
	{
		if(right == null)
			return new Pair<Boolean, NodeTypeChangeDeclNode>(Boolean.valueOf(false), retypeOfNode);
		boolean noAmbiguousRetypes = true;
		Pair<Boolean, NodeTypeChangeDeclNode> result = right.getRhsGraph().noAmbiguousRetypes(node, retypeOfNode);
		noAmbiguousRetypes &= result.first.booleanValue();
		retypeOfNode = result.second;
		for(AlternativeDeclNode alternative : pattern.alts.getChildren()) {
			NodeTypeChangeDeclNode tempRetype = null;
			for(AlternativeCaseDeclNode alternativeCase : alternative.getChildren()) {
				result = alternativeCase.noAmbiguousRetypes(alternativeCase.right, node, retypeOfNode);
				noAmbiguousRetypes &= result.first.booleanValue();
				if(tempRetype == null)
					tempRetype = result.second;
			}
			if(retypeOfNode == null)
				retypeOfNode = tempRetype;
		}
		for(IteratedDeclNode iterated : pattern.iters.getChildren()) {
			result = iterated.noAmbiguousRetypes(iterated.right, node, retypeOfNode);
			noAmbiguousRetypes &= result.first.booleanValue();
			if(retypeOfNode == null)
				retypeOfNode = result.second;
		}		
		return new Pair<Boolean, NodeTypeChangeDeclNode>(Boolean.valueOf(noAmbiguousRetypes), retypeOfNode);
	}

	protected Pair<Boolean, EdgeTypeChangeDeclNode> noAmbiguousRetypes(RhsDeclNode right, EdgeDeclNode edge, EdgeTypeChangeDeclNode retypeOfEdge)
	{
		if(right == null)
			return new Pair<Boolean, EdgeTypeChangeDeclNode>(Boolean.valueOf(false), retypeOfEdge);
		boolean noAmbiguousRetypes = true;
		Pair<Boolean, EdgeTypeChangeDeclNode> result = right.getRhsGraph().noAmbiguousRetypes(edge, retypeOfEdge);
		noAmbiguousRetypes &= result.first.booleanValue();
		retypeOfEdge = result.second;
		for(AlternativeDeclNode alternative : pattern.alts.getChildren()) {
			EdgeTypeChangeDeclNode tempRetype = null;
			for(AlternativeCaseDeclNode alternativeCase : alternative.getChildren()) {
				result = alternativeCase.noAmbiguousRetypes(alternativeCase.right, edge, retypeOfEdge);
				noAmbiguousRetypes &= result.first.booleanValue();
				if(tempRetype == null)
					tempRetype = result.second;
			}
			if(retypeOfEdge == null)
				retypeOfEdge = tempRetype;
		}
		for(IteratedDeclNode iterated : pattern.iters.getChildren()) {
			result = iterated.noAmbiguousRetypes(iterated.right, edge, retypeOfEdge);
			noAmbiguousRetypes &= result.first.booleanValue();
			if(retypeOfEdge == null)
				retypeOfEdge = result.second;
		}		
		return new Pair<Boolean, EdgeTypeChangeDeclNode>(Boolean.valueOf(noAmbiguousRetypes), retypeOfEdge);
	}

	protected void constructIRaux(Rule constructedRule, RhsDeclNode right)
	{
		// add Params to the IR
		addParams(constructedRule);

		// add replacement parameters to the IR
		PatternGraphRhs rightPattern = null;
		if(right != null) {
			rightPattern = right.getPatternGraph(pattern.getPatternGraph());
		} else {
			return;
		}

		// add replacement parameters to the current graph
		for(DeclNode decl : right.patternGraph.getParamDecls()) {
			if(decl instanceof NodeCharacter) {
				rightPattern.addReplParameter(decl.checkIR(Node.class));
				rightPattern.addSingleNode(((NodeCharacter)decl).getNode());
			} else if(decl instanceof VarDeclNode) {
				rightPattern.addReplParameter(decl.checkIR(Variable.class));
				rightPattern.addVariable(((VarDeclNode)decl).getVariable());
			} else {
				throw new IllegalArgumentException("unknown Class: " + decl);
			}
		}

		// and also to the nested alternatives and iterateds
		addReplacementParamsToNestedAlternativesAndIterateds(constructedRule, right);
	}

	protected void addParams(MatchingAction constructedMatchingAction)
	{
		PatternGraphLhs patternGraph = constructedMatchingAction.getPattern();

		for(DeclNode decl : pattern.getParamDecls()) {
			Entity entity = decl.checkIR(Entity.class);
			if(entity.isDefToBeYieldedTo())
				constructedMatchingAction.addDefParameter(entity);
			else
				constructedMatchingAction.addParameter(entity);
			
			if(decl instanceof VarDeclNode) { // nodes/edges already have been added
				patternGraph.addVariable(((VarDeclNode)decl).getVariable());
			}
		}
	}

	protected static void addReplacementParamsToNestedAlternativesAndIterateds(Rule constructedRule, RhsDeclNode right)
	{
		// add replacement parameters to the nested alternatives and iterateds
		PatternGraphLhs patternGraph = constructedRule.getPattern();
		for(DeclNode decl : right.patternGraph.getParamDecls()) {
			if(decl instanceof NodeDeclNode) {
				addReplacementNodeParamToNestedAlternativesAndIterateds((NodeDeclNode)decl, patternGraph);
			} else if(decl instanceof VarDeclNode) {
				addReplacementVarParamToNestedAlternativesAndIterateds((VarDeclNode)decl, patternGraph);
			} else {
				throw new IllegalArgumentException("unknown Class: " + decl);
			}
		}
	}

	private static void addReplacementNodeParamToNestedAlternativesAndIterateds(NodeDeclNode decl, PatternGraphLhs patternGraph)
	{
		for(Alternative alt : patternGraph.getAlts()) {
			for(Rule altCase : alt.getAlternativeCases()) {
				altCase.getRight().addReplParameter(decl.checkIR(Node.class));
				altCase.getRight().addSingleNode(decl.getNode());
			}
		}
		for(Rule iter : patternGraph.getIters()) {
			iter.getRight().addReplParameter(decl.checkIR(Node.class));
			iter.getRight().addSingleNode(decl.getNode());
		}
	}

	private static void addReplacementVarParamToNestedAlternativesAndIterateds(VarDeclNode decl, PatternGraphLhs patternGraph)
	{
		for(Alternative alt : patternGraph.getAlts()) {
			for(Rule altCase : alt.getAlternativeCases()) {
				altCase.getRight().addReplParameter(decl.checkIR(Variable.class));
				altCase.getRight().addVariable(decl.getVariable());
			}
		}
		for(Rule iter : patternGraph.getIters()) {
			iter.getRight().addReplParameter(decl.checkIR(Variable.class));
			iter.getRight().addVariable(decl.getVariable());
		}
	}

	/**
	 * add NACs for induced- or DPO-semantic
	 */
	protected void constructImplicitNegs(PatternGraphLhs left)
	{
		PatternGraphLhsNode leftNode = pattern;
		ImplicitNegComputer implicitNegComputer = new ImplicitNegComputer(leftNode);
		ImplicitNegComputerInduced implicitNegComputerInduced = new ImplicitNegComputerInduced(leftNode);
		
		for(PatternGraphLhs neg : implicitNegComputer.getImplicitNegGraphs()) {
			left.addNegGraph(neg);
		}
		for(PatternGraphLhs neg : implicitNegComputerInduced.getImplicitNegGraphs()) {
			left.addNegGraph(neg);
		}
	}

	// TODO use this to create IR patterns, that is currently not supported by
	//      any backend
	/*private IR constructPatternIR() {
		PatternGraph left = pattern.getPatternGraph();
	
		// return if the pattern graph already constructed the IR object
		// that may happens in recursive patterns
		if (isIRAlreadySet()) {
			return getIR();
		}
	
		Vector<PatternGraph> right = new Vector<PatternGraph>();
		for (int i = 0; i < this.right.children.size(); i++) {
			right.add(this.right.children.get(i).getPatternGraph(left));
		}
	
		// return if the pattern graph already constructed the IR object
		// that may happens in recursive patterns
		if (isIRAlreadySet()) {
			return getIR();
		}
	
		Pattern pattern = new Pattern(getIdentNode().getIdent(), left, right);
	
		constructImplicitNegs(left);
		constructIRaux(pattern);
	
		// add Eval statements to the IR
		for (int i = 0; i < this.right.children.size(); i++) {
			for (Assignment n : this.right.children.get(i).getAssignments()) {
				pattern.addEval(i,n);
			}
		}
	
		return pattern;
	}*/
}
