/*
 GrGen: graph rewrite generator tool.
 Copyright (C) 2007  IPD Goos, Universit"at Karlsruhe, Germany

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
 * ActionsGen.java
 *
 * Generates the actions file for the SearchPlanBackend2 backend.
 *
 * @author Moritz Kroll, Edgar Jakumeit
 * @version $Id: ActionsGen.java 18216 2008-03-22 21:18:36Z eja $
 */

package de.unika.ipd.grgen.be.Csharp;

import de.unika.ipd.grgen.ir.*;

import java.util.*;

public class ModifyGen extends CSharpBase {
	public ModifyGen() {
	}

	//////////////////////////////////
	// Modification part generation //
	//////////////////////////////////

	public void genTestModify(StringBuffer sb, Test test, boolean isSubpattern) {
		if(!isSubpattern) {
			nodesNeededAsElements.clear();
			nodesNeededAsAttributes.clear();
			edgesNeededAsElements.clear();
			edgesNeededAsAttributes.clear();
	
			collectReturnElements(test);
	
			sb.append("\t\tpublic override IGraphElement[] Modify(LGSPGraph graph, LGSPMatch match)\n");
			sb.append("\t\t{  // test does not have modifications\n");
			extractElementsFromMatch(sb, test.getPattern().getNameOfGraph());
			emitReturnStatement(sb, test);
			sb.append("\t\t}\n");
	
			sb.append("\t\tpublic override IGraphElement[] ModifyNoReuse(LGSPGraph graph, LGSPMatch match)\n");
			sb.append("\t\t{  // test does not have modifications\n");
			extractElementsFromMatch(sb, test.getPattern().getNameOfGraph());
			emitReturnStatement(sb, test);
			sb.append("\t\t}\n");
		}
		else {
			sb.append("\t\tpublic override IGraphElement[] Modify(LGSPGraph graph, LGSPMatch match)\n");
			sb.append("\t\t{  // currently empty\n");
			sb.append("\t\t\treturn EmptyReturnElements;\n");
			sb.append("\t\t}\n");
	
			sb.append("\t\tpublic override IGraphElement[] ModifyNoReuse(LGSPGraph graph, LGSPMatch match)\n");
			sb.append("\t\t{  // currently empty\n");
			sb.append("\t\t\treturn EmptyReturnElements;\n");
			sb.append("\t\t}\n");
		}
	}
	
