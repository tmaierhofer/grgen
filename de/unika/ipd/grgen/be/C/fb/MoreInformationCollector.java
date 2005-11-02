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
 * Extracts all the information needed by the FrameBasedBackend
 * from the GrGen-internal IR
 *
 * @author Adam Szalkowski
 * @version $Id$
 */

package de.unika.ipd.grgen.be.C.fb;
import de.unika.ipd.grgen.ir.*;
import java.util.*;
import de.unika.ipd.grgen.util.Attributes;

public class MoreInformationCollector extends InformationCollector {
	
	/* maps an eval list to the action_id it belong to */
	protected Map<Collection, Integer> evalListMap = new HashMap<Collection, Integer>();
	
	/* replacement and pattern edges involved in an eval */
	protected Map<Collection, Collection> evalInvolvedNodes = new HashMap<Collection, Collection>();
	protected Map<Collection, Collection> evalInvolvedEdges = new HashMap<Collection, Collection>();
	
	/* maps action id to eval list */
	protected Map<Collection, Action> evalActions = new HashMap<Collection, Action>();
	
	/* edge and node attributes involved in that eval */
	protected Map[] involvedEvalNodeAttrIds;
	protected Map[] involvedEvalEdgeAttrIds;
	
	//returns id of corresponding pattern edge id if edge is kept
	//else -1 if edge is new one
	//usage: replacementEdgeIsPresevedNode[act_id][replacement_edge_num]
	protected int replacementEdgeIsPreservedEdge[][];
	
	//returns id of corresponding replacement edge id if edge is kept
	//else -1 if edge is to be deleted
	//usage: patternEdgeIsToBeKept[act_id][pattern_edge_num]
	protected int patternEdgeIsToBeKept[][];
	
	private static final int min_subgraph_size = 4;
	
	
	/*
	 * collect some information about evals
	 *
	 */
	protected void collectEvalInfo() {
		involvedEvalNodeAttrIds = new Map[ actionMap.size() ];
		involvedEvalEdgeAttrIds = new Map[ actionMap.size() ];
		
		for(Iterator<Action> it = actionMap.keySet().iterator(); it.hasNext(); ) {
			//get the current action
			Action act = it.next();
			
			if (act instanceof Rule) {
				Rule rule = (Rule) act;
				Integer act_id = actionMap.get(act);
				
				Collection<Object> rule_evals = rule.getEvals();
				
				evalListMap.put( rule_evals, act_id );
				evalActions.put( rule_evals, act );
				
				Collection<Entity> involvedNodes = new HashSet<Entity>();
				Collection<Entity> involvedEdges = new HashSet<Entity>();
				involvedEvalNodeAttrIds[ act_id.intValue() ] = new HashMap();
				involvedEvalEdgeAttrIds[ act_id.intValue() ] = new HashMap();
				
				for(  Iterator<Object> it2 = rule_evals.iterator(); it2.hasNext(); ) {
					Assignment eval = (Assignment)it2.next();
					Qualification target = eval.getTarget();
					Expression expr = eval.getExpression();
					
					/* generate an expression that consists of both parts of the Assignment to use the already implemented methods for gathering InvolvedNodes/Edges etc. */
					Operator op = new Operator( (PrimitiveType)target.getType(), Operator.EQ );
					op.addOperand( target );
					op.addOperand( expr );
					
					//...extract the pattern nodes and edges involved in the evaluation
					involvedNodes.addAll( collectInvolvedNodes( op ) );
					involvedEdges.addAll( collectInvolvedEdges( op ) );
					
					/* for all evaluations the pairs (pattern_node_num, attr_id), which occur
					 in qualifications at the leaves of the eval, are needed.
					 To obtain that compute a map
					 act_id -> pattern_node_num_ -> { attr_ids }
					 implemented by an Array of Maps; usage is:
					 involvedPatternNodeAttrIds[act_id].get(pattern_node_num)
					 which yields a Collection of attr-ids.
					 */
					
					//collect the attr ids in dependency of evaluation and the pattern node
					
					//descent to the conditions leaves and look for qualifications
					__recursive_qual_collect( act_id.intValue(),
											 involvedEvalNodeAttrIds[ act_id.intValue() ],
											 involvedEvalEdgeAttrIds[ act_id.intValue() ],
											 op );
				}
				
				//add Collections of involved Nodes/Edges to prepared Maps
				evalInvolvedNodes.put( rule_evals, involvedNodes );
				evalInvolvedEdges.put( rule_evals, involvedEdges );
			}
		}
	}
	
