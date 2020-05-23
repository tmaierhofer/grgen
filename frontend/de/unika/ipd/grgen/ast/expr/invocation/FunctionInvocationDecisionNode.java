/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

/**
 * @author Edgar Jakumeit
 */

package de.unika.ipd.grgen.ast.expr.invocation;

import java.util.Collection;
import java.util.Vector;

import de.unika.ipd.grgen.ast.*;
import de.unika.ipd.grgen.ast.expr.BuiltinFunctionInvocationBaseNode;
import de.unika.ipd.grgen.ast.expr.CopyExprNode;
import de.unika.ipd.grgen.ast.expr.ExprNode;
import de.unika.ipd.grgen.ast.expr.IdentExprNode;
import de.unika.ipd.grgen.ast.expr.graph.AdjacentNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.BoundedReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.BoundedReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.BoundedReachableNodeWithRemainingDepthExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CanonizeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountAdjacentNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountBoundedReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountBoundedReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountEdgesExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountIncidentEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountNodesExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.CountReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.DefinedSubgraphExprNode;
import de.unika.ipd.grgen.ast.expr.graph.EdgeByNameExprNode;
import de.unika.ipd.grgen.ast.expr.graph.EdgeByUniqueExprNode;
import de.unika.ipd.grgen.ast.expr.graph.EdgesExprNode;
import de.unika.ipd.grgen.ast.expr.graph.EmptyExprNode;
import de.unika.ipd.grgen.ast.expr.graph.EqualsAnyExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IncidentEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.InducedSubgraphExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsAdjacentNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsBoundedReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsBoundedReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsIncidentEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.IsReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.NeighborhoodQueryExprNode;
import de.unika.ipd.grgen.ast.expr.graph.NodeByNameExprNode;
import de.unika.ipd.grgen.ast.expr.graph.NodeByUniqueExprNode;
import de.unika.ipd.grgen.ast.expr.graph.NodesExprNode;
import de.unika.ipd.grgen.ast.expr.graph.OppositeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.ReachableEdgeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.ReachableNodeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.SizeExprNode;
import de.unika.ipd.grgen.ast.expr.graph.SourceExprNode;
import de.unika.ipd.grgen.ast.expr.graph.TargetExprNode;
import de.unika.ipd.grgen.ast.expr.graph.UniqueofExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.AbsExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.ArcSinCosTanExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.ByteMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.ByteMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.CeilExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.DoubleMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.DoubleMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.EExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.FloatMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.FloatMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.FloorExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.IntMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.IntMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.LogExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.LongMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.LongMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.MaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.MinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.PiExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.PowExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.RoundExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.SgnExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.ShortMaxExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.ShortMinExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.SinCosTanExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.SqrExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.SqrtExprNode;
import de.unika.ipd.grgen.ast.expr.numeric.TruncateExprNode;
import de.unika.ipd.grgen.ast.expr.procenv.ExistsFileExprNode;
import de.unika.ipd.grgen.ast.expr.procenv.ImportExprNode;
import de.unika.ipd.grgen.ast.expr.procenv.NowExprNode;
import de.unika.ipd.grgen.ast.expr.procenv.RandomNode;
import de.unika.ipd.grgen.ast.type.TypeNode;
import de.unika.ipd.grgen.ast.type.executable.FunctionTypeNode;
import de.unika.ipd.grgen.ir.IR;
import de.unika.ipd.grgen.parser.ParserEnvironment;

public class FunctionInvocationDecisionNode extends FunctionInvocationBaseNode
{
	static {
		setName(FunctionInvocationDecisionNode.class, "function invocation decision expression");
	}

	static TypeNode functionTypeNode = new FunctionTypeNode();

	public IdentNode functionIdent;
	private BuiltinFunctionInvocationBaseNode result;

	ParserEnvironment env;

	public FunctionInvocationDecisionNode(IdentNode functionIdent, CollectNode<ExprNode> arguments, ParserEnvironment env)
	{
		super(functionIdent.getCoords(), arguments);
		this.functionIdent = becomeParent(functionIdent);
		this.env = env;
	}

