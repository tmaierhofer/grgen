/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 2.1
 * Copyright (C) 2008 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under GPL v3 (see LICENSE.txt included in the packaging of this file)
 */

/**
 * @author Sebastian Hack
 * @version $Id$
 */
package de.unika.ipd.grgen.ast;

import de.unika.ipd.grgen.ir.Edge;

/**
 * Something, that looks like an edge.
 */
public interface EdgeCharacter {

	/**
	 * Get the IR edge for this AST edge.
	 * @return The IR edge.
	 */
	Edge getEdge();

}