	/**
	 * Method collectReplacementEdgeIsPreservedEdgeInfo
	 *
	 */
	private void collectReplacementEdgeIsPreservedEdgeInfo() {
		replacementEdgeIsPreservedEdge =
			new int[n_graph_actions][max_n_replacement_edges];
		
		//init the array with -1
		for (int i = 0; i < n_graph_actions; i++)
			for (int j = 0; j < max_n_replacement_edges; j++)
				replacementEdgeIsPreservedEdge[i][j] = -1;
		
		//for all edges preserved set the corresponding array entry to the
		//appropriate pattern edge number
		Iterator<Action> act_it = actionMap.keySet().iterator();
		for ( ; act_it.hasNext() ; ) {
			MatchingAction action = (MatchingAction) act_it.next();
			int act_id = actionMap.get(action).intValue();
			
			if (action instanceof Rule) {
				//compute the set of replacement edges preserved by this action
				Collection<IR> replacement_edges_preserved = new HashSet<IR>();
				replacement_edges_preserved.addAll(
													  ((Rule) action).getRight().getEdges() );
				replacement_edges_preserved.retainAll(action.getPattern().getEdges());
				//for all those preserved replacement edges store the
				//corresponding pattern edge
				Iterator<IR> preserved_edge_it =
					replacement_edges_preserved.iterator();
				for ( ; preserved_edge_it.hasNext() ; ) {
					Edge edge = (Edge) preserved_edge_it.next();
					int edge_num =
						((Integer) replacement_edge_num[act_id].get(edge)).intValue();
					replacementEdgeIsPreservedEdge[act_id][edge_num] =
						((Integer) pattern_edge_num[act_id].get(edge)).intValue();
				}
			}
		}
	}
	
	/**
	 * Method collectPatternEdgesToBeKeptInfo
	 *
	 */
	private void collectPatternEdgesToBeKeptInfo() {
		patternEdgeIsToBeKept = new int[n_graph_actions][max_n_pattern_edges];
		
		//init the arrays with -1
		for (int i = 0; i < n_graph_actions; i++)
			for (int j = 0; j < max_n_pattern_edges; j++)
				patternEdgeIsToBeKept[i][j] = -1;
		
		//for all edges to be kept set the corresponding array entry to the
		//appropriate replacement edge number
		Iterator<Action> act_it = actionMap.keySet().iterator();
		for ( ; act_it.hasNext() ; ) {
			MatchingAction action = (MatchingAction) act_it.next();
			int act_id = actionMap.get(action).intValue();
			
			//compute the set of pattern edges to be kept for this action
			Collection<IR> pattern_edges_to_keep = new HashSet<IR>();
			pattern_edges_to_keep.addAll(action.getPattern().getEdges());
			if (action instanceof Rule) {
				Graph replacement = ((Rule)action).getRight();
				pattern_edges_to_keep.retainAll(replacement.getEdges());
				//iterate over the pattern edges to be kept and store their
				//corresponding replacement edge number
				Iterator<IR> kept_edge_it = pattern_edges_to_keep.iterator();
				for ( ; kept_edge_it.hasNext() ; ) {
					Edge edge = (Edge) kept_edge_it.next();
					int edge_num =
						((Integer) pattern_edge_num[act_id].get(edge)).intValue();
					patternEdgeIsToBeKept[act_id][edge_num] =
						((Integer) replacement_edge_num[act_id].get(edge)).intValue();
				}
			}
		}
	}
	
	protected int max_n_negative_nodes = 0;
	protected int max_n_negative_edges = 0;
	protected int max_n_negative_patterns = 0;
	protected int n_negative_patterns[];
	protected Map[][] negative_node_num;
	protected Map[][] negative_edge_num;
	protected Map[] negMap;
	
