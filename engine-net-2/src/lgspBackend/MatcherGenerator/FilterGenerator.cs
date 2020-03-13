/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Edgar Jakumeit

using System;
using System.Collections.Generic;
using de.unika.ipd.grGen.libGr;

namespace de.unika.ipd.grGen.lgsp
{
    /// <summary>
    /// The C#-part responsible for generating the post-matches filters.
    /// </summary>
    public static class FilterGenerator
    {
        public static void GenerateFilterStubs(SourceBuilder source, LGSPRulePattern rulePattern)
        {
            String rulePatternClassName = rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";

            foreach(IFilter filter in rulePattern.Filters)
            {
                if(filter is IFilterFunction)
                {
                    IFilterFunction filterFunction = (IFilterFunction)filter;
                    if(!filterFunction.IsExternal)
                        continue;

                    if(filter.Package != null)
                    {
                        source.AppendFrontFormat("namespace {0}\n", filter.Package);
                        source.AppendFront("{\n");
                        source.Indent();
                    }
                    source.AppendFront("public partial class MatchFilters\n");
                    source.AppendFront("{\n");
                    source.Indent();

                    source.AppendFrontFormat("//public static void Filter_{0}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {1} matches", filter.Name, matchesListType);
                    for(int i = 0; i < filterFunction.Inputs.Length; ++i)
                    {
                        source.AppendFormat(", {0} {1}", TypesHelper.TypeName(filterFunction.Inputs[i]), filterFunction.InputNames[i]);
                    }
                    source.Append(")\n");

                    source.Unindent();
                    source.AppendFront("}\n");
                    if(filter.Package != null)
                    {
                        source.Unindent();
                        source.AppendFront("}\n");
                    }
                }
            }
        }

        public static void GenerateFilters(SourceBuilder source, LGSPRulePattern rulePattern)
        {
            foreach(IFilter filter in rulePattern.Filters)
            {
                if(filter is IFilterAutoGenerated)
                {
                    IFilterAutoGenerated filterAutoGenerated = (IFilterAutoGenerated)filter;

                    if(filterAutoGenerated.PackageOfApplyee != null)
                    {
                        source.AppendFrontFormat("namespace {0}\n", filterAutoGenerated.PackageOfApplyee);
                        source.AppendFront("{\n");
                        source.Indent();
                    }
                    source.AppendFront("public partial class MatchFilters\n");
                    source.AppendFront("{\n");
                    source.Indent();

                    if(filterAutoGenerated.PlainName == "auto")
                        GenerateAutomorphyFilter(source, rulePattern);
                    else
                    {
                        if(filterAutoGenerated.PlainName == "orderAscendingBy")
                            GenerateOrderByFilter(source, rulePattern, (LGSPFilterAutoGenerated)filterAutoGenerated, true);
                        if(filterAutoGenerated.PlainName == "orderDescendingBy")
                            GenerateOrderByFilter(source, rulePattern, (LGSPFilterAutoGenerated)filterAutoGenerated, false);
                        if(filterAutoGenerated.PlainName == "groupBy")
                            GenerateGroupByFilter(source, rulePattern, filterAutoGenerated.Entities[0]);
                        if(filterAutoGenerated.PlainName == "keepSameAsFirst")
                            GenerateKeepSameFilter(source, rulePattern, filterAutoGenerated.Entities[0], true);
                        if(filterAutoGenerated.PlainName == "keepSameAsLast")
                            GenerateKeepSameFilter(source, rulePattern, filterAutoGenerated.Entities[0], false);
                        if(filterAutoGenerated.PlainName == "keepOneForEach")
                            GenerateKeepOneForEachFilter(source, rulePattern, filterAutoGenerated.Entities[0]);
                    }

                    source.Unindent();
                    source.AppendFront("}\n");
                    if(filterAutoGenerated.PackageOfApplyee != null)
                    {
                        source.Unindent();
                        source.AppendFront("}\n");
                    }
                }
            }
        }

