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
 * @author shack
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ast.util.Checker;
import de.unika.ipd.grgen.ast.util.CollectChecker;
import de.unika.ipd.grgen.ast.util.CollectResolver;
import de.unika.ipd.grgen.ast.util.DeclarationResolver;
import de.unika.ipd.grgen.ast.util.SimpleChecker;
import de.unika.ipd.grgen.ir.Action;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.Model;
import de.unika.ipd.grgen.ir.Unit;

/**
 * The main node of the text. It is the root of the AST.
 */
public class UnitNode extends BaseNode {
	static {
		setName(UnitNode.class, "unit declaration");
	}

	CollectNode<BaseNode> models;

	// of type PatternTestDeclNode or PatternRuleDeclNode
	CollectNode<PatternTestDeclNode> subpatterns;
	CollectNode<IdentNode> subpatternsUnresolved;

	// of type TestDeclNode or RuleDeclNode
	CollectNode<TestDeclNode> actions;
	CollectNode<IdentNode> actionsUnresolved;

	/**
	 * The name for this unit node
	 */
	private String unitname;

	/**
	 * The filename for this main node.
	 */
	private String filename;

	public UnitNode(String unitname, String filename, CollectNode<BaseNode> models, CollectNode<IdentNode> subpatterns, CollectNode<IdentNode> actions) {
		this.models = models;
		becomeParent(this.models);
		this.subpatternsUnresolved = subpatterns;
		becomeParent(this.subpatternsUnresolved);
		this.actionsUnresolved = actions;
		becomeParent(this.actionsUnresolved);
		this.unitname = unitname;
		this.filename = filename;
	}

	/** returns children of this node */
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(models);
		children.add(getValidVersion(subpatternsUnresolved, subpatterns));
		children.add(getValidVersion(actionsUnresolved, actions));
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("models");
		childrenNames.add("subpatterns");
		childrenNames.add("actions");
		return childrenNames;
	}

	private static final CollectResolver<TestDeclNode> actionsResolver = new CollectResolver<TestDeclNode>(
			new DeclarationResolver<TestDeclNode>(TestDeclNode.class));

	private static final CollectResolver<PatternTestDeclNode> subpatternsResolver = new CollectResolver<PatternTestDeclNode>(
			new DeclarationResolver<PatternTestDeclNode>(PatternTestDeclNode.class));

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolveLocal() */
	protected boolean resolveLocal() {
		actions     = actionsResolver.resolve(actionsUnresolved, this);
		subpatterns = subpatternsResolver.resolve(subpatternsUnresolved, this);

		return actions != null && subpatterns != null;
	}

	/** Check the collect nodes containing the model declarations, subpattern declarations, action declarations
	 *  @see de.unika.ipd.grgen.ast.BaseNode#checkLocal() */
	protected boolean checkLocal() {
		Checker modelChecker = new CollectChecker(new SimpleChecker(ModelNode.class));
		return modelChecker.check(models, error);
	}

	/**
	 * Get the IR unit node for this AST node.
	 * @return The Unit for this AST node.
	 */
	public Unit getUnit() {
		return (Unit) checkIR(Unit.class);
	}

	/**
	 * Construct the IR object for this AST node.
	 * For a main node, this is a unit.
	 * @see de.unika.ipd.grgen.ast.BaseNode#constructIR()
	 */
	protected IR constructIR() {
		Unit res = new Unit(unitname, filename);

		for(BaseNode n : models.getChildren()) {
			Model model = ((ModelNode)n).getModel();
			res.addModel(model);
		}

		for(PatternTestDeclNode n : subpatterns.getChildren()) {
			Action act = n.getAction();
			res.addSubpattern(act);
		}

		for(TestDeclNode n : actions.getChildren()) {
			Action act = n.getAction();
			res.addAction(act);
		}

		return res;
	}
}
