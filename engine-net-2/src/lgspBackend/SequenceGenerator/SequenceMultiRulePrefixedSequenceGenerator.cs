/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 5.0
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Edgar Jakumeit

using System;
using de.unika.ipd.grGen.libGr;

namespace de.unika.ipd.grGen.lgsp
{
    class SequenceMultiRulePrefixedSequenceGenerator
    {
        internal readonly SequenceMultiRulePrefixedSequence seqMulti;
        internal readonly SequenceRulePrefixedSequence seqRulePrefixedSequence;
        internal readonly SequenceRuleCall seqRule;
        internal readonly SequenceExpressionGenerator seqExprGen;
        internal readonly SequenceGeneratorHelper seqHelper;

        internal readonly SequenceExpression[] ArgumentExpressions;
        internal readonly String specialStr;
        internal readonly String matchingPatternClassName;
        internal readonly String patternName;
        internal readonly String plainRuleName;
        internal readonly String ruleName;
        internal readonly String matchType;
        internal readonly String matchName;
        internal readonly String matchesType;
        internal readonly String matchesName;

        internal readonly String returnParameterDeclarations;
        internal readonly String returnArguments;
        internal readonly String returnAssignments;
        internal readonly String returnParameterDeclarationsAllCall;
        internal readonly String intermediateReturnAssignmentsAllCall;
        internal readonly String returnAssignmentsAllCall;


        public SequenceMultiRulePrefixedSequenceGenerator(SequenceMultiRulePrefixedSequence seqMulti, SequenceRulePrefixedSequence seqRulePrefixedSequence, SequenceExpressionGenerator seqExprGen, SequenceGeneratorHelper seqHelper)
        {
            this.seqMulti = seqMulti; // parent
            this.seqRulePrefixedSequence = seqRulePrefixedSequence;
            this.seqExprGen = seqExprGen;
            this.seqHelper = seqHelper;

            seqRule = seqRulePrefixedSequence.Rule;

            ArgumentExpressions = seqRule.ArgumentExpressions;
            specialStr = seqRule.Special ? "true" : "false";
            matchingPatternClassName = "GRGEN_ACTIONS." + TypesHelper.GetPackagePrefixDot(seqRule.Package) + "Rule_" + seqRule.Name;
            patternName = seqRule.Name;
            plainRuleName = TypesHelper.PackagePrefixedNameDoubleColon(seqRule.Package, seqRule.Name);
            ruleName = "rule_" + TypesHelper.PackagePrefixedNameUnderscore(seqRule.Package, seqRule.Name);
            matchType = matchingPatternClassName + "." + NamesOfEntities.MatchInterfaceName(patternName);
            matchName = "match_" + seqRule.Id;
            matchesType = "GRGEN_LIBGR.IMatchesExact<" + matchType + ">";
            matchesName = "matches_" + seqRule.Id;

            seqHelper.BuildReturnParameters(seqRule, seqRule.ReturnVars,
                out returnParameterDeclarations, out returnArguments, out returnAssignments,
                out returnParameterDeclarationsAllCall, out intermediateReturnAssignmentsAllCall, out returnAssignmentsAllCall);
        }

        public void EmitMatching(SourceBuilder source, SequenceGenerator seqGen, String matchListName)
        {
            String parameters = seqHelper.BuildParameters(seqRule, ArgumentExpressions, source);
            source.AppendFront(matchesType + " " + matchesName + " = " + ruleName
                + ".Match(procEnv, procEnv.MaxMatches"
                + parameters + ");\n");
            source.AppendFront("procEnv.PerformanceInfo.MatchesFound += " + matchesName + ".Count;\n");
            for(int i = 0; i < seqRule.Filters.Count; ++i)
            {
                seqExprGen.EmitFilterCall(source, (SequenceFilterCallCompiled)seqRule.Filters[i], patternName, matchesName, seqRule.PackagePrefixedName, false);
            }

            source.AppendFront("if(" + matchesName + ".Count != 0) {\n");
            source.Indent();
            source.AppendFront(matchesName + " = (" + matchesType + ")" + matchesName + ".Clone();\n");
            source.AppendFrontFormat("{0}.AddRange({1});\n", matchListName, matchesName);
            source.Unindent();
            source.AppendFront("}\n");
        }

        public void EmitRewriting(SourceBuilder source, SequenceGenerator seqGen, String matchListName, String enumeratorName,
            String firstRewrite, bool fireDebugEvents)
        {
            source.AppendFrontFormat("case \"{0}\":\n", plainRuleName);
            source.AppendFront("{\n");
            source.Indent();

            source.AppendFront(matchType + " " + matchName + " = (" + matchType + ")" + enumeratorName + ".Current;\n");
            if(fireDebugEvents)
                source.AppendFront("procEnv.Matched(" + matchesName + ", null, " + specialStr + ");\n");
            if(fireDebugEvents)
                source.AppendFront("procEnv.Finishing(" + matchesName + ", " + specialStr + ");\n");
            source.AppendFront("if(!" + firstRewrite + ") procEnv.RewritingNextMatch();\n");
            if(returnParameterDeclarations.Length != 0)
                source.AppendFront(returnParameterDeclarations + "\n");
            source.AppendFront(ruleName + ".Modify(procEnv, " + matchName + returnArguments + ");\n");
            if(returnAssignments.Length != 0)
                source.AppendFront(returnAssignments + "\n");
            source.AppendFront("++procEnv.PerformanceInfo.RewritesPerformed;\n");
            source.AppendFront(firstRewrite + " = false;\n");
            if(fireDebugEvents)
                source.AppendFront("procEnv.Finished(" + matchesName + ", " + specialStr + ");\n");

            seqGen.EmitSequence(seqRulePrefixedSequence.Sequence, source);

            source.AppendFront("break;\n");

            source.Unindent();
            source.AppendFront("}\n");
        }
    }
}
