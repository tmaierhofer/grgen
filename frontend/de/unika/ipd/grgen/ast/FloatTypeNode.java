/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 3.5
 * Copyright (C) 2003-2012 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

package de.unika.ipd.grgen.ast;

import de.unika.ipd.grgen.ir.FloatType;

/**
 * The floating point basic type.
 */
public class FloatTypeNode extends BasicTypeNode
{
	static {
		setName(FloatTypeNode.class, "float type");
	}

	@Override
	protected FloatType constructIR() {
		return new FloatType(getIdentNode().getIdent());
	}

	@Override
	public String toString() {
		return "float";
	}
};