	@Override
	public Collection<? extends BaseNode> getChildren()
	{
		Vector<BaseNode> children = new Vector<BaseNode>();
		//children.add(methodIdent);	// HACK: We don't have a declaration, so avoid failure during check phase
		children.add(arguments);
		if(isResolved())
			children.add(result);
		return children;
	}

	@Override
	public Collection<String> getChildrenNames()
	{
		Vector<String> childrenNames = new Vector<String>();
		//childrenNames.add("methodIdent");
		childrenNames.add("params");
		if(isResolved())
			childrenNames.add("result");
		return childrenNames;
	}

	protected boolean resolveLocal()
	{
		result = decide(functionIdent.toString());
		return result != null;
	}
	
	private BuiltinFunctionInvocationBaseNode decide(String functionName)
	{
		switch(functionName) {
		case "minMath":
			if(arguments.size() != 2) {
				reportError("Math::min(.,.) takes two parameters.");
				return null;
			} else
				return new MinExprNode(getCoords(), arguments.get(0), arguments.get(1));
		case "maxMath":
			if(arguments.size() != 2) {
				reportError("Math::max(.,.) takes two parameters.");
				return null;
			} else
				return new MaxExprNode(getCoords(), arguments.get(0), arguments.get(1));
		case "sinMath":
			if(arguments.size() != 1) {
				reportError("Math::sin(.) takes one parameter.");
				return null;
			} else {
				return new SinCosTanExprNode(getCoords(), SinCosTanExprNode.TrigonometryFunctionType.sin,
						arguments.get(0));
			}
		case "cosMath":
			if(arguments.size() != 1) {
				reportError("Math::cos(.) takes one parameter.");
				return null;
			} else {
				return new SinCosTanExprNode(getCoords(), SinCosTanExprNode.TrigonometryFunctionType.cos,
						arguments.get(0));
			}
		case "tanMath":
			if(arguments.size() != 1) {
				reportError("Math::tan(.) takes one parameter.");
				return null;
			} else {
				return new SinCosTanExprNode(getCoords(), SinCosTanExprNode.TrigonometryFunctionType.tan,
						arguments.get(0));
			}
		case "arcsinMath":
			if(arguments.size() != 1) {
				reportError("Math::arcsin(.) takes one parameter.");
				return null;
			} else {
				return new ArcSinCosTanExprNode(getCoords(), ArcSinCosTanExprNode.ArcusTrigonometryFunctionType.arcsin,
						arguments.get(0));
			}
		case "arccosMath":
			if(arguments.size() != 1) {
				reportError("Math::arccos(.) takes one parameter.");
				return null;
			} else {
				return new ArcSinCosTanExprNode(getCoords(), ArcSinCosTanExprNode.ArcusTrigonometryFunctionType.arccos,
						arguments.get(0));
			}
		case "arctanMath":
			if(arguments.size() != 1) {
				reportError("Math::arctan(.) takes one parameter.");
				return null;
			} else {
				return new ArcSinCosTanExprNode(getCoords(), ArcSinCosTanExprNode.ArcusTrigonometryFunctionType.arctan,
						arguments.get(0));
			}
		case "sqrMath":
			if(arguments.size() == 1)
				return new SqrExprNode(getCoords(), arguments.get(0));
			else {
				reportError("Math::sqr(.) takes one parameter.");
				return null;
			}
		case "sqrtMath":
			if(arguments.size() == 1)
				return new SqrtExprNode(getCoords(), arguments.get(0));
			else {
				reportError("Math::sqrt(.) takes one parameter.");
				return null;
			}
		case "powMath":
			if(arguments.size() == 2)
				return new PowExprNode(getCoords(), arguments.get(0), arguments.get(1));
			else if(arguments.size() == 1)
				return new PowExprNode(getCoords(), arguments.get(0));
			else {
				reportError("Math::pow(.,.)/Math::pow(.) takes one or two parameters (one means base e).");
				return null;
			}
		case "logMath":
			if(arguments.size() == 2)
				return new LogExprNode(getCoords(), arguments.get(0), arguments.get(1));
			else if(arguments.size() == 1)
				return new LogExprNode(getCoords(), arguments.get(0));
			else {
				reportError("Math::log(.,.)/Math::log(.) takes one or two parameters (one means base e).");
				return null;
			}
		case "absMath":
			if(arguments.size() != 1) {
				reportError("Math::abs(.) takes one parameter.");
				return null;
			} else
				return new AbsExprNode(getCoords(), arguments.get(0));
		case "piMath":
			if(arguments.size() != 0) {
				reportError("Math::pi() takes no parameters.");
				return null;
			} else
				return new PiExprNode(getCoords());
		case "eMath":
			if(arguments.size() != 0) {
				reportError("Math::e() takes no parameters.");
				return null;
			} else
				return new EExprNode(getCoords());
		case "byteMinMath":
			if(arguments.size() != 0) {
				reportError("Math::byteMin() takes no parameters.");
				return null;
			} else
				return new ByteMinExprNode(getCoords());
		case "byteMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::byteMax() takes no parameters.");
				return null;
			} else
				return new ByteMaxExprNode(getCoords());
		case "shortMinMath":
			if(arguments.size() != 0) {
				reportError("Math::shortMin() takes no parameters.");
				return null;
			} else
				return new ShortMinExprNode(getCoords());
		case "shortMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::shortMax() takes no parameters.");
				return null;
			} else
				return new ShortMaxExprNode(getCoords());
		case "intMinMath":
			if(arguments.size() != 0) {
				reportError("Math::intMin() takes no parameters.");
				return null;
			} else
				return new IntMinExprNode(getCoords());
		case "intMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::intMax() takes no parameters.");
				return null;
			} else
				return new IntMaxExprNode(getCoords());
		case "longMinMath":
			if(arguments.size() != 0) {
				reportError("Math::longMin() takes no parameters.");
				return null;
			} else
				return new LongMinExprNode(getCoords());
		case "longMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::longMax() takes no parameters.");
				return null;
			} else
				return new LongMaxExprNode(getCoords());
		case "floatMinMath":
			if(arguments.size() != 0) {
				reportError("Math::floatMin() takes no parameters.");
				return null;
			} else
				return new FloatMinExprNode(getCoords());
		case "floatMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::floatMax() takes no parameters.");
				return null;
			} else
				return new FloatMaxExprNode(getCoords());
		case "doubleMinMath":
			if(arguments.size() != 0) {
				reportError("Math::doubleMin() takes no parameters.");
				return null;
			} else
				return new DoubleMinExprNode(getCoords());
		case "doubleMaxMath":
			if(arguments.size() != 0) {
				reportError("Math::doubleMax() takes no parameters.");
				return null;
			} else
				return new DoubleMaxExprNode(getCoords());
		case "ceilMath":
			if(arguments.size() != 1) {
				reportError("Math::ceil(.) takes one parameter.");
				return null;
			} else
				return new CeilExprNode(getCoords(), arguments.get(0));
		case "floorMath":
			if(arguments.size() != 1) {
				reportError("Math::floor(.) takes one parameter.");
				return null;
			} else
				return new FloorExprNode(getCoords(), arguments.get(0));
		case "roundMath":
			if(arguments.size() != 1) {
				reportError("Math::round(.) takes one parameter.");
				return null;
			} else
				return new RoundExprNode(getCoords(), arguments.get(0));
		case "truncateMath":
			if(arguments.size() != 1) {
				reportError("Math::truncate(.) takes one parameter.");
				return null;
			} else
				return new TruncateExprNode(getCoords(), arguments.get(0));
		case "sgnMath":
			if(arguments.size() != 1) {
				reportError("Math::sgn(.) takes one parameter.");
				return null;
			} else
				return new SgnExprNode(getCoords(), arguments.get(0));
		case "random":
			if(arguments.size() == 1)
				return new RandomNode(getCoords(), arguments.get(0));
			else if(arguments.size() == 0)
				return new RandomNode(getCoords(), null);
			else {
				reportError("random(.)/random() takes one or no parameters.");
				return null;
			}
		case "nodes":
			if(arguments.size() > 1) {
				reportError("nodes() takes one or none parameter.");
				return null;
			} else {
				return new NodesExprNode(getCoords(),
						arguments.size() == 1 ? arguments.get(0) : new IdentExprNode(env.getNodeRoot()));
			}
		case "edges":
			if(arguments.size() > 1) {
				reportError("edges() takes one or none parameter.");
				return null;
			} else {
				return new EdgesExprNode(getCoords(),
						arguments.size() == 1 ? arguments.get(0) : new IdentExprNode(env.getArbitraryEdgeRoot()));
			}
		case "countNodes":
			if(arguments.size() > 1) {
				reportError("countNodes() takes one or none parameter.");
				return null;
			} else {
				return new CountNodesExprNode(getCoords(),
						arguments.size() == 1 ? arguments.get(0) : new IdentExprNode(env.getNodeRoot()));
			}
		case "countEdges":
			if(arguments.size() > 1) {
				reportError("countEdges() takes one or none parameter.");
				return null;
			} else {
				return new CountEdgesExprNode(getCoords(),
						arguments.size() == 1 ? arguments.get(0) : new IdentExprNode(env.getArbitraryEdgeRoot()));
			}
		case "nowTime":
			if(arguments.size() > 0) {
				reportError("Time::now() takes no parameters.");
				return null;
			} else
				return new NowExprNode(getCoords());
		case "empty":
			if(arguments.size() > 0) {
				reportError("empty() takes no parameters.");
				return null;
			} else
				return new EmptyExprNode(getCoords());
		case "size":
			if(arguments.size() > 0) {
				reportError("size() takes no parameters.");
				return null;
			} else
				return new SizeExprNode(getCoords());
		case "source":
			if(arguments.size() == 1)
				return new SourceExprNode(getCoords(), arguments.get(0), env.getNodeRoot());
			else {
				reportError(functionName + "() takes 1 parameter.");
				return null;
			}
		case "target":
			if(arguments.size() == 1)
				return new TargetExprNode(getCoords(), arguments.get(0), env.getNodeRoot());
			else {
				reportError(functionName + "() takes 1 parameter.");
				return null;
			}
		case "opposite":
			if(arguments.size() == 2)
				return new OppositeExprNode(getCoords(), arguments.get(0), arguments.get(1), env.getNodeRoot());
			else {
				reportError(functionName + "() takes 2 parameters.");
				return null;
			}
		case "nodeByName":
			if(arguments.size() >= 1 && arguments.size() <= 2) {
				return new NodeByNameExprNode(getCoords(), arguments.get(0),
						arguments.size() == 2 ? arguments.get(1) : new IdentExprNode(env.getNodeRoot()));
			} else {
				reportError(functionName + "() takes one or two parameters.");
				return null;
			}
		case "edgeByName":
			if(arguments.size() >= 1 && arguments.size() <= 2) {
				return new EdgeByNameExprNode(getCoords(), arguments.get(0),
						arguments.size() == 2 ? arguments.get(1) : new IdentExprNode(env.getArbitraryEdgeRoot()));
			} else {
				reportError(functionName + "() takes one or two parameters.");
				return null;
			}
		case "nodeByUnique":
			if(arguments.size() >= 1 && arguments.size() <= 2) {
				return new NodeByUniqueExprNode(getCoords(), arguments.get(0),
						arguments.size() == 2 ? arguments.get(1) : new IdentExprNode(env.getNodeRoot()));
			} else {
				reportError(functionName + "() takes one or two parameters.");
				return null;
			}
		case "edgeByUnique":
			if(arguments.size() >= 1 && arguments.size() <= 2) {
				return new EdgeByUniqueExprNode(getCoords(), arguments.get(0),
						arguments.size() == 2 ? arguments.get(1) : new IdentExprNode(env.getArbitraryEdgeRoot()));
			} else {
				reportError(functionName + "() takes one or two parameters.");
				return null;
			}
		case "incoming":
		case "outgoing":
		case "incident":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new IncidentEdgeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new IncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new IncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction, arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "adjacentIncoming":
		case "adjacentOutgoing":
		case "adjacent":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new AdjacentNodeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new AdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new AdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction, arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "countIncoming":
		case "countOutgoing":
		case "countIncident":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new CountIncidentEdgeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new CountIncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountIncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "countAdjacentIncoming":
		case "countAdjacentOutgoing":
		case "countAdjacent":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new CountAdjacentNodeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new CountAdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountAdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "isIncoming":
		case "isOutgoing":
		case "isIncident":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new IsIncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new IsIncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsIncidentEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2), direction,
						arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "isAdjacentIncoming":
		case "isAdjacentOutgoing":
		case "isAdjacent":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new IsAdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new IsAdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsAdjacentNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2), direction,
						arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "reachableEdgesIncoming":
		case "reachableEdgesOutgoing":
		case "reachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new ReachableEdgeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new ReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new ReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction, arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "reachableIncoming":
		case "reachableOutgoing":
		case "reachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new ReachableNodeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new ReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new ReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction, arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "countReachableEdgesIncoming":
		case "countReachableEdgesOutgoing":
		case "countReachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new CountReachableEdgeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new CountReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "countReachableIncoming":
		case "countReachableOutgoing":
		case "countReachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 1) {
				return new CountReachableNodeExprNode(getCoords(), arguments.get(0),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 2) {
				return new CountReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), direction,
						arguments.get(2));
			} else {
				reportError(functionName + "() takes 1-3 parameters.");
				return null;
			}
		}
		case "isReachableIncoming":
		case "isReachableOutgoing":
		case "isReachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new IsReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new IsReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "isReachableEdgesIncoming":
		case "isReachableEdgesOutgoing":
		case "isReachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new IsReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new IsReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "boundedReachableEdgesIncoming":
		case "boundedReachableEdgesOutgoing":
		case "boundedReachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new BoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new BoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new BoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "boundedReachableIncoming":
		case "boundedReachableOutgoing":
		case "boundedReachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new BoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new BoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new BoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "boundedReachableWithRemainingDepthIncoming":
		case "boundedReachableWithRemainingDepthOutgoing":
		case "boundedReachableWithRemainingDepth":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new BoundedReachableNodeWithRemainingDepthExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new BoundedReachableNodeWithRemainingDepthExprNode(getCoords(), arguments.get(0), arguments.get(1),
						arguments.get(2), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new BoundedReachableNodeWithRemainingDepthExprNode(getCoords(), arguments.get(0), arguments.get(1),
						arguments.get(2), direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "countBoundedReachableEdgesIncoming":
		case "countBoundedReachableEdgesOutgoing":
		case "countBoundedReachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new CountBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new CountBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "countBoundedReachableIncoming":	
		case "countBoundedReachableOutgoing":
		case "countBoundedReachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 2) {
				return new CountBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 3) {
				return new CountBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new CountBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						direction, arguments.get(3));
			} else {
				reportError(functionName + "() takes 2-4 parameters.");
				return null;
			}
		}
		case "isBoundedReachableIncoming":
		case "isBoundedReachableOutgoing":
		case "isBoundedReachable":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 3) {
				return new IsBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						arguments.get(3), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 5) {
				return new IsBoundedReachableNodeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						arguments.get(3), direction, arguments.get(4));
			} else {
				reportError(functionName + "() takes 3-5 parameters.");
				return null;
			}
		}
		case "isBoundedReachableEdgesIncoming":
		case "isBoundedReachableEdgesOutgoing":
		case "isBoundedReachableEdges":
		{
			int direction = getDirection(functionName);
			if(arguments.size() == 3) {
				return new IsBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						new IdentExprNode(env.getArbitraryEdgeRoot()), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 4) {
				return new IsBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						arguments.get(3), direction, new IdentExprNode(env.getNodeRoot()));
			} else if(arguments.size() == 5) {
				return new IsBoundedReachableEdgeExprNode(getCoords(), arguments.get(0), arguments.get(1), arguments.get(2),
						arguments.get(3), direction, arguments.get(4));
			} else {
				reportError(functionName + "() takes 3-5 parameters.");
				return null;
			}
		}
		case "inducedSubgraph":
			if(arguments.size() != 1) {
				reportError("inducedSubgraph(.) takes one parameter.");
				return null;
			} else
				return new InducedSubgraphExprNode(getCoords(), arguments.get(0));
		case "definedSubgraph":
			if(arguments.size() != 1) {
				reportError("definedSubgraph(.) takes one parameter.");
				return null;
			} else
				return new DefinedSubgraphExprNode(getCoords(), arguments.get(0));
		case "equalsAny":
			if(arguments.size() != 2) {
				reportError("equalsAny(.,.) takes two parameters.");
				return null;
			} else
				return new EqualsAnyExprNode(getCoords(), arguments.get(0), arguments.get(1), true);
		case "equalsAnyStructurally":
			if(arguments.size() != 2) {
				reportError("equalsAnyStructurally(.,.) takes two parameters.");
				return null;
			} else
				return new EqualsAnyExprNode(getCoords(), arguments.get(0), arguments.get(1), false);
		case "existsFile":
			if(arguments.size() != 1) {
				reportError("File::exists(.) takes one parameter.");
				return null;
			} else
				return new ExistsFileExprNode(getCoords(), arguments.get(0));
		case "importFile":
			if(arguments.size() != 1) {
				reportError("File::import(.) takes one parameter.");
				return null;
			} else
				return new ImportExprNode(getCoords(), arguments.get(0));
		case "copy":
			if(arguments.size() != 1) {
				reportError("copy(.) takes one parameter.");
				return null;
			} else
				return new CopyExprNode(getCoords(), arguments.get(0));
		case "canonize":
			if(arguments.size() != 1) {
				reportError("canonize(.) takes one parameter.");
				return null;
			} else
				return new CanonizeExprNode(getCoords(), arguments.get(0));
		case "uniqueof":
			if(arguments.size() > 1) {
				reportError("uniqueof(.) takes none or one parameter.");
				return null;
			} else if(arguments.size() == 1)
				return new UniqueofExprNode(getCoords(), arguments.get(0));
			else
				return new UniqueofExprNode(getCoords(), null);
		default:
			reportError("no function " + functionName + " known");
			return null;
		}
	}

	public static int getDirection(String functionName)
	{
		switch(functionName) {
		case "adjacentIncoming":
		case "countAdjacentIncoming":
		case "isAdjacentIncoming":
		case "reachableIncoming":
		case "countReachableIncoming":
		case "isReachableIncoming":
		case "boundedReachableIncoming":
		case "boundedReachableWithRemainingDepthIncoming":
		case "countBoundedReachableIncoming":
		case "isBoundedReachableIncoming":
			return NeighborhoodQueryExprNode.INCOMING;
		case "adjacentOutgoing":
		case "countAdjacentOutgoing":
		case "isAdjacentOutgoing":
		case "reachableOutgoing":
		case "countReachableOutgoing":
		case "isReachableOutgoing":
		case "boundedReachableOutgoing":
		case "boundedReachableWithRemainingDepthOutgoing":
		case "countBoundedReachableOutgoing":
		case "isBoundedReachableOutgoing":
			return NeighborhoodQueryExprNode.OUTGOING;
		case "adjacent":
		case "countAdjacent":
		case "isAdjacent":
		case "reachable":
		case "countReachable":
		case "isReachable":
		case "boundedReachable":
		case "boundedReachableWithRemainingDepth":
		case "countBoundedReachable":
		case "isBoundedReachable":
			return NeighborhoodQueryExprNode.ADJACENT;
		case "incoming":
		case "countIncoming":
		case "isIncoming":
		case "reachableEdgesIncoming":
		case "countReachableEdgesIncoming":
		case "isReachableEdgesIncoming":
		case "boundedReachableEdgesIncoming":
		case "countBoundedReachableEdgesIncoming":
		case "isBoundedReachableEdgesIncoming":
			return NeighborhoodQueryExprNode.INCOMING;
		case "outgoing":
		case "countOutgoing":
		case "isOutgoing":
		case "reachableEdgesOutgoing":
		case "countReachableEdgesOutgoing":
		case "isReachableEdgesOutgoing":
		case "boundedReachableEdgesOutgoing":
		case "countBoundedReachableEdgesOutgoing":
		case "isBoundedReachableEdgesOutgoing":
			return NeighborhoodQueryExprNode.OUTGOING;
		case "incident":
		case "countIncident":
		case "isIncident":
		case "reachableEdges":
		case "countReachableEdges":
		case "isReachableEdges":
		case "boundedReachableEdges":
		case "countBoundedReachableEdges":
		case "isBoundedReachableEdges":
			return NeighborhoodQueryExprNode.INCIDENT;
		}
		
		return -1;
	}
	
	@Override
	protected boolean checkLocal()
	{
		return true;
	}

	@Override
	public TypeNode getType()
	{
		return result.getType();
	}

	public ExprNode getResult()
	{
		return result;
	}

	@Override
	protected IR constructIR()
	{
		return result.getIR();
	}
}
