// This file has been generated automatically by GrGen.
// Do not modify this file! Any changes will be lost!
// Generated from "test.grg" on Mon Jan 05 22:41:29 CET 2009

using System;
using System.Collections.Generic;
using System.Text;
using GRGEN_LIBGR = de.unika.ipd.grGen.libGr;
using GRGEN_LGSP = de.unika.ipd.grGen.lgsp;
using GRGEN_EXPR = de.unika.ipd.grGen.expression;
using de.unika.ipd.grGen.Model_complModel;

namespace de.unika.ipd.grGen.Action_test
{
	public class Rule_testRule : GRGEN_LGSP.LGSPRulePattern
	{
		private static Rule_testRule instance = null;
		public static Rule_testRule Instance { get { if (instance==null) { instance = new Rule_testRule(); instance.initialize(); } return instance; } }

		private static object[] ReturnArray = new object[0];

		public static GRGEN_LIBGR.NodeType[] testRule_node_a_AllowedTypes = null;
		public static GRGEN_LIBGR.NodeType[] testRule_node_f_AllowedTypes = null;
		public static GRGEN_LIBGR.NodeType[] testRule_node_m_AllowedTypes = null;
		public static bool[] testRule_node_a_IsAllowedType = null;
		public static bool[] testRule_node_f_IsAllowedType = null;
		public static bool[] testRule_node_m_IsAllowedType = null;
		public static GRGEN_LIBGR.EdgeType[] testRule_edge__edge0_AllowedTypes = null;
		public static GRGEN_LIBGR.EdgeType[] testRule_edge__edge1_AllowedTypes = null;
		public static bool[] testRule_edge__edge0_IsAllowedType = null;
		public static bool[] testRule_edge__edge1_IsAllowedType = null;
		public enum testRule_NodeNums { @a, @f, @m, };
		public enum testRule_EdgeNums { @_edge0, @_edge1, };
		public enum testRule_VariableNums { };
		public enum testRule_SubNums { };
		public enum testRule_AltNums { };



		GRGEN_LGSP.PatternGraph pat_testRule;


#if INITIAL_WARMUP
		public Rule_testRule()
#else
		private Rule_testRule()
#endif
		{
			name = "testRule";

			inputs = new GRGEN_LIBGR.GrGenType[] { };
			inputNames = new string[] { };
			outputs = new GRGEN_LIBGR.GrGenType[] { };
		}
		public override void initialize()
		{
			bool[,] testRule_isNodeHomomorphicGlobal = new bool[3, 3] {
				{ false, false, false, },
				{ false, false, false, },
				{ false, false, false, },
			};
			bool[,] testRule_isEdgeHomomorphicGlobal = new bool[2, 2] {
				{ false, false, },
				{ false, false, },
			};
			GRGEN_LGSP.PatternNode testRule_node_a = new GRGEN_LGSP.PatternNode((int) NodeTypes.@D231_4121, "ID231_4121", "testRule_node_a", "a", testRule_node_a_AllowedTypes, testRule_node_a_IsAllowedType, 5.5F, -1);
			GRGEN_LGSP.PatternNode testRule_node_f = new GRGEN_LGSP.PatternNode((int) NodeTypes.@B21, "IB21", "testRule_node_f", "f", testRule_node_f_AllowedTypes, testRule_node_f_IsAllowedType, 5.5F, -1);
			GRGEN_LGSP.PatternNode testRule_node_m = new GRGEN_LGSP.PatternNode((int) NodeTypes.@D2211_2222_31, "ID2211_2222_31", "testRule_node_m", "m", testRule_node_m_AllowedTypes, testRule_node_m_IsAllowedType, 5.5F, -1);
			GRGEN_LGSP.PatternEdge testRule_edge__edge0 = new GRGEN_LGSP.PatternEdge(true, (int) EdgeTypes.@Edge, "GRGEN_LIBGR.IEdge", "testRule_edge__edge0", "_edge0", testRule_edge__edge0_AllowedTypes, testRule_edge__edge0_IsAllowedType, 5.5F, -1);
			GRGEN_LGSP.PatternEdge testRule_edge__edge1 = new GRGEN_LGSP.PatternEdge(true, (int) EdgeTypes.@Edge, "GRGEN_LIBGR.IEdge", "testRule_edge__edge1", "_edge1", testRule_edge__edge1_AllowedTypes, testRule_edge__edge1_IsAllowedType, 5.5F, -1);
			pat_testRule = new GRGEN_LGSP.PatternGraph(
				"testRule",
				"",
				false,
				new GRGEN_LGSP.PatternNode[] { testRule_node_a, testRule_node_f, testRule_node_m }, 
				new GRGEN_LGSP.PatternEdge[] { testRule_edge__edge0, testRule_edge__edge1 }, 
				new GRGEN_LGSP.PatternVariable[] {  }, 
				new GRGEN_LGSP.PatternGraphEmbedding[] {  }, 
				new GRGEN_LGSP.Alternative[] {  }, 
				new GRGEN_LGSP.PatternGraph[] {  }, 
				new GRGEN_LGSP.PatternGraph[] {  }, 
				new GRGEN_LGSP.PatternCondition[] {  }, 
				new bool[3, 3] {
					{ true, false, false, },
					{ false, true, false, },
					{ false, false, true, },
				},
				new bool[2, 2] {
					{ true, false, },
					{ false, true, },
				},
				testRule_isNodeHomomorphicGlobal,
				testRule_isEdgeHomomorphicGlobal
			);
			pat_testRule.edgeToSourceNode.Add(testRule_edge__edge0, testRule_node_a);
			pat_testRule.edgeToTargetNode.Add(testRule_edge__edge0, testRule_node_f);
			pat_testRule.edgeToSourceNode.Add(testRule_edge__edge1, testRule_node_f);
			pat_testRule.edgeToTargetNode.Add(testRule_edge__edge1, testRule_node_m);

			testRule_node_a.PointOfDefinition = pat_testRule;
			testRule_node_f.PointOfDefinition = pat_testRule;
			testRule_node_m.PointOfDefinition = pat_testRule;
			testRule_edge__edge0.PointOfDefinition = pat_testRule;
			testRule_edge__edge1.PointOfDefinition = pat_testRule;

			patternGraph = pat_testRule;
		}