	public void genRuleModify(StringBuffer sb, Rule rule, boolean reuseNodeAndEdges, boolean isSubpattern) {
		StringBuffer sb2 = new StringBuffer();
		StringBuffer sb3 = new StringBuffer();

		PatternGraph patternGraph = rule.getLeft();
		PatternGraph replaceGraph = rule.getRight();
		String patternName = patternGraph.getNameOfGraph();

		// Emit function header
		sb.append("\t\tpublic override IGraphElement[] "
					  + (reuseNodeAndEdges ? "Modify" : "ModifyNoReuse")
					  + "(LGSPGraph graph, LGSPMatch match)\n");
		sb.append("\t\t{\n");

		// The resulting code has the following order:
		//  - Extract nodes from match as LGSPNode instances
		//  - Extract nodes from match or from already extracted nodes as interface instances
		//  - Extract edges from match as LGSPEdge instances
		//  - Extract edges from match or from already extracted edges as interface instances
		//  - Extract node types
		//  - Extract edge types
		//  - Create variables for used attributes of reusee
		//  - Create new nodes or reuse nodes
		//  - Retype nodes
		//  - Create new edges or reuse edges
		//  - Retype edges
		//  - Attribute reevaluation
		//  - Create variables for used attributes of non-reusees needed for emits
		//  - Check deleted elements for retyping due to homomorphy
		//  - Remove edges
		//  - Remove nodes
		//  - Emit
		//  - Check returned elements for deletion and retyping due to homomorphy
		//  - Return

		// Initialize used data structures
		reusedElements.clear();
		accessViaInterface.clear();
		neededAttributes.clear();
		neededAttributesForEmit.clear();
		nodesNeededAsElements.clear();
		edgesNeededAsElements.clear();
		nodesNeededAsTypes.clear();
		edgesNeededAsTypes.clear();
		nodesNeededAsAttributes.clear();
		edgesNeededAsAttributes.clear();
		forceAttributeToVar.clear();

		// Common elements are elements of the LHS which are unmodified by RHS
		commonNodes = rule.getCommonNodes();
		commonEdges = rule.getCommonEdges();

		// New elements are elements from the RHS which are not common
		newNodes = new HashSet<Node>(replaceGraph.getNodes());
		newNodes.removeAll(commonNodes);
		newEdges = new HashSet<Edge>(replaceGraph.getEdges());
		newEdges.removeAll(commonEdges);

		// Deleted elements are elements from the LHS which are not common
		delNodes = new HashSet<Node>(patternGraph.getNodes());
		delNodes.removeAll(commonNodes);
		delEdges = new HashSet<Edge>(patternGraph.getEdges());
		delEdges.removeAll(commonEdges);

		// Collect all elements which are new or retyped
		newOrRetypedNodes = new HashSet<Node>(newNodes);
		for(Node node : replaceGraph.getNodes()) {
			if(node.changesType())
				newOrRetypedNodes.add(node.getRetypedNode());
		}
		newOrRetypedEdges = new HashSet<Edge>(newEdges);
		for(Edge edge : replaceGraph.getEdges()) {
			if(edge.changesType())
				newOrRetypedEdges.add(edge.getRetypedEdge());
		}

		// Collect all elements which must be accessed via interface
		accessViaInterface.addAll(patternGraph.getNodes());
		accessViaInterface.addAll(patternGraph.getEdges());
		for(Node node : replaceGraph.getNodes()) {
			if(node.inheritsType())
				accessViaInterface.add(node);
			else if(node.changesType())
				accessViaInterface.add(node.getRetypedEntity());
		}
		for(Edge edge : replaceGraph.getEdges()) {
			if(edge.inheritsType())
				accessViaInterface.add(edge);
			else if(edge.changesType())
				accessViaInterface.add(edge.getRetypedEntity());
		}

		// Collect all entities with their attributes needed by imperative statements
		for(ImperativeStmt istmt : replaceGraph.getImperativeStmts()) {
			if(istmt instanceof Emit) {
				Emit emit = (Emit) istmt;
				for(Expression arg : emit.getArguments())
					collectNeededAttributes(arg);
			}
			else if (istmt instanceof Exec) {
				Exec exec = (Exec) istmt;
				for(Entity param : exec.getArguments()) {
					if(param instanceof Node)
						nodesNeededAsElements.add((Node) param);
					else if(param instanceof Edge)
						edgesNeededAsElements.add((Edge) param);
					else if(param instanceof Variable)
						System.err.println("genRuleModify(): TODO NYI Variable " + param +"  TODO"); // TODO is it correct to do noting
					else
						assert false : "XGRS argument of unknown type: " + param.getClass();
				}
			}
			else assert false : "unknown ImperativeStmt: " + istmt + " in " + rule;
		}

		// Copy all entries generated by collectNeededAttributes for emit stuff
		for(Map.Entry<GraphEntity, HashSet<Entity>> entry : neededAttributes.entrySet()) {
			HashSet<Entity> neededAttrs = entry.getValue();
			HashSet<Entity> neededAttrsForEmit = neededAttributesForEmit.get(entry.getKey());
			if(neededAttrsForEmit == null) {
				neededAttributesForEmit.put(entry.getKey(), neededAttrsForEmit = new LinkedHashSet<Entity>());
			}
			neededAttrsForEmit.addAll(neededAttrs);
		}

		// Collect all entities with their attributes needed by evals
		for(Assignment ass : rule.getEvals()) {
			Entity entity = ass.getTarget().getOwner();
			if(entity instanceof Node)
				nodesNeededAsElements.add((Node) entity);
			else if(entity instanceof Edge)
				edgesNeededAsElements.add((Edge) entity);
			else
				throw new UnsupportedOperationException("Unsupported entity (" + entity + ")");

			collectNeededAttributes(ass.getTarget());
			collectNeededAttributes(ass.getExpression());
		}

		// Generate new nodes
		genRewriteNewNodes(sb2, reuseNodeAndEdges);

		// Generate node type changes
		for(Node node : replaceGraph.getNodes()) {
			if(!node.changesType()) continue;
			String new_type;
			RetypedNode rnode = node.getRetypedNode();

			if(rnode.inheritsType()) {
				Node typeofElem = (Node) getConcreteTypeofElem(rnode);
				new_type = formatEntity(typeofElem) + "_type";
				nodesNeededAsElements.add(typeofElem);
				nodesNeededAsTypes.add(typeofElem);
			} else {
				new_type = formatTypeClass(rnode.getType()) + ".typeVar";
			}

			nodesNeededAsElements.add(node);
			sb2.append("\t\t\tLGSPNode " + formatEntity(rnode) + " = graph.Retype("
						   + formatEntity(node) + ", " + new_type + ");\n");
			if(nodesNeededAsAttributes.contains(rnode) && accessViaInterface.contains(rnode)) {
				sb2.append("\t\t\t" + formatVarDeclWithCast(rnode.getType(), "I", "i" + formatEntity(rnode))
							   + formatEntity(rnode) + ";\n");
			}
		}

		// Generate new edges
		genRewriteNewEdges(sb2, rule, reuseNodeAndEdges);

		// Generate edge type changes
		for(Edge edge : replaceGraph.getEdges()) {
			if(!edge.changesType()) continue;
			String new_type;
			RetypedEdge redge = edge.getRetypedEdge();

			if(redge.inheritsType()) {
				Edge typeofElem = (Edge) getConcreteTypeofElem(redge);
				new_type = formatEntity(typeofElem) + "_type";
				edgesNeededAsElements.add(typeofElem);
				edgesNeededAsTypes.add(typeofElem);
			} else {
				new_type = formatTypeClass(redge.getType()) + ".typeVar";
			}

			edgesNeededAsElements.add(edge);
			sb2.append("\t\t\tLGSPEdge " + formatEntity(redge) + " = graph.Retype("
						   + formatEntity(edge) + ", " + new_type + ");\n");
			if(edgesNeededAsAttributes.contains(redge) && accessViaInterface.contains(redge)) {
				sb2.append("\t\t\t" + formatVarDeclWithCast(redge.getType(), "I", "i" + formatEntity(redge))
							   + formatEntity(redge) + ";\n");
			}
		}

		// Generate attribute re-calculations
		genEvals(sb3, rule);

		// Create variables for used attributes of non-reusees needed for emits
		for(Map.Entry<GraphEntity, HashSet<Entity>> entry : neededAttributesForEmit.entrySet()) {
			GraphEntity owner = entry.getKey();
			if(reusedElements.contains(owner)) continue;

			String grEntName = formatEntity(owner);
			for(Entity entity : entry.getValue()) {
				genVariable(sb3, grEntName, entity);
				sb3.append(" = ");
				genQualAccess(sb3, owner, entity);
				sb3.append(";\n");

				HashSet<Entity> forcedAttrs = forceAttributeToVar.get(owner);
				if(forcedAttrs == null)
					forceAttributeToVar.put(owner, forcedAttrs = new HashSet<Entity>());
				forcedAttrs.add(entity);
			}
		}

		// Check deleted elements for retyping through homomorphy
		for(Edge edge : delEdges) {
			if(!edge.isMaybeRetyped()) continue;

			String edgeName = formatEntity(edge);
			sb3.append("\t\t\tif(" + edgeName + ".ReplacedByEdge != null) "
					+ edgeName + " = " + edgeName + ".ReplacedByEdge;\n");
		}
		for(Node node : delNodes) {
			if(!node.isMaybeRetyped()) continue;

			String nodeName = formatEntity(node);
			sb3.append("\t\t\tif(" + nodeName + ".ReplacedByNode != null) "
					+ nodeName + " = " + nodeName + ".ReplacedByNode;\n");
		}

		// Remove edges
		for(Edge edge : delEdges) {
			if(reusedElements.contains(edge)) continue;
			edgesNeededAsElements.add(edge);
			sb3.append("\t\t\tgraph.Remove(" + formatEntity(edge) + ");\n");
		}

		// Remove nodes
		for(Node node : delNodes) {
			nodesNeededAsElements.add(node);
			sb3.append("\t\t\tgraph.RemoveEdges(" + formatEntity(node) + ");\n");
			sb3.append("\t\t\tgraph.Remove(" + formatEntity(node) + ");\n");
		}

		// Generate imperative statements
		int xgrsID = 0;
		for(ImperativeStmt istmt : replaceGraph.getImperativeStmts()) {
			if(istmt instanceof Emit) {
				Emit emit =(Emit) istmt;
				for(Expression arg : emit.getArguments()) {
					sb3.append("\t\t\tgraph.EmitWriter.Write(");
					genExpression(sb3, arg);
					sb3.append(");\n");
				}
			} else if (istmt instanceof Exec) {
				Exec exec = (Exec) istmt;
				sb3.append("\t\t\tApplyXGRS_" + xgrsID++ + "(graph");
				for(Entity param : exec.getArguments()) {
					if(param instanceof Variable) continue;
					sb3.append(", ");
					sb3.append(formatEntity(param));
				}
				sb3.append(");\n");
			} else assert false : "unknown ImperativeStmt: " + istmt + " in " + rule;
		}

		// Check returned elements for deletion or retyping due to homomorphy
		for(Entity ent : rule.getReturns()) {
			if(!(ent instanceof GraphEntity)) continue;

			GraphEntity grEnt = (GraphEntity) ent;
			if(grEnt.isMaybeRetyped()) {
				String elemName = formatEntity(grEnt);
				String kind = formatNodeOrEdge(grEnt);
				sb3.append("\t\t\tif(" + elemName + ".ReplacedBy" + kind + " != null) "
					+ elemName + " = " + elemName + ".ReplacedBy" + kind + ";\n");
			}
			if(grEnt.isMaybeDeleted())
				sb3.append("\t\t\tif(!" + formatEntity(ent) + ".Valid) " + formatEntity(ent) + " = null;\n");
		}

		// Emit return
		collectReturnElements(rule);
		emitReturnStatement(sb3, rule);

		// Emit end of function
		sb3.append("\t\t}\n");

		// nodes/edges needed from match, but not the new nodes
		nodesNeededAsElements.removeAll(newNodes);
		nodesNeededAsAttributes.removeAll(newNodes);
		edgesNeededAsElements.removeAll(newEdges);
		edgesNeededAsAttributes.removeAll(newEdges);

		//
		// Finalize method using the infos collected and the already generated code
		//

		extractElementsFromMatch(sb, patternName);

		// Generate needed types
		for(Node node : nodesNeededAsTypes) {
			String name = formatEntity(node);
			sb.append("\t\t\tNodeType " + name + "_type = " + name + ".type;\n");
		}
		for(Edge edge : edgesNeededAsTypes) {
			String name = formatEntity(edge);
			sb.append("\t\t\tEdgeType " + name + "_type = " + name + ".type;\n");
		}

		// Create variables for used attributes of reused elements
		for(Map.Entry<GraphEntity, HashSet<Entity>> entry : neededAttributes.entrySet()) {
			if(!reusedElements.contains(entry.getKey())) continue;

			String grEntName = formatEntity(entry.getKey());
			for(Entity entity : entry.getValue()) {
				String attrName = formatIdentifiable(entity);
				genVariable(sb, grEntName, entity);
				sb.append(" = i" + grEntName + ".@" + attrName + ";\n");
			}
		}

		// New nodes/edges (re-use), retype nodes/edges
		sb.append(sb2);

		// Attribute re-calc, attr vars for emit, remove, emit, return
		sb.append(sb3);
	}

