/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 2.1
 * Copyright (C) 2008 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under GPL v3 (see LICENSE.txt included in the packaging of this file)
 */

/**
 * Model.java
 *
 * @author Sebastian Hack
 */

package de.unika.ipd.grgen.ir;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Model extends Identifiable {
	private List<Model> usedModels = new LinkedList<Model>();
	private List<Type> types = new LinkedList<Type>();

	private Set<NodeType> nodeTypes = new LinkedHashSet<NodeType>();
	private Set<EdgeType> edgeTypes = new LinkedHashSet<EdgeType>();
	private Set<EnumType> enumTypes = new LinkedHashSet<EnumType>();


	public Model(Ident ident) {
		super("model", ident);
	}

	public void addUsedModel(Model model) {
		usedModels.add(model);
		for(Type type : model.getTypes())
			addType(type);
	}

	/** Add the given type to the type model. */
	public void addType(Type type) {
		types.add(type);
		if(type instanceof NodeType) nodeTypes.add((NodeType) type);
		else if(type instanceof EdgeType) edgeTypes.add((EdgeType) type);
		else if(type instanceof EnumType) enumTypes.add((EnumType) type);
		else if(!(type instanceof PrimitiveType))
			assert false : "Unexpected type added to model: " + type;
	}

	/** @return The types in the type model. */
	public Collection<Type> getTypes() {
		return Collections.unmodifiableCollection(types);
	}

	public Collection<NodeType> getNodeTypes() {
		return Collections.unmodifiableCollection(nodeTypes);
	}

	public Collection<EdgeType> getEdgeTypes() {
		return Collections.unmodifiableCollection(edgeTypes);
	}

	public Collection<EnumType> getEnumTypes() {
		return Collections.unmodifiableCollection(enumTypes);
	}

	public Collection<Model> getUsedModels() {
		return Collections.unmodifiableCollection(usedModels);
	}

	/** Canonicalize the type model. */
	protected void canonicalizeLocal() {
		//Collections.sort(types, Identifiable.COMPARATOR);
		//Collections.sort(types);

		for(Type ty : types) {
			ty.canonicalize();
			if (ty instanceof EdgeType)
				((EdgeType)ty).canonicalizeConnectionAsserts();
		}
	}

	void addToDigest(StringBuffer sb) {
		sb.append(this);
		sb.append('[');

		for(Model model : usedModels)
			model.addToDigest(sb);

		for(Type ty : types) {
			ty.addToDigest(sb);
		}

		sb.append(']');
	}

	public void addFields(Map<String, Object> fields) {
		super.addFields(fields);
		fields.put("usedModels", usedModels.iterator());
		fields.put("types", types.iterator());
	}
}