		public override object[] Modify(GRGEN_LGSP.LGSPGraph graph, GRGEN_LIBGR.IMatch _curMatch)
		{
			Match_testRule curMatch = (Match_testRule)_curMatch;
			GRGEN_LGSP.LGSPNode node_a = curMatch._node_a;
			GRGEN_LGSP.LGSPNode node_f = curMatch._node_f;
			GRGEN_LGSP.LGSPNode node_m = curMatch._node_m;
			graph.SettingAddedNodeNames( testRule_addedNodeNames );
			GRGEN_LGSP.LGSPNode node_are = graph.Retype(node_a, NodeType_D2211_2222_31.typeVar);
			@ID2211_2222_31 inode_are = (@ID2211_2222_31) node_are;
			GRGEN_LGSP.LGSPNode node_fre = graph.Retype(node_f, NodeType_D231_4121.typeVar);
			@ID231_4121 inode_fre = (@ID231_4121) node_fre;
			GRGEN_LGSP.LGSPNode node_mre = graph.Retype(node_m, NodeType_D11_2221.typeVar);
			@ID11_2221 inode_mre = (@ID11_2221) node_mre;
			graph.SettingAddedEdgeNames( testRule_addedEdgeNames );
			int tempvar_i = 1234;
			graph.ChangingNodeAttribute(node_are, NodeType_D2211_2222_31.AttributeType_d2211_2222_31, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_are.@d2211_2222_31 = tempvar_i;
			tempvar_i = 5678;
			graph.ChangingNodeAttribute(node_fre, NodeType_D231_4121.AttributeType_d231_4121, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_fre.@d231_4121 = tempvar_i;
			tempvar_i = 9012;
			graph.ChangingNodeAttribute(node_mre, NodeType_D11_2221.AttributeType_d11_2221, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_mre.@d11_2221 = tempvar_i;
			return EmptyReturnElements;
		}
		private static String[] testRule_addedNodeNames = new String[] {  };
		private static String[] testRule_addedEdgeNames = new String[] {  };

		public override object[] ModifyNoReuse(GRGEN_LGSP.LGSPGraph graph, GRGEN_LIBGR.IMatch _curMatch)
		{
			Match_testRule curMatch = (Match_testRule)_curMatch;
			GRGEN_LGSP.LGSPNode node_a = curMatch._node_a;
			GRGEN_LGSP.LGSPNode node_f = curMatch._node_f;
			GRGEN_LGSP.LGSPNode node_m = curMatch._node_m;
			graph.SettingAddedNodeNames( testRule_addedNodeNames );
			GRGEN_LGSP.LGSPNode node_are = graph.Retype(node_a, NodeType_D2211_2222_31.typeVar);
			@ID2211_2222_31 inode_are = (@ID2211_2222_31) node_are;
			GRGEN_LGSP.LGSPNode node_fre = graph.Retype(node_f, NodeType_D231_4121.typeVar);
			@ID231_4121 inode_fre = (@ID231_4121) node_fre;
			GRGEN_LGSP.LGSPNode node_mre = graph.Retype(node_m, NodeType_D11_2221.typeVar);
			@ID11_2221 inode_mre = (@ID11_2221) node_mre;
			graph.SettingAddedEdgeNames( testRule_addedEdgeNames );
			int tempvar_i = 1234;
			graph.ChangingNodeAttribute(node_are, NodeType_D2211_2222_31.AttributeType_d2211_2222_31, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_are.@d2211_2222_31 = tempvar_i;
			tempvar_i = 5678;
			graph.ChangingNodeAttribute(node_fre, NodeType_D231_4121.AttributeType_d231_4121, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_fre.@d231_4121 = tempvar_i;
			tempvar_i = 9012;
			graph.ChangingNodeAttribute(node_mre, NodeType_D11_2221.AttributeType_d11_2221, GRGEN_LIBGR.AttributeChangeType.Assign, tempvar_i, null);
			inode_mre.@d11_2221 = tempvar_i;
			return EmptyReturnElements;
		}

		static Rule_testRule() {
		}

		public interface IMatch_testRule : GRGEN_LIBGR.IMatch
		{
			//Nodes
			ID231_4121 node_a { get; }
			IB21 node_f { get; }
			ID2211_2222_31 node_m { get; }
			//Edges
			GRGEN_LIBGR.IEdge edge__edge0 { get; }
			GRGEN_LIBGR.IEdge edge__edge1 { get; }
			//Variables
			//EmbeddedGraphs
			//Alternatives
		}

		public class Match_testRule : GRGEN_LGSP.ListElement<Match_testRule>, IMatch_testRule
		{
			public ID231_4121 node_a { get { return (ID231_4121)_node_a; } }
			public IB21 node_f { get { return (IB21)_node_f; } }
			public ID2211_2222_31 node_m { get { return (ID2211_2222_31)_node_m; } }
			public GRGEN_LGSP.LGSPNode _node_a;
			public GRGEN_LGSP.LGSPNode _node_f;
			public GRGEN_LGSP.LGSPNode _node_m;
			public enum testRule_NodeNums { @a, @f, @m, END_OF_ENUM };
			public IEnumerable<GRGEN_LIBGR.INode> Nodes { get { return new GRGEN_LGSP.Nodes_Enumerable(this); } }
			public IEnumerator<GRGEN_LIBGR.INode> NodesEnumerator { get { return new GRGEN_LGSP.Nodes_Enumerator(this); } }
			public int NumberOfNodes { get { return 3;} }
			public GRGEN_LIBGR.INode getNodeAt(int index)
			{
				switch(index) {
				case (int)testRule_NodeNums.@a: return _node_a;
				case (int)testRule_NodeNums.@f: return _node_f;
				case (int)testRule_NodeNums.@m: return _node_m;
				default: return null;
				}
			}
			
			public GRGEN_LIBGR.IEdge edge__edge0 { get { return (GRGEN_LIBGR.IEdge)_edge__edge0; } }
			public GRGEN_LIBGR.IEdge edge__edge1 { get { return (GRGEN_LIBGR.IEdge)_edge__edge1; } }
			public GRGEN_LGSP.LGSPEdge _edge__edge0;
			public GRGEN_LGSP.LGSPEdge _edge__edge1;
			public enum testRule_EdgeNums { @_edge0, @_edge1, END_OF_ENUM };
			public IEnumerable<GRGEN_LIBGR.IEdge> Edges { get { return new GRGEN_LGSP.Edges_Enumerable(this); } }
			public IEnumerator<GRGEN_LIBGR.IEdge> EdgesEnumerator { get { return new GRGEN_LGSP.Edges_Enumerator(this); } }
			public int NumberOfEdges { get { return 2;} }
			public GRGEN_LIBGR.IEdge getEdgeAt(int index)
			{
				switch(index) {
				case (int)testRule_EdgeNums.@_edge0: return _edge__edge0;
				case (int)testRule_EdgeNums.@_edge1: return _edge__edge1;
				default: return null;
				}
			}
			
			public enum testRule_VariableNums { END_OF_ENUM };
			public IEnumerable<object> Variables { get { return new GRGEN_LGSP.Variables_Enumerable(this); } }
			public IEnumerator<object> VariablesEnumerator { get { return new GRGEN_LGSP.Variables_Enumerator(this); } }
			public int NumberOfVariables { get { return 0;} }
			public object getVariableAt(int index)
			{
				switch(index) {
				default: return null;
				}
			}
			
			public enum testRule_SubNums { END_OF_ENUM };
			public IEnumerable<GRGEN_LIBGR.IMatch> EmbeddedGraphs { get { return new GRGEN_LGSP.EmbeddedGraphs_Enumerable(this); } }
			public IEnumerator<GRGEN_LIBGR.IMatch> EmbeddedGraphsEnumerator { get { return new GRGEN_LGSP.EmbeddedGraphs_Enumerator(this); } }
			public int NumberOfEmbeddedGraphs { get { return 0;} }
			public GRGEN_LIBGR.IMatch getEmbeddedGraphAt(int index)
			{
				switch(index) {
				default: return null;
				}
			}
			
			public enum testRule_AltNums { END_OF_ENUM };
			public IEnumerable<GRGEN_LIBGR.IMatch> Alternatives { get { return new GRGEN_LGSP.Alternatives_Enumerable(this); } }
			public IEnumerator<GRGEN_LIBGR.IMatch> AlternativesEnumerator { get { return new GRGEN_LGSP.Alternatives_Enumerator(this); } }
			public int NumberOfAlternatives { get { return 0;} }
			public GRGEN_LIBGR.IMatch getAlternativeAt(int index)
			{
				switch(index) {
				default: return null;
				}
			}
			
			public GRGEN_LIBGR.IPatternGraph Pattern { get { return Rule_testRule.instance.pat_testRule; } }
			public override string ToString() { return "Match of " + Pattern.Name; }
		}

	}


    public class Action_testRule : GRGEN_LGSP.LGSPAction
    {
        public Action_testRule() {
            rulePattern = Rule_testRule.Instance;
            patternGraph = rulePattern.patternGraph;
            DynamicMatch = myMatch;
            matches = new GRGEN_LGSP.LGSPMatchesList<Rule_testRule.Match_testRule>(this);
        }

        public override string Name { get { return "testRule"; } }
        private GRGEN_LGSP.LGSPMatchesList<Rule_testRule.Match_testRule> matches;

        public static GRGEN_LGSP.LGSPAction Instance { get { return instance; } }
        private static Action_testRule instance = new Action_testRule();

        public GRGEN_LIBGR.IMatches myMatch(GRGEN_LGSP.LGSPGraph graph, int maxMatches, object[] parameters)
        {
            matches.Clear();
            int negLevel = 0;
            // Lookup testRule_edge__edge1 
            int type_id_candidate_testRule_edge__edge1 = 1;
            for(GRGEN_LGSP.LGSPEdge head_candidate_testRule_edge__edge1 = graph.edgesByTypeHeads[type_id_candidate_testRule_edge__edge1], candidate_testRule_edge__edge1 = head_candidate_testRule_edge__edge1.typeNext; candidate_testRule_edge__edge1 != head_candidate_testRule_edge__edge1; candidate_testRule_edge__edge1 = candidate_testRule_edge__edge1.typeNext)
            {
                uint prev__candidate_testRule_edge__edge1;
                prev__candidate_testRule_edge__edge1 = candidate_testRule_edge__edge1.flags & (uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel;
                candidate_testRule_edge__edge1.flags |= (uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel;
                // Implicit Source testRule_node_f from testRule_edge__edge1 
                GRGEN_LGSP.LGSPNode candidate_testRule_node_f = candidate_testRule_edge__edge1.source;
                if(candidate_testRule_node_f.type.TypeID!=6) {
                    candidate_testRule_edge__edge1.flags = candidate_testRule_edge__edge1.flags & ~((uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel) | prev__candidate_testRule_edge__edge1;
                    continue;
                }
                // Implicit Target testRule_node_m from testRule_edge__edge1 
                GRGEN_LGSP.LGSPNode candidate_testRule_node_m = candidate_testRule_edge__edge1.target;
                if(candidate_testRule_node_m.type.TypeID!=17) {
                    candidate_testRule_edge__edge1.flags = candidate_testRule_edge__edge1.flags & ~((uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel) | prev__candidate_testRule_edge__edge1;
                    continue;
                }
                // Extend Incoming testRule_edge__edge0 from testRule_node_f 
                GRGEN_LGSP.LGSPEdge head_candidate_testRule_edge__edge0 = candidate_testRule_node_f.inhead;
                if(head_candidate_testRule_edge__edge0 != null)
                {
                    GRGEN_LGSP.LGSPEdge candidate_testRule_edge__edge0 = head_candidate_testRule_edge__edge0;
                    do
                    {
                        if(candidate_testRule_edge__edge0.type.TypeID!=1) {
                            continue;
                        }
                        if((candidate_testRule_edge__edge0.flags & (uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel) != 0)
                        {
                            continue;
                        }
                        // Implicit Source testRule_node_a from testRule_edge__edge0 
                        GRGEN_LGSP.LGSPNode candidate_testRule_node_a = candidate_testRule_edge__edge0.source;
                        if(candidate_testRule_node_a.type.TypeID!=18) {
                            continue;
                        }
                        Rule_testRule.Match_testRule match = matches.GetNextUnfilledPosition();
                        match._node_a = candidate_testRule_node_a;
                        match._node_f = candidate_testRule_node_f;
                        match._node_m = candidate_testRule_node_m;
                        match._edge__edge0 = candidate_testRule_edge__edge0;
                        match._edge__edge1 = candidate_testRule_edge__edge1;
                        matches.PositionWasFilledFixIt();
                        // if enough matches were found, we leave
                        if(maxMatches > 0 && matches.Count >= maxMatches)
                        {
                            candidate_testRule_node_f.MoveInHeadAfter(candidate_testRule_edge__edge0);
                            graph.MoveHeadAfter(candidate_testRule_edge__edge1);
                            candidate_testRule_edge__edge1.flags = candidate_testRule_edge__edge1.flags & ~((uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel) | prev__candidate_testRule_edge__edge1;
                            return matches;
                        }
                    }
                    while( (candidate_testRule_edge__edge0 = candidate_testRule_edge__edge0.inNext) != head_candidate_testRule_edge__edge0 );
                }
                candidate_testRule_edge__edge1.flags = candidate_testRule_edge__edge1.flags & ~((uint) GRGEN_LGSP.LGSPElemFlags.IS_MATCHED << negLevel) | prev__candidate_testRule_edge__edge1;
            }
            return matches;
        }
    }


    public class testActions : de.unika.ipd.grGen.lgsp.LGSPActions
    {
        public testActions(de.unika.ipd.grGen.lgsp.LGSPGraph lgspgraph, String modelAsmName, String actionsAsmName)
            : base(lgspgraph, modelAsmName, actionsAsmName)
        {
            InitActions();
        }

        public testActions(de.unika.ipd.grGen.lgsp.LGSPGraph lgspgraph)
            : base(lgspgraph)
        {
            InitActions();
        }

        private void InitActions()
        {
            actions.Add("testRule", (de.unika.ipd.grGen.lgsp.LGSPAction) Action_testRule.Instance);
        }

        public override String Name { get { return "testActions"; } }
        public override String ModelMD5Hash { get { return "6a630d39ca3371b697e3fb227fb1f51a"; } }
    }
}