	public void genAddedGraphElementsArray(StringBuffer sb, boolean isRule) {
		if(isRule) {
			genAddedGraphElementsArray(sb, true, newNodes);
			genAddedGraphElementsArray(sb, false, newEdges);
		} 
		else {
			sb.append("\t\tprivate static String[] addedNodeNames = new String[] {};\n");
			sb.append("\t\tpublic override String[] AddedNodeNames { get { return addedNodeNames; } }\n");
			sb.append("\t\tprivate static String[] addedEdgeNames = new String[] {};\n");
			sb.append("\t\tpublic override String[] AddedEdgeNames { get { return addedEdgeNames; } }\n");
		}
	}
	
	private void genAddedGraphElementsArray(StringBuffer sb, boolean isNode, Collection<? extends GraphEntity> set) {
		String NodesOrEdges = isNode?"Node":"Edge";
		sb.append("\t\tprivate static String[] added" + NodesOrEdges + "Names = new String[] ");
		genSet(sb, set, "\"", "\"", true);
		sb.append(";\n");
		sb.append("\t\tpublic override String[] Added" + NodesOrEdges
					  + "Names { get { return added" + NodesOrEdges + "Names; } }\n");
	}

	private void emitReturnStatement(StringBuffer sb, MatchingAction rule) {
		if(rule.getReturns().isEmpty())
			sb.append("\t\t\treturn EmptyReturnElements;\n");
		else {
			sb.append("\t\t\treturn new IGraphElement[] { ");
			for(Entity ent : rule.getReturns())
				sb.append(formatEntity(ent) + ", ");
			sb.append("};\n");
		}
	}

