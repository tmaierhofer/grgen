/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author shack
 */

package de.unika.ipd.grgen.ast;

import de.unika.ipd.grgen.ast.exprevals.DeclExprNode;
import de.unika.ipd.grgen.ast.exprevals.ExprNode;
import de.unika.ipd.grgen.ir.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

/**
 * Base class for all action type ast nodes
 */
public abstract class ActionDeclNode extends DeclNode
{
	protected PatternGraphNode pattern;

	public ActionDeclNode(IdentNode id, TypeNode type, PatternGraphNode left) {
		super(id, type);

		this.pattern = left;
		becomeParent(this.pattern);
	}

	/**
	 * Get the IR object for this action node.
	 * The IR object is instance of Rule.
	 * @return The IR object.
	 */
	protected Rule getAction() {
		return checkIR(Rule.class);
	}

	protected PatternGraphNode getParentPatternGraph(BaseNode node) {
		if (node == null) {
			return null;
		}

		Queue<Collection<BaseNode>> queue = new LinkedList<Collection<BaseNode>>();
		for (Collection<BaseNode> parents = node.getParents(); parents != null; parents = queue.poll()) {
			for (BaseNode parent : parents) {
				if (parent instanceof PatternGraphNode) {
					return (PatternGraphNode)parent;
				}
				Collection<BaseNode> grandParents = parent.getParents();
				if (grandParents != null && !grandParents.isEmpty()) {
					queue.add(grandParents);
				}
			}
		}

		return null;
	}

	protected boolean resolveFilters(ArrayList<FilterAutoNode> filters) {
		boolean filtersOk = true;
		for(FilterAutoNode filter : filters) {
			if(filter instanceof FilterAutoSuppliedNode) {
				filtersOk &= ((FilterAutoSuppliedNode)filter).resolve();
			} else { //if(filter instanceof FilterAutoGeneratedNode)
				filtersOk &= ((FilterAutoGeneratedNode)filter).resolve();
			}
		}
		return filtersOk;
	}

	protected boolean checkFilters(PatternGraphNode pattern, ArrayList<FilterAutoNode> filters) {
		boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
		HashSet<String> alreadySeenFilters = new HashSet<String>();
		for(FilterAutoNode fa : filters) {
			if(fa instanceof FilterAutoGeneratedNode) {
				FilterAutoGeneratedNode filter = (FilterAutoGeneratedNode)fa;
				String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
				if(alreadySeenFilters.contains(filterNameWithEntitySuffix)) {
					reportError(filterNameWithEntitySuffix + " was already declared, only one declaration admissible.");
					allFilterEntitiesExistAndAreOfAdmissibleType = false;
				} else {
					alreadySeenFilters.add(filterNameWithEntitySuffix);
				}
				if(filter.name.equals("orderAscendingBy") || filter.name.equals("orderDescendingBy")
					|| filter.name.equals("groupBy") || filter.name.equals("keepSameAsFirst")
					|| filter.name.equals("keepSameAsLast") || filter.name.equals("keepOneForEach")) {
					for(String filterEntity : filter.entities) {
						allFilterEntitiesExistAndAreOfAdmissibleType &= checkFilterVariable(pattern, filterNameWithEntitySuffix, filterEntity);
					}
					if(filter.name.equals("groupBy") || filter.name.equals("keepSameAsFirst")
						|| filter.name.equals("keepSameAsLast") || filter.name.equals("keepOneForEach")) {
						if(filter.entities.size()!=1) {
							reportError(filterNameWithEntitySuffix + " must be declared with exactly one variable, but is declared with " + filter.entities.size() + " variables");
							allFilterEntitiesExistAndAreOfAdmissibleType = false;
						}
					}
				}
			}
		}
		return allFilterEntitiesExistAndAreOfAdmissibleType;
	}

