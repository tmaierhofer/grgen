/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.unika.ipd.grgen.ast.BaseNode;
import de.unika.ipd.grgen.ast.MatchClassFilterAutoGeneratedNode;
import de.unika.ipd.grgen.ast.MatchClassFilterCharacter;
import de.unika.ipd.grgen.ast.MemberAccessor;
import de.unika.ipd.grgen.ast.decl.DeclNode;
import de.unika.ipd.grgen.ast.decl.executable.MatchClassFilterFunctionDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.DummyNodeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.EdgeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.NodeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.VarDeclNode;
import de.unika.ipd.grgen.ast.expr.array.ArrayAccumulationMethodNode;
import de.unika.ipd.grgen.ast.pattern.PatternGraphNode;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.executable.MatchClassFilterAutoGenerated;
import de.unika.ipd.grgen.ir.executable.MatchClassFilterFunction;
import de.unika.ipd.grgen.ir.pattern.PatternGraph;
import de.unika.ipd.grgen.ir.pattern.Variable;
import de.unika.ipd.grgen.ir.type.DefinedMatchType;

public class DefinedMatchTypeNode extends DeclaredTypeNode implements MemberAccessor
{
	static {
		setName(DefinedMatchTypeNode.class, "defined match type");
	}

	@Override
	public String getName()
	{
		return getTypeName();
	}

	@Override
	public String getTypeName()
	{
		return "match<class " + getIdentNode().toString() + ">";
	}

	/////////
	protected ArrayList<MatchClassFilterCharacter> filters;
	private PatternGraphNode pattern;

	public DefinedMatchTypeNode(PatternGraphNode pattern)
	{
		this.pattern = pattern;
		becomeParent(this.pattern);
	}

	/////////////
	public void addFilters(ArrayList<MatchClassFilterCharacter> filters)
	{
		this.filters = filters;
	}

	@Override
	public Collection<BaseNode> getChildren()
	{
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(pattern);
		return children;
	}

	@Override
	public Collection<String> getChildrenNames()
	{
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("pattern");
		return childrenNames;
	}

	@Override
	protected boolean resolveLocal()
	{
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
	protected boolean checkLocal()
	{
		// maybe TODO: check pattern graph member
		return checkFilters();
	}

	private boolean checkFilters()
	{
		boolean filtersOk = true;
		for(MatchClassFilterCharacter filter : filters) {
			if(filter instanceof MatchClassFilterFunctionDeclNode) {
				filtersOk &= ((MatchClassFilterFunctionDeclNode)filter).check();
			} else { //if(filter instanceof MatchClassFilterAutoGeneratedNode)
				filtersOk &= ((MatchClassFilterAutoGeneratedNode)filter).check();
			}
		}
		boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
		HashSet<String> alreadySeenFilters = new HashSet<String>();
		for(MatchClassFilterCharacter fc : filters) {
			if(fc instanceof MatchClassFilterAutoGeneratedNode) {
				MatchClassFilterAutoGeneratedNode filter = (MatchClassFilterAutoGeneratedNode)fc;
				String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
				if(alreadySeenFilters.contains(filterNameWithEntitySuffix)) {
					getIdentNode().reportError(filterNameWithEntitySuffix
							+ " was already declared, only one declaration admissible.");
					allFilterEntitiesExistAndAreOfAdmissibleType = false;
				} else {
					alreadySeenFilters.add(filterNameWithEntitySuffix);
				}
				allFilterEntitiesExistAndAreOfAdmissibleType &= checkAutoGeneratedFilter(filter);
			}
		}
		return filtersOk & allFilterEntitiesExistAndAreOfAdmissibleType;
	}

	private boolean checkAutoGeneratedFilter(MatchClassFilterAutoGeneratedNode filter)
	{
		String filterNameWithEntitySuffix = filter.getFilterNameWithEntitySuffix();
		switch(filter.name) {
		case "orderAscendingBy":
		case "orderDescendingBy":
		{
			boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
			for(String filterEntity : filter.entities) {
				allFilterEntitiesExistAndAreOfAdmissibleType &= pattern.checkFilterVariable(getIdentNode(),
						filter.name, filterEntity);
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
						filter.name, filterEntity);
			}
			if(filter.entities.size() != 1) {
				getIdentNode().reportError(filterNameWithEntitySuffix
						+ " must be declared with exactly one variable, but is declared with "
						+ filter.entities.size() + " variables");
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
				VarDeclNode filterAccumulationVariable = tryGetVar(filter.entities.get(1));
				if(filterAccumulationVariable == null) {
					getIdentNode().reportError(filterNameWithEntitySuffix + ": unknown accumulation variable "
							+ filter.entities.get(1));
					return false;
				}
				TypeNode filterAccumulationVariableType = filterAccumulationVariable.getDeclType();
				if(!accumulationMethod.isValidTargetTypeOfAccumulation(filterAccumulationVariableType)) {
					getIdentNode().reportError("The array accumulation method " + filter.entities.get(2)
							+ " is not applicable to the type " + filterAccumulationVariableType
							+ " of the accumulation variable " + filter.entities.get(1)
							+ " / its result cannot be assigned to the accumulation variable."
							+ " (Accumulatable are: " + TypeNode.getAccumulatableTypesAsString()
							+ "; a valid target type for all accumulation methods is double)");
					return false;
				}
				return true;
			}
		default:
			assert(false);
			return false;
		}
	}

	public NodeDeclNode tryGetNode(String name)
	{
		return pattern.tryGetNode(name);
	}

	public EdgeDeclNode tryGetEdge(String name)
	{
		return pattern.tryGetEdge(name);
	}

	public VarDeclNode tryGetVar(String name)
	{
		return pattern.tryGetVar(name);
	}

	public DeclNode tryGetMember(String name)
	{
		NodeDeclNode node = pattern.tryGetNode(name);
		if(node != null)
			return node;
		EdgeDeclNode edge = pattern.tryGetEdge(name);
		if(edge != null)
			return edge;
		return pattern.tryGetVar(name);
	}

	public Collection<NodeDeclNode> getNodes()
	{
		Set<NodeDeclNode> nodes = new HashSet<NodeDeclNode>();
		for(NodeDeclNode node : pattern.getNodes()) {
			if(!(node instanceof DummyNodeDeclNode))
				nodes.add(node);
		}
		return Collections.unmodifiableSet(nodes);
	}

	public Collection<EdgeDeclNode> getEdges()
	{
		return pattern.getEdges();
	}

	public Collection<VarDeclNode> getVariables()
	{
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
	public DefinedMatchType getDefinedMatchType()
	{
		return checkIR(DefinedMatchType.class);
	}

	@Override
	protected IR constructIR()
	{
		if(isIRAlreadySet()) {
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
