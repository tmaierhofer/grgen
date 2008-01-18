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
 * @author Sebastian Hack
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;

import de.unika.ipd.grgen.ast.util.DeclarationResolver;
import de.unika.ipd.grgen.ast.util.SimpleChecker;
import de.unika.ipd.grgen.ir.Entity;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.Qualification;
import de.unika.ipd.grgen.parser.Coords;
import java.util.Collection;
import java.util.Vector;

/**
 * AST node that represents a qualified identifier
 * i.e. expressions like this one: a.b.c.d
 */
public class QualIdentNode extends BaseNode implements DeclaredCharacter {
	static {
		setName(QualIdentNode.class, "Qualification");
	}

	protected IdentNode owner;
	private DeclNode resolvedOwner;

	protected IdentNode member;
	private MemberDeclNode resolvedMember;

	/**
	 * Make a new identifier qualify node.
	 * @param coords The coordinates.
	 */
	public QualIdentNode(Coords coords, IdentNode owner, IdentNode member) {
		super(coords);
		this.owner = owner;
		becomeParent(this.owner);
		this.member = member;
		becomeParent(this.member);
	}

	/** returns children of this node */
	public Collection<BaseNode> getChildren() {
		Vector<BaseNode> children = new Vector<BaseNode>();
		children.add(getValidVersion(owner, resolvedOwner));
		children.add(getValidVersion(member, resolvedMember));
		return children;
	}

	/** returns names of the children, same order as in getChildren */
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("owner");
		childrenNames.add("member");
		return childrenNames;
	}

	/** @see de.unika.ipd.grgen.ast.BaseNode#resolve() */
	protected boolean resolve() {
		if(isResolved()) {
			return resolutionResult();
		}

		/* 1) resolve left hand side identifier, yielding a declaration of a type owning a scope
		 * 2) the scope owned by the lhs allows the ident node of the right hand side to fix/find its definition therein
		 * 3) resolve now complete/correct right hand side identifier into its declaration */
		boolean successfullyResolved = true;
		DeclarationResolver<DeclNode> ownerResolver = new DeclarationResolver<DeclNode>(DeclNode.class);
		resolvedOwner = ownerResolver.resolve(owner);
		successfullyResolved = resolvedOwner!=null && successfullyResolved;
		ownedResolutionResult(owner, resolvedOwner);

		if (resolvedOwner != null && (resolvedOwner instanceof NodeCharacter || resolvedOwner instanceof EdgeCharacter)) {
			TypeNode ownerType = (TypeNode)resolvedOwner.getDeclType();

			if(ownerType instanceof ScopeOwner) {
				ScopeOwner o = (ScopeOwner) ownerType;
				o.fixupDefinition(member);
				DeclarationResolver<MemberDeclNode> memberResolver = new DeclarationResolver<MemberDeclNode>(MemberDeclNode.class);
				resolvedMember = memberResolver.resolve(member);
				successfullyResolved = resolvedMember!=null && successfullyResolved;
				ownedResolutionResult(member, resolvedMember);
			} else {
				reportError("Left hand side of '.' does not own a scope");
				successfullyResolved = false;
			}
		} else {
			reportError("Left hand side of '.' is neither a node nor an edge");
			successfullyResolved = false;
		}
		nodeResolvedSetResult(successfullyResolved); // local result
		if(!successfullyResolved) {
			debug.report(NOTE, "resolve error");
		}

		successfullyResolved = owner.resolve() && successfullyResolved;
		successfullyResolved = member.resolve() && successfullyResolved;
		return successfullyResolved;
	}

	/** @see de.unika.ipd.grgen.ast.BaseNode#checkLocal() */
	protected boolean checkLocal() {
		return (new SimpleChecker(DeclNode.class)).check(getValidVersion(owner, resolvedOwner), error)
			& (new SimpleChecker(MemberDeclNode.class)).check(getValidVersion(member, resolvedMember), error);
	}

	/** @see de.unika.ipd.grgen.ast.DeclaredCharacter#getDecl() */
	public DeclNode getDecl() {
		assert isResolved();

		return resolvedMember;
	}

	public DeclNode getOwner() {
		assert isResolved();

		return resolvedOwner;
	}

	protected IR constructIR() {
		Entity owner = (Entity) getValidVersion(this.owner, resolvedOwner).checkIR(Entity.class);
		Entity member = (Entity) getValidVersion(this.member, resolvedMember).checkIR(Entity.class);

		return new Qualification(owner, member);
	}
}
