/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Moritz Kroll, Edgar Jakumeit

using System;
using System.Collections.Generic;
using de.unika.ipd.grGen.libGr;

namespace de.unika.ipd.grGen.lgsp
{
    /// <summary>
    /// An implementation of the IGraphProcessingEnvironment, to be used with LGSPGraphs.
    /// </summary>
    public class LGSPActionExecutionEnvironment : IActionExecutionEnvironment
    {
        private readonly PerformanceInfo perfInfo = new PerformanceInfo();
        private bool highlightingUnderway = false;
        private int maxMatches = 0;
        private readonly Dictionary<IAction, IAction> actionMapStaticToNewest = new Dictionary<IAction, IAction>();
        public LGSPGraph graph { get { return usedGraphs.Peek(); } }
        protected readonly Stack<LGSPGraph> usedGraphs;
        public LGSPNamedGraph namedGraph { get { return namedGraphOnTop; } }
        protected LGSPNamedGraph namedGraphOnTop;
        public LGSPActions curActions;
        protected readonly Dictionary<String, String> customCommandsToDescriptions;


        public LGSPActionExecutionEnvironment(LGSPGraph graph, LGSPActions actions)
        {
            // TODO: evt. IGraph + BaseActions und dann hier cast auf LGSP, mal gucken was an Schnittstelle besser pa�t
            this.usedGraphs = new Stack<LGSPGraph>();
            this.usedGraphs.Push(graph);
            this.namedGraphOnTop = graph as LGSPNamedGraph;
            this.curActions = actions;
            InitActionsProfile(actions);
            this.customCommandsToDescriptions = new Dictionary<string, string>();
            FillCustomCommandDescriptions();
        }

        public IGraph Graph
        {
            get { return graph; }
            set {
                if(usedGraphs.Count != 1)
                    throw new Exception("Can't replace graph while a subgraph is used by an action/sequence!");
                else
                {
                    usedGraphs.Clear();
                    usedGraphs.Push((LGSPGraph)value);
                    namedGraphOnTop = value as LGSPNamedGraph;
                }
            }
        }

        public INamedGraph NamedGraph
        {
            get { return namedGraph; }
        }

        public IActions Actions
        {
            get { return curActions; }
            set { curActions = (LGSPActions)value; InitActionsProfile((LGSPActions)value); }
        }

        public void InitActionsProfile(LGSPActions actions)
        {
            if(actions == null)
                return;
            if(!actions.Profile)
                return;

            perfInfo.ActionProfiles.Clear();
            foreach(IAction action in actions.Actions)
            {
                ActionProfile actionProfile = new ActionProfile();
                int branchingFactor = ((LGSPAction)action).patternGraph.branchingFactor;
                actionProfile.averagesPerThread = new ProfileAverages[branchingFactor];
                for(int i = 0; i < branchingFactor; ++i)
                    actionProfile.averagesPerThread[i] = new ProfileAverages();
                perfInfo.ActionProfiles.Add(action.PackagePrefixedName, actionProfile);
            }
        }
        

        public PerformanceInfo PerformanceInfo
        {
            get { return perfInfo; }
        }

        public bool HighlightingUnderway
        {
            get { return highlightingUnderway; }
            set { highlightingUnderway = value; }
        }

        public int MaxMatches
        {
            get { return maxMatches; }
            set { maxMatches = value; }
        }

        private void FillCustomCommandDescriptions()
        {
            customCommandsToDescriptions.Add("set_max_matches", 
                "- set_max_matches: Sets the maximum number of matches to be found\n" +
                "     during matching (for all-bracketed rule calls like [r]).\n");
        }

        public IDictionary<String, String> CustomCommandsAndDescriptions
        {
            get
            {
                return customCommandsToDescriptions;
            }
        }

        public virtual void Custom(params object[] args)
        {
            if(args.Length == 0)
                throw new ArgumentException("No command given");

            String command = (String)args[0];
            switch(command)
            {
            case "set_max_matches":
                if(args.Length != 2)
                    throw new ArgumentException("Usage: set_max_matches <integer>\nIf <integer> <= 0, all matches will be matched.");

                int newMaxMatches;
                if(!int.TryParse((String)args[1], out newMaxMatches))
                    throw new ArgumentException("Illegal integer value specified: \"" + (String)args[1] + "\"");
                MaxMatches = newMaxMatches;
                return;

            default:
                throw new ArgumentException("Unknown command: " + command);
            }
        }


        #region Graph rewriting

        public IAction GetNewestActionVersion(IAction action)
        {
            IAction newest;
            if(!actionMapStaticToNewest.TryGetValue(action, out newest))
                return action;
            return newest;
        }

        public void SetNewestActionVersion(IAction staticAction, IAction newAction)
        {
            actionMapStaticToNewest[staticAction] = newAction;
        }

        public List<object[]> Replace(IMatches matches, int which)
        {
            List<object[]> returns;
            object[] retElems = null;
            if(which != -1)
            {
                if(which < 0 || which >= matches.Count)
                    throw new ArgumentOutOfRangeException("\"which\" is out of range!");

                returns = matches.Producer.Reserve(0); 
                retElems = matches.Producer.Modify(this, matches.GetMatch(which));
                returns.Add(retElems);
                PerformanceInfo.RewritesPerformed++;
            }
            else
            {
                bool first = true;
                returns = matches.Producer.Reserve(matches.Count);
                int curResultNum = 0;
                foreach(IMatch match in matches)
                {
                    if(first) first = false;
                    else if(OnRewritingNextMatch != null) OnRewritingNextMatch();
                    retElems = matches.Producer.Modify(this, match);
                    object[] curResult = returns[curResultNum];
                    for(int i = 0; i < retElems.Length; ++i)
                        curResult[i] = retElems[i];
                    PerformanceInfo.RewritesPerformed++;
                    curResultNum++;
                }
            }
            return returns;
        }

        #endregion Graph rewriting


        #region Events

        public event AfterMatchHandler OnMatched;
        public event BeforeFinishHandler OnFinishing;
        public event RewriteNextMatchHandler OnRewritingNextMatch;
        public event AfterFinishHandler OnFinished;

        public void Matched(IMatches matches, IMatch match, bool special)
        {
            AfterMatchHandler handler = OnMatched;
            if(handler != null) handler(matches, match, special);
        }

        public void Finishing(IMatches matches, bool special)
        {
            BeforeFinishHandler handler = OnFinishing;
            if(handler != null) handler(matches, special);
        }

        public void RewritingNextMatch()
        {
            RewriteNextMatchHandler handler = OnRewritingNextMatch;
            if(handler != null) handler();
        }

        public void Finished(IMatches matches, bool special)
        {
            AfterFinishHandler handler = OnFinished;
            if(handler != null) handler(matches, special);
        }

        #endregion Events
    }
}