	protected int patternNodeIsNegativeNode[][][];
	protected int patternEdgeIsNegativeEdge[][][];
	
	
	private void collectNegativeInfo() {
		/* get the overall maximum numbers of nodes and edges of all pattern
		 and replacement graphs respaectively */
		max_n_negative_nodes = 0;
		max_n_negative_edges = 0;
		max_n_negative_patterns = 0;
		
		n_negative_patterns = new int[n_graph_actions];
		negMap = new Map[n_graph_actions];
		
		for(Iterator<Action> it = actionMap.keySet().iterator(); it.hasNext(); ) {
			//get the current action
			Action act = it.next();
			int act_id = actionMap.get(act).intValue();
			int negs = 0;
			
			negMap[act_id] = new HashMap();
			
			//check wether its graphs node and edge set sizes are greater
			if (act instanceof MatchingAction) {
				int size;
				
				for(Graph negPattern : ((MatchingAction)act).getNegs()) {
					
					negMap[act_id].put(negPattern, new Integer(negs++));
					
					size = negPattern.getNodes().size();
					if (size > max_n_negative_nodes)	max_n_negative_nodes = size;
					size = negPattern.getEdges().size();
					if (size > max_n_negative_edges) max_n_negative_edges = size;
				}
			}
			n_negative_patterns[act_id] = negs;
			if (negs > max_n_negative_patterns) max_n_negative_patterns = negs;
		}
		
		
		/* compute the numbers of nodes/edges of all negative-pattern-graphs */
		negative_node_num = new Map[n_graph_actions][max_n_negative_patterns];
		negative_edge_num = new Map[n_graph_actions][max_n_negative_patterns];
		
		for(Iterator<Action> it = actionMap.keySet().iterator(); it.hasNext(); ) {
			/* get the current action*/
			Action act = it.next();
			int act_id = actionMap.get(act).intValue();
			
			/* if action has negative pattern graphs, compute node/edge numbers */
			if (act instanceof MatchingAction) {
				
				for(Iterator<PatternGraph> neg_it = negMap[act_id].keySet().iterator(); neg_it.hasNext(); ) {
					PatternGraph neg_pattern = neg_it.next();
					
					int neg_num = ((Integer)negMap[act_id].get(neg_pattern)).intValue();
					negative_node_num[act_id][neg_num] = new HashMap();
					negative_edge_num[act_id][neg_num] = new HashMap();
					
					/* fill the map with pairs (node, node_num) */
					int node_num = 0;
					Iterator node_it =
						neg_pattern.getNodes().iterator();
					for ( ; node_it.hasNext(); ) {
						Node node = (Node) node_it.next();
						negative_node_num[act_id][neg_num].put(node, new Integer(node_num++));
					}
					assert node_num == neg_pattern.getNodes().size():
						"Wrong number of node_nums was created";
					
					/* fill the map with pairs (edge, edge_num) */
					int edge_num = 0;
					Iterator edge_it =
						neg_pattern.getEdges().iterator();
					for ( ; edge_it.hasNext(); ) {
						Edge edge = (Edge) edge_it.next();
						negative_edge_num[act_id][neg_num].put(edge, new Integer(edge_num++));
					}
					assert edge_num == neg_pattern.getEdges().size():
						"Wrong number of edge_nums was created";
				}
			} else {
				//negative_node_num[act_id][neg_num] = null;
				//negative_edge_num[act_id][neg_num] = null;
			}
		}
	}
	
	protected void collectPatternNodeIsNegativeNodeInfo() {
		patternNodeIsNegativeNode =
			new int[n_graph_actions][max_n_negative_patterns][max_n_pattern_nodes];
		
		//init the array with -1
		for (int i = 0; i < n_graph_actions; i++)
			for (int j = 0; j < max_n_negative_patterns; j++)
				for (int k = 0; k < max_n_pattern_nodes; k++)
					patternNodeIsNegativeNode[i][j][k] = -1;
		
		//for all negative patterns insert the correspondig negative node numbers
		//for the pattern nodes that are also present in that negative pattern
		Iterator<Action> act_it = actionMap.keySet().iterator();
		for ( ; act_it.hasNext() ; ) {
			MatchingAction action = (MatchingAction) act_it.next();
			int act_id = actionMap.get(action).intValue();
			
			if (action instanceof MatchingAction) {
				
				for(Iterator<PatternGraph> neg_it = negMap[act_id].keySet().iterator(); neg_it.hasNext(); ) {
					PatternGraph neg_pattern = neg_it.next();
					
					int neg_num = ((Integer)negMap[act_id].get(neg_pattern)).intValue();
					
					Collection<IR> negatives_also_in_pattern = new HashSet<IR>();
					negatives_also_in_pattern.addAll( neg_pattern.getNodes() );
					negatives_also_in_pattern.retainAll( action.getPattern().getNodes() );
					
					for(Iterator<IR> neg_patt_it = negatives_also_in_pattern.iterator(); neg_patt_it.hasNext(); ) {
						Node node = (Node) neg_patt_it.next();
						int node_num = ((Integer) pattern_node_num[act_id].get(node)).intValue();
						
						patternNodeIsNegativeNode[act_id][neg_num][node_num] =
							((Integer)negative_node_num[act_id][neg_num].get(node)).intValue();
					}
				}
			}
		}
	}
	
