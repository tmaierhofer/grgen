using System;
using System.Collections.Generic;
using de.unika.ipd.grGen.libGr;
using de.unika.ipd.grGen.lgsp;

namespace de.unika.ipd.grGen.models.MutexPimped
{
	//
	// Enums
	//

	public class Enums
	{
	}

	//
	// Node types
	//

	public enum NodeTypes { @Process, @Resource, @Node };

	// *** Node Process ***

	public interface INode_Process : INode_Node
	{
	}

	public sealed class Node_Process : LGSPNode, INode_Process
	{
		private static int poolLevel = 0;
		private static Node_Process[] pool = new Node_Process[10];
		public Node_Process() : base(NodeType_Process.typeVar)
		{
		}
		public override INode Clone() { return new Node_Process(this); }

		private Node_Process(Node_Process oldElem) : base(NodeType_Process.typeVar)
		{
		}
		public static Node_Process CreateNode(LGSPGraph graph)
		{
			Node_Process node;
			if(poolLevel == 0)
				node = new Node_Process();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node);
			return node;
		}

		public static Node_Process CreateNode(LGSPGraph graph, String varName)
		{
			Node_Process node;
			if(poolLevel == 0)
				node = new Node_Process();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node, varName);
			return node;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The node type \"Process\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The node type \"Process\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class NodeType_Process : NodeType
	{
		public static NodeType_Process typeVar = new NodeType_Process();
		public static bool[] isA = new bool[] { true, false, true, };
		public static bool[] isMyType = new bool[] { true, false, false, };
		public NodeType_Process() : base((int) NodeTypes.@Process)
		{
		}
		public override String Name { get { return "Process"; } }
		public override INode CreateNode() { return new Node_Process(); }
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override INode CreateNodeWithCopyCommons(INode oldINode)
		{
			LGSPNode oldNode = (LGSPNode) oldINode;
			Node_Process newNode = new Node_Process();
			return newNode;
		}

	}

	// *** Node Resource ***

	public interface INode_Resource : INode_Node
	{
	}

	public sealed class Node_Resource : LGSPNode, INode_Resource
	{
		private static int poolLevel = 0;
		private static Node_Resource[] pool = new Node_Resource[10];
		public Node_Resource() : base(NodeType_Resource.typeVar)
		{
		}
		public override INode Clone() { return new Node_Resource(this); }

		private Node_Resource(Node_Resource oldElem) : base(NodeType_Resource.typeVar)
		{
		}
		public static Node_Resource CreateNode(LGSPGraph graph)
		{
			Node_Resource node;
			if(poolLevel == 0)
				node = new Node_Resource();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node);
			return node;
		}

		public static Node_Resource CreateNode(LGSPGraph graph, String varName)
		{
			Node_Resource node;
			if(poolLevel == 0)
				node = new Node_Resource();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node, varName);
			return node;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The node type \"Resource\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The node type \"Resource\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class NodeType_Resource : NodeType
	{
		public static NodeType_Resource typeVar = new NodeType_Resource();
		public static bool[] isA = new bool[] { false, true, true, };
		public static bool[] isMyType = new bool[] { false, true, false, };
		public NodeType_Resource() : base((int) NodeTypes.@Resource)
		{
		}
		public override String Name { get { return "Resource"; } }
		public override INode CreateNode() { return new Node_Resource(); }
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override INode CreateNodeWithCopyCommons(INode oldINode)
		{
			LGSPNode oldNode = (LGSPNode) oldINode;
			Node_Resource newNode = new Node_Resource();
			return newNode;
		}

	}

	// *** Node Node ***

	public interface INode_Node : IAttributes
	{
	}

	public sealed class Node_Node : LGSPNode, INode_Node
	{
		private static int poolLevel = 0;
		private static Node_Node[] pool = new Node_Node[10];
		public Node_Node() : base(NodeType_Node.typeVar)
		{
		}
		public override INode Clone() { return new Node_Node(this); }