	private void extractElementsFromMatch(StringBuffer sb, String patternName) {
		for(Node node : nodesNeededAsElements) {
			if(node.isRetyped()) continue;
			sb.append("\t\t\tLGSPNode " + formatEntity(node)
					+ " = match.Nodes[(int) " + patternName + "_NodeNums.@"
					+ formatIdentifiable(node) + "];\n");
		}
		for(Node node : nodesNeededAsAttributes) {
			if(node.isRetyped()) continue;
			sb.append("\t\t\t" + formatVarDeclWithCast(node.getType(), "I", "i" + formatEntity(node)));
			if(nodesNeededAsElements.contains(node))
				sb.append(formatEntity(node) + ";\n");
			else
				sb.append("match.Nodes[(int) " + patternName + "_NodeNums.@"
						+ formatIdentifiable(node) + "];\n");
		}
		for(Edge edge : edgesNeededAsElements) {
			if(edge.isRetyped()) continue;
			sb.append("\t\t\tLGSPEdge " + formatEntity(edge)
					+ " = match.Edges[(int) " + patternName + "_EdgeNums.@"
					+ formatIdentifiable(edge) + "];\n");
		}
		for(Edge edge : edgesNeededAsAttributes) {
			if(edge.isRetyped()) continue;
			sb.append("\t\t\t" + formatVarDeclWithCast(edge.getType(), "I", "i" + formatEntity(edge)));
			if(edgesNeededAsElements.contains(edge))
				sb.append(formatEntity(edge) + ";\n");
			else
				sb.append("match.Edges[(int) " + patternName + "_EdgeNums.@"
						+ formatIdentifiable(edge) + "];\n");
		}
	}