	protected void collectPatternEdgeIsNegativeEdgeInfo() {
		patternEdgeIsNegativeEdge =
			new int[n_graph_actions][max_n_negative_patterns][max_n_pattern_edges];
		
		//init the array with -1
		for (int i = 0; i < n_graph_actions; i++)
			for (int j = 0; j < max_n_negative_patterns; j++)
				for (int k = 0; k < max_n_pattern_edges; k++)
					patternEdgeIsNegativeEdge[i][j][k] = -1;
		
		//for all negative patterns insert the correspondig negative edge numbers
		//for the pattern edges that are also present in that negative pattern
		Iterator<Action> act_it = actionMap.keySet().iterator();
		for ( ; act_it.hasNext() ; ) {
			MatchingAction action = (MatchingAction) act_it.next();
			int act_id = actionMap.get(action).intValue();
			
			if (action instanceof MatchingAction) {
				
				for(Iterator<PatternGraph> neg_it = negMap[act_id].keySet().iterator(); neg_it.hasNext(); ) {
					PatternGraph neg_pattern = neg_it.next();
					
					int neg_num = ((Integer)negMap[act_id].get(neg_pattern)).intValue();
					
					Collection<IR> negatives_also_in_pattern = new HashSet<IR>();
					negatives_also_in_pattern.addAll( neg_pattern.getEdges() );
					negatives_also_in_pattern.retainAll( action.getPattern().getEdges() );
					
					for(Iterator<IR> neg_patt_it = negatives_also_in_pattern.iterator(); neg_patt_it.hasNext(); ) {
						Edge edge = (Edge) neg_patt_it.next();
						int edge_num = ((Integer) pattern_edge_num[act_id].get(edge)).intValue();
						
						patternEdgeIsNegativeEdge[act_id][neg_num][edge_num] =
							((Integer)negative_edge_num[act_id][neg_num].get(edge)).intValue();
					}
				}
			}
		}
	}
	
	protected Map<Collection, Integer> typeConditionsPatternNum = new HashMap<Collection, Integer>();
	protected Map<Expression, Integer> conditionsPatternNum = new HashMap<Expression, Integer>();
	
