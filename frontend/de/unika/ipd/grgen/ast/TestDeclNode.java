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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import de.unika.ipd.grgen.ast.exprevals.*;
import de.unika.ipd.grgen.ast.util.CollectResolver;
import de.unika.ipd.grgen.ast.util.DeclarationTypeResolver;
import de.unika.ipd.grgen.ir.DefinedMatchType;
import de.unika.ipd.grgen.ir.Edge;
import de.unika.ipd.grgen.ir.Entity;
import de.unika.ipd.grgen.ir.exprevals.Expression;
import de.unika.ipd.grgen.ir.FilterAutoGenerated;
import de.unika.ipd.grgen.ir.FilterAutoSupplied;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.MatchingAction;
import de.unika.ipd.grgen.ir.PatternGraph;
import de.unika.ipd.grgen.ir.Rule;
import de.unika.ipd.grgen.ir.Variable;


/**
 * AST node class representing tests
 */
public class TestDeclNode extends ActionDeclNode {
	static {
		setName(TestDeclNode.class, "test declaration");
	}

	protected CollectNode<BaseNode> returnFormalParametersUnresolved;
	protected CollectNode<TypeNode> returnFormalParameters;
	protected ArrayList<FilterAutoNode> filters;
	private TestTypeNode type;
	protected PatternGraphNode pattern;
	protected CollectNode<IdentNode> implementedMatchTypesUnresolved;
	protected CollectNode<DefinedMatchTypeNode> implementedMatchTypes;

	private static final TypeNode testType = new TestTypeNode();

	protected TestDeclNode(IdentNode id, TypeNode type, PatternGraphNode pattern, CollectNode<IdentNode> implementedMatchTypes,
							CollectNode<BaseNode> rets) {
		super(id, type);
		this.returnFormalParametersUnresolved = rets;
		becomeParent(this.returnFormalParametersUnresolved);
		this.pattern = pattern;
		becomeParent(this.pattern);
		implementedMatchTypesUnresolved	= implementedMatchTypes;
		becomeParent(implementedMatchTypesUnresolved);
		this.filters = new ArrayList<FilterAutoNode>();
	}

	public TestDeclNode(IdentNode id, PatternGraphNode pattern, CollectNode<IdentNode> implementedMatchTypes,
			CollectNode<BaseNode> rets) {
		this(id, testType, pattern, implementedMatchTypes, rets);
	}

	public void addFilters(ArrayList<FilterAutoNode> filters) {
		this.filters.addAll(filters);
	}

