/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast.expr.set;

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ast.*;
import de.unika.ipd.grgen.ast.expr.ConstNode;
import de.unika.ipd.grgen.ast.expr.DeclExprNode;
import de.unika.ipd.grgen.ast.expr.ExprNode;
import de.unika.ipd.grgen.ast.typedecl.SetTypeNode;
import de.unika.ipd.grgen.ast.util.MemberResolver;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.expr.Expression;
import de.unika.ipd.grgen.ir.expr.set.SetInit;
import de.unika.ipd.grgen.ir.Entity;
import de.unika.ipd.grgen.ir.typedecl.SetType;
import de.unika.ipd.grgen.parser.Coords;

//TODO: there's a lot of code which could be handled in a common way regarding the containers set|map|array|deque
//should be unified in abstract base classes and algorithms working on them

public class SetInitNode extends ExprNode
{
	static {
		setName(SetInitNode.class, "set init");
	}

	private CollectNode<ExprNode> setItems = new CollectNode<ExprNode>();

	// if set init node is used in model, for member init
	//     then lhs != null, setType == null
	// if set init node is used in actions, for anonymous const set with specified type
	//     then lhs == null, setType != null -- adjust type of set items to this type
	private BaseNode lhsUnresolved;
	private DeclNode lhs;
	private SetTypeNode setType;

	public SetInitNode(Coords coords, IdentNode member, SetTypeNode setType)
	{
		super(coords);

		if(member != null) {
			lhsUnresolved = becomeParent(member);
		} else {
			this.setType = setType;
		}
	}

	@Override
	public Collection<? extends BaseNode> getChildren()
	{
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(setItems);
		return children;
	}

	@Override
	public Collection<String> getChildrenNames()
	{
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("setItems");
		return childrenNames;
	}

	public void addSetItem(ExprNode item)
	{
		setItems.addChild(item);
	}

	private static final MemberResolver<DeclNode> lhsResolver = new MemberResolver<DeclNode>();

	@Override
	protected boolean resolveLocal()
	{
		if(lhsUnresolved != null) {
			if(!lhsResolver.resolve(lhsUnresolved))
				return false;
			lhs = lhsResolver.getResult(DeclNode.class);
			return lhsResolver.finish();
		} else {
			if(setType == null)
				setType = createSetType();
			return setType.resolve();
		}
	}

	@Override
	protected boolean checkLocal()
	{
		boolean success = true;

		SetTypeNode setType;
		if(lhs != null) {
			TypeNode type = lhs.getDeclType();
			assert type instanceof SetTypeNode : "Lhs should be a Set<Value>";
			setType = (SetTypeNode)type;
		} else {
			setType = this.setType;
		}

		for(ExprNode item : setItems.getChildren()) {
			if(item.getType() != setType.valueType) {
				if(this.setType != null) {
					ExprNode oldValueExpr = item;
					ExprNode newValueExpr = item.adjustType(setType.valueType, getCoords());
					setItems.replace(oldValueExpr, newValueExpr);
					if(newValueExpr == ConstNode.getInvalid()) {
						success = false;
						item.reportError("Value type \"" + oldValueExpr.getType()
								+ "\" of initializer doesn't fit to value type \"" + setType.valueType + "\" of set.");
					}
				} else {
					success = false;
					item.reportError("Value type \"" + item.getType()
							+ "\" of initializer doesn't fit to value type \"" + setType.valueType
							+ "\" of set (all items must be of exactly the same type).");
				}
			}
		}

		if(lhs == null && this.setType == null) {
			this.setType = setType;
		}

		if(!isConstant() && lhs != null) {
			reportError("Only constant items allowed in set initialization in model");
			success = false;
		}

		return success;
	}

	protected SetTypeNode createSetType()
	{
		TypeNode itemTypeNode = setItems.getChildren().iterator().next().getType();
		IdentNode itemTypeIdent = ((DeclaredTypeNode)itemTypeNode).getIdentNode();
		return new SetTypeNode(itemTypeIdent);
	}

	/**
	 * Checks whether the set only contains constants.
	 * @return True, if all set items are constant.
	 */
	public boolean isConstant()
	{
		for(ExprNode item : setItems.getChildren()) {
			if(!(item instanceof ConstNode || isEnumValue(item)))
				return false;
		}
		return true;
	}

	protected boolean isEnumValue(ExprNode expr)
	{
		if(!(expr instanceof DeclExprNode))
			return false;
		if(!(((DeclExprNode)expr).isEnumValue()))
			return false;
		return true;
	}

	public boolean contains(ConstNode node)
	{
		for(ExprNode item : setItems.getChildren()) {
			if(item instanceof ConstNode) {
				ConstNode itemConst = (ConstNode)item;
				if(node.getValue().equals(itemConst.getValue()))
					return true;
			}
		}
		return false;
	}

	@Override
	public TypeNode getType()
	{
		assert(isResolved());
		if(lhs != null) {
			TypeNode type = lhs.getDeclType();
			return (SetTypeNode)type;
		} else {
			return setType;
		}
	}

	public CollectNode<ExprNode> getItems()
	{
		return setItems;
	}

	@Override
	protected IR constructIR()
	{
		Vector<Expression> items = new Vector<Expression>();
		for(ExprNode item : setItems.getChildren()) {
			items.add(item.checkIR(Expression.class));
		}
		Entity member = lhs != null ? lhs.getEntity() : null;
		SetType type = setType != null ? setType.checkIR(SetType.class) : null;
		return new SetInit(items, member, type, isConstant());
	}

	public SetInit getSetInit()
	{
		return checkIR(SetInit.class);
	}

	public static String getUseStr()
	{
		return "set initialization";
	}
}
