/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 2.0
 * Copyright (C) 2008 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under GPL v3 (see LICENSE.txt included in the packaging of this file)
 */

/**
 * @author shack, Daniel Grund
 * @version $Id$
 */
package de.unika.ipd.grgen.ir;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A graph rewrite rule or subrule, with none, one, or arbitrary many (not yet) replacements.
 */
public class Rule extends MatchingAction {
	/** Names of the children of this node. */
	private static final String[] childrenNames = {
		"left", "right", "eval"
	};

	/** The right hand side of the rule. */
	private final PatternGraph right;

	/** The evaluation assignments of this rule. */
	private final Collection<Assignment> evals = new LinkedList<Assignment>();

	/** A list of the replacement parameters */
	private final List<Entity> replParams = new LinkedList<Entity>();


	/**
	 * Make a new rule.
	 * @param ident The identifier with which the rule was declared.
	 * @param left The left side graph of the rule.
	 * @param right The right side graph of the rule.
	 */
	public Rule(Ident ident, PatternGraph left, PatternGraph right) {
		super("rule", ident, left);
		setChildrenNames(childrenNames);
		this.right = right;
		if(right==null) {
			left.setNameSuffix("test");
		}
		else {
			left.setName("L");
			right.setName("R");
		}
	}

	/** @return A collection containing all eval assignments of this rule. */
	public Collection<Assignment> getEvals() {
		return Collections.unmodifiableCollection(evals);
	}

	/** Add an assignment to the list of evaluations. */
	public void addEval(Assignment a) {
		evals.add(a);
	}

	/** Add a replacement parameter to the rule. */
	public void addReplParameter(Node id) {
		replParams.add(id);
	}

	/** Get all replacement parameters of this rule (may currently contain only nodes). */
	public List<Entity> getReplParameters() {
		return Collections.unmodifiableList(replParams);
	}

	/**
	 *  @return A set with nodes, that occur on the left _and_ on the right side of the rule.
	 *  		The set also contains retyped nodes.
	 */
	public Collection<Node> getCommonNodes() {
		Collection<Node> common = new HashSet<Node>(pattern.getNodes());
		common.retainAll(right.getNodes());
		return common;
	}

	/**
	 * @return A set with edges, that occur on the left _and_ on the right side of the rule.
	 *         The set also contains all retyped edges.
	 */
	public Collection<Edge> getCommonEdges() {
		Collection<Edge> common = new HashSet<Edge>(pattern.getEdges());
		common.retainAll(right.getEdges());
		return common;
	}

	/** @return A set with subpatterns, that occur on the left _and_ on the right side of the rule. */
	public Collection<SubpatternUsage> getCommonSubpatternUsages() {
		Collection<SubpatternUsage> common = new HashSet<SubpatternUsage>(pattern.getSubpatternUsages());
		common.retainAll(right.getSubpatternUsages());
		return common;
	}

	/** @return The left hand side graph. */
	public PatternGraph getLeft() {
		return pattern;
	}

	/** @return The right hand side graph. */
	public PatternGraph getRight() {
		return right;
	}
}
