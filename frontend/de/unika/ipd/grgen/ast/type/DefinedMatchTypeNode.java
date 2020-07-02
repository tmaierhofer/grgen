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
import de.unika.ipd.grgen.ast.MatchClassAutoNode;
import de.unika.ipd.grgen.ast.MatchClassFilterCharacter;
import de.unika.ipd.grgen.ast.decl.DeclNode;
import de.unika.ipd.grgen.ast.decl.executable.MatchClassFilterAutoGeneratedDeclNode;
import de.unika.ipd.grgen.ast.decl.executable.MatchClassFilterFunctionDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.DummyNodeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.EdgeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.NodeDeclNode;
import de.unika.ipd.grgen.ast.decl.pattern.VarDeclNode;
import de.unika.ipd.grgen.ast.expr.array.ArrayAccumulationMethodNode;
import de.unika.ipd.grgen.ast.pattern.PatternGraphLhsNode;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.executable.MatchClassFilterAutoGenerated;
import de.unika.ipd.grgen.ir.executable.MatchClassFilterFunction;
import de.unika.ipd.grgen.ir.pattern.PatternGraphLhs;
import de.unika.ipd.grgen.ir.pattern.Variable;
import de.unika.ipd.grgen.ir.type.DefinedMatchType;

public class DefinedMatchTypeNode extends MatchTypeNode
{
	static {
		setName(DefinedMatchTypeNode.class, "defined match type");
	}

	protected ArrayList<MatchClassFilterCharacter> filters;
	private PatternGraphLhsNode pattern;
	private MatchClassAutoNode auto;

	public DefinedMatchTypeNode(PatternGraphLhsNode pattern)
	{
		this.pattern = pattern;
		becomeParent(this.pattern);
	}

	public DefinedMatchTypeNode(MatchClassAutoNode auto)
	{
		this.auto = auto;
		becomeParent(this.auto);
	}

	@Override
	public String getTypeName()
	{
		return "match<class " + getIdentNode().toString() + ">";
	}

	public void addFilters(ArrayList<MatchClassFilterCharacter> filters)
	{
		this.filters = filters;
	}

	@Override
	public Collection<BaseNode> getChildren()
	{
		Vector<BaseNode> children = new Vector<BaseNode>();
		if(pattern != null)
			children.add(pattern);
		if(auto != null)
			children.add(auto);
		return children;
	}

	@Override
	public Collection<String> getChildrenNames()
	{
		Vector<String> childrenNames = new Vector<String>();
		if(pattern != null)
			childrenNames.add("pattern");
		if(auto != null)
			childrenNames.add("auto");
		return childrenNames;
	}

	@Override
	protected boolean resolveLocal()
	{
		boolean autoOk = true;
		if(pattern == null) {
			autoOk = auto.resolve();
			
			if(autoOk) {
				pattern = becomeParent(auto.getPatternGraph());
				auto.fillPatternGraph(pattern);
			}
		}
		
		boolean filtersOk = true;
		for(MatchClassFilterCharacter filter : filters) {
			if(filter instanceof MatchClassFilterFunctionDeclNode) {
				filtersOk &= ((MatchClassFilterFunctionDeclNode)filter).resolve();
			} else { //if(filter instanceof MatchClassFilterAutoGeneratedNode)
				filtersOk &= ((MatchClassFilterAutoGeneratedDeclNode)filter).resolve();
			}
		}

		return autoOk & filtersOk;
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
				filtersOk &= ((MatchClassFilterAutoGeneratedDeclNode)filter).check();
			}
		}
		boolean allFilterEntitiesExistAndAreOfAdmissibleType = true;
		HashSet<String> alreadySeenFilters = new HashSet<String>();
		for(MatchClassFilterCharacter fc : filters) {
			if(fc instanceof MatchClassFilterAutoGeneratedDeclNode) {
				MatchClassFilterAutoGeneratedDeclNode filter = (MatchClassFilterAutoGeneratedDeclNode)fc;
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

	private boolean checkAutoGeneratedFilter(MatchClassFilterAutoGeneratedDeclNode filter)
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
							+ " (Allowed are: " + accumulationMethod.getValidTargetTypesOfAccumulation() + ")");
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

	@Override
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

	public Set<NodeDeclNode> getNodes()
	{
		Set<NodeDeclNode> nodes = new HashSet<NodeDeclNode>();
		for(NodeDeclNode node : pattern.getNodes()) {
			if(!(node instanceof DummyNodeDeclNode))
				nodes.add(node);
		}
		return Collections.unmodifiableSet(nodes);
	}

	public Set<EdgeDeclNode> getEdges()
	{
		return pattern.getEdges();
	}

	public Set<VarDeclNode> getVariables()
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

	public Set<DeclNode> getEntities()
	{
		return pattern.getEntities();
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

		PatternGraphLhs patternGraph = pattern.getPatternGraph();
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
				((MatchClassFilterAutoGeneratedDeclNode)filter).checkIR(MatchClassFilterAutoGenerated.class);
			}
		}

		return definedMatchType;
	}
}
