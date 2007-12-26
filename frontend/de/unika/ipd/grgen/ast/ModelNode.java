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
 * ModelNode.java
 *
 * @author Sebastian Hack
 */

package de.unika.ipd.grgen.ast;

import de.unika.ipd.grgen.ast.util.*;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.ir.Ident;
import de.unika.ipd.grgen.ir.Model;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;


public class ModelNode extends DeclNode
{
	static {
		setName(ModelNode.class, "model declaration");
	}
	
	protected static final TypeNode modelType = new ModelTypeNode();
	
	/** Index of the decl collect node in the children. */
	protected static final int DECLS = 2;
		
	public ModelNode(IdentNode id) {
		super(id, modelType);
	}
	
	/** implementation of Walkable @see de.unika.ipd.grgen.util.Walkable#getWalkableChildren() */
	public Collection<? extends BaseNode> getWalkableChildren() {
		return children;
	}

	/** get names of the walkable children, same order as in getWalkableChildren */
	public Collection<String> getChildrenNames() {
		Vector<String> childrenNames = new Vector<String>();
		childrenNames.add("ident"); 
		childrenNames.add("type");
		childrenNames.add("decls");
		return childrenNames;
	}

  	/** @see de.unika.ipd.grgen.ast.BaseNode#resolve() */
	protected boolean resolve() {
		if(isResolved()) {
			return resolutionResult();
		}
		
		debug.report(NOTE, "resolve in: " + getId() + "(" + getClass() + ")");
		boolean successfullyResolved = true;
		Resolver declResolver = new CollectResolver(new DeclResolver(TypeDeclNode.class));
		successfullyResolved = declResolver.resolve(this, DECLS) && successfullyResolved;
		nodeResolvedSetResult(successfullyResolved); // local result
		if(!successfullyResolved) {
			debug.report(NOTE, "resolve error");
		}
		
		successfullyResolved = getChild(IDENT).resolve() && successfullyResolved;
		successfullyResolved = getChild(TYPE).resolve() && successfullyResolved;
		successfullyResolved = getChild(DECLS).resolve() && successfullyResolved;
		return successfullyResolved;
	}
	
	/** @see de.unika.ipd.grgen.ast.BaseNode#check() */
	protected boolean check() {
		if(!resolutionResult()) {
			return false;
		}
		if(isChecked()) {
			return getChecked();
		}
		
		boolean childrenChecked = true;
		if(!visitedDuringCheck()) {
			setCheckVisited();
			
			childrenChecked = getChild(IDENT).check() && childrenChecked;
			childrenChecked = getChild(TYPE).check() && childrenChecked;
			childrenChecked = getChild(DECLS).check() && childrenChecked;
		}
		
		boolean locallyChecked = checkLocal();
		nodeCheckedSetResult(locallyChecked);
		
		return childrenChecked && locallyChecked;
	}
	
	/**
	 * The main node has an ident node and a collect node with
	 * - group declarations
	 * - edge class decls
	 * - node class decls
	 * as child.
	 * @see de.unika.ipd.grgen.ast.BaseNode#checkLocal()
	 */
	protected boolean checkLocal() {
		Checker checker = new CollectChecker(new SimpleChecker(TypeDeclNode.class));
		return checker.check(getChild(DECLS), error) && checkInhCycleFree();
	}
	
	/**
	 * Get the IR model node for this AST node.
	 * @return The model for this AST node.
	 */
	public Model getModel() {
		return (Model) checkIR(Model.class);
	}
	
	/**
	 * Construct the IR object for this AST node.
	 * For a main node, this is a unit.
	 * @see de.unika.ipd.grgen.ast.BaseNode#constructIR()
	 */
	protected IR constructIR()
	{
		Ident id = (Ident) getChild(IDENT).checkIR(Ident.class);
		Model res = new Model(id);
		for(BaseNode children : getChild(DECLS).getChildren()) {
			TypeDeclNode typeDecl = (TypeDeclNode)children;
			res.addType(((TypeNode) typeDecl.getDeclType()).getType());
		}
		return res;
	}
	
	private boolean checkInhCycleFree_rec(InheritanceTypeNode inhType,
										  Collection<BaseNode> inProgress,
										  Collection<BaseNode> done)
	{
		inProgress.add(inhType);
		for (BaseNode t : inhType.getDirectSuperTypes()) {
			if ( ! (t instanceof InheritanceTypeNode)) {
				continue;
			}

			assert (
				((inhType instanceof NodeTypeNode) && (t instanceof NodeTypeNode)) ||
				((inhType instanceof EdgeTypeNode) && (t instanceof EdgeTypeNode))
			): "nodes should extend nodes and edges should extend edges";

			InheritanceTypeNode superType = (InheritanceTypeNode) t;
			
			if ( inProgress.contains(superType) ) {
				inhType.getIdentNode().reportError(
					"\"" + inhType.getIdentNode() + "\" extends \"" + superType.getIdentNode() +
					"\", which introduces a cycle to the type hierarchy");
				return false;
			}
			if ( ! done.contains(superType) ) {
				if ( ! checkInhCycleFree_rec(superType, inProgress, done) ) {
					return false;
				}
			}
		}
		inProgress.remove(inhType);
		done.add(inhType);
		return true;
	}
	
	/**
	 * ensure there are no cycles in the inheritance hierarchy
	 * @return	<code>true</code> if there are no cycles,
	 * 			<code>false</code> otherwise
	 */
	private boolean checkInhCycleFree()
	{
		Collection<BaseNode> coll = getChild(DECLS).getChildren();
		for (BaseNode t : coll) {
			TypeNode type = (TypeNode) ((TypeDeclNode)t).getDeclType();

			if ( !(type instanceof InheritanceTypeNode) ) { 
				continue ;
			}

			Collection<BaseNode> inProgress = new HashSet<BaseNode>();
			Collection<BaseNode> done = new HashSet<BaseNode>();
			
			boolean isCycleFree =
				checkInhCycleFree_rec( (InheritanceTypeNode)type, inProgress, done);

			if ( ! isCycleFree ) {
				return false;
			}
		}
		return true;
	}
			
			
	/*
	Collection<BaseNode> alreadyExtended = new HashSet<BaseNode>();
	TypeNode type = (TypeNode) ((TypeDeclNode)t).getDeclType();
	alreadyExtended.add(type);

	for ( BaseNode tt : alreadyExtended ) {
		
		if ( !(tt instanceof InheritanceTypeNode) ) continue ;

		InheritanceTypeNode inhType = (InheritanceTypeNode) tt;
		Collection<BaseNode> superTypes = inhType.getDirectSuperTypes();

		for ( BaseNode s : superTypes ) {
			if ( alreadyExtended.contains(s) ) {
				s.reportError("extending \"" + s + "\" causes cyclic inheritance");
				return false;
			}
		}
		alreadyExtended.addAll(superTypes);
	}
	*/
}