	/* it is a little bit stupid to do this again. so merge it into InformationCollector if it really works */
	protected void collectNegativePatternConditionsInfo() {
		
		//init a subexpression counter
		int subConditionCounter = n_conditions;
		
		//iterate over all actions
		for(Iterator<Action> it = actionMap.keySet().iterator(); it.hasNext(); ) {
			//get the current action
			Action act = it.next();
			int act_id = actionMap.get(act).intValue();
			
			if (act instanceof MatchingAction) {
				
				//iterate over negative patterns
				for(Iterator<PatternGraph> neg_it = negMap[ act_id ].keySet().iterator(); neg_it.hasNext(); ) {
					PatternGraph neg_pattern = neg_it.next();
					int neg_num = ((Integer)negMap[act_id].get(neg_pattern)).intValue();
					
					//iterate over all conditions of the current action
					Iterator condition_it =
						neg_pattern.getConditions().iterator();
					for ( ; condition_it.hasNext(); ) {
						
						// divide the expression to all AND-connected parts, which do
						//not have an AND-Operator as root themselves
						Expression condition = (Expression) condition_it.next();
						Collection<Expression> subConditions = decomposeAndParts(condition);
						
						//for all the subcinditions just computed...
						for ( Iterator<Expression> sub_cond_it = subConditions.iterator(); sub_cond_it.hasNext(); ) {
							Expression sub_condition = sub_cond_it.next();
							
							assert conditionNumbers.get(sub_condition) == null;
							
							//...create condition numbers
							conditionNumbers.put(sub_condition, new Integer(subConditionCounter++));
							
							//...extract the pattern nodes and edges involved in the condition
							Collection<Entity> involvedNodes = collectInvolvedNodes(sub_condition);
							Collection<Entity> involvedEdges = collectInvolvedEdges(sub_condition);
							//and at these Collections to prepared Maps
							conditionsInvolvedNodes.put(sub_condition, involvedNodes);
							conditionsInvolvedEdges.put(sub_condition, involvedEdges);
							
							//...store the id of the action that condition belogs to
							conditionsActionId.put(sub_condition, actionMap.get(act));
							
							//...store the action the condition belongs to
							conditionsAction.put(sub_condition, act);
							
							//..store the negative pattern num the conditions belongs to
							conditionsPatternNum.put(sub_condition, new Integer(neg_num+1));
							
							//store the subcondition in an ordered Collection
							conditions.add(sub_condition);
						}
					}
				}
			}
		}
		//store the overall number of (sub)conditions
		n_conditions = subConditionCounter;
		
		
		
		
		/* for all conditions (not type conditions!) the pairs
		 (pattern_node_num, attr_id), which occur
		 in qualifications at the leaves of the condition, are needed.
		 To obtain that compute a map
		 condition_num -> pattern_node_num_ -> { attr_ids }
		 implemented by an Array of Maps; usage is:
		 involvedPatternNodeAttrIds[cond_num].get(pattern_node_num)
		 which yields a Collection of attr-ids.
		 */
		involvedPatternNodeAttrIds = new Map[n_conditions];
		involvedPatternEdgeAttrIds = new Map[n_conditions];
		//init that Arrays with empty maps
		for (Iterator<Expression> it = conditions.iterator(); it.hasNext(); ) {
			Expression cond = it.next();
			int cond_num = conditionNumbers.get(cond).intValue();
			involvedPatternNodeAttrIds[cond_num] = new HashMap();
			involvedPatternEdgeAttrIds[cond_num] = new HashMap();
		}
		//collect the attr ids in dependency of condition and the pattern node
		for (Iterator<Expression> it = conditions.iterator(); it.hasNext(); ) {
			Expression cond = it.next();
			int cond_num = conditionNumbers.get(cond).intValue();
			int act_id = conditionsActionId.get(cond).intValue();
			
			//descent to the conditions leafes and look for qualifications
			Map<Entity, Collection> node_map = new HashMap<Entity, Collection>();
			Map<Entity, Collection> edge_map = new HashMap<Entity, Collection>();
			__recursive_qual_collect(act_id, node_map, edge_map, cond);
			involvedPatternNodeAttrIds[cond_num] = node_map;
			involvedPatternEdgeAttrIds[cond_num] = edge_map;
		}
	}
	