	private boolean checkFilterVariable(PatternGraphNode pattern, String filterNameWithEntitySuffix, String filterVariable) {
		if(pattern.getVariable(filterVariable)==null) {
			reportError(filterNameWithEntitySuffix + ": unknown variable " + filterVariable);
			return false;
		}
		TypeNode filterVariableType = pattern.getVariable(filterVariable).getDeclType();
		if(!filterVariableType.isFilterableType()) {
			reportError(filterNameWithEntitySuffix + ": the variable " + filterVariable + " must be of one of the following types: " + filterVariableType.getFilterableTypesAsString());
			return false;
		}
		return true;
	}
	
	protected boolean checkLeft() {
		// check if reused names of edges connect the same nodes in the same direction with the same edge kind for each usage
		boolean edgeReUse = false;
		edgeReUse = true;

		//get the negative graphs and the pattern of this TestDeclNode
		// NOTE: the order affect the error coords
		Collection<PatternGraphNode> leftHandGraphs = new LinkedList<PatternGraphNode>();
		leftHandGraphs.add(pattern);
		for (PatternGraphNode pgn : pattern.negs.getChildren()) {
			leftHandGraphs.add(pgn);
		}

		GraphNode[] graphs = leftHandGraphs.toArray(new GraphNode[0]);
		Collection<EdgeCharacter> alreadyReported = new HashSet<EdgeCharacter>();

		for (int i=0; i<graphs.length; i++) {
			for (int o=i+1; o<graphs.length; o++) {
				for (BaseNode iBN : graphs[i].getConnections()) {
					if (! (iBN instanceof ConnectionNode)) {
						continue;
					}
					ConnectionNode iConn = (ConnectionNode)iBN;

					for (BaseNode oBN : graphs[o].getConnections()) {
						if (! (oBN instanceof ConnectionNode)) {
							continue;
						}
						ConnectionNode oConn = (ConnectionNode)oBN;

						if (iConn.getEdge().equals(oConn.getEdge()) && !alreadyReported.contains(iConn.getEdge())) {
							NodeCharacter oSrc, oTgt, iSrc, iTgt;
							oSrc = oConn.getSrc();
							oTgt = oConn.getTgt();
							iSrc = iConn.getSrc();
							iTgt = iConn.getTgt();

							assert ! (oSrc instanceof NodeTypeChangeNode):
								"no type changes in test actions";
							assert ! (oTgt instanceof NodeTypeChangeNode):
								"no type changes in test actions";
							assert ! (iSrc instanceof NodeTypeChangeNode):
								"no type changes in test actions";
							assert ! (iTgt instanceof NodeTypeChangeNode):
								"no type changes in test actions";

							//check only if there's no dangling edge
							if ( !((iSrc instanceof NodeDeclNode) && ((NodeDeclNode)iSrc).isDummy())
								&& !((oSrc instanceof NodeDeclNode) && ((NodeDeclNode)oSrc).isDummy())
								&& iSrc != oSrc ) {
								alreadyReported.add(iConn.getEdge());
								iConn.reportError("Reused edge does not connect the same nodes");
								edgeReUse = false;
							}

							//check only if there's no dangling edge
							if ( !((iTgt instanceof NodeDeclNode) && ((NodeDeclNode)iTgt).isDummy())
								&& !((oTgt instanceof NodeDeclNode) && ((NodeDeclNode)oTgt).isDummy())
								&& iTgt != oTgt && !alreadyReported.contains(iConn.getEdge())) {
								alreadyReported.add(iConn.getEdge());
								iConn.reportError("Reused edge does not connect the same nodes");
								edgeReUse = false;
							}


							if (iConn.getConnectionKind() != oConn.getConnectionKind()) {
								alreadyReported.add(iConn.getEdge());
								iConn.reportError("Reused edge does not have the same connection kind");
								edgeReUse = false;
							}
						}
					}
				}
			}
		}

		return edgeReUse;
	}
	