	private void collectReturnElements(MatchingAction action) {
		for(Entity ent : action.getReturns()) {
			if(ent instanceof Node)
				nodesNeededAsElements.add((Node) ent);
			else if(ent instanceof Edge)
				edgesNeededAsElements.add((Edge) ent);
			else
				throw new IllegalArgumentException("unknown Entity: " + ent);
		}
	}

	/**
	 * Scans an expression for all read attributes and collects
	 * them in the neededAttributes hash map and their owners in
	 * the appropriate <kind>NeededAsAttributes hash set.
	 */
	private void collectNeededAttributes(Expression expr) {
		if(expr instanceof Operator) {
			Operator op = (Operator) expr;
			for(int i = 0; i < op.arity(); i++)
				collectNeededAttributes(op.getOperand(i));
		}
		else if(expr instanceof Qualification) {
			Qualification qual = (Qualification) expr;
			GraphEntity entity = (GraphEntity) qual.getOwner();
			HashSet<Entity> neededAttrs = neededAttributes.get(entity);
			if(neededAttrs == null) {
				neededAttributes.put(entity, neededAttrs = new LinkedHashSet<Entity>());
			}
			neededAttrs.add(qual.getMember());
			if(entity instanceof Node)
				nodesNeededAsAttributes.add((Node) entity);
			else if(entity instanceof Edge)
				edgesNeededAsAttributes.add((Edge) entity);
			else
				throw new UnsupportedOperationException("Unsupported entity (" + entity + ")");
		}
		else if(expr instanceof Cast) {
			Cast cast = (Cast) expr;
			collectNeededAttributes(cast.getExpression());
		}
	}
	
	////////////////////////////
	// New element generation //
	////////////////////////////

	private void genRewriteNewNodes(StringBuffer sb2, boolean reuseNodeAndEdges) {
		//reuseNodeAndEdges = false;							// TODO: reimplement this!!

		LinkedList<Node> tmpNewNodes = new LinkedList<Node>(newNodes);

		/* LinkedList<Node> tmpDelNodes = new LinkedList<Node>(delNodes);
		 if(reuseNodeAndEdges) {
		 NN: for(Iterator<Node> i = tmpNewNodes.iterator(); i.hasNext();) {
		 Node node = i.next();
		 // Can we reuse the node
		 for(Iterator<Node> j = tmpDelNodes.iterator(); j.hasNext();) {
		 Node delNode = j.next();
		 if(delNode.getNodeType() == node.getNodeType()) {
		 sb2.append("\t\t\tLGSPNode " + formatEntity(node) + " = " + formatEntity(delNode) + ";\n");
		 sb2.append("\t\t\tgraph.ReuseNode(" + formatEntity(delNode) + ", null);\n");
		 delNodes.remove(delNode);
		 j.remove();
		 i.remove();
		 nodesNeededAsElements.add(delNode);
		 reusedElements.add(delNode);
		 continue NN;
		 }
		 }
		 }
		 NN: for(Iterator<Node> i = tmpNewNodes.iterator(); i.hasNext();) {
		 Node node = i.next();
		 // Can we reuse the node
		 for(Iterator<Node> j = tmpDelNodes.iterator(); j.hasNext();) {
		 Node delNode = j.next();
		 if(!delNode.getNodeType().getAllMembers().isEmpty()) {
		 String type = computeGraphEntityType(node);
		 sb2.append("\t\t\tLGSPNode " + formatEntity(node) + " = " + formatEntity(delNode) + ";\n");
		 sb2.append("\t\t\tgraph.ReuseNode(" + formatEntity(delNode) + ", " + type + ");\n");
		 delNodes.remove(delNode);
		 j.remove();
		 i.remove();
		 nodesNeededAsElements.add(delNode);
		 reusedElements.add(delNode);
		 continue NN;
		 }
		 }
		 }
		 }
		 NN:*/ for(Iterator<Node> i = tmpNewNodes.iterator(); i.hasNext();) {
			Node node = i.next();

			/*String type = computeGraphEntityType(node);
			 // Can we reuse the node
			 if(reuseNodeAndEdges && !tmpDelNodes.isEmpty()) {
			 Node delNode = tmpDelNodes.getFirst();
			 sb2.append("\t\t\tLGSPNode " + formatEntity(node) + " = " + formatEntity(delNode) + ";\n");
			 sb2.append("\t\t\tgraph.ReuseNode(" + formatEntity(delNode) + ", " + type + ");\n");
			 delNodes.remove(delNode);
			 tmpDelNodes.removeFirst();
			 i.remove();
			 nodesNeededAsElements.add(delNode);
			 reusedElements.add(delNode);
			 continue NN;
			 }*/
			if(node.inheritsType()) {
				Node typeofElem = (Node) getConcreteTypeofElem(node);
				nodesNeededAsElements.add(typeofElem);
				nodesNeededAsTypes.add(typeofElem);
				sb2.append("\t\t\tLGSPNode " + formatEntity(node) + " = (LGSPNode) "
							   + formatEntity(typeofElem) + "_type.CreateNode();\n"
							   + "\t\t\tgraph.AddNode(" + formatEntity(node) + ");\n");
				if(nodesNeededAsAttributes.contains(node) && accessViaInterface.contains(node)) {
					sb2.append("\t\t\t" + formatVarDeclWithCast(node.getType(), "I", "i" + formatEntity(node))
								   + formatEntity(node) + ";\n");
				}
			}
			else {
				String etype = formatElementClass(node.getType());
				sb2.append("\t\t\t" + etype + " " + formatEntity(node) + " = " + etype + ".CreateNode(graph);\n");
			}
		}
	}

