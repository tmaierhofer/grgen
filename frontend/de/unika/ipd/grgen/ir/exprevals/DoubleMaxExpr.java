/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2018 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

package de.unika.ipd.grgen.ir.exprevals;

public class DoubleMaxExpr extends Expression {
	public DoubleMaxExpr() {
		super("doublemax expr", DoubleType.getType());
	}

	public void collectNeededEntities(NeededEntities needs) {
	}
}
