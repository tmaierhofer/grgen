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

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ast.util.DeclarationTypeResolver;
import de.unika.ipd.grgen.ir.exprevals.EvalStatements;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.PatternGraph;
import de.unika.ipd.grgen.ir.Rule;


/**
 * AST node for an alternative case pattern, maybe including replacements.
 */
public class AlternativeCaseNode extends ActionDeclNode  {
	static {
		setName(AlternativeCaseNode.class, "alternative case");
	}

	protected RhsDeclNode right;
	private AlternativeCaseTypeNode type;

	/** Type for this declaration. */
	private static final TypeNode subpatternType = new AlternativeCaseTypeNode();

	/**
	 * Make a new alternative case rule.
	 * @param id The identifier of this rule.
	 * @param left The left hand side (The pattern to match).
	 * @param right The right hand side.
	 */
	public AlternativeCaseNode(IdentNode id, PatternGraphNode left, RhsDeclNode right) {
		super(id, subpatternType, left);
		this.right = right;
		becomeParent(this.right);
	}

	/** returns children of this node */
	@Override
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(ident);
		children.add(getValidVersion(typeUnresolved, type));
		children.add(pattern);
		if(right != null)
			children.add(right);
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	@Override
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("ident");
		childrenNames.add("type");
		childrenNames.add("pattern");
		if(right != null)
			childrenNames.add("right");
		return childrenNames;
	}

	protected static final DeclarationTypeResolver<AlternativeCaseTypeNode> typeResolver =
		new DeclarationTypeResolver<AlternativeCaseTypeNode>(AlternativeCaseTypeNode.class);

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolveLocal() */
	@Override
	protected boolean resolveLocal() {
		type = typeResolver.resolve(typeUnresolved, this);

		return type != null;
	}

	/**
	 * Check, if the rule type node is right.
	 * The children of a rule type are
	 * 1) a pattern for the left side.
	 * 2) a pattern for the right side.
	 * @see de.unika.ipd.grgen.ast.BaseNode#checkLocal()
	 */
	@Override
	protected boolean checkLocal() {
		if(right != null)
			right.warnElemAppearsInsideAndOutsideDelete(pattern);

		boolean leftHandGraphsOk = checkLeft();

		boolean noReturnInPatternOk = true;
		if(pattern.returns.size() > 0) {
			error.error(getCoords(), "No return statements in pattern parts of rules allowed");
			noReturnInPatternOk = false;
		}

		boolean noReturnInAlterntiveCaseReplacement = true;
		if(right != null) {
			if(right.graph.returns.size() > 0) {
				error.error(getCoords(), "No return statements in alternative cases allowed");
				noReturnInAlterntiveCaseReplacement = false;
			}
		}

		boolean rhsReuseOk = true;
		boolean execParamsNotDeleted = true;
		boolean sameNumberOfRewriteParts = sameNumberOfRewriteParts(right, "alternative case");
		boolean noNestedRewriteParameters = true;
		boolean abstr = true;
		if(right != null) {
			rhsReuseOk = checkRhsReuse(right);
			execParamsNotDeleted = checkExecParamsNotDeleted(right);
			noNestedRewriteParameters = noNestedRewriteParameters(right, "alternative case");
			abstr = noAbstractElementInstantiatedNestedPattern(right);
		}
		
		return leftHandGraphsOk & sameNumberOfRewriteParts && noNestedRewriteParameters
			& rhsReuseOk & noReturnInPatternOk & noReturnInAlterntiveCaseReplacement
			& execParamsNotDeleted & abstr;
	}

	/**
	 * @see de.unika.ipd.grgen.ast.BaseNode#constructIR()
	 */
	// TODO support only one rhs
	@Override
	protected IR constructIR() {
		PatternGraph left = pattern.getPatternGraph();

		// return if the pattern graph already constructed the IR object
		// that may happens in recursive patterns
		if (isIRAlreadySet()) {
			if(right != null) {
				addReplacementParamsToNestedAlternativesAndIterateds((Rule)getIR(), right);
			}
			return getIR();
		}

		PatternGraph rightPattern = null;
		if(right != null) {
			rightPattern = right.getPatternGraph(left);
		}

		// return if the pattern graph already constructed the IR object
		// that may happens in recursive patterns
		if (isIRAlreadySet()) {
			if(right != null) {
				addReplacementParamsToNestedAlternativesAndIterateds((Rule)getIR(), right);
			}
			return getIR();
		}

		Rule altCaseRule = new Rule(getIdentNode().getIdent(), left, rightPattern);

		constructImplicitNegs(left);
		constructIRaux(altCaseRule, right);

		// add Eval statements to the IR
		if(right != null) {
			for (EvalStatements n : right.getRHSGraph().getYieldEvalStatements()) {
				altCaseRule.addEval(n);
			}
		}

		return altCaseRule;
	}

	/**
	 * add NACs for induced- or DPO-semantic
	 */
	private void constructImplicitNegs(PatternGraph left) {
		PatternGraphNode leftNode = pattern;
		for (PatternGraph neg : leftNode.getImplicitNegGraphs()) {
			left.addNegGraph(neg);
		}
	}

	@Override
	public AlternativeCaseTypeNode getDeclType() {
		assert isResolved();

		return type;
	}

	public static String getKindStr() {
		return "alternative case node";
	}

	public static String getUseStr() {
		return "alternative case";
	}
}
