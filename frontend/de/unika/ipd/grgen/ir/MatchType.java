/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ir;

public class MatchType extends Type implements ContainedInPackage {
	private String packageContainedIn;
	private Rule action;

	public MatchType(Rule action) {
		super("match type", action.getIdent());
		this.action = action;
	}

	protected MatchType(Rule action, Ident iterated) {
		super("match type", iterated);
		this.action = action;
	}

	public String getPackageContainedIn() {
		return packageContainedIn;
	}
	
	public void setPackageContainedIn(String packageContainedIn) {
		this.packageContainedIn = packageContainedIn;
	}

	public Rule getAction() {
		return action;
	}

	/** @see de.unika.ipd.grgen.ir.Type#classify() */
	public int classify() {
		return IS_MATCH;
	}
}
