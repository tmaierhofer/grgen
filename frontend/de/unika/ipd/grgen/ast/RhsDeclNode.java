/*
 GrGen: graph rewrite generator tool.
 Copyright (C) 2005  IPD Goos, Universit"at Karlsruhe, Germany

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * @author Sebastian Buchwald
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import de.unika.ipd.grgen.ast.util.DeclarationTypeResolver;
import de.unika.ipd.grgen.ir.Assignment;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.PatternGraph;


/**
 * AST node for a replacement right-hand side.
 */
public abstract class RhsDeclNode extends DeclNode {
	static {
		setName(RhsDeclNode.class, "right-hand side declaration");
	}

	GraphNode graph;
	CollectNode<AssignNode> eval;
	RhsTypeNode type;

	/** Type for this declaration. */
	protected static final TypeNode rhsType = new RhsTypeNode();

	/**
	 * Make a new right-hand side.
	 * @param id The identifier of this RHS.
	 * @param graph The right hand side graph.
	 * @param eval The evaluations.
	 */
	public RhsDeclNode(IdentNode id, GraphNode graph, CollectNode<AssignNode> eval) {
		super(id, rhsType);
		this.graph = graph;
		becomeParent(this.graph);
		this.eval = eval;
		becomeParent(this.eval);
	}

	protected Collection<DeclNode> getMaybeDeleted(PatternGraphNode pattern) {
		Collection<DeclNode> ret = new LinkedHashSet<DeclNode>();
		ret.addAll(getDelete(pattern));

		// check if a deleted node exists
		Collection<NodeDeclNode> nodes = new LinkedHashSet<NodeDeclNode>();
		for (DeclNode declNode : ret) {

			if (declNode instanceof NodeDeclNode) {
	        	nodes.add((NodeDeclNode) declNode);
	        }
        }


		if (nodes.size() > 0) {
			// add homomorphic nodes
			for (NodeDeclNode node : nodes) {
				ret.addAll(pattern.getCorrespondentHomSet(node));
            }

    		Collection<ConnectionNode> conns = getResultingConnections(pattern);
    		for (ConnectionNode conn : conns) {
    			if (sourceOrTargetNodeIncluded(pattern, ret, conn.getEdge())) {
    				ret.add(conn.getEdge());
    			}
            }

			// nodes of dangling edges are homomorphic to all other nodes,
    		// especially the deleted ones :-)
    		for (ConnectionNode conn : conns) {
    			EdgeDeclNode edge = conn.getEdge();
    			while (edge instanceof EdgeTypeChangeNode) {
    				edge = ((EdgeTypeChangeNode) edge).getOldEdge();
    			}
    			boolean srcIsDummy = true;
    			boolean tgtIsDummy = true;
    			for (ConnectionNode innerConn : conns) {
        			if (edge.equals(innerConn.getEdge())) {
        				srcIsDummy &= innerConn.getSrc().isDummy();
        				tgtIsDummy &= innerConn.getTgt().isDummy();
        			}
                }

    			if (srcIsDummy || tgtIsDummy) {
    				ret.add(edge);
    			}
            }
		}

		// add homomorphic edges
		Collection<EdgeDeclNode> edges = new LinkedHashSet<EdgeDeclNode>();
		for (DeclNode declNode : ret) {
	        if (declNode instanceof EdgeDeclNode) {
	        	edges.add((EdgeDeclNode) declNode);
	        }
        }
		for (EdgeDeclNode edge : edges) {
			ret.addAll(pattern.getCorrespondentHomSet(edge));
        }

		return ret;
	}

	protected abstract Collection<ConnectionNode> getResultingConnections(PatternGraphNode pattern);

	/** returns children of this node */
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(ident);
		children.add(getValidVersion(typeUnresolved, type));
		children.add(graph);
		children.add(eval);
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("ident");
		childrenNames.add("type");
		childrenNames.add("right");
		childrenNames.add("eval");
		return childrenNames;
	}

	protected static final DeclarationTypeResolver<RhsTypeNode> typeResolver =	new DeclarationTypeResolver<RhsTypeNode>(RhsTypeNode.class);

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolveLocal() */
	protected boolean resolveLocal() {
		type = typeResolver.resolve(typeUnresolved, this);

		return type != null;
	}

	/**
	 * @see de.unika.ipd.grgen.ast.BaseNode#checkLocal()
	 */
	protected boolean checkLocal() {
		for(DeclNode replParam : graph.getParamDecls()) {
			if(!(replParam instanceof NodeDeclNode)) {
				// edges as replacement parameters are not really needed but very troublesome, keep them out for now
				replParam.reportError("only nodes supported as replacement parameters, "
						+ replParam.ident.toString() + " isn't one");
				return false;
			}
		}
		return true;
	}

	/**
	 * @see de.unika.ipd.grgen.ast.BaseNode#constructIR()
	 */
	protected IR constructIR() {
		assert false;

		return null;
	}

	protected Collection<Assignment> getAssignments() {
		Collection<Assignment> ret = new LinkedHashSet<Assignment>();

		for (AssignNode n : eval.getChildren()) {
			ret.add((Assignment) n.checkIR(Assignment.class));
		}

		return ret;
	}

	protected abstract PatternGraph getPatternGraph(PatternGraph left);

	@Override
	public RhsTypeNode getDeclType() {
		assert isResolved();

		return type;
	}

	protected abstract Set<DeclNode> getDelete(PatternGraphNode pattern);

	/**
	 * Return all reused edges (with their nodes), that excludes new edges of
	 * the right-hand side.
	 */
	protected abstract Collection<ConnectionNode> getReusedConnections(PatternGraphNode pattern);

	/**
	 * Return all reused nodes, that excludes new nodes of the right-hand side.
	 */
	protected abstract Set<BaseNode> getReusedNodes(PatternGraphNode pattern);

	protected abstract void warnElemAppearsInsideAndOutsideDelete(PatternGraphNode pattern);

	protected boolean sourceOrTargetNodeIncluded(PatternGraphNode pattern, Collection<? extends BaseNode> coll,
            EdgeDeclNode edgeDecl)
    {
    	for (BaseNode n : pattern.getConnections()) {
            if (n instanceof ConnectionNode) {
            	ConnectionNode conn = (ConnectionNode) n;
            	if (conn.getEdge().equals(edgeDecl)) {
            		if (coll.contains(conn.getSrc())
            				|| coll.contains(conn.getTgt())) {
            			return true;
            		}
            	}
            }
        }
    	return false;
    }
}