	/**
	 * Returns the iterated inherited type element for a given element
	 * or null, if the given element does not inherit its type from another element.
	 */
	private GraphEntity getConcreteTypeofElem(GraphEntity elem) {
		GraphEntity typeofElem = elem;
		while(typeofElem.inheritsType())
			typeofElem = typeofElem.getTypeof();
		return typeofElem == elem ? null : typeofElem;
	}

	// TODO use or remove it
	/*private String computeGraphEntityType(Node node) {
	 String type;
	 if(node.inheritsType()) {
	 Node typeofElem = (Node) getConcreteTypeof(node);
	 type = formatEntity(typeofElem) + "_type";
	 nodesNeededAsElements.add(typeofElem);
	 nodesNeededAsTypes.add(typeofElem);
	 } else {
	 type = formatTypeClass(node.getType()) + ".typeVar";
	 }
	 return type;
	 }*/

	private void genRewriteNewEdges(StringBuffer sb2, Rule rule, boolean reuseNodeAndEdges) {
		PatternGraph leftSide = rule.getLeft();
		PatternGraph rightSide = rule.getRight();

		NE:	for(Edge edge : newEdges) {
			String etype = formatElementClass(edge.getType());

			Node src_node = rightSide.getSource(edge);
			Node tgt_node = rightSide.getTarget(edge);

			if(src_node.changesType()) src_node = src_node.getRetypedNode();
			if(tgt_node.changesType()) tgt_node = tgt_node.getRetypedNode();

			if(commonNodes.contains(src_node))
				nodesNeededAsElements.add(src_node);

			if(commonNodes.contains(tgt_node))
				nodesNeededAsElements.add(tgt_node);

			if(edge.inheritsType()) {
				Edge typeofElem = (Edge) getConcreteTypeofElem(edge);
				edgesNeededAsElements.add(typeofElem);
				edgesNeededAsTypes.add(typeofElem);

				sb2.append("\t\t\tLGSPEdge " + formatEntity(edge) + " = (LGSPEdge) "
							   + formatEntity(typeofElem) + "_type.CreateEdge("
							   + formatEntity(src_node) + ", " + formatEntity(tgt_node) + ");\n"
							   + "\t\t\tgraph.AddEdge(" + formatEntity(edge) + ");\n");
				if(edgesNeededAsAttributes.contains(edge) && accessViaInterface.contains(edge)) {
					sb2.append("\t\t\t" + formatVarDeclWithCast(edge.getType(), "I", "i" + formatEntity(edge))
								   + formatEntity(edge) + ";\n");
				}
				continue;
			}
			else if(reuseNodeAndEdges) {
				Edge bestDelEdge = null;
				int bestPoints = -1;

				// Can we reuse a deleted edge of the same type?
				for(Edge delEdge : delEdges) {
					if(reusedElements.contains(delEdge)) continue;
					if(delEdge.getType() != edge.getType()) continue;

					int curPoints = 0;
					if(leftSide.getSource(delEdge) == src_node)
						curPoints++;
					if(leftSide.getTarget(delEdge) == tgt_node)
						curPoints++;
					if(curPoints > bestPoints) {
						bestPoints = curPoints;
						bestDelEdge = delEdge;
						if(curPoints == 2) break;
					}
				}

				if(bestDelEdge != null) {
					// We may be able to reuse the edge instead of deleting it,
					// if the runtime type of the reused edge has the exact type.
					// This is a veritable performance optimization, as object creation is costly.

					String newEdgeName = formatEntity(edge);
					String reusedEdgeName = formatEntity(bestDelEdge);
					String src = formatEntity(src_node);
					String tgt = formatEntity(tgt_node);

					sb2.append("\t\t\t" + etype + " " + newEdgeName + ";\n"
								   + "\t\t\tif(" + reusedEdgeName + ".type == "
								   + formatTypeClass(edge.getType()) + ".typeVar)\n"
								   + "\t\t\t{\n"
								   + "\t\t\t\t// re-using " + reusedEdgeName + " as " + newEdgeName + "\n"
								   + "\t\t\t\t" + newEdgeName + " = (" + etype + ") " + reusedEdgeName + ";\n"
								   + "\t\t\t\tgraph.ReuseEdge(" + reusedEdgeName + ", ");

					if(leftSide.getSource(bestDelEdge) != src_node)
						sb2.append(src + ", ");
					else
						sb2.append("null, ");

					if(leftSide.getTarget(bestDelEdge) != tgt_node)
						sb2.append(tgt + "");
					else
						sb2.append("null");

					// If the runtime type does not match, delete the edge and create a new one
					sb2.append(");\n"
								   + "\t\t\t}\n"
								   + "\t\t\telse\n"
								   + "\t\t\t{\n"
								   + "\t\t\t\tgraph.Remove(" + reusedEdgeName + ");\n"
								   + "\t\t\t\t" + newEdgeName + " = " + etype
								   + ".CreateEdge(graph, " + src + ", " + tgt + ");\n"
								   + "\t\t\t}\n");

					edgesNeededAsElements.add(bestDelEdge);
					reusedElements.add(bestDelEdge);
					continue NE;
				}
			}

			// Create the edge
			sb2.append("\t\t\t" + etype + " " + formatEntity(edge) + " = " + etype
						   + ".CreateEdge(graph, " + formatEntity(src_node)
						   + ", " + formatEntity(tgt_node) + ");\n");
		}
	}

