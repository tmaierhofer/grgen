/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 2.1
 * Copyright (C) 2008 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under GPL v3 (see LICENSE.txt included in the packaging of this file)
 */

using System;
using de.unika.ipd.grGen.lgsp;
using de.unika.ipd.grGen.libGr;
using de.unika.ipd.grGen.Action_Alternatives;
using de.unika.ipd.grGen.Model_Alternatives;

namespace Alternatives
{
    class AlternativeExample
    {
        LGSPGraph graph;
        AlternativesActions actions;

        void DoAlt()
        {
            graph = new LGSPGraph(new AlternativesGraphModel());
            actions = new AlternativesActions(graph);

			graph.PerformanceInfo = new PerformanceInfo();

            actions.ApplyGraphRewriteSequence("createComplex");

			Console.WriteLine(graph.PerformanceInfo.MatchesFound + " matches found.");
			Console.WriteLine(graph.PerformanceInfo.RewritesPerformed + " rewrites performed.");
			graph.PerformanceInfo.Reset();

            IMatches matches = actions.GetAction("Complex").Match(graph, 0, null);
            Console.WriteLine(matches.Count + " matches found.");
        }

        static void Main(string[] args)
        {
            AlternativeExample alt = new AlternativeExample();
            alt.DoAlt();
        }
    }
}
