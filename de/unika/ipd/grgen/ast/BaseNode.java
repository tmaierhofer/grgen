/**
 * Created: Wed Jul  2 15:29:49 2003
 *
 * @author Sebastian Hack
 * @version $Id$
 */

package de.unika.ipd.grgen.ast;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.unika.ipd.grgen.ast.util.Checker;
import de.unika.ipd.grgen.ast.util.Resolver;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.parser.Coords;
import de.unika.ipd.grgen.parser.Scope;
import de.unika.ipd.grgen.util.Base;
import de.unika.ipd.grgen.util.BooleanResultVisitor;
import de.unika.ipd.grgen.util.GraphDumpable;
import de.unika.ipd.grgen.util.GraphDumper;
import de.unika.ipd.grgen.util.PostWalker;
import de.unika.ipd.grgen.util.PrePostWalker;
import de.unika.ipd.grgen.util.PreWalker;
import de.unika.ipd.grgen.util.Walkable;
import de.unika.ipd.grgen.util.Walker;

/**
 * The base class for AST nodes.
 * Base AST storage in ANTLR is insufficient due to the
 * children/sibling storing scheme. This reimplemented here.
 *
 */
public abstract class BaseNode extends Base
	implements GraphDumpable, Walkable {
	
	/**
	 * AST global name map, that maps from Class to String.
	 * The reason for this is, that in some situations, only the Class object
	 * is available and no instance of the class itself.
	 */
	private static final Map names = new HashMap();
	
	/** coordinates for builtin types and declarations */
	public static final Coords BUILTIN = new Coords(0, 0, "<builtin>");
	
	/** A dummy AST node used in case of an error */
	protected static final BaseNode NULL = new NullNode();
	
	/** Default array with the children names of the node */
	private static final String[] noChildrenNames = { };
	
	/** The current scope, with hich the scopes of the new BaseNodes are initialized. */
	private static Scope currScope = Scope.getInvalid();
	
	/** Print verbose error messages. */
	private static boolean verboseErrorMsg = true;
	
	/** Has this base node already been checked? */
	private boolean checked = false;
	
	/** Has this base node already been type checked? */
	private boolean typeChecked = false;
	
	/** The result of the check, if checked. */
	private boolean checkResult = false;
	
	/** The result of the type checking. */
	private boolean typeCheckResult = false;
	
	/** The list of resolvers. */
	private Map resolvers = new HashMap();
	
	/** The scope in which this node occurred. */
	private Scope scope;
	
	/** array with the children names */
	private String[] childrenNames = noChildrenNames;
	
	/** Location in the source corresponding to this node */
	private Coords coords = Coords.getInvalid();
	
	/** Vector of the children of this node */
	private final Vector children = new Vector();
	
	/** The parent node of this node. */
	private Set parents = new HashSet();
	
	/** The ir object for this node. */
	private IR irObject = null;
	
	/** The result of the resulotion. */
	private boolean resolveResult = false;
	
	/** Has this base node already be resolved. */
	private boolean resolved = false;
	
	/**
	 * Strip the package name from the class name.
	 * @param cls The class.
	 * @return stripped class name.
	 */
	protected static String shortClassName(Class cls) {
		String s = cls.getName();
		return s.substring(s.lastIndexOf('.') + 1);
	}
	
	/**
	 * Gets an error node
	 * @return an error node
	 */
	public static BaseNode getErrorNode() {
		return NULL;
	}
	
	/**
	 * Get the name of a class.
	 * <code>cls</code> should be the Class object of a subclass of
	 * <code>BaseNode</code>. If this class is registered in the {@link #names}
	 * map, the name is returned, otherwise the name of the class.
	 * @param cls A class to get its name.
	 * @return The registered name of the class or the class name.
	 */
	public static String getName(Class cls) {
		return names.containsKey(cls) ? (String) names.get(cls)
			: "<" + shortClassName(cls) + ">";
	}
	
	/**
	 * Set the name of a AST node class.
	 * @param cls The AST node class.
	 * @param name A human readable name for that class.
	 */
	public static void setName(Class cls, String name) {
		names.put(cls, name);
	}
	
	/**
	 * Set a new current scope.
	 * This function is called from the parser as new scopes are entered
	 * or left.
	 * @param scope The new current scope.
	 */
	public static void setCurrScope(Scope scope) {
		currScope = scope;
	}
	
	/**
	 * Enable or disable more verbose messages.
	 * @param verbose If true, the AST classes generate slightly more verbose
	 * error messages.
	 */
	public static void setVerbose(boolean verbose) {
		verboseErrorMsg = verbose;
	}
	
	/**
	 * Check the whole AST with a given root.
	 * @param node The root.
	 * @return true, if every node in the AST is right, false, if not.
	 */
	public static final boolean checkAST(BaseNode node) {
		
		BooleanResultVisitor visitor = new BooleanResultVisitor(true) {
			public void visit(Walkable w) {
				BaseNode n = (BaseNode) w;
				boolean res = n.getCheck();
				if(!res)
					setResult(false);
			}
		};
		
		Walker w = new PostWalker(visitor);
		w.walk(node);
		
		return visitor.booleanResult();
	}
	
	/**
	 * Resolve the whole AST with a given root.
	 * @param node The root.
	 * @return true, if every node in the AST was resolved right, false, if not.
	 */
	public static final boolean resolveAST(BaseNode node) {
		
		BooleanResultVisitor visitor = new BooleanResultVisitor(true) {
			public void visit(Walkable w) {
				BaseNode n = (BaseNode) w;
				boolean res = n.getResolve();
				if(!res)
					setResult(false);
			}
		};
		
		Walker w = new PreWalker(visitor);
		w.walk(node);
		
		return visitor.booleanResult();
	}
	
	/**
	 * Finish up the AST.
	 * This method runs all resolvers, checks the AST and type checks it.
	 * It should be called after complete AST construction from
	 * the driver.
	 * @param node The root node of the AST.
	 * @return true, if everything went right, false, if not.
	 */
	public static final boolean manifestAST(BaseNode node) {
		
		// Resolve visitor
		final BooleanResultVisitor resolveVisitor = new BooleanResultVisitor(true) {
			public void visit(Walkable w) {
				BaseNode n = (BaseNode) w;
				boolean res = n.getResolve();
				if(!res)
					setResult(false);
			}
		};
		
		// check and type check visitor
		final BooleanResultVisitor checkVisitor = new BooleanResultVisitor(true) {
			public void visit(Walkable w) {
				BaseNode n = (BaseNode) w;
				boolean correctlyResolved = n.getResolve();
				if(correctlyResolved) {
					boolean childCheck = n.getCheck();
					boolean typeCheck = false;
					if(childCheck)
						typeCheck = n.getTypeCheck();
					
					if(!(childCheck && typeCheck))
						setResult(false);
				}
			}
		};
		
		Walker w = new PrePostWalker(resolveVisitor, checkVisitor);
		w.walk(node);
		return resolveVisitor.booleanResult() && checkVisitor.booleanResult();
	}
	
	/**
	 * Make new base node with coordinates.
	 * @param coords The coords of this node.
	 */
	protected BaseNode(Coords coords) {
		this();
		this.coords = coords;
	}
	
	/**
	 * Make a new base node without a location.
	 * It is assumed, that the location is set afterwards using
	 * {@link #setLocation(Location)}.
	 */
	protected BaseNode() {
		this.scope = currScope;
	}
	
	/**
	 * Get the parent node of this node.
	 * @return The parent node of this node. null if this node is the root.
	 */
	protected Iterator getParents() {
		return parents.iterator();
	}
	
	/**
	 * Check, if this AST node is a root node (i.e. it has no predecessors)
	 * @return true, if it's a root node, false, if not.
	 */
	public boolean isRoot() {
		return parents.isEmpty();
	}
	
	/**
	 * Set the names of the children of this node.
	 * By default the children names array is empty, so the number of
	 * the child is used as its name.
	 * @param names An array containing the names of the children.
	 */
	protected void setChildrenNames(String[] names) {
		childrenNames = names;
	}
	
	/**
	 * Get the coords of this node.
	 * @return The coords.
	 */
	public Coords getCoords() {
		return coords;
	}
	
	/**
	 * Set the location of this node.
	 * @param loc The location.
	 */
	public void setCoords(Coords coords) {
		this.coords = coords;
	}
	
	/**
	 * Get the name of this node.
	 * @return The name
	 */
	public String getName() {
		Class cls = getClass();
		String name = getName(cls);
		
		if(verboseErrorMsg)
			name += " <" + getId() + "," + shortClassName(cls) + ">";
		
		return name;
	}
	
	/**
	 * Set the name of the node.
	 * @param name The new name.
	 */
	protected void setName(String name) {
		names.put(getClass(), name);
	}
	
	/**
	 * Get the scope of the AST node.
	 * @return The scope in which the base node was created.
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * @return true, if this node is an error node
	 */
	public boolean isError() {
		return false;
	}
	
	/**
	 * Report an error message concerning this node
	 * @param msg The message to report.
	 */
	public final void reportError(String msg) {
		error.error(getCoords(), "At " + getName() + ": " + msg + ".");
	}
	
	/**
	 * Add a child to the children list.
	 * @param n AST node to add to the children list.
	 */
	public final void addChild(BaseNode n) {
		children.add(n);
		n.parents.add(this);
	}
	
	/**
	 * Add the children of another node to this one.
	 * @param n The other node
	 */
	public final void addChildren(BaseNode n) {
		Iterator it = n.getChildren();
		while(it.hasNext()) {
			addChild((BaseNode) it.next());
		}
	}
	
	/**
	 * Get the children of this node
	 * @return An iterator which will iterate over all child nodes of this one.
	 */
	public final Iterator getChildren() {
		return children.iterator();
	}
	
	/**
	 * Get the child at a given position
	 * @param i The position to get the child from
	 * @return The child at position i, or a Error node, if this node contained
	 * less than i nodes.
	 */
	public final BaseNode getChild(int i) {
		return i < children.size() ? (BaseNode) children.get(i) : NULL;
	}
	
	/**
	 * Get the number of children this node has.
	 * @return The number of children of this node.
	 */
	public final int children() {
		return children.size();
	}
	
	/**
	 * Replace the child at a given position.
	 * @param i The position.
	 * @param n The the new child to be replace the old.
	 * @return The old one.
	 */
	public final BaseNode replaceChild(int i, BaseNode n) {
		BaseNode res = NULL;
		if(i < children.size()) {
			res = (BaseNode) children.get(i);
			children.set(i, n);
			n.parents.add(this);
		}
		
		return res;
	}
	
	/**
	 * Replace this node with another one.
	 * This AST node becomes replaced by another node in all its parents.
	 * @param n The other node.
	 * @return This node.
	 */
	public final BaseNode replaceWith(BaseNode n) {
		for(Iterator it = parents.iterator(); it.hasNext();) {
			BaseNode parent = (BaseNode) it.next();
			int index = parent.children.indexOf(this);
			assert index >= 0 : "Node must be in the children array of its parent";
			
			parent.replaceChild(index, n);
		}
		
		return this;
	}
	
	/**
	 * Checks a child of this node to of a certain type.
	 * If it is not, an error is reported via the ErrorFacility.
	 * @param child The number of the child to check
	 * @param cls The class the child has to be of
	 * @return true, if the selected child node was of type cls
	 */
	public final boolean checkChild(int childNum, Class cls) {
		boolean res = false;
		
		BaseNode child = getChild(childNum);
		if(cls.isInstance(child))
			res = true;
		else
			reportError("child " + childNum + " \"" + child.getName() + "\""
							+ " needs to be instance of \"" + shortClassName(cls) + "\"");
		return res;
	}
	
	/**
	 * Apply a checker to a specific child
	 * @param childNum The number of the child
	 * @param checker The checker to apply
	 * @return The result of the checker applied to the child.
	 */
	public final boolean checkChild(int childNum, Checker checker) {
		return checker.check(getChild(childNum), error);
	}
	
	/**
	 * Checks whether all children of this node are an instance of a
	 * certain class
	 * @param cls The class the children should be an instance of
	 * @return true, if all children of this node were an instance of cls
	 */
	public final boolean checkAllChildren(Class cls) {
		boolean res = true;
		Iterator it = getChildren();
		
		while(it.hasNext()) {
			Object n = it.next();
			if(!cls.isInstance(n))
				res = false;
		}
		return res;
	}
	
	/**
	 * Check all children with a checker. The checker is applied to each
	 * child of this node
	 * @see Checker
	 * @param c The checker
	 * @return true, if the checker returned true for every node, false
	 * otherwise.
	 */
	public final boolean checkAllChildren(Checker c) {
		boolean res = true;
		Iterator it = getChildren();
		
		while(it.hasNext()) {
			BaseNode n = (BaseNode) it.next();
			boolean checkRes = c.check(n, error);
			res = res && checkRes;
		}
		
		return res;
	}
	
	/**
	 * Check the sanity of this AST node. This ensures, that all the children have
	 * correct types. subclasses have to implement that the right way.
	 * @return true, if this node is in a correct state.
	 */
	protected boolean check() {
		return true;
	}
	
	/**
	 * Check the types of this AST node.
	 * Subclasses should implement this, if neccessary.
	 * @return true, if all types are right. False, if not.
	 */
	protected boolean typeCheck() {
		return true;
	}
	
	/**
	 * Check this AST node.
	 * If the node has been checked before, the result of the former
	 * check ist returned.
	 * @return true, if the node is ok, false if not.
	 */
	public final boolean getCheck() {
		if(!checked) {
			checked = true;
			checkResult = check();
		}
		
		return checkResult;
	}
	
	/**
	 * Check, if all types on this AST node are right.
	 * @return true, if all types were right, false, if not.
	 */
	public final boolean getTypeCheck() {
		if(!typeChecked) {
			typeChecked = true;
			typeCheckResult = typeCheck();
		}
		
		return typeCheckResult;
	}
	
	/**
	 * Extra info for the node, that is used by {@link #getNodeInfo()}
	 * to compose the node info.
	 * @return extra info for the node (return null, if no extra info
	 * shall be available).
	 */
	protected String extraNodeInfo() {
		return null;
	}
	
	/**
	 * Ordinary to string cast method
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpableNode#getNodeColor()
	 */
	public Color getNodeColor() {
		return Color.WHITE;
	}
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpableNode#getNodeId()
	 */
	public String getNodeId() {
		return getId();
	}
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpableNode#getNodeInfo()
	 */
	public String getNodeInfo() {
		String extra = extraNodeInfo();
		return "ID: " + getId() + (extra != null ? "\n" + extra : "");
	}
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpableNode#getNodeLabel()
	 */
	public String getNodeLabel() {
		return this.toString();
	}
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpableNode#getNodeShape()
	 */
	public int getNodeShape() {
		return GraphDumper.DEFAULT;
	}
	
	/**
	 * @see de.unika.ipd.grgen.util.GraphDumpable#getEdgeLabel(int)
	 */
	public String getEdgeLabel(int edge) {
		return edge < childrenNames.length ? childrenNames[edge] : "" + edge;
	}
	
	/**
	 * The walkable children are the children of this node
	 * @see de.unika.ipd.grgen.util.Walkable#getWalkableChildren()
	 */
	public Iterator getWalkableChildren() {
		return children.iterator();
	}
	
	/**
	 * Get the IR object for this AST node.
	 * This method gets the IR object, if it was already constructed.
	 * If not, it calls {@link #constructIR()} to construct the
	 * IR object and stores the result. This assures, that for each AST
	 * node, {@link #constructIR()} is just called once.
	 * @return The constructed/stored IR object.
	 */
	public final IR getIR() {
		if(irObject == null)
			irObject = constructIR();
		return irObject;
	}
	
	/**
	 * Checks, if the ir object of this ast node is instance of a certain
	 * Class <code>cls</code>.
	 * If it is not, an assertion is raised, else, the ir object
	 * is returned.
	 * @param cls The class to check the ir object for.
	 * @return The ir object.
	 */
	public final IR checkIR(Class cls) {
		IR ir = getIR();
		String msg = "checking ir object in \"" + getName()
			+ "\" should be \"" + cls + "\" is \"" + ir.getClass() + "\"";
		
		debug.report(NOTE, msg);
		assert cls.isInstance(ir) : msg;
		
		return ir;
	}
	
	/**
	 * Construct the IR object.
	 * This method should never be called. It is used by {@link #getIR()}.
	 * @return The constructed IR object.
	 */
	protected IR constructIR() {
		return IR.getBad();
	}
	
	/**
	 * Resolve the identifier nodes.
	 * Each AST node can add resolvers to a list administrated by
	 * {@link BaseNode}. These resolvers replace the identifier node in the
	 * childrens of this node by something, that can be produced out of it.
	 * For example, an identifier representing a declared type is replaced by
	 * the declared type. The behaviour depends on the {@link Resolver}.
	 *
	 * This method first call all resolvers registered in this node
	 * and descends to the this node's children by invoking
	 * {@link #getResolve()} on each child.
	 *
	 * A base node subclass can overload this method, to apply another
	 * policy of resultion.
	 *
	 * @return true, if all resolvers finished their job and no error
	 * occurred, false, if there was some error.
	 */
	protected boolean resolve() {
		boolean local = true;
		debug.report(NOTE, "resolve in: " + getId() + "(" + getClass() + ")");
		
		for(Iterator i = resolvers.keySet().iterator(); i.hasNext();) {
			Integer pos = (Integer) i.next();
			Resolver resolver = (Resolver) resolvers.get(pos);
			
			if(!resolver.resolve(this, pos.intValue())) {
				debug.report(NOTE, "resolve error");
				local = false;
				resolver.printErrors();
			}
		}
		setResolved(local);
		return local;
	}
	
	/**
	 * Get the result of the resolution.
	 * If this node has already been resolved, then return the result of
	 * that former resolution, if not, resolve it and store the result.
	 * @return The result of the reolution. true, if everything went right,
	 * false, if something went wrong.
	 */
	public final boolean getResolve() {
		if(!resolved)
			resolveResult = resolve();
		
		return resolveResult;
	}
	
	/**
	 * Mark this node as resolved and give the result of the resolution.
	 * @param resolveResult The reult of the resultion.
	 */
	protected final void setResolved(boolean resolveResult) {
		resolved = true;
		this.resolveResult = resolveResult;
	}
	
	/**
	 * Check, if this node has been resolved already.
	 * @return true, if this node has been resolved, false, if not.
	 */
	protected final boolean isResolved() {
		return resolved;
	}
	
	/**
	 * Assert, that the node has been resolved.
	 * This function can be used in methods of sublcasses of this one,
	 * to mark that a resolution has to take place before the particular
	 * method takes place.
	 */
	protected final void assertResolved() {
		assert isResolved() : "This node has to be resolved first.";
	}
	
	/**
	 * Add a resolver to this node.
	 * If a resolver was already entered for a given position, it is
	 * overwritten. This is sensible, since sub classes may need to
	 * overwrite resolvers entered by superclasses.
	 * @see #resolve()
	 * @param pos The child to resolve.
	 * @param r A resolver to process the child at position <code>pos</code>.
	 */
	protected final void addResolver(int pos, Resolver r) {
		resolvers.put(new Integer(pos), r);
	}
	
	/**
	 * Clear all resolvers in this node.
	 */
	protected final void clearResolvers() {
		resolvers.clear();
	}
	
}
