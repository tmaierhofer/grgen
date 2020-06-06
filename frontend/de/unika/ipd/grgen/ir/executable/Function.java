/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ir.executable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.unika.ipd.grgen.ir.ContainedInPackage;
import de.unika.ipd.grgen.ir.Entity;
import de.unika.ipd.grgen.ir.Ident;
import de.unika.ipd.grgen.ir.stmt.EvalStatement;
import de.unika.ipd.grgen.ir.type.Type;

/**
 * A function.
 */
public class Function extends FunctionBase implements ContainedInPackage
{
	private String packageContainedIn;

	/** A list of the parameters */
	private List<Entity> params = new LinkedList<Entity>();

	/** A list of the parameter types, computed from the parameters */
	private List<Type> parameterTypes = null;

	/** The computation statements */
	private List<EvalStatement> computationStatements = new LinkedList<EvalStatement>();

	public Function(String name, Ident ident, Type retType)
	{
		super(name, ident, retType);
	}

	@Override
	public String getPackageContainedIn()
	{
		return packageContainedIn;
	}

	public void setPackageContainedIn(String packageContainedIn)
	{
		this.packageContainedIn = packageContainedIn;
	}

	/** Add a parameter to the function. */
	public void addParameter(Entity entity)
	{
		params.add(entity);
	}

	/** Get all parameters of this function. */
	public List<Entity> getParameters()
	{
		return Collections.unmodifiableList(params);
	}

	/** Add a computation statement to the function. */
	public void addComputationStatement(EvalStatement eval)
	{
		computationStatements.add(eval);
	}

	/** Get all computation statements of this function. */
	public List<EvalStatement> getComputationStatements()
	{
		return Collections.unmodifiableList(computationStatements);
	}

	/** Get all parameter types of this external function. */
	@Override
	public List<Type> getParameterTypes()
	{
		if(parameterTypes == null) {
			parameterTypes = new LinkedList<Type>();
			for(Entity entity : getParameters()) {
				parameterTypes.add(entity.getType());
			}
		}
		return Collections.unmodifiableList(parameterTypes);
	}
}