	protected void collectNegativePatternTypeConditionsInfo() {
		/* collect the type constaraints of the node of all actions pattern graphs */
		int typeConditionCounter = n_conditions;
		
		for(Iterator<Action> it = actionMap.keySet().iterator(); it.hasNext(); ) {
			//get the current action
			Action act = it.next();
			int act_id = actionMap.get(act).intValue();
			
			if (act instanceof MatchingAction) {
				
				//iterate over negative patterns
				for(Iterator<PatternGraph> neg_it = negMap[ act_id ].keySet().iterator(); neg_it.hasNext(); ) {
					PatternGraph neg_pattern = neg_it.next();
					int neg_num = ((Integer)negMap[act_id].get(neg_pattern)).intValue();
					
					/* for all nodes of the current MatchingActions negative pattern graphs
					 extract that nodes type constraints */
					Iterator pattern_node_it = neg_pattern.getNodes().iterator();
					for ( ; pattern_node_it.hasNext() ; ) {
						Node node = (Node) pattern_node_it.next();
						
						//if node has type costraints, register the as conditions
						if (! node.getConstraints().isEmpty()) {
							
							//note that a type condition is the set of all types,
							//the corresponding node/edge is not allowed to be of
							Collection type_condition = node.getConstraints();
							
							//...create condition numbers
							typeConditionNumbers.put(type_condition, new Integer(typeConditionCounter++));
							
							//...extract the pattern nodes and edges involved in the condition
							Collection<Node> involvedNodes = new HashSet<Node>();
							involvedNodes.add(node);
							//and at these Collections to prepared Maps
							typeConditionsInvolvedNodes.put(type_condition, involvedNodes);
							typeConditionsInvolvedEdges.put(type_condition, Collections.EMPTY_SET);
							
							//...store the id of the action that condition belogs to
							typeConditionsActionId.put(type_condition, actionMap.get(act));
							
							//...store the action the condition belongs to
							typeConditionsAction.put(type_condition, act);
							
							//..store the negative pattern num the conditions belongs to
							typeConditionsPatternNum.put(type_condition, new Integer(neg_num+1));
							
							//store the subcondition in an ordered Collection
							typeConditions.add(type_condition);
						}
					}
					//do the same thing for all edges of the current pattern
					Iterator pattern_edge_it = neg_pattern.getEdges().iterator();
					for ( ; pattern_edge_it.hasNext() ; ) {
						Edge edge = (Edge) pattern_edge_it.next();
						
						//if node has type costraints, register the as conditions
						if (! edge.getConstraints().isEmpty()) {
							
							//note that a type condition is the set of all types,
							//the corresponding edge is not allowed to be of
							Collection type_condition = edge.getConstraints();
							
							//...create condition numbers
							typeConditionNumbers.put(type_condition, new Integer(typeConditionCounter++));
							
							//...extract the pattern edges and edges involved in the condition
							Collection<Edge> involvedEdges = new HashSet<Edge>();
							involvedEdges.add(edge);
							//and at these Collections to prepared Maps
							typeConditionsInvolvedNodes.put(type_condition, Collections.EMPTY_SET);
							typeConditionsInvolvedEdges.put(type_condition, involvedEdges);
							
							//...store the id of the action that condition belogs to
							typeConditionsActionId.put(type_condition, actionMap.get(act));
							
							//...store the action the condition belongs to
							typeConditionsAction.put(type_condition, act);
							
							//..store the negative pattern num the conditions belongs to
							typeConditionsPatternNum.put(type_condition, new Integer(neg_num+1));
							
							//store the subcondition in an ordered Collection
							typeConditions.add(type_condition);
						}
					}
				}
			}
		}
		//update the overall number of conditions, such that type
		//conditions are also included
		n_conditions = typeConditionCounter;
		
	}
	
	protected int[] n_subgraphs;
	protected int[] first_subgraph;
	protected int max_n_subgraphs;
	//protected Map[] subGraphMap;
	protected LinkedList[] nodesOfSubgraph;
	protected LinkedList[] edgesOfSubgraph;
	protected Map<Node, Integer> subgraphOfNode;
	protected Map<Edge, Integer> subgraphOfEdge;
	