		private Node_Node(Node_Node oldElem) : base(NodeType_Node.typeVar)
		{
		}
		public static Node_Node CreateNode(LGSPGraph graph)
		{
			Node_Node node;
			if(poolLevel == 0)
				node = new Node_Node();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node);
			return node;
		}

		public static Node_Node CreateNode(LGSPGraph graph, String varName)
		{
			Node_Node node;
			if(poolLevel == 0)
				node = new Node_Node();
			else
			{
				node = pool[--poolLevel];
				node.inhead = null;
				node.outhead = null;
				node.hasVariables = false;
			}
			graph.AddNode(node, varName);
			return node;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The node type \"Node\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The node type \"Node\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class NodeType_Node : NodeType
	{
		public static NodeType_Node typeVar = new NodeType_Node();
		public static bool[] isA = new bool[] { false, false, true, };
		public static bool[] isMyType = new bool[] { true, true, true, };
		public NodeType_Node() : base((int) NodeTypes.@Node)
		{
		}
		public override String Name { get { return "Node"; } }
		public override INode CreateNode() { return new Node_Node(); }
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override INode CreateNodeWithCopyCommons(INode oldINode)
		{
			LGSPNode oldNode = (LGSPNode) oldINode;
			Node_Node newNode = new Node_Node();
			return newNode;
		}

	}

	//
	// Node model
	//

	public sealed class MutexPimpedNodeModel : INodeModel
	{
		public MutexPimpedNodeModel()
		{
			NodeType_Process.typeVar.subOrSameGrGenTypes = NodeType_Process.typeVar.subOrSameTypes = new NodeType[] {
				NodeType_Process.typeVar,
			};
			NodeType_Process.typeVar.superOrSameGrGenTypes = NodeType_Process.typeVar.superOrSameTypes = new NodeType[] {
				NodeType_Process.typeVar,
				NodeType_Node.typeVar,
			};
			NodeType_Resource.typeVar.subOrSameGrGenTypes = NodeType_Resource.typeVar.subOrSameTypes = new NodeType[] {
				NodeType_Resource.typeVar,
			};
			NodeType_Resource.typeVar.superOrSameGrGenTypes = NodeType_Resource.typeVar.superOrSameTypes = new NodeType[] {
				NodeType_Resource.typeVar,
				NodeType_Node.typeVar,
			};
			NodeType_Node.typeVar.subOrSameGrGenTypes = NodeType_Node.typeVar.subOrSameTypes = new NodeType[] {
				NodeType_Node.typeVar,
				NodeType_Process.typeVar,
				NodeType_Resource.typeVar,
			};
			NodeType_Node.typeVar.superOrSameGrGenTypes = NodeType_Node.typeVar.superOrSameTypes = new NodeType[] {
				NodeType_Node.typeVar,
			};
		}
		public bool IsNodeModel { get { return true; } }
		public NodeType RootType { get { return NodeType_Node.typeVar; } }
		GrGenType ITypeModel.RootType { get { return NodeType_Node.typeVar; } }
		public NodeType GetType(String name)
		{
			switch(name)
			{
				case "Process" : return NodeType_Process.typeVar;
				case "Resource" : return NodeType_Resource.typeVar;
				case "Node" : return NodeType_Node.typeVar;
			}
			return null;
		}
		GrGenType ITypeModel.GetType(String name)
		{
			return GetType(name);
		}
		private NodeType[] types = {
			NodeType_Process.typeVar,
			NodeType_Resource.typeVar,
			NodeType_Node.typeVar,
		};
		public NodeType[] Types { get { return types; } }
		GrGenType[] ITypeModel.Types { get { return types; } }
		private Type[] typeTypes = {
			typeof(NodeType_Process),
			typeof(NodeType_Resource),
			typeof(NodeType_Node),
		};
		public Type[] TypeTypes { get { return typeTypes; } }
		private AttributeType[] attributeTypes = {
		};
		public IEnumerable<AttributeType> AttributeTypes { get { return attributeTypes; } }
	}

	//
	// Edge types
	//

	public enum EdgeTypes { @blocked, @held_by, @next, @release, @request, @token, @Edge };

	// *** Edge blocked ***

	public interface IEdge_blocked : IEdge_Edge
	{
	}

	public sealed class Edge_blocked : LGSPEdge, IEdge_blocked
	{
		private static int poolLevel = 0;
		private static Edge_blocked[] pool = new Edge_blocked[10];
		public Edge_blocked(LGSPNode source, LGSPNode target)
			: base(EdgeType_blocked.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_blocked(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_blocked(Edge_blocked oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_blocked.typeVar, newSource, newTarget)
		{
		}
		public static Edge_blocked CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_blocked edge;
			if(poolLevel == 0)
				edge = new Edge_blocked(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_blocked CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_blocked edge;
			if(poolLevel == 0)
				edge = new Edge_blocked(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"blocked\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"blocked\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_blocked : EdgeType
	{
		public static EdgeType_blocked typeVar = new EdgeType_blocked();
		public static bool[] isA = new bool[] { true, false, false, false, false, false, true, };
		public static bool[] isMyType = new bool[] { true, false, false, false, false, false, false, };
		public EdgeType_blocked() : base((int) EdgeTypes.@blocked)
		{
		}
		public override String Name { get { return "blocked"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_blocked((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_blocked newEdge = new Edge_blocked((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge held_by ***

	public interface IEdge_held_by : IEdge_Edge
	{
	}

	public sealed class Edge_held_by : LGSPEdge, IEdge_held_by
	{
		private static int poolLevel = 0;
		private static Edge_held_by[] pool = new Edge_held_by[10];
		public Edge_held_by(LGSPNode source, LGSPNode target)
			: base(EdgeType_held_by.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_held_by(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_held_by(Edge_held_by oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_held_by.typeVar, newSource, newTarget)
		{
		}
		public static Edge_held_by CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_held_by edge;
			if(poolLevel == 0)
				edge = new Edge_held_by(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_held_by CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_held_by edge;
			if(poolLevel == 0)
				edge = new Edge_held_by(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"held_by\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"held_by\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_held_by : EdgeType
	{
		public static EdgeType_held_by typeVar = new EdgeType_held_by();
		public static bool[] isA = new bool[] { false, true, false, false, false, false, true, };
		public static bool[] isMyType = new bool[] { false, true, false, false, false, false, false, };
		public EdgeType_held_by() : base((int) EdgeTypes.@held_by)
		{
		}
		public override String Name { get { return "held_by"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_held_by((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_held_by newEdge = new Edge_held_by((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge next ***

	public interface IEdge_next : IEdge_Edge
	{
	}

	public sealed class Edge_next : LGSPEdge, IEdge_next
	{
		private static int poolLevel = 0;
		private static Edge_next[] pool = new Edge_next[10];
		public Edge_next(LGSPNode source, LGSPNode target)
			: base(EdgeType_next.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_next(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_next(Edge_next oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_next.typeVar, newSource, newTarget)
		{
		}
		public static Edge_next CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_next edge;
			if(poolLevel == 0)
				edge = new Edge_next(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_next CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_next edge;
			if(poolLevel == 0)
				edge = new Edge_next(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"next\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"next\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_next : EdgeType
	{
		public static EdgeType_next typeVar = new EdgeType_next();
		public static bool[] isA = new bool[] { false, false, true, false, false, false, true, };
		public static bool[] isMyType = new bool[] { false, false, true, false, false, false, false, };
		public EdgeType_next() : base((int) EdgeTypes.@next)
		{
		}
		public override String Name { get { return "next"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_next((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_next newEdge = new Edge_next((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge release ***

	public interface IEdge_release : IEdge_Edge
	{
	}

	public sealed class Edge_release : LGSPEdge, IEdge_release
	{
		private static int poolLevel = 0;
		private static Edge_release[] pool = new Edge_release[10];
		public Edge_release(LGSPNode source, LGSPNode target)
			: base(EdgeType_release.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_release(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_release(Edge_release oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_release.typeVar, newSource, newTarget)
		{
		}
		public static Edge_release CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_release edge;
			if(poolLevel == 0)
				edge = new Edge_release(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_release CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_release edge;
			if(poolLevel == 0)
				edge = new Edge_release(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"release\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"release\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_release : EdgeType
	{
		public static EdgeType_release typeVar = new EdgeType_release();
		public static bool[] isA = new bool[] { false, false, false, true, false, false, true, };
		public static bool[] isMyType = new bool[] { false, false, false, true, false, false, false, };
		public EdgeType_release() : base((int) EdgeTypes.@release)
		{
		}
		public override String Name { get { return "release"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_release((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_release newEdge = new Edge_release((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge request ***

	public interface IEdge_request : IEdge_Edge
	{
	}

	public sealed class Edge_request : LGSPEdge, IEdge_request
	{
		private static int poolLevel = 0;
		private static Edge_request[] pool = new Edge_request[10];
		public Edge_request(LGSPNode source, LGSPNode target)
			: base(EdgeType_request.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_request(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_request(Edge_request oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_request.typeVar, newSource, newTarget)
		{
		}
		public static Edge_request CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_request edge;
			if(poolLevel == 0)
				edge = new Edge_request(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_request CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_request edge;
			if(poolLevel == 0)
				edge = new Edge_request(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"request\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"request\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_request : EdgeType
	{
		public static EdgeType_request typeVar = new EdgeType_request();
		public static bool[] isA = new bool[] { false, false, false, false, true, false, true, };
		public static bool[] isMyType = new bool[] { false, false, false, false, true, false, false, };
		public EdgeType_request() : base((int) EdgeTypes.@request)
		{
		}
		public override String Name { get { return "request"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_request((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_request newEdge = new Edge_request((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge token ***

	public interface IEdge_token : IEdge_Edge
	{
	}

	public sealed class Edge_token : LGSPEdge, IEdge_token
	{
		private static int poolLevel = 0;
		private static Edge_token[] pool = new Edge_token[10];
		public Edge_token(LGSPNode source, LGSPNode target)
			: base(EdgeType_token.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_token(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_token(Edge_token oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_token.typeVar, newSource, newTarget)
		{
		}
		public static Edge_token CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_token edge;
			if(poolLevel == 0)
				edge = new Edge_token(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_token CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_token edge;
			if(poolLevel == 0)
				edge = new Edge_token(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"token\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"token\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_token : EdgeType
	{
		public static EdgeType_token typeVar = new EdgeType_token();
		public static bool[] isA = new bool[] { false, false, false, false, false, true, true, };
		public static bool[] isMyType = new bool[] { false, false, false, false, false, true, false, };
		public EdgeType_token() : base((int) EdgeTypes.@token)
		{
		}
		public override String Name { get { return "token"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_token((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_token newEdge = new Edge_token((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	// *** Edge Edge ***

	public interface IEdge_Edge : IAttributes
	{
	}

	public sealed class Edge_Edge : LGSPEdge, IEdge_Edge
	{
		private static int poolLevel = 0;
		private static Edge_Edge[] pool = new Edge_Edge[10];
		public Edge_Edge(LGSPNode source, LGSPNode target)
			: base(EdgeType_Edge.typeVar, source, target)
		{
		}
		public override IEdge Clone(INode newSource, INode newTarget)
		{ return new Edge_Edge(this, (LGSPNode) newSource, (LGSPNode) newTarget); }

		private Edge_Edge(Edge_Edge oldElem, LGSPNode newSource, LGSPNode newTarget)
			: base(EdgeType_Edge.typeVar, newSource, newTarget)
		{
		}
		public static Edge_Edge CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target)
		{
			Edge_Edge edge;
			if(poolLevel == 0)
				edge = new Edge_Edge(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge);
			return edge;
		}

		public static Edge_Edge CreateEdge(LGSPGraph graph, LGSPNode source, LGSPNode target, String varName)
		{
			Edge_Edge edge;
			if(poolLevel == 0)
				edge = new Edge_Edge(source, target);
			else
			{
				edge = pool[--poolLevel];
				edge.hasVariables = false;
				edge.source = source;
				edge.target = target;
			}
			graph.AddEdge(edge, varName);
			return edge;
		}

		public override void Recycle()
		{
			if(poolLevel < 10)
				pool[poolLevel++] = this;
		}

		public override object GetAttribute(string attrName)
		{
			throw new NullReferenceException(
				"The edge type \"Edge\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void SetAttribute(string attrName, object value)
		{
			throw new NullReferenceException(
				"The edge type \"Edge\" does not have the attribute \" + attrName + \"\"!");
		}
		public override void ResetAllAttributes()
		{
		}
	}

	public sealed class EdgeType_Edge : EdgeType
	{
		public static EdgeType_Edge typeVar = new EdgeType_Edge();
		public static bool[] isA = new bool[] { false, false, false, false, false, false, true, };
		public static bool[] isMyType = new bool[] { true, true, true, true, true, true, true, };
		public EdgeType_Edge() : base((int) EdgeTypes.@Edge)
		{
		}
		public override String Name { get { return "Edge"; } }
		public override IEdge CreateEdge(INode source, INode target)
		{
			return new Edge_Edge((LGSPNode) source, (LGSPNode) target);
		}
		public override int NumAttributes { get { return 0; } }
		public override IEnumerable<AttributeType> AttributeTypes { get { yield break; } }
		public override AttributeType GetAttributeType(String name) { return null; }
		public override bool IsA(GrGenType other)
		{
			return (this == other) || isA[other.TypeID];
		}
		public override IEdge CreateEdgeWithCopyCommons(INode source, INode target, IEdge oldIEdge)
		{
			LGSPEdge oldEdge = (LGSPEdge) oldIEdge;
			Edge_Edge newEdge = new Edge_Edge((LGSPNode) source, (LGSPNode) target);
			return newEdge;
		}

	}

	//
	// Edge model
	//

	public sealed class MutexPimpedEdgeModel : IEdgeModel
	{
		public MutexPimpedEdgeModel()
		{
			EdgeType_blocked.typeVar.subOrSameGrGenTypes = EdgeType_blocked.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_blocked.typeVar,
			};
			EdgeType_blocked.typeVar.superOrSameGrGenTypes = EdgeType_blocked.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_blocked.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_held_by.typeVar.subOrSameGrGenTypes = EdgeType_held_by.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_held_by.typeVar,
			};
			EdgeType_held_by.typeVar.superOrSameGrGenTypes = EdgeType_held_by.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_held_by.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_next.typeVar.subOrSameGrGenTypes = EdgeType_next.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_next.typeVar,
			};
			EdgeType_next.typeVar.superOrSameGrGenTypes = EdgeType_next.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_next.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_release.typeVar.subOrSameGrGenTypes = EdgeType_release.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_release.typeVar,
			};
			EdgeType_release.typeVar.superOrSameGrGenTypes = EdgeType_release.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_release.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_request.typeVar.subOrSameGrGenTypes = EdgeType_request.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_request.typeVar,
			};
			EdgeType_request.typeVar.superOrSameGrGenTypes = EdgeType_request.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_request.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_token.typeVar.subOrSameGrGenTypes = EdgeType_token.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_token.typeVar,
			};
			EdgeType_token.typeVar.superOrSameGrGenTypes = EdgeType_token.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_token.typeVar,
				EdgeType_Edge.typeVar,
			};
			EdgeType_Edge.typeVar.subOrSameGrGenTypes = EdgeType_Edge.typeVar.subOrSameTypes = new EdgeType[] {
				EdgeType_Edge.typeVar,
				EdgeType_blocked.typeVar,
				EdgeType_held_by.typeVar,
				EdgeType_next.typeVar,
				EdgeType_release.typeVar,
				EdgeType_request.typeVar,
				EdgeType_token.typeVar,
			};
			EdgeType_Edge.typeVar.superOrSameGrGenTypes = EdgeType_Edge.typeVar.superOrSameTypes = new EdgeType[] {
				EdgeType_Edge.typeVar,
			};
		}
		public bool IsNodeModel { get { return false; } }
		public EdgeType RootType { get { return EdgeType_Edge.typeVar; } }
		GrGenType ITypeModel.RootType { get { return EdgeType_Edge.typeVar; } }
		public EdgeType GetType(String name)
		{
			switch(name)
			{
				case "blocked" : return EdgeType_blocked.typeVar;
				case "held_by" : return EdgeType_held_by.typeVar;
				case "next" : return EdgeType_next.typeVar;
				case "release" : return EdgeType_release.typeVar;
				case "request" : return EdgeType_request.typeVar;
				case "token" : return EdgeType_token.typeVar;
				case "Edge" : return EdgeType_Edge.typeVar;
			}
			return null;
		}
		GrGenType ITypeModel.GetType(String name)
		{
			return GetType(name);
		}
		private EdgeType[] types = {
			EdgeType_blocked.typeVar,
			EdgeType_held_by.typeVar,
			EdgeType_next.typeVar,
			EdgeType_release.typeVar,
			EdgeType_request.typeVar,
			EdgeType_token.typeVar,
			EdgeType_Edge.typeVar,
		};
		public EdgeType[] Types { get { return types; } }
		GrGenType[] ITypeModel.Types { get { return types; } }
		private Type[] typeTypes = {
			typeof(EdgeType_blocked),
			typeof(EdgeType_held_by),
			typeof(EdgeType_next),
			typeof(EdgeType_release),
			typeof(EdgeType_request),
			typeof(EdgeType_token),
			typeof(EdgeType_Edge),
		};
		public Type[] TypeTypes { get { return typeTypes; } }
		private AttributeType[] attributeTypes = {
		};
		public IEnumerable<AttributeType> AttributeTypes { get { return attributeTypes; } }
	}

	//
	// IGraphModel implementation
	//

	public sealed class MutexPimpedGraphModel : IGraphModel
	{
		private MutexPimpedNodeModel nodeModel = new MutexPimpedNodeModel();
		private MutexPimpedEdgeModel edgeModel = new MutexPimpedEdgeModel();
		private ValidateInfo[] validateInfos = {
			new ValidateInfo(EdgeType_blocked.typeVar, NodeType_Resource.typeVar, NodeType_Process.typeVar, 0, int.MaxValue, 0, int.MaxValue),
			new ValidateInfo(EdgeType_held_by.typeVar, NodeType_Resource.typeVar, NodeType_Process.typeVar, 1, int.MaxValue, 0, int.MaxValue),
			new ValidateInfo(EdgeType_next.typeVar, NodeType_Process.typeVar, NodeType_Process.typeVar, 0, 1, 0, 1),
			new ValidateInfo(EdgeType_release.typeVar, NodeType_Resource.typeVar, NodeType_Process.typeVar, 1, int.MaxValue, 0, int.MaxValue),
			new ValidateInfo(EdgeType_request.typeVar, NodeType_Process.typeVar, NodeType_Resource.typeVar, 0, int.MaxValue, 0, int.MaxValue),
			new ValidateInfo(EdgeType_token.typeVar, NodeType_Resource.typeVar, NodeType_Process.typeVar, 1, int.MaxValue, 0, int.MaxValue),
		};

		public String Name { get { return "MutexPimped"; } }
		public INodeModel NodeModel { get { return nodeModel; } }
		public IEdgeModel EdgeModel { get { return edgeModel; } }
		public IEnumerable<ValidateInfo> ValidateInfo { get { return validateInfos; } }
		public String MD5Hash { get { return "0ecfadd9bae7c1bee09ddc57f323923f"; } }
	}
}
