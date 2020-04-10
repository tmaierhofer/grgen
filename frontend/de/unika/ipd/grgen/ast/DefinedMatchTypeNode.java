/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.unika.ipd.grgen.ir.DefinedMatchType;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.MatchClassFilterAutoGenerated;
import de.unika.ipd.grgen.ir.MatchClassFilterFunction;
import de.unika.ipd.grgen.ir.PatternGraph;
import de.unika.ipd.grgen.ir.Variable;

public class DefinedMatchTypeNode extends DeclaredTypeNode {
	static {
		setName(DefinedMatchTypeNode.class, "defined match type");
	}

	@Override
	public String getName() {
		return "match<class " + getIdentNode().toString() + "> type";
	}

	/////////
	protected ArrayList<MatchClassFilterCharacter> filters;
	private PatternGraphNode pattern;
	
			
	public DefinedMatchTypeNode(PatternGraphNode pattern) {
		this.pattern = pattern;
		becomeParent(this.pattern);
	}

	/////////////
	public void addFilters(ArrayList<MatchClassFilterCharacter> filters) {
		this.filters = filters;
	}

	@Override
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(pattern);
		return children;
	}

	@Override
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("pattern");
		return childrenNames;
	}

	@Override
	protected boolean resolveLocal() {
		boolean filtersOk = true;
		for(MatchClassFilterCharacter filter : filters) {
			if(filter instanceof MatchClassFilterFunctionDeclNode) {
				filtersOk &= ((MatchClassFilterFunctionDeclNode)filter).resolve();
			} else { //if(filter instanceof MatchClassFilterAutoGeneratedNode)
				filtersOk &= ((MatchClassFilterAutoGeneratedNode)filter).resolve();
			}
		}

		return filtersOk;
	}

	@Override
	protected boolean checkLocal() {
		// maybe TODO: check pattern graph member
		return checkFilters();
	}

	private boolean checkFilters() {
		boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
		HashSet<String> alreadySeenFilters = new HashSet<String>();
		for(MatchClassFilterCharacter fc : filters) {
			if(fc instanceof MatchClassFilterAutoGeneratedNode) {
				MatchClassFilterAutoGeneratedNode filter = (MatchClassFilterAutoGeneratedNode)fc;
				String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
				if(alreadySeenFilters.contains(filterNameWithEntitySuffix)) {
					getIdentNode().reportError(filterNameWithEntitySuffix + " was already declared, only one declaration admissible.");
					allFilterEntitiesExistAndAreOfAdmissibleType = false;
				} else {
					alreadySeenFilters.add(filterNameWithEntitySuffix);
				}
				if(filter.name.equals("orderAscendingBy") || filter.name.equals("orderDescendingBy")
					|| filter.name.equals("groupBy") || filter.name.equals("keepSameAsFirst")
					|| filter.name.equals("keepSameAsLast") || filter.name.equals("keepOneForEach")) {
					for(String filterEntity : filter.entities) {
						allFilterEntitiesExistAndAreOfAdmissibleType &= checkFilterVariable(filter.name, filterEntity);
					}
					if(filter.name.equals("groupBy") || filter.name.equals("keepSameAsFirst")
						|| filter.name.equals("keepSameAsLast") || filter.name.equals("keepOneForEach")) {
						if(filter.entities.size()!=1) {
							getIdentNode().reportError(filterNameWithEntitySuffix + " must be declared with exactly one variable, but is declared with " + filter.entities.size() + " variables");
							allFilterEntitiesExistAndAreOfAdmissibleType = false;
						}
					}
				}
			}
		}
		return allFilterEntitiesExistAndAreOfAdmissibleType;
	}

	private boolean checkFilterVariable(String filterNameWithEntitySuffix, String filterVariable) {
		if(pattern.getVariable(filterVariable)==null) {
			getIdentNode().reportError(filterNameWithEntitySuffix + ": unknown variable " + filterVariable);
			return false;
		}
		TypeNode filterVariableType = pattern.getVariable(filterVariable).getDeclType();
		if(!filterVariableType.isFilterableType()) {
			getIdentNode().reportError(filterNameWithEntitySuffix + ": the variable " + filterVariable + " must be of one of the following types: " + filterVariableType.getFilterableTypesAsString());
			return false;
		}
		return true;
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

	public Collection<NodeDeclNode> getNodes() {
		Set<NodeDeclNode> nodes = new HashSet<NodeDeclNode>();
		for(NodeDeclNode node : pattern.getNodes()) {
			if(!(node instanceof DummyNodeDeclNode))
				nodes.add(node);
		}
		return Collections.unmodifiableSet(nodes);
	}

	public Collection<EdgeDeclNode> getEdges() {
		return pattern.getEdges();
	}

	public Collection<VarDeclNode> getVariables() {
		Set<VarDeclNode> vars = new HashSet<VarDeclNode>(pattern.getDefVariablesToBeYieldedTo().getChildren());
		for(DeclNode varCand : pattern.getParamDecls()) {
			if(!(varCand instanceof VarDeclNode))
				continue;
			VarDeclNode var = (VarDeclNode)varCand;
			vars.add(var);
		}
		return Collections.unmodifiableSet(vars);
	}

	/** Returns the IR object for this defined match type node. */
	public DefinedMatchType getDefinedMatchType() {
		return checkIR(DefinedMatchType.class);
	}

	@Override
	protected IR constructIR() {
		if (isIRAlreadySet()) {
			return (DefinedMatchType)getIR();
		}

		PatternGraph patternGraph = pattern.getPatternGraph();
		for(DeclNode varCand : pattern.getParamDecls()) {
			if(!(varCand instanceof VarDeclNode))
				continue;
			VarDeclNode var = (VarDeclNode)varCand;
			patternGraph.addVariable(var.checkIR(Variable.class));
		}

		DefinedMatchType definedMatchType = new DefinedMatchType(getIdentNode().toString(), getIdentNode().getIdent(),
				patternGraph);

		// mark this node as already visited
		setIR(definedMatchType);

		// filters add themselves to the match type when their IR is constructed
		for(MatchClassFilterCharacter filter : filters) {
			if(filter instanceof MatchClassFilterFunctionDeclNode) {
				((MatchClassFilterFunctionDeclNode)filter).checkIR(MatchClassFilterFunction.class);
			} else { //if(filter instanceof MatchClassFilterAutoGeneratedNode)
				((MatchClassFilterAutoGeneratedNode)filter).checkIR(MatchClassFilterAutoGenerated.class);
			}
		}

		return definedMatchType;
	}
}