	/** Checks, whether the reused nodes and edges of the RHS are consistent with the LHS.
	 * If consistent, replace the dummy nodes with the nodes the pattern edge is
	 * incident to (if these aren't dummy nodes themselves, of course). */
	protected boolean checkRhsReuse(RhsDeclNode right) {
		boolean res = true;

		HashMap<EdgeDeclNode, NodeDeclNode> redirectedFrom = new HashMap<EdgeDeclNode, NodeDeclNode>();
		HashMap<EdgeDeclNode, NodeDeclNode> redirectedTo = new HashMap<EdgeDeclNode, NodeDeclNode>();

		Collection<EdgeDeclNode> alreadyReported = new HashSet<EdgeDeclNode>();
		for (ConnectionNode rConn : right.getReusedConnections(pattern)) {
			EdgeDeclNode re = rConn.getEdge();

			if (re instanceof EdgeTypeChangeNode) {
				re = ((EdgeTypeChangeNode)re).getOldEdge();
			}

			for (BaseNode lc : pattern.getConnections()) {
				if (!(lc instanceof ConnectionNode)) {
					continue;
				}

				ConnectionNode lConn = (ConnectionNode) lc;

				EdgeDeclNode le = lConn.getEdge();

				if ( ! le.equals(re) ) {
					continue;
				}

				if (lConn.getConnectionKind() != rConn.getConnectionKind()) {
					res = false;
					rConn.reportError("Reused edge does not have the same connection kind");
					// if you don't add to alreadyReported erroneous errors can occur,
					// e.g. lhs=x-e->y, rhs=y-e-x
					alreadyReported.add(re);
				}

				NodeDeclNode lSrc = lConn.getSrc();
				NodeDeclNode lTgt = lConn.getTgt();
				NodeDeclNode rSrc = rConn.getSrc();
				NodeDeclNode rTgt = rConn.getTgt();

				HashSet<BaseNode> rhsNodes = new HashSet<BaseNode>();
				rhsNodes.addAll(right.getReusedNodes(pattern));

				if (rSrc instanceof NodeTypeChangeNode) {
					rSrc = ((NodeTypeChangeNode)rSrc).getOldNode();
					rhsNodes.add(rSrc);
				}
				if (rTgt instanceof NodeTypeChangeNode) {
					rTgt = ((NodeTypeChangeNode)rTgt).getOldNode();
					rhsNodes.add(rTgt);
				}

				if ( ! lSrc.isDummy() ) {
					if ( rSrc.isDummy() ) {
						if ( rhsNodes.contains(lSrc) ) {
							//replace the dummy src node by the src node of the pattern connection
							rConn.setSrc(lSrc);
						} else if ( ! alreadyReported.contains(re) ) {
							res = false;
							rConn.reportError("The source node of reused edge \"" + le + "\" must be reused, too");
							alreadyReported.add(re);
						}
					} else if (lSrc != rSrc && (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE)!=ConnectionNode.REDIRECT_SOURCE && ! alreadyReported.contains(re)) {
						res = false;
						rConn.reportError("Reused edge \"" + le + "\" does not connect the same nodes (and is not declared to redirect source)");
						alreadyReported.add(re);
					}
				}
				
				if ( (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE)==ConnectionNode.REDIRECT_SOURCE ) {
					if(rSrc.isDummy()) {
						res = false;
						rConn.reportError("An edge with source redirection must be given a source node.");
					}
					
					if(lSrc.equals(rSrc)) {
						rConn.reportWarning("Redirecting edge to same source again.");
					}
					
					if(redirectedFrom.containsKey(le)) {
						res = false;
						rConn.reportError("Can't redirect edge source more than once.");
					}
					redirectedFrom.put(le, rSrc);
				}

				if ( ! lTgt.isDummy() ) {
					if ( rTgt.isDummy() ) {
						if ( rhsNodes.contains(lTgt) ) {
							//replace the dummy tgt node by the tgt node of the pattern connection
							rConn.setTgt(lTgt);
						} else if ( ! alreadyReported.contains(re) ) {
							res = false;
							rConn.reportError("The target node of reused edge \"" + le + "\" must be reused, too");
							alreadyReported.add(re);
						}
					} else if ( lTgt != rTgt && (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET)!=ConnectionNode.REDIRECT_TARGET && ! alreadyReported.contains(re)) {
						res = false;
						rConn.reportError("Reused edge \"" + le + "\" does not connect the same nodes (and is not declared to redirect target)");
						alreadyReported.add(re);
					}
				}
				
				if ( (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET)==ConnectionNode.REDIRECT_TARGET ) {
					if(rTgt.isDummy()) {
						res = false;
						rConn.reportError("An edge with target redirection must be given a target node.");
					}
					
					if(lTgt.equals(rTgt)) {
						rConn.reportWarning("Redirecting edge to same target again.");
					}
					
					if(redirectedTo.containsKey(le)) {
						res = false;
						rConn.reportError("Can't redirect edge target more than once.");
					}
					redirectedTo.put(le, rSrc);
				}

				//check, whether RHS "adds" a node to a dangling end of a edge
				if ( ! alreadyReported.contains(re) ) {
					if ( lSrc.isDummy() && ! rSrc.isDummy() && (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_SOURCE)!=ConnectionNode.REDIRECT_SOURCE ) {
						res = false;
						rConn.reportError("Reused edge dangles on LHS, but has a source node on RHS");
						alreadyReported.add(re);
					}
					if ( lTgt.isDummy() && ! rTgt.isDummy() && (rConn.getRedirectionKind() & ConnectionNode.REDIRECT_TARGET)!=ConnectionNode.REDIRECT_TARGET ) {
						res = false;
						rConn.reportError("Reused edge dangles on LHS, but has a target node on RHS");
						alreadyReported.add(re);
					}
				}
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
	protected boolean checkExecParamsNotDeleted(RhsDeclNode right) {
		assert isResolved();

		boolean valid = true;

		Set<DeclNode> delete = right.getDelete(pattern);
		Collection<DeclNode> maybeDeleted = right.getMaybeDeleted(pattern);

		for (BaseNode x : right.graph.imperativeStmts.getChildren()) {
			if(!(x instanceof ExecNode)) continue;

			ExecNode exec = (ExecNode) x;
			for(CallActionNode callAction : exec.callActions.getChildren()) {
				for(ExprNode arg : callAction.params.getChildren()) {
					if(!(arg instanceof DeclExprNode)) continue;

					ConstraintDeclNode declNode = ((DeclExprNode) arg).getConstraintDeclNode();
					if(declNode != null) {
						if(delete.contains(declNode)) {
							arg.reportError("The deleted " + declNode.getUseString()
									+ " \"" + declNode.ident + "\" must not be passed to an exec statement");
							valid = false;
						}
						else if (maybeDeleted.contains(declNode)) {
							declNode.maybeDeleted = true;

							if(!declNode.getIdentNode().getAnnotations().isFlagSet("maybeDeleted")) {
								valid = false;

								String errorMessage = "Parameter \"" + declNode.ident + "\" of exec statement may be deleted"
										+ ", possibly it's homomorphic with a deleted " + declNode.getUseString();
								errorMessage += " (use a [maybeDeleted] annotation if you think that this does not cause problems)";

								if(declNode instanceof EdgeDeclNode) {
									errorMessage += " or \"" + declNode.ident + "\" is a dangling " + declNode.getUseString()
											+ " and a deleted node exists";
								}
								arg.reportError(errorMessage);
							}
						}
					}
				}
			}
		}
		
		return valid;
	}
	
	protected boolean SameNumberOfRewritePartsAndNoNestedRewriteParameters(RhsDeclNode right, String actionKind) {
		boolean res = true;

		for(AlternativeNode alt : pattern.alts.getChildren()) {
			for(AlternativeCaseNode altCase : alt.getChildren()) {
				if((right == null) != (altCase.right == null)) {
					error.error(getCoords(), "Different number of replacement patterns/rewrite parts in " + actionKind + " " + ident.toString()
							+ " and nested alternative case " + altCase.ident.toString());
					res = false;
					continue;
				}

				if(right == null) continue;

				Vector<DeclNode> parametersInNestedAlternativeCase =
					altCase.right.graph.getParamDecls();

				if(parametersInNestedAlternativeCase.size()!=0) {
					error.error(altCase.getCoords(), "No replacement parameters allowed in nested alternative cases; given in " + altCase.ident.toString());
					res = false;
					continue;
				}
			}
		}

		for(IteratedNode iter : pattern.iters.getChildren()) {
			if((right == null) != (iter.right == null)) {
				error.error(getCoords(), "Different number of replacement patterns/rewrite parts in " + actionKind + " " + ident.toString()
						+ " and nested iterated/multiple/optional " + iter.ident.toString());
				res = false;
				continue;
			}

			if(right == null) continue;

			Vector<DeclNode> parametersInNestedIterated =
				iter.right.graph.getParamDecls();

			if(parametersInNestedIterated.size()!=0) {
				error.error(iter.getCoords(), "No replacement parameters allowed in nested iterated/multiple/optional; given in " + iter.ident.toString());
				res = false;
				continue;
			}
		}

		return res;
	}
	
	protected boolean noAbstractElementInstantiatedNestedPattern(RhsDeclNode right) {		
		boolean abstr = true;

nodeAbstrLoop:
        for (NodeDeclNode node : right.graph.getNodes()) {
            if (!node.inheritsType() && node.getDeclType().isAbstract()) {
                if ((node.context & CONTEXT_PARAMETER) == CONTEXT_PARAMETER) {
                    continue;
                }
                for (PatternGraphNode pattern = this.pattern; pattern != null;
                        pattern = getParentPatternGraph(pattern)) {
                    if (pattern.getNodes().contains(node)) {
                        continue nodeAbstrLoop;
                    }
                }
                error.error(node.getCoords(), "Instances of abstract nodes are not allowed");
                abstr = false;
            }
        }

edgeAbstrLoop:
        for (EdgeDeclNode edge : right.graph.getEdges()) {
            if (!edge.inheritsType() && edge.getDeclType().isAbstract()) {
                if ((edge.context & CONTEXT_PARAMETER) == CONTEXT_PARAMETER) {
                    continue;
                }
                for (PatternGraphNode pattern = this.pattern; pattern != null;
                        pattern = getParentPatternGraph(pattern)) {
                    if (pattern.getEdges().contains(edge)) {
                        continue edgeAbstrLoop;
                    }
                }
				error.error(edge.getCoords(), "Instances of abstract edges are not allowed");
				abstr = false;
			}
		}
	
		return abstr;
	}
	
	protected boolean noAbstractElementInstantiated(RhsDeclNode right) {		
		boolean abstr = true;

        for(NodeDeclNode node : right.graph.getNodes()) {
            if(!node.inheritsType() && node.getDeclType().isAbstract()
            		&& !pattern.getNodes().contains(node) && (node.context&CONTEXT_PARAMETER)!=CONTEXT_PARAMETER) {
                error.error(node.getCoords(), "Instances of abstract nodes are not allowed");
                abstr = false;
            }
        }
        for(EdgeDeclNode edge : right.graph.getEdges()) {
            if(!edge.inheritsType() && edge.getDeclType().isAbstract()
            		&& !pattern.getEdges().contains(edge) && (edge.context&CONTEXT_PARAMETER)!=CONTEXT_PARAMETER) {
				error.error(edge.getCoords(), "Instances of abstract edges are not allowed");
				abstr = false;
			}
		}
	
		return abstr;
	}
}