	//////////////////////////
	// Eval part generation //
	//////////////////////////

	private void genEvals(StringBuffer sb, Rule rule) {
		boolean def_b = false, def_i = false, def_s = false, def_f = false, def_d = false, def_o = false;
		for(Assignment ass : rule.getEvals()) {
			String varName, varType;
			Entity entity = ass.getTarget().getOwner();

			switch(ass.getTarget().getType().classify()) {
				case Type.IS_BOOLEAN:
					varName = "var_b";
					varType = def_b?"":"bool ";
					def_b = true;
					break;
				case Type.IS_INTEGER:
					varName = "var_i";
					varType = def_i?"":"int ";
					def_i = true;
					break;
				case Type.IS_FLOAT:
					varName = "var_f";
					varType = def_f?"":"float ";
					def_f = true;
					break;
				case Type.IS_DOUBLE:
					varName = "var_d";
					varType = def_d?"":"double ";
					def_d = true;
					break;
				case Type.IS_STRING:
					varName = "var_s";
					varType = def_s?"":"String ";
					def_s = true;
					break;
				case Type.IS_OBJECT:
					varName = "var_o";
					varType = def_o?"":"Object ";
					def_o = true;
					break;
				default:
					throw new IllegalArgumentException();
			}

			sb.append("\t\t\t" + varType + varName + " = ");
			if(ass.getTarget().getType() instanceof EnumType)
				sb.append("(int) ");
			genExpression(sb, ass.getExpression());
			sb.append(";\n");

			String kindStr = null;
			boolean isDeletedElem = false;
			if(entity instanceof Node) {
				kindStr = "Node";
				isDeletedElem = delNodes.contains(entity);
			}
			else if(entity instanceof Edge) {
				kindStr = "Edge";
				isDeletedElem = delEdges.contains(entity);
			}
			else assert false : "Entity is neither a node nor an edge (" + entity + ")!";

			if(!isDeletedElem) {
				sb.append("\t\t\tgraph.Changing" + kindStr + "Attribute(" + formatEntity(entity) +
							  ", " + kindStr + "Type_" + formatIdentifiable(ass.getTarget().getMember().getOwner()) +
							  ".AttributeType_" + formatIdentifiable(ass.getTarget().getMember()) + ", ");
				genExpression(sb, ass.getTarget());
				sb.append(", " + varName + ");\n");
			}

			sb.append("\t\t\t");
			genExpression(sb, ass.getTarget());
			sb.append(" = ");
			if(ass.getTarget().getType() instanceof EnumType)
				sb.append("(ENUM_" + formatIdentifiable(ass.getTarget().getType()) + ") ");
			sb.append(varName + ";\n");
		}
	}