        public static void GenerateMatchClassFilterStubs(SourceBuilder source, MatchClassInfo matchClass)
        {
            String matchClassName = matchClass.Name;
            String matchInterfaceName = NamesOfEntities.MatchInterfaceName(matchClassName);
            String matchesListType = "IList<GRGEN_LIBGR.IMatch>";

            foreach(IFilter filter in matchClass.Filters)
            {
                if(filter is IFilterFunction)
                {
                    IFilterFunction filterFunction = (IFilterFunction)filter;
                    if(!filterFunction.IsExternal)
                        continue;

                    if(filter.Package != null)
                    {
                        source.AppendFrontFormat("namespace {0}\n", filter.Package);
                        source.AppendFront("{\n");
                        source.Indent();
                    }
                    source.AppendFront("public partial class MatchClassFilters\n");
                    source.AppendFront("{\n");
                    source.Indent();

                    source.AppendFrontFormat("//public static void Filter_{0}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {1} matches", filter.Name, matchesListType);
                    for(int i = 0; i < filterFunction.Inputs.Length; ++i)
                    {
                        source.AppendFormat(", {0} {1}", TypesHelper.TypeName(filterFunction.Inputs[i]), filterFunction.InputNames[i]);
                    }
                    source.Append(")\n");

                    source.Unindent();
                    source.AppendFront("}\n");
                    if(filter.Package != null)
                    {
                        source.Unindent();
                        source.AppendFront("}\n");
                    }
                }
            }
        }

        public static void GenerateMatchClassFilters(SourceBuilder source, MatchClassInfo matchClass)
        {
            foreach(IFilter filter in matchClass.Filters)
            {
                if(filter is IFilterAutoGenerated)
                {
                    IFilterAutoGenerated filterAutoGenerated = (IFilterAutoGenerated)filter;

                    if(filterAutoGenerated.PackageOfApplyee != null)
                    {
                        source.AppendFrontFormat("namespace {0}\n", filterAutoGenerated.PackageOfApplyee);
                        source.AppendFront("{\n");
                        source.Indent();
                    }
                    source.AppendFront("public partial class MatchClassFilters\n");
                    source.AppendFront("{\n");
                    source.Indent();

                    if(filterAutoGenerated.PlainName == "orderAscendingBy")
                        GenerateMatchClassOrderByFilter(source, matchClass, (LGSPFilterAutoGenerated)filterAutoGenerated, true);
                    if(filterAutoGenerated.PlainName == "orderDescendingBy")
                        GenerateMatchClassOrderByFilter(source, matchClass, (LGSPFilterAutoGenerated)filterAutoGenerated, false);
                    if(filterAutoGenerated.PlainName == "groupBy")
                        GenerateMatchClassGroupByFilter(source, matchClass, filterAutoGenerated.Entities[0]);
                    if(filterAutoGenerated.PlainName == "keepSameAsFirst")
                        GenerateMatchClassKeepSameFilter(source, matchClass, filterAutoGenerated.Entities[0], true);
                    if(filterAutoGenerated.PlainName == "keepSameAsLast")
                        GenerateMatchClassKeepSameFilter(source, matchClass, filterAutoGenerated.Entities[0], false);
                    if(filterAutoGenerated.PlainName == "keepOneForEach")
                        GenerateMatchClassKeepOneForEachFilter(source, matchClass, filterAutoGenerated.Entities[0]);

                    source.Unindent();
                    source.AppendFront("}\n");
                    if(filterAutoGenerated.PackageOfApplyee != null)
                    {
                        source.Unindent();
                        source.AppendFront("}\n");
                    }
                }
            }
        }