	private void collectSubGraphInfo() {
		n_subgraphs = new int[actionMap.size()];
		first_subgraph = new int[actionMap.size()];
		//subGraphMap = new HashMap[actionMap.size()];
		nodesOfSubgraph = new LinkedList[actionMap.size()];
		edgesOfSubgraph = new LinkedList[actionMap.size()];
		subgraphOfNode = new HashMap<Node, Integer>();
		subgraphOfEdge = new HashMap<Edge, Integer>();
		
		max_n_subgraphs = 0;
		
		
		for ( Iterator<Action> act_it = actionMap.keySet().iterator(); act_it.hasNext(); ) {
			MatchingAction action = (MatchingAction) act_it.next();
			PatternGraph pattern = action.getPattern();
			int act_id = actionMap.get(action).intValue();
			
			int subgraph = 0;
			
			Collection<IR> remainingNodes = new HashSet<IR>();
			Collection<IR> remainingEdges = new HashSet<IR>();
			
			remainingNodes.addAll( pattern.getNodes() );
			remainingEdges.addAll( pattern.getEdges() );
			
			nodesOfSubgraph[act_id] = new LinkedList();
			edgesOfSubgraph[act_id] = new LinkedList();
			
			n_subgraphs[act_id] = 0;
			//subGraphMap[act_id] = new HashMap();
			
			
			while( !remainingNodes.isEmpty() ) {
				Node node;
				Collection<Node> currentSubgraphNodes = new HashSet<Node>();
				Collection<Edge> currentSubgraphEdges = new HashSet<Edge>();
				
				nodesOfSubgraph[act_id].addLast( currentSubgraphNodes );
				edgesOfSubgraph[act_id].addLast( currentSubgraphEdges );
				
				
				node = (Node)remainingNodes.iterator().next();
				remainingNodes.remove(node);
				
				subgraphOfNode.put( node, new Integer(subgraph) );
				currentSubgraphNodes.add(node);
				
				__deep_first_collect_subgraph_info(remainingNodes, remainingEdges, currentSubgraphNodes, currentSubgraphEdges, subgraph, node, action, pattern);
				
				subgraph++;
			}
			
			
			if(nodesOfSubgraph[act_id].size() > 1) {
				//if a subgraph is smaller than 5 then merge it with the next smallest one
				Collection smallest_subgraph;
				Collection smallest_subgraph_edges;
				do {
					smallest_subgraph = (Collection)nodesOfSubgraph[act_id].getFirst();
					smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].getFirst();
					assert nodesOfSubgraph[act_id].size() ==  edgesOfSubgraph[act_id].size();
					for(int i=0; i<nodesOfSubgraph[act_id].size(); i++) {
						if(((Collection)nodesOfSubgraph[act_id].get(i)).size() < smallest_subgraph.size()) {
							smallest_subgraph = (Collection)nodesOfSubgraph[act_id].get(i);
							smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].get(i);
						}
					}
					
					if(smallest_subgraph.size() >= min_subgraph_size)
						break;
					
					boolean succ = nodesOfSubgraph[act_id].remove(smallest_subgraph);
					assert succ;
					succ = edgesOfSubgraph[act_id].remove(smallest_subgraph_edges);
					assert succ;
					
					
					
					Collection next_smallest_subgraph = (Collection)nodesOfSubgraph[act_id].getFirst();
					Collection next_smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].getFirst();
					assert nodesOfSubgraph[act_id].size() ==  edgesOfSubgraph[act_id].size();
					for(int i=0; i<nodesOfSubgraph[act_id].size(); i++) {
						if(((Collection)nodesOfSubgraph[act_id].get(i)).size() < next_smallest_subgraph.size()) {
							next_smallest_subgraph = (Collection)nodesOfSubgraph[act_id].get(i);
							next_smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].get(i);
						}
					}
					