	/** returns children of this node */
	@Override
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(ident);
		children.add(getValidVersion(typeUnresolved, type));
		children.add(getValidVersion(returnFormalParametersUnresolved, returnFormalParameters));
		children.add(pattern);
		children.add(getValidVersion(implementedMatchTypesUnresolved, implementedMatchTypes));
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	@Override
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("ident");
		childrenNames.add("type");
		childrenNames.add("ret");
		childrenNames.add("pattern");
		childrenNames.add("implementedMatchTypes");
		return childrenNames;
	}

	private static final DeclarationTypeResolver<TestTypeNode> typeResolver = new DeclarationTypeResolver<TestTypeNode>(TestTypeNode.class);
	private static final CollectResolver<DefinedMatchTypeNode> matchTypeResolver = new CollectResolver<DefinedMatchTypeNode>(
			new DeclarationTypeResolver<DefinedMatchTypeNode>(DefinedMatchTypeNode.class));
	private static final CollectResolver<TypeNode> retTypeResolver = new CollectResolver<TypeNode>(
			new DeclarationTypeResolver<TypeNode>(TypeNode.class));

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolveLocal() */
	@Override
	protected boolean resolveLocal() {
		type = typeResolver.resolve(typeUnresolved, this);
		for(IdentNode mtid : implementedMatchTypesUnresolved.getChildren()) {
			if(!(mtid instanceof PackageIdentNode)) {
				fixupDefinition(mtid, mtid.getScope());
			}
		}
		implementedMatchTypes = matchTypeResolver.resolve(implementedMatchTypesUnresolved, this);
		returnFormalParameters = retTypeResolver.resolve(returnFormalParametersUnresolved, this);

		return type != null && returnFormalParameters != null && implementedMatchTypes != null && resolveFilters(filters);
	}

	/**
	 * Check if actual return arguments are conformant to the formal return parameters.
	 */
	// TODO is this method called twice for RuleDeclNode?
	protected boolean checkReturns(CollectNode<ExprNode> returnArgs) {
		boolean res = true;

		int declaredNumRets = returnFormalParameters.size();
		int actualNumRets = returnArgs.size();
retLoop:for (int i = 0; i < Math.min(declaredNumRets, actualNumRets); i++) {
			ExprNode retExpr = returnArgs.get(i);
			TypeNode retExprType = retExpr.getType();

			TypeNode retDeclType = returnFormalParameters.get(i);
			if(!retExprType.isCompatibleTo(retDeclType)) {
				res = false;
				String exprTypeName;
				if(retExprType instanceof InheritanceTypeNode)
					exprTypeName = ((InheritanceTypeNode) retExprType).getIdentNode().toString();
				else
					exprTypeName = retExprType.toString();
				ident.reportError("Cannot convert " + (i + 1) + ". return parameter from \""
						+ exprTypeName + "\" to \"" + returnFormalParameters.get(i).toString() + "\"");
				continue;
			}

			if(!(retExpr instanceof DeclExprNode)) continue;
			ConstraintDeclNode retElem = ((DeclExprNode) retExpr).getConstraintDeclNode();
			if(retElem == null) continue;

			InheritanceTypeNode declaredRetType = retElem.getDeclType();

			Set<? extends ConstraintDeclNode> homSet;
			if(retElem instanceof NodeDeclNode)
				homSet = pattern.getHomomorphic((NodeDeclNode) retElem);
			else
				homSet = pattern.getHomomorphic((EdgeDeclNode) retElem);

			for(ConstraintDeclNode homElem : homSet) {
				if(homElem == retElem) continue;

				ConstraintDeclNode retypedElem = homElem.getRetypedElement();
				if(retypedElem == null) continue;

				InheritanceTypeNode retypedElemType = retypedElem.getDeclType();
				if(retypedElemType.isA(declaredRetType)) continue;

				res = false;
				returnArgs.reportError("Return parameter \"" + retElem.getIdentNode() + "\" is homomorphic to \""
						+ homElem.getIdentNode() + "\", which gets retyped to the incompatible type \""
						+ retypedElemType.getIdentNode() + "\"");
				continue retLoop;
			 }
		}

		//check the number of returned elements
		if (actualNumRets != declaredNumRets) {
			res = false;
			if (declaredNumRets == 0) {
				returnArgs.reportError("No return values declared for rule \"" + ident + "\"");
			} else if(actualNumRets == 0) {
				reportError("Missing return statement for rule \"" + ident + "\"");
			} else {
				returnArgs.reportError("Return statement has wrong number of parameters");
			}
		}
		return res;
	}

	private boolean SameNumberOfRewriteParts() {
		boolean res = true;

		for(AlternativeNode alt : pattern.alts.getChildren()) {
			for(AlternativeCaseNode altCase : alt.getChildren()) {
				if(altCase.right.getChildren().size() != 0) {
					error.error(getCoords(), "Different number of replacement patterns/rewrite parts in test " + ident.toString()
							+ " and nested alternative case " + altCase.ident.toString());
					res = false;
					continue;
				}
			}
		}

		for(IteratedNode iter : pattern.iters.getChildren()) {
			if(iter.right.getChildren().size() != 0) {
				error.error(getCoords(), "Different number of replacement patterns/rewrite parts in test " + ident.toString()
						+ " and nested iterated/multiple/optional " + iter.ident.toString());
				res = false;
				continue;
			}
		}

		return res;
	}

	@Override
	protected boolean checkLocal() {
		boolean childs = true;

		// check if reused names of edges connect the same nodes in the same direction with the same edge kind for each usage
		boolean edgeReUse = false;
		if (childs) {
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
		}

		boolean returnParams = true;
		if(!(this instanceof RuleDeclNode))
			returnParams = checkReturns(pattern.returns);

		boolean noRewriteParts = true;
		if(!(this instanceof RuleDeclNode))
			noRewriteParts = SameNumberOfRewriteParts();

		return checkFilters(pattern, filters) && noRewriteParts && childs && edgeReUse && returnParams && checkMatchTypesImplemented();
	}

	public boolean checkMatchTypesImplemented()
	{
		boolean isOk = true;
		
		for(DefinedMatchTypeNode matchType : implementedMatchTypes.getChildren()) {
			isOk &= checkMatchTypeImplemented(matchType);
		}
		
		return isOk;
	}

	public boolean checkMatchTypeImplemented(DefinedMatchTypeNode matchType)
	{
		boolean isOk = true;
		
		String actionName = getIdentNode().toString();
		String matchTypeName = matchType.getIdentNode().toString();
		
		HashMap<String, NodeDeclNode> knownNodes = new HashMap<String, NodeDeclNode>();
		for(NodeDeclNode node : pattern.getNodes()) {
			knownNodes.put(node.getIdentNode().toString(), node);
		}

		for(NodeDeclNode node : matchType.getNodes()) {
			String nodeName = node.getIdentNode().toString();
			if(!knownNodes.containsKey(nodeName)) {
				getIdentNode().reportError("Action " + actionName + " does not implement the node " + nodeName + " expected from " + matchTypeName);
				isOk = false;
			} else {
				NodeDeclNode nodeFromPattern = knownNodes.get(nodeName);
				NodeTypeNode type = node.getDeclType();
				NodeTypeNode typeOfNodeFromPattern = nodeFromPattern.getDeclType();
				if(!type.isEqual(typeOfNodeFromPattern)) {
					getIdentNode().reportError("The type of the node " + nodeName + " from the action " + actionName + " does not equal the type of the node from the match class " + matchTypeName 
							+ ". In the match class, " + getTypeName(type) + " is declared, but in the pattern, " + getTypeName(typeOfNodeFromPattern) + " is declared.");
					isOk = false;
				}
				if(nodeFromPattern.defEntityToBeYieldedTo && !node.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The node " + nodeName + " from the action " + actionName + " is a def to be yielded to node, while it is a node to be matched (or received as input to the pattern) in the match class " + matchTypeName);
					isOk = false;
				}
				if(!nodeFromPattern.defEntityToBeYieldedTo && node.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The node " + nodeName + " from the action " + actionName + " is a node to be matched (or received as input to the pattern), while it is a def to be yielded to node in the match class " + matchTypeName);
					isOk = false;
				}
			}
		}

		HashMap<String, EdgeDeclNode> knownEdges = new HashMap<String, EdgeDeclNode>();
		for(EdgeDeclNode edge : pattern.getEdges()) {
			knownEdges.put(edge.getIdentNode().toString(), edge);
		}

		for(EdgeDeclNode edge: matchType.getEdges()) {
			String edgeName = edge.getIdentNode().toString();
			if(!knownEdges.containsKey(edgeName)) {
				getIdentNode().reportError("Action " + actionName + " does not implement the edge " + edgeName + " expected from " + matchTypeName);
				isOk = false;
			} else {
				EdgeDeclNode edgeFromPattern = knownEdges.get(edgeName);
				EdgeTypeNode type = edge.getDeclType();
				EdgeTypeNode typeOfEdgeFromPattern = edgeFromPattern.getDeclType();
				if(!type.isEqual(typeOfEdgeFromPattern)) {
					getIdentNode().reportError("The type of the edge " + edgeName + " from the action " + actionName + " does not equal the type of the edge from the match class " + matchTypeName 
							+ ". In the match class, " + getTypeName(type) + " is declared, but in the pattern, " + getTypeName(typeOfEdgeFromPattern) + " is declared.");
					isOk = false;
				}
				if(edgeFromPattern.defEntityToBeYieldedTo && !edge.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The edge " + edgeName + " from the action " + actionName + " is a def to be yielded to edge, while it is an edge to be matched (or received as input to the pattern) in the match class " + matchTypeName);
					isOk = false;
				}
				if(!edgeFromPattern.defEntityToBeYieldedTo && edge.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The edge " + edgeName + " from the action " + actionName + " is an edge to be matched (or received as input to the pattern), while it is a def to be yielded to edge in the match class " + matchTypeName);
					isOk = false;
				}
			}
		}

		HashMap<String, VarDeclNode> knownVariables = new HashMap<String, VarDeclNode>();
		for(VarDeclNode var : pattern.getDefVariablesToBeYieldedTo().getChildren()) {
			knownVariables.put(var.getIdentNode().toString(), var);
		}
		for(DeclNode varCand : pattern.getParamDecls()) {
			if(!(varCand instanceof VarDeclNode))
				continue;
			VarDeclNode var = (VarDeclNode)varCand;
			knownVariables.put(var.getIdentNode().toString(), var);
		}
		
		for(VarDeclNode var : matchType.getVariables()) {
			String varName = var.getIdentNode().toString();
			if(!knownVariables.containsKey(varName)) {
				getIdentNode().reportError("Action " + actionName + " does not implement the variable " + varName + " expected from " + matchTypeName);
				isOk = false;
			} else {
				VarDeclNode varFromPattern = knownVariables.get(varName);
				TypeNode type = var.getDeclType();
				TypeNode typeOfVarFromPattern = varFromPattern.getDeclType();
				if(!type.isEqual(typeOfVarFromPattern)) {
					getIdentNode().reportError("The type of the variable " + varName + " from the action " + actionName + " does not equal the type of the variable from the match class " + matchTypeName 
							+ ". In the match class, " + getTypeName(type) + " is declared, but in the pattern, " + getTypeName(typeOfVarFromPattern) + " is declared.");
					isOk = false;
				}
				if(varFromPattern.defEntityToBeYieldedTo && !var.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The variable " + varName + " from the action " + actionName + " is a def to be yielded to var, while it is a var to be received as input to the pattern in the match class " + matchTypeName);
					isOk = false;
				}
				if(!varFromPattern.defEntityToBeYieldedTo && var.defEntityToBeYieldedTo) {
					getIdentNode().reportError("The variable " + varName + " from the action " + actionName + " is a variable to be received as input to the pattern, while it is a def to be yielded to var in the match class " + matchTypeName);
					isOk = false;
				}
			}
		}

		return isOk;
	}

	private String getTypeName(TypeNode type) {
		String typeName;
		if(type instanceof InheritanceTypeNode)
			typeName = ((InheritanceTypeNode) type).getIdentNode().toString();
		else
			typeName = type.toString();
		return typeName;
	}

	public boolean checkControlFlow() {
		return true;
	}

	public Collection<DefinedMatchTypeNode> getImplementedMatchClasses() {
		return implementedMatchTypes.getChildren();
	}

	protected void constructIRaux(MatchingAction ma, CollectNode<ExprNode> aReturns) {
		PatternGraph patternGraph = ma.getPattern();

		// add Params to the IR
		for(DeclNode decl : pattern.getParamDecls()) {
			ma.addParameter(decl.checkIR(Entity.class));

			// TODO: parameters were already added to the graph -> needed here again?
			if(decl instanceof NodeCharacter) {
				patternGraph.addSingleNode(((NodeCharacter)decl).getNode());
			} else if (decl instanceof EdgeCharacter) {
				Edge e = ((EdgeCharacter)decl).getEdge();
				patternGraph.addSingleEdge(e);
			} else if(decl instanceof VarDeclNode) {
				patternGraph.addVariable(((VarDeclNode) decl).getVariable());
			} else {
				throw new IllegalArgumentException("unknown Class: " + decl);
			}
		}

		// add Return-Params to the IR
		for(ExprNode aReturnAST : aReturns.getChildren()) {
			Expression aReturn = aReturnAST.checkIR(Expression.class);
			// actual return-parameter
			ma.addReturn(aReturn);
		}
		
		// filters add themselves to the rule when their IR is constructed
		for(FilterAutoNode filter : filters) {
			if(filter instanceof FilterAutoSuppliedNode) {
				((FilterAutoSuppliedNode)filter).checkIR(FilterAutoSupplied.class);
			} else { //if(filter instanceof FilterAutoGeneratedNode)
				((FilterAutoGeneratedNode)filter).checkIR(FilterAutoGenerated.class);
			}
		}
	}

	@Override
	public TypeNode getDeclType() {
		assert isResolved();

		return type;
	}

	public static String getKindStr() {
		return "action declaration";
	}

	public static String getUseStr() {
		return "action";
	}
	
	public NodeDeclNode tryGetNode(String name) {
		return pattern.tryGetNode(name);
	}

	public EdgeDeclNode tryGetEdge(String name) {
		return pattern.tryGetEdge(name);
	}

	public VarDeclNode tryGetVar(String name) {
		return pattern.tryGetVar(name);
	}

	@Override
	protected IR constructIR() {
		PatternGraph left = pattern.getPatternGraph();
		for(DeclNode varCand : pattern.getParamDecls()) {
			if(!(varCand instanceof VarDeclNode))
				continue;
			VarDeclNode var = (VarDeclNode)varCand;
			left.addVariable(var.checkIR(Variable.class));
		}

		// return if the pattern graph already constructed the IR object
		// that may happens in recursive patterns
		if (isIRAlreadySet()) {
			return getIR();
		}

		Rule testRule = new Rule(getIdentNode().getIdent(), left, null);

		// mark this node as already visited
		setIR(testRule);

		for(DefinedMatchTypeNode implementedMatchClassNode : implementedMatchTypes.getChildren()) {
			DefinedMatchType implementedMatchClass = implementedMatchClassNode.checkIR(DefinedMatchType.class);
			testRule.addImplementedMatchClass(implementedMatchClass);
		}

		constructImplicitNegs(left);
		constructIRaux(testRule, pattern.returns);

		return testRule;
	}

	/**
     * add NACs for induced- or DPO-semantic
     */
    protected void constructImplicitNegs(PatternGraph left)
    {
    	PatternGraphNode leftNode = pattern;
    	for (PatternGraph neg : leftNode.getImplicitNegGraphs()) {
    		left.addNegGraph(neg);
    	}
    }
}