        private static void GenerateAutomorphyFilter(SourceBuilder source, LGSPRulePattern rulePattern)
        {
            String rulePatternClassName = TypesHelper.GetPackagePrefixDot(rulePattern.PatternGraph.Package) + rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchClassName = rulePatternClassName + "." + NamesOfEntities.MatchClassName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";
            String filterName = "auto";
            
            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n", 
                rulePattern.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFront("if(matches.Count < 2)\n");
            source.AppendFront("\treturn;\n");
            source.AppendFrontFormat("List<{0}> matchesArray = matches.ToList();\n", matchInterfaceName);

            source.AppendFrontFormat("if(matches.Count < 5 || {0}.Instance.patternGraph.nodes.Length + {0}.Instance.patternGraph.edges.Length < 1)\n", rulePatternClassName);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFront("for(int i = 0; i < matchesArray.Count; ++i)\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFront("if(matchesArray[i] == null)\n");
            source.AppendFront("\tcontinue;\n");
            source.AppendFront("for(int j = i + 1; j < matchesArray.Count; ++j)\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFront("if(matchesArray[j] == null)\n");
            source.AppendFront("\tcontinue;\n");
            source.AppendFront("if(GRGEN_LIBGR.SymmetryChecker.AreSymmetric(matchesArray[i], matchesArray[j], procEnv.graph))\n");
            source.AppendFront("\tmatchesArray[j] = null;\n");
            source.Unindent();
            source.AppendFront("}\n");
            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");
            source.AppendFront("else\n");
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("Dictionary<int, {0}> foundMatchesOfSameMainPatternHash = new Dictionary<int, {0}>();\n", 
                matchClassName);
            source.AppendFront("for(int i = 0; i < matchesArray.Count; ++i)\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFrontFormat("{0} match = ({0})matchesArray[i];\n", matchClassName);
            source.AppendFront("int duplicateMatchHash = 0;\n");
            source.AppendFront("for(int j = 0; j < match.NumberOfNodes; ++j) duplicateMatchHash ^= match.getNodeAt(j).GetHashCode();\n");
            source.AppendFront("for(int j = 0; j < match.NumberOfEdges; ++j) duplicateMatchHash ^= match.getEdgeAt(j).GetHashCode();\n");
            source.AppendFront("bool contained = foundMatchesOfSameMainPatternHash.ContainsKey(duplicateMatchHash);\n");
            source.AppendFront("if(contained)\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFrontFormat("{0} duplicateMatchCandidate = foundMatchesOfSameMainPatternHash[duplicateMatchHash];\n", matchClassName);
            source.AppendFront("do\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFront("if(GRGEN_LIBGR.SymmetryChecker.AreSymmetric(match, duplicateMatchCandidate, procEnv.graph))\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFront("matchesArray[i] = null;\n");
            source.AppendFrontFormat("goto label_auto_{0};\n", rulePatternClassName.Replace('.', '_'));
            source.Unindent();
            source.AppendFront("}\n");
            source.Unindent();
            source.AppendFront("}\n");
            source.AppendFront("while((duplicateMatchCandidate = duplicateMatchCandidate.nextWithSameHash) != null);\n");
            source.Unindent();
            source.AppendFront("}\n");
            source.AppendFront("if(!contained)\n");
            source.AppendFront("\tfoundMatchesOfSameMainPatternHash[duplicateMatchHash] = match;\n");
            source.AppendFront("else\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFrontFormat("{0} duplicateMatchCandidate = foundMatchesOfSameMainPatternHash[duplicateMatchHash];\n", matchClassName);
            source.AppendFront("while(duplicateMatchCandidate.nextWithSameHash != null) duplicateMatchCandidate = duplicateMatchCandidate.nextWithSameHash;\n");
            source.AppendFront("duplicateMatchCandidate.nextWithSameHash = match;\n");
            source.Unindent();
            source.AppendFront("}\n");
            source.AppendFormat("label_auto_{0}: ;\n", rulePatternClassName.Replace('.', '_'));
            source.Unindent();
            source.AppendFront("}\n");
            source.AppendFrontFormat("foreach({0} toClean in foundMatchesOfSameMainPatternHash.Values) toClean.CleanNextWithSameHash();\n", matchClassName);
            
            source.Unindent();
            source.AppendFront("}\n");

            source.AppendFront("matches.FromList();\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateOrderByFilter(SourceBuilder source, LGSPRulePattern rulePattern, LGSPFilterAutoGenerated filter, bool ascending)
        {
            String rulePatternClassName = TypesHelper.GetPackagePrefixDot(rulePattern.PatternGraph.Package) + rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";
            String filterName = ascending ? "orderAscendingBy_" + filter.EntitySuffixUnderscore : "orderDescendingBy_" + filter.EntitySuffixUnderscore;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n", 
                rulePattern.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = matches.ToList();\n", matchInterfaceName);
            source.AppendFrontFormat("matchesArray.Sort(new Comparer_{0}_{1}());\n", rulePattern.name, filterName);
            source.AppendFront("matches.FromList();\n");

            source.Unindent();
            source.AppendFront("}\n");

            source.AppendFrontFormat("class Comparer_{0}_{1} : Comparer<{2}>\n", rulePattern.name, filterName, matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("public override int Compare({0} left, {0} right)\n", matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();
            GenerateOrderByFilter(source, filter.Entities, ascending, false, 0);
            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateMatchClassOrderByFilter(SourceBuilder source, MatchClassInfo matchClass, LGSPFilterAutoGenerated filter, bool ascending)
        {
            String matchInterfaceName = NamesOfEntities.MatchInterfaceName(matchClass.name);
            String matchesListType = "IList<GRGEN_LIBGR.IMatch>";
            String filterName = ascending ? "orderAscendingBy_" + filter.EntitySuffixUnderscore : "orderDescendingBy_" + filter.EntitySuffixUnderscore;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                matchClass.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = GRGEN_LIBGR.MatchListHelper.ToList<{0}>(matches);\n", matchInterfaceName);
            source.AppendFrontFormat("matchesArray.Sort(new Comparer_{0}_{1}());\n", matchClass.name, filterName);
            source.AppendFrontFormat("GRGEN_LIBGR.MatchListHelper.FromList(matches, matchesArray);\n");

            source.Unindent();
            source.AppendFront("}\n");

            source.AppendFrontFormat("class Comparer_{0}_{1} : Comparer<{2}>\n", matchClass.name, filterName, matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("public override int Compare({0} left, {0} right)\n", matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();
            GenerateOrderByFilter(source, filter.Entities, ascending, false, 0);
            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateOrderByFilter(SourceBuilder source, List<String> filterVariables, bool ascending, bool leaf, int pos)
        {
            String filterVariable = filterVariables[pos];
            if(pos + 1 < filterVariables.Count && !leaf)
            {
                source.AppendFrontFormat("if(left.{0}.CompareTo(right.{0})==0)", NamesOfEntities.MatchName(filterVariable, EntityType.Variable));
                source.Indent();
                GenerateOrderByFilter(source, filterVariables, ascending, false, pos + 1);
                source.Unindent();
                source.AppendFrontFormat("else");
                source.Indent();
                GenerateOrderByFilter(source, filterVariables, ascending, true, pos);
                source.Unindent();
            }
            else
            {
                if(ascending)
                    source.AppendFrontFormat("return left.{0}.CompareTo(right.{0});\n", NamesOfEntities.MatchName(filterVariable, EntityType.Variable));
                else
                    source.AppendFrontFormat("return -left.{0}.CompareTo(right.{0});\n", NamesOfEntities.MatchName(filterVariable, EntityType.Variable));
            }
        }

        private static void GenerateGroupByFilter(SourceBuilder source, LGSPRulePattern rulePattern, String filterVariable)
        {
            String rulePatternClassName = TypesHelper.GetPackagePrefixDot(rulePattern.PatternGraph.Package) + rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";
            String filterName = "groupBy_" + filterVariable;

            if(true) // does the type of the variable to group-by support ordering? then order, is more efficient than equality comparisons
            {
                source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                    rulePattern.name, filterName, matchesListType);
                source.AppendFront("{\n");
                source.Indent();

                source.AppendFrontFormat("List<{0}> matchesArray = matches.ToList();\n", matchInterfaceName);
                source.AppendFrontFormat("matchesArray.Sort(new Comparer_{0}_{1}());\n", rulePattern.name, filterName);
                source.AppendFront("matches.FromList();\n");

                source.Unindent();
                source.AppendFront("}\n");

                source.AppendFrontFormat("class Comparer_{0}_{1} : Comparer<{2}>\n", rulePattern.name, filterName, matchInterfaceName);
                source.AppendFront("{\n");
                source.Indent();

                source.AppendFrontFormat("public override int Compare({0} left, {0} right)\n", matchInterfaceName);
                source.AppendFront("{\n");
                source.Indent();
                source.AppendFrontFormat("return left.{0}.CompareTo(right.{0});\n", NamesOfEntities.MatchName(filterVariable, EntityType.Variable));
                source.Unindent();
                source.AppendFront("}\n");

                source.Unindent();
                source.AppendFront("}\n");
            }
            else
            {
                // handle also GenerateMatchClassGroupByFilter if tackled

                // ensure that if two elements are equal, they are neighbours or only separated by equal elements
                // not needed yet, as of now we only support numerical types and string, that support ordering in addition to equality comparison
                /*List<T> matchesArray;
                for(int i = 0; i < matchesArray.Count - 1; ++i)
                {
                    for(int j = i + 1; j < matchesArray.Count; ++j)
                    {
                        if(matchesArray[i] == matchesArray[j])
                        {
                            T tmp = matchesArray[i + 1];
                            matchesArray[i + 1] = matchesArray[j];
                            matchesArray[j] = tmp;
                            break;
                        }
                    }
                }*/
            }
        }

        private static void GenerateMatchClassGroupByFilter(SourceBuilder source, MatchClassInfo matchClass, String filterVariable)
        {
            String matchInterfaceName = NamesOfEntities.MatchInterfaceName(matchClass.name);
            String matchesListType = "IList<GRGEN_LIBGR.IMatch>";
            String filterName = "groupBy_" + filterVariable;

            // see GenerateGroupByFilter for potential optimization

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                matchClass.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = GRGEN_LIBGR.MatchListHelper.ToList<{0}>(matches);\n", matchInterfaceName);
            source.AppendFrontFormat("matchesArray.Sort(new Comparer_{0}_{1}());\n", matchClass.name, filterName);
            source.AppendFrontFormat("GRGEN_LIBGR.MatchListHelper.FromList(matches, matchesArray);\n");

            source.Unindent();
            source.AppendFront("}\n");

            source.AppendFrontFormat("class Comparer_{0}_{1} : Comparer<{2}>\n", matchClass.name, filterName, matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("public override int Compare({0} left, {0} right)\n", matchInterfaceName);
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFrontFormat("return left.{0}.CompareTo(right.{0});\n", NamesOfEntities.MatchName(filterVariable, EntityType.Variable));
            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateKeepSameFilter(SourceBuilder source, LGSPRulePattern rulePattern, String filterVariable, bool sameAsFirst)
        {
            String rulePatternClassName = TypesHelper.GetPackagePrefixDot(rulePattern.PatternGraph.Package) + rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";
            String filterName = sameAsFirst ? "keepSameAsFirst_" + filterVariable : "keepSameAsLast_" + filterVariable;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n", 
                rulePattern.name, filterName, matchesListType);

            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = matches.ToList();\n", matchInterfaceName);

            String matchEntity = NamesOfEntities.MatchName(filterVariable, EntityType.Variable);
            if(sameAsFirst)
            {
                source.AppendFront("int pos = 0 + 1;\n");
                source.AppendFront("while(pos < matchesArray.Count)\n");
                source.AppendFront("{\n");
                source.AppendFrontFormat("\tif(matchesArray[pos].{0} != matchesArray[0].{0})\n", matchEntity);
                source.AppendFront("\t\tmatchesArray[pos] = null;\n");
                source.AppendFront("\t++pos;\n");
                source.AppendFront("}\n");
            }
            else
            {
                source.AppendFront("int pos = matchesArray.Count-1 - 1;\n");
                source.AppendFront("while(pos >= 0)\n");
                source.AppendFront("{\n");
                source.AppendFrontFormat("\tif(matchesArray[pos].{0} != matchesArray[matchesArray.Count-1].{0})\n", matchEntity);
                source.AppendFront("\t\tmatchesArray[pos] = null;\n");
                source.AppendFront("\t--pos;\n");
                source.AppendFront("}\n");
            }

            source.AppendFront("matches.FromList();\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateMatchClassKeepSameFilter(SourceBuilder source, MatchClassInfo matchClass, String filterVariable, bool sameAsFirst)
        {
            String matchInterfaceName = NamesOfEntities.MatchInterfaceName(matchClass.name);
            String matchesListType = "IList<GRGEN_LIBGR.IMatch>";
            String filterName = sameAsFirst ? "keepSameAsFirst_" + filterVariable : "keepSameAsLast_" + filterVariable;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                matchClass.name, filterName, matchesListType);

            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = GRGEN_LIBGR.MatchListHelper.ToList<{0}>(matches);\n", matchInterfaceName);

            String matchEntity = NamesOfEntities.MatchName(filterVariable, EntityType.Variable);
            if(sameAsFirst)
            {
                source.AppendFront("int pos = 0 + 1;\n");
                source.AppendFront("while(pos < matchesArray.Count)\n");
                source.AppendFront("{\n");
                source.AppendFrontFormat("\tif(matchesArray[pos].{0} != matchesArray[0].{0})\n", matchEntity);
                source.AppendFront("\t\tmatchesArray[pos] = null;\n");
                source.AppendFront("\t++pos;\n");
                source.AppendFront("}\n");
            }
            else
            {
                source.AppendFront("int pos = matchesArray.Count-1 - 1;\n");
                source.AppendFront("while(pos >= 0)\n");
                source.AppendFront("{\n");
                source.AppendFrontFormat("\tif(matchesArray[pos].{0} != matchesArray[matchesArray.Count-1].{0})\n", matchEntity);
                source.AppendFront("\t\tmatchesArray[pos] = null;\n");
                source.AppendFront("\t--pos;\n");
                source.AppendFront("}\n");
            }

            source.AppendFrontFormat("GRGEN_LIBGR.MatchListHelper.FromList(matches, matchesArray);\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateKeepOneForEachFilter(SourceBuilder source, LGSPRulePattern rulePattern, String filterVariable)
        {
            String rulePatternClassName = TypesHelper.GetPackagePrefixDot(rulePattern.PatternGraph.Package) + rulePattern.GetType().Name;
            String matchInterfaceName = rulePatternClassName + "." + NamesOfEntities.MatchInterfaceName(rulePattern.name);
            String matchesListType = "GRGEN_LIBGR.IMatchesExact<" + matchInterfaceName + ">";
            String filterName = "keepOneForEach_" + filterVariable;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                 rulePattern.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = matches.ToList();\n", matchInterfaceName);

            String matchEntity = NamesOfEntities.MatchName(filterVariable, EntityType.Variable);
            String typeOfEntity = getTypeOfFilterVariable(rulePattern, matchEntity);
            source.AppendFrontFormat("Dictionary<{0}, object> seenValues = new Dictionary<{0}, object>();\n", typeOfEntity);
            source.AppendFront("for(int pos = 0; pos < matchesArray.Count; ++pos)\n");
            source.AppendFront("{\n");
            source.AppendFrontFormat("\tif(seenValues.ContainsKey(matchesArray[pos].{0}))\n", matchEntity);
            source.AppendFront("\t\tmatchesArray[pos] = null;\n");
            source.AppendFront("\telse\n");
            source.AppendFrontFormat("\t\tseenValues.Add(matchesArray[pos].{0}, null);\n", matchEntity);
            source.AppendFront("}\n");

            source.AppendFront("matches.FromList();\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static void GenerateMatchClassKeepOneForEachFilter(SourceBuilder source, MatchClassInfo matchClass, String filterVariable)
        {
            String matchInterfaceName = NamesOfEntities.MatchInterfaceName(matchClass.name);
            String matchesListType = "IList<GRGEN_LIBGR.IMatch>";
            String filterName = "keepOneForEach_" + filterVariable;

            source.AppendFrontFormat("public static void Filter_{0}_{1}(GRGEN_LGSP.LGSPGraphProcessingEnvironment procEnv, {2} matches)\n",
                 matchClass.name, filterName, matchesListType);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFrontFormat("List<{0}> matchesArray = GRGEN_LIBGR.MatchListHelper.ToList<{0}>(matches);\n", matchInterfaceName);

            String matchEntity = NamesOfEntities.MatchName(filterVariable, EntityType.Variable);
            String typeOfEntity = getTypeOfFilterVariable(matchClass, matchEntity);
            source.AppendFrontFormat("Dictionary<{0}, object> seenValues = new Dictionary<{0}, object>();\n", typeOfEntity);
            source.AppendFront("for(int pos = 0; pos < matchesArray.Count; ++pos)\n");
            source.AppendFront("{\n");
            source.AppendFrontFormat("\tif(seenValues.ContainsKey(matchesArray[pos].{0}))\n", matchEntity);
            source.AppendFront("\t\tmatchesArray[pos] = null;\n");
            source.AppendFront("\telse\n");
            source.AppendFrontFormat("\t\tseenValues.Add(matchesArray[pos].{0}, null);\n", matchEntity);
            source.AppendFront("}\n");

            source.AppendFrontFormat("GRGEN_LIBGR.MatchListHelper.FromList(matches, matchesArray);\n");

            source.Unindent();
            source.AppendFront("}\n");
        }

        private static String getTypeOfFilterVariable(LGSPRulePattern rulePattern, String matchEntity)
        {
            foreach(IPatternVariable var in rulePattern.patternGraph.variables)
            {
                if(var.Name == rulePattern.PatternGraph.Name + "_" + matchEntity)
                    return TypesHelper.TypeName(var.Type);
            }

            return null;
        }

        private static String getTypeOfFilterVariable(MatchClassInfo matchClass, String matchEntity)
        {
            foreach(IPatternVariable var in matchClass.variables)
            {
                if(var.Name == matchClass.Name + "_" + matchEntity)
                    return TypesHelper.TypeName(var.Type);
            }

            return null;
        }

        public static void GenerateMatchClassFilterers(SourceBuilder source, MatchClassInfo matchClass)
        {
            source.AppendFront("\n");

            if(matchClass.Package != null)
            {
                source.AppendFront("namespace " + matchClass.Package + "\n");
                source.AppendFront("{\n");
                source.Indent();
            }

            String matchClassName = matchClass.Name;
            String matchesType = "List<GRGEN_LIBGR.IMatch>";
            String infoClassName = "MatchClassInfo_" + matchClass.Name;
            String filtererClassName = "MatchClassFilterer_" + matchClass.Name;

            source.AppendFront("public class " + filtererClassName + " : GRGEN_LIBGR.MatchClassFilterer\n");
            source.AppendFront("{\n");
            source.Indent();
            source.AppendFront("private static " + filtererClassName + " instance = null;\n");
            source.AppendFront("public static " + filtererClassName + " Instance { get { if (instance==null) { "
                    + "instance = new " + filtererClassName + "(); } return instance; } }\n");
            source.AppendFront("\n");

            source.AppendFront("private " + filtererClassName + "()\n");
            source.AppendFront("\t: base(" + infoClassName + ".Instance)\n");
            source.AppendFront("{\n");
            source.AppendFront("}\n");
            source.AppendFront("\n");

            source.AppendFront("public override void Filter(GRGEN_LIBGR.IActionExecutionEnvironment actionEnv, IList<GRGEN_LIBGR.IMatch> matches, GRGEN_LIBGR.FilterCallBase filter)\n");
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFront("switch(filter.FullName) {\n");
            source.Indent();
            foreach(IFilter filter in matchClass.Filters)
            {
                if(filter is IFilterAutoSupplied)
                {
                    IFilterAutoSupplied filterAutoSupplied = (IFilterAutoSupplied)filter;
                    source.AppendFrontFormat("case \"{0}\": ", filterAutoSupplied.Name);
                    source.AppendFormat("GRGEN_LIBGR.MatchListHelper.Filter_{0}((List<GRGEN_LIBGR.IMatch>)matches",
                        filterAutoSupplied.Name);
                    for(int i = 0; i < filterAutoSupplied.Inputs.Length; ++i)
                    {
                        source.AppendFormat(", ({0})(filter.Arguments[{1}])", TypesHelper.TypeName(filterAutoSupplied.Inputs[i]), i);
                    }
                    source.Append("); break;\n");
                }
                else if(filter is IFilterAutoGenerated)
                {
                    LGSPFilterAutoGenerated filterAutoGenerated = (LGSPFilterAutoGenerated)filter;
                    source.AppendFrontFormat("case \"{0}\": ", filterAutoGenerated.Name);
                    source.AppendFormat("GRGEN_ACTIONS.{0}MatchClassFilters.", 
                        TypesHelper.GetPackagePrefixDot(filterAutoGenerated.PackageOfApplyee));
                    source.AppendFormat("Filter_{0}_{1}((GRGEN_LGSP.LGSPGraphProcessingEnvironment)actionEnv, ({2})matches); break;\n",
                        matchClassName, filterAutoGenerated.NameWithUnderscoreSuffix, matchesType);
                }
                else if(filter is IFilterFunction)
                {
                    IFilterFunction filterFunction = (IFilterFunction)filter;
                    if(filterFunction.Package == null
                        || (matchClass.Package != null && filterFunction.Package == matchClass.Package && matchClass.GetFilter(filterFunction.Name) == null))
                    {
                        source.AppendFrontFormat("case \"{0}\": ", filterFunction.Name);
                        source.AppendFormat("GRGEN_ACTIONS.{0}MatchClassFilters.Filter_{1}((GRGEN_LGSP.LGSPGraphProcessingEnvironment)actionEnv, ({2})matches",
                            TypesHelper.GetPackagePrefixDot(filterFunction.Package), filterFunction.Name, matchesType);
                        for(int i = 0; i < filterFunction.Inputs.Length; ++i)
                        {
                            source.AppendFormat(", ({0})(filter.Arguments[{1}])", TypesHelper.TypeName(filterFunction.Inputs[i]), i);
                        }
                        source.Append("); break;\n");
                    }
                    if(filterFunction.Package != null)
                    {
                        source.AppendFrontFormat("case \"{0}::{1}\": ", filterFunction.Package, filterFunction.Name);
                        source.AppendFormat("GRGEN_ACTIONS.{0}MatchClassFilters.Filter_{1}((GRGEN_LGSP.LGSPGraphProcessingEnvironment)actionEnv, ({2})matches",
                            TypesHelper.GetPackagePrefixDot(filterFunction.Package), filterFunction.Name, matchesType);
                        for(int i = 0; i < filterFunction.Inputs.Length; ++i)
                        {
                            source.AppendFormat(", ({0})(filter.Arguments[{1}])", TypesHelper.TypeName(filterFunction.Inputs[i]), i);
                        }
                        source.Append("); break;\n");
                    }
                }
            }
            source.AppendFront("default: throw new Exception(\"Unknown filter name \" + filter.FullName+ \"!\");\n");
            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");

            source.Unindent();
            source.AppendFront("}\n");

            if(matchClass.Package != null)
            {
                source.Unindent();
                source.AppendFront("}\n");
            }
        }
    }
}
