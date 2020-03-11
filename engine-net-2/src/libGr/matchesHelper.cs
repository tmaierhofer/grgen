/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Edgar Jakumeit

using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace de.unika.ipd.grGen.libGr
{
    /// <summary>
    /// A helper class used in flattening an array of Matches objects (containing Match objects) to an array of Match objects,
    /// and handling an array of Match objects (as it is used by the multi rule all call and multi backtracking language constructs). 
    /// </summary>
    public static class MatchListHelper
    {
        public static void Add(List<IMatch> matchList, List<IMatches> matchesList)
        {
            foreach(IMatches matches in matchesList)
            {
                matchList.AddRange(matches);
            }
        }

        public static void Clone(List<IMatches> matchesList, List<IMatch> matchList)
        {
            Dictionary<IMatch, IMatch> originalToClone = new Dictionary<IMatch, IMatch>();
            for(int i = 0; i < matchesList.Count; ++i)
            {
                matchesList[i] = matchesList[i].Clone(originalToClone);
            }
            List<IMatch> originalMatchList = new List<IMatch>(matchList);
            matchList.Clear();
            foreach(IMatch originalMatch in originalMatchList)
            {
                matchList.Add(originalToClone[originalMatch]);
            }
        }

        public static List<T> ToList<T>(IList<IMatch> source) where T : IMatch
        {
            List<T> newList = new List<T>(source.Count);
            for(int i = 0; i < source.Count; ++i)
            {
                newList.Add((T)source[i]);
            }
            return newList;
        }

        public static void FromList<T>(IList<IMatch> target, List<T> source) where T : IMatch
        {
            target.Clear();
            for(int i=0; i<source.Count; ++i)
            {
                if(source[i] != null)
                    target.Add(source[i]);
            }
        }

        /// <summary>
        /// For filtering with the auto-supplied filter keepFirst
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="count">The number of matches to keep</param>
        public static void Filter_keepFirst(List<IMatch> matchList, int count)
        {
            count = Math.Min(matchList.Count, count);
            matchList.RemoveRange(count, matchList.Count - count);
        }

        /// <summary>
        /// For filtering with the auto-supplied filter keepLast
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="count">The number of matches to keep</param>
        public static void Filter_keepLast(List<IMatch> matchList, int count)
        {
            count = Math.Min(matchList.Count, count);
            matchList.RemoveRange(0, matchList.Count - count);
        }

        /// <summary>
        /// For filtering with the auto-supplied filter removeFirst
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="count">The number of matches to remove</param>
        public static void Filter_removeFirst(List<IMatch> matchList, int count)
        {
            count = Math.Min(matchList.Count, count);
            matchList.RemoveRange(0, count);
        }

        /// <summary>
        /// For filtering with the auto-supplied filter removeLast
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="count">The number of matches to remove</param>
        public static void Filter_removeLast(List<IMatch> matchList, int count)
        {
            count = Math.Min(matchList.Count, count);
            matchList.RemoveRange(matchList.Count - count, count);
        }

        /// <summary>
        /// For filtering with the auto-supplied filter keepFirstFraction
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="fraction">The fraction of matches to keep</param>
        public static void Filter_keepFirstFraction(List<IMatch> matchList, double fraction)
        {
            Filter_keepFirst(matchList, (int)Math.Ceiling(fraction * matchList.Count));
        }

        /// <summary>
        /// For filtering with the auto-supplied filter keepLastFraction
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="fraction">The fraction of matches to keep</param>
        public static void Filter_keepLastFraction(List<IMatch> matchList, double fraction)
        {
            Filter_keepLast(matchList, (int)Math.Ceiling(fraction * matchList.Count));
        }

        /// <summary>
        /// For filtering with the auto-supplied filter removeFirstFraction
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="fraction">The fraction of matches to remove</param>
        public static void Filter_removeFirstFraction(List<IMatch> matchList, double fraction)
        {
            Filter_removeFirst(matchList, (int)Math.Ceiling(fraction * matchList.Count));
        }

        /// <summary>
        /// For filtering with the auto-supplied filter removeLastFraction
        /// </summary>
        /// <param name="matchList">The list with the matches</param>
        /// <param name="fraction">The fraction of matches to remove</param>
        public static void Filter_removeLastFraction(List<IMatch> matchList, double fraction)
        {
            Filter_removeLast(matchList, (int)Math.Ceiling(fraction * matchList.Count));
        }
    }

    public static class MatchPrinter
    {
        public static string ToString(IMatch match, IGraph graph)
        {
            return ToString(match, graph, "");
        }

        public static string ToString(IMatch match, IGraph graph, string indent)
        {
            StringBuilder sb = new StringBuilder(4096);

            sb.Append(indent + "nodes: ");
            foreach(INode node in match.Nodes)
            {
                sb.Append(EmitHelper.ToStringAutomatic(node, graph));
                sb.Append(" ");
            }
            sb.Append("\n");

            sb.Append(indent + "edges: ");
            foreach(IEdge edge in match.Edges)
            {
                sb.Append(EmitHelper.ToStringAutomatic(edge, graph));
                sb.Append(" ");
            }
            sb.Append("\n");

            if(match.NumberOfIndependents > 0)
            {
                sb.Append(indent + "independents: \n");
                foreach(IMatch independent in match.Independents)
                {
                    sb.Append(ToString(independent, graph, indent + "  "));
                }
                sb.Append("\n");
            }

            if(match.NumberOfAlternatives > 0)
            {
                sb.Append(indent + "alternatives: \n");
                foreach(IMatch alternativeCase in match.Alternatives)
                {
                    sb.Append(ToString(alternativeCase, graph, indent + "  "));
                }
                sb.Append("\n");
            }

            if(match.NumberOfIterateds > 0)
            {
                sb.Append(indent + "iterateds: \n");
                foreach(IMatches iterated in match.Iterateds)
                {
                    sb.Append(indent + " iterated: \n");
                    foreach(IMatch iteratedMatch in iterated)
                    {
                        sb.Append(ToString(iteratedMatch, graph, indent + "  "));
                    }
                }
                sb.Append("\n");
            }

            if(match.NumberOfEmbeddedGraphs > 0)
            {
                sb.Append(indent + "subpatterns: \n");
                foreach(IMatch subpattern in match.EmbeddedGraphs)
                {
                    sb.Append(ToString(subpattern, graph, indent + "  "));
                }
                sb.Append("\n");
            }

            return sb.ToString();
        }
    }

    public static class MatchedElementsValidityChecker
    {
        public static void Check(IMatch match)
        {
            for(int i=0; i < match.NumberOfNodes; ++i)
            {
                if(match.Pattern.Nodes[i].Annotations.ContainsAnnotation("validityCheck"))
                {
                    if(match.Pattern.Nodes[i].Annotations["validityCheck"].Equals("false"))
                        continue;
                }

                INode node = match.getNodeAt(i);
                if(!node.Valid)
                    throw new Exception(GetExceptionMessage("node", match.Pattern.Nodes[i].Name));
            }

            for(int i=0; i < match.NumberOfEdges; ++i)
            {
                if(match.Pattern.Edges[i].Annotations.ContainsAnnotation("validityCheck"))
                {
                    if(match.Pattern.Edges[i].Annotations["validityCheck"].Equals("false"))
                        continue;
                }

                IEdge edge = match.getEdgeAt(i);
                if(!edge.Valid && edge.Source.Valid && edge.Target.Valid) // an edge that is referenced by not in the graph anymore because its node was deleted is not causing an exception (SPO-like)
                    throw new Exception(GetExceptionMessage("edge", match.Pattern.Edges[i].Name));
            }

            for(int i=0; i < match.NumberOfIndependents; ++i)
            {
                IMatch independent = match.getIndependentAt(i);
                Check(independent);
            }

            for(int i=0; i < match.NumberOfAlternatives; ++i)
            {
                IMatch alternativeCase = match.getAlternativeAt(i);
                Check(alternativeCase);
            }

            for(int i=0; i < match.NumberOfIterateds; ++i)
            {
                IMatches iterated = match.getIteratedAt(i);
                foreach(IMatch iteratedMatch in iterated)
                {
                    Check(iteratedMatch);
                }
            }

            for(int i=0; i < match.NumberOfEmbeddedGraphs; ++i)
            {
                IMatch subpattern = match.getEmbeddedGraphAt(i);
                Check(subpattern);
            }
        }

        private static String GetExceptionMessage(String typeOfGraphElement, String nameOfPatternElement)
        {
            StringBuilder sb = new StringBuilder();
            sb.AppendFormat("The {0} that was matched to {1} is invalid by now, i.e. it is not contained in the graph anymore. ", typeOfGraphElement, nameOfPatternElement);
            sb.AppendFormat("It is probably accessed during the upcoming rewrite. Accessing already removed zombie {0}s should be avoided. ", typeOfGraphElement);
            sb.AppendFormat("This situation is typically caused by overlapping matches (of an all-bracketed rule, or the some-of-set-braces). ");
            sb.AppendFormat("Most likely a preceding rewrite of a match part already removed it. It depends on the exact actions of the upcoming rewrite of this match part what will happen. ");
            sb.AppendFormat("It may cause no harm. It will create real issues if the rewrites are conflicting. ");
            sb.AppendFormat("The situation that a {0} matched multiple times is to be deleted multiple times is handled gracefully. ", typeOfGraphElement);
            sb.AppendFormat("The situation that a {0} matched multiple times is to be retyped multiple times will cause crashes (conflict). ", typeOfGraphElement);
            sb.AppendFormat("The situation that a {0} matched multiple times is to be deleted and also retyped will cause crashes (conflict). ", typeOfGraphElement);
            sb.AppendFormat("Obtaining the name is a conflict that can lead to strange effects; fetching attributes could give stale results. ");
            sb.AppendFormat("(A {0} may be matched multiple times through the same pattern element, but also through different pattern elements.) ", typeOfGraphElement);
            return sb.ToString();
        }
    }
}