	//////////////////////
	// Expression stuff //
	//////////////////////

	protected void genQualAccess(StringBuffer sb, Qualification qual) {
		Entity owner = qual.getOwner();
		Entity member = qual.getMember();
		genQualAccess(sb, owner, member);
	}

	protected void genQualAccess(StringBuffer sb, Entity owner, Entity member) {
		if(accessViaVariable((GraphEntity) owner, member)) {
			sb.append("var_" + formatEntity(owner) + "_" + formatIdentifiable(member));
		}
		else {
			if(accessViaInterface.contains(owner))
				sb.append("i");

			sb.append(formatEntity(owner) + ".@" + formatIdentifiable(member));
		}
	}

	protected void genMemberAccess(StringBuffer sb, Entity member) {
		throw new UnsupportedOperationException("Member expressions not allowed in actions!");
	}

	private boolean accessViaVariable(GraphEntity elem, Entity attr) {
		if(!reusedElements.contains(elem)) {
			HashSet<Entity> forcedAttrs = forceAttributeToVar.get(elem);
			return forcedAttrs != null && forcedAttrs.contains(attr);
		}
		HashSet<Entity> neededAttrs = neededAttributes.get(elem);
		return neededAttrs != null && neededAttrs.contains(attr);
	}

	private void genVariable(StringBuffer sb, String ownerName, Entity entity) {
		String varTypeName;
		String attrName = formatIdentifiable(entity);
		Type type = entity.getType();
		if(type instanceof EnumType)
			varTypeName = "ENUM_" + formatIdentifiable(type);
		else {
			switch(type.classify()) {
				case Type.IS_BOOLEAN:
					varTypeName = "bool";
					break;
				case Type.IS_INTEGER:
					varTypeName = "int";
					break;
				case Type.IS_FLOAT:
					varTypeName = "float";
					break;
				case Type.IS_DOUBLE:
					varTypeName = "double";
					break;
				case Type.IS_STRING:
					varTypeName = "String";
					break;
				case Type.IS_OBJECT:
				case Type.IS_UNKNOWN:
					varTypeName = "Object";
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		sb.append("\t\t\t" + varTypeName + " var_" + ownerName + "_" + attrName);
	}

	///////////////////////
	// Private variables //
	///////////////////////

	private HashSet<Node> newNodes;
	private HashSet<Edge> newEdges;
	private HashSet<Node> delNodes;
	private HashSet<Edge> delEdges;
	private Collection<Node> commonNodes;
	private Collection<Edge> commonEdges;
	private HashSet<Node> newOrRetypedNodes;
	private HashSet<Edge> newOrRetypedEdges;
	private HashSet<GraphEntity> reusedElements = new HashSet<GraphEntity>();
	private HashSet<GraphEntity> accessViaInterface = new HashSet<GraphEntity>();

	private HashMap<GraphEntity, HashSet<Entity>> neededAttributes = new LinkedHashMap<GraphEntity, HashSet<Entity>>();
	private HashMap<GraphEntity, HashSet<Entity>> neededAttributesForEmit = new LinkedHashMap<GraphEntity, HashSet<Entity>>();

	private HashSet<Node> nodesNeededAsElements = new LinkedHashSet<Node>();
	private HashSet<Edge> edgesNeededAsElements = new LinkedHashSet<Edge>();
	private HashSet<Node> nodesNeededAsAttributes = new LinkedHashSet<Node>();
	private HashSet<Edge> edgesNeededAsAttributes = new LinkedHashSet<Edge>();
	private HashSet<Node> nodesNeededAsTypes = new LinkedHashSet<Node>();
	private HashSet<Edge> edgesNeededAsTypes = new LinkedHashSet<Edge>();

	private HashMap<GraphEntity, HashSet<Entity>> forceAttributeToVar = new LinkedHashMap<GraphEntity, HashSet<Entity>>();
}