					//merge the two subgraphs
					next_smallest_subgraph.addAll(smallest_subgraph);
					next_smallest_subgraph_edges.addAll(smallest_subgraph_edges);
				} while(nodesOfSubgraph[act_id].size() > 1);
				
				//move smallest subgraph to the beginning of the list
				smallest_subgraph = (Collection)nodesOfSubgraph[act_id].getFirst();
				smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].getFirst();
				for(int i=0; i<nodesOfSubgraph[act_id].size(); i++) {
					if(((Collection)nodesOfSubgraph[act_id].get(i)).size() < smallest_subgraph.size()) {
						smallest_subgraph = (Collection)nodesOfSubgraph[act_id].get(i);
						smallest_subgraph_edges = (Collection)edgesOfSubgraph[act_id].get(i);
					}
				}
				nodesOfSubgraph[act_id].remove(smallest_subgraph);
				edgesOfSubgraph[act_id].remove(smallest_subgraph_edges);
				nodesOfSubgraph[act_id].addFirst(smallest_subgraph);
				edgesOfSubgraph[act_id].addFirst(smallest_subgraph_edges);
			}
			
			n_subgraphs[act_id] = nodesOfSubgraph[act_id].size();
			
			
			if(max_n_subgraphs < n_subgraphs[act_id])
				max_n_subgraphs = n_subgraphs[act_id];
			
			for(subgraph = 0; subgraph < edgesOfSubgraph[act_id].size(); subgraph++) {
				Collection subgraph_edges = (Collection)edgesOfSubgraph[act_id].get(subgraph);
				for(Iterator edge_it = subgraph_edges.iterator(); edge_it.hasNext(); ) {
					Edge edge = (Edge) edge_it.next();
					subgraphOfEdge.put( edge, new Integer(subgraph) );
				}
			}
			
			for(subgraph = 0; subgraph < nodesOfSubgraph[act_id].size(); subgraph++) {
				Collection subgraph_nodes = (Collection)nodesOfSubgraph[act_id].get(subgraph);
				for(Iterator edge_it = subgraph_nodes.iterator(); edge_it.hasNext(); ) {
					Node node = (Node) edge_it.next();
					subgraphOfNode.put( node, new Integer(subgraph) );
				}
			}
			
			
			
			int max_prio = 0;
			if(pattern.getNodes().size() > 0) {
				//get any node as initial node
				Node max_prio_node = (Node) pattern.getNodes().iterator().next();
				for (Iterator node_it = pattern.getNodes().iterator(); node_it.hasNext(); )	{
					Node node = (Node) node_it.next();
					
					//get the nodes priority
					int prio = 0;
					Attributes a = node.getAttributes();
					if (a != null)
						if (a.containsKey("prio") && a.isInteger("prio"))
							prio = ((Integer) a.get("prio")).intValue();
					
					//if the current priority is greater, update the maximum priority node
					if (prio > max_prio) {
						max_prio = prio;
						max_prio_node = node;
					}
				}
				first_subgraph[act_id] = ((Integer)subgraphOfNode.get(max_prio_node)).intValue();
			} else {
				first_subgraph[act_id] = 0;
			}
		}
	}
	
	private void __deep_first_collect_subgraph_info(
		Collection<IR> remainingNodes, Collection<IR> remainingEdges,
		Collection<Node> currentSubgraphNodes, Collection<Edge> currentSubgraphEdges,
		int subgraph,
		final Node node, MatchingAction action,
		final PatternGraph pattern) {
		//final PatternGraph pattern = action.getPattern();
		
		//a collection of all edges incident to the current node. The collection
		//is ordered by the priority of the nodes at the far end of each edge.
		//nodes without priority get the priority 0.
		Collection<Edge> incidentEdges = new HashSet<Edge>();
		
		//put all edges incident to the current node in that collection
		pattern.getOutgoing(node, incidentEdges);
		pattern.getIncoming(node, incidentEdges);
		
		//iterate over all those incident edges...
		Iterator<Edge> incident_edge_it = incidentEdges.iterator();
		for ( ; incident_edge_it.hasNext(); ) {
			Edge edge = incident_edge_it.next();
			
			//...and check whether the current edge has already been visited
			if ( remainingEdges.contains(edge) ) {
				
				//if the edge has not been visited yet mark it as visited
				currentSubgraphEdges.add(edge);
				
				//mark the current edge as visited
				remainingEdges.remove(edge);
				
				//if the far node is not yet visited follow the current edge to
				//continue the deep first traversal
				if ( remainingNodes.contains(getFarEndNode(edge, node, pattern)) ) {
					
					//mark the edge and the far end node as visited
					currentSubgraphNodes.add(getFarEndNode(edge, node, pattern));
					
					remainingNodes.remove(getFarEndNode(edge, node, pattern));
					//continue recursicly the deep fisrt traversal of the pattern graph
					__deep_first_collect_subgraph_info(remainingNodes, remainingEdges, currentSubgraphNodes, currentSubgraphEdges, subgraph, getFarEndNode(edge, node, pattern), action, pattern);
				}
			}
		}
	}
	
	private Node getFarEndNode(Edge e, Node fromNode, Graph graph) {
		Node farEndNode = null;
		if (graph.getTarget(e) == fromNode)
			farEndNode = graph.getSource(e);
		if (graph.getSource(e) == fromNode)
			farEndNode = graph.getTarget(e);
		
		return farEndNode;
	}
	
	protected void collectActionInfo() {
		super.collectActionInfo();
		collectPatternEdgesToBeKeptInfo();
		collectReplacementEdgeIsPreservedEdgeInfo();
		collectNegativeInfo();
		collectPatternNodeIsNegativeNodeInfo();
		collectPatternEdgeIsNegativeEdgeInfo();
		collectNegativePatternConditionsInfo();
		collectNegativePatternTypeConditionsInfo();
		collectSubGraphInfo();
    }
}


