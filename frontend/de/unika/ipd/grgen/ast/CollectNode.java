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
 * @file CollectNode.java
 * @author shack
 * @date Jul 21, 2003
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;

import java.awt.Color;

/**
 * An AST node that represents a collection of other nodes.
 * children: *:BaseNode
 *
 * Normally AST nodes contain a fixed number of children,
 * which are accessed by their fixed index within the children vector.
 * This node collects a statically unknown number of children AST nodes,
 * originating in unbounded list constructs in the parsing syntax.
 */
public class CollectNode extends BaseNode
{
	static {
		setName(CollectNode.class, "collect");
	}

	public CollectNode() {
		super();
	}

	/** @see de.unika.ipd.grgen.ast.BaseNode#doResolve() */
	protected boolean doResolve() {
		if(isResolved()) {
			return getResolve();
		}
		
		boolean successfullyResolved = resolve();
		for(int i=0; i<children(); ++i) {
			successfullyResolved = getChild(i).doResolve() && successfullyResolved;
		}
		return successfullyResolved;
	}
	
	/** @see de.unika.ipd.grgen.ast.BaseNode#doCheck() */
	protected boolean doCheck() {
		if(!getResolve()) {
			return false;
		}
		if(isChecked()) {
			return getChecked();
		}
		
		boolean successfullyChecked = getCheck();
		if(successfullyChecked) {
			successfullyChecked = getTypeCheck();
		}
		for(int i=0; i<children(); ++i) {
			successfullyChecked = getChild(i).doCheck() && successfullyChecked;
		}
		return successfullyChecked;
	}
	
	/**
	 * The collect node is always in a correct state.
	 * Use #checkAllChildren(Class) to check for the state
	 * of the children
	 * @see de.unika.ipd.grgen.ast.BaseNode#checkAllChildren(Class)
	 */
	protected boolean check() {
		return true;
	}

	public Color getNodeColor() {
		return Color.GRAY;
	}
}
