/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 4.5
 * Copyright (C) 2003-2020 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos; and free programmers
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

// by Edgar Jakumeit

using System;
using System.Text;
using System.Collections.Generic;

namespace de.unika.ipd.grGen.libGr
{
    /// <summary>
    /// An object representing a filter call (of an action filter or a match class filter).
    /// It specifies the filter and potential arguments.
    /// </summary>
    public class FilterCall
    {
        /// <summary>
        /// The name of the filter.
        /// </summary>
        public readonly String Name;

        /// <summary>
        /// null if this is a call of a global filter, otherwise the (resolved) package the call target is contained in.
        /// </summary>
        public String Package;

        /// <summary>
        /// The name of the filter, prefixed by the (resolved) package it is contained in (separated by a double colon), if it is contained in a package.
        /// </summary>
        public String PackagePrefixedName;

        /////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary>
        /// null if this is a single-rule filter, otherwise the match class of the filter.
        /// </summary>
        public readonly String MatchClassName;

        /// <summary>
        /// null if the match class is global, otherwise the (resolved) package the match class is contained in.
        /// </summary>
        public String MatchClassPackage;

        /// <summary>
        /// The name of the match class, prefixed by the (resolved) package it is contained in (separated by a double colon), if it is contained in a package.
        /// </summary>
        public String MatchClassPackagePrefixedName;

        /////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary>
        /// The package this invocation is contained in (the calling source, not the filter call target (or match class target)).
        /// Needed to resolve names from the local package accessed without package prefix.
        /// </summary>
        public readonly String PrePackageContext;

        /// <summary>
        /// null if this is a call of a global filter, otherwise the package the call target is contained in.
        /// May be even null for a call of a package target, if done from a context where the package is set.
        /// </summary>
        public readonly String PrePackage;

        /// <summary>
        /// null if this is a global match class, otherwise the package the match class is contained in.
        /// May be even null for a match class from a package, if done from a context where the package is set.
        /// </summary>
        public readonly String MatchClassPrePackage;

        /////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary>
        /// The entities the filter is based on, in case of a (def-variable based) auto-generated filter (empty for auto), otherwise null.
        /// </summary>
        public readonly String[] Entities;

        /// <summary>
        /// True in case this is the call of an auto-supplied filter.
        /// In this case, there must be exactly one Argument or ArgumentExpression given.
        /// </summary>
        public readonly bool IsAutoSupplied;

        /// <summary>
        /// An array of expressions used to compute the input arguments for a filter function (or auto-supplied filter).
        /// It must have the same length as Arguments.
        /// If an entry is null, the according entry in Arguments is used unchanged.
        /// Otherwise the entry in Arguments is filled with the evaluation result of the expression.
        /// The sequence parser generates argument expressions for every entry;
        /// they may be omitted by a user assembling an invocation at API level.
        /// </summary>
        public readonly SequenceExpression[] ArgumentExpressions;

        /// <summary>
        /// Buffer to store the argument values for the filter function call (or auto-supplied filter call);
        /// used by libGr to avoid unneccessary memory allocations.
        /// </summary>
        public readonly object[] Arguments;


        /// <summary>
        /// Instantiates a new FilterCall object for a filter function
        /// </summary>
        public FilterCall(String prePackage, String name, String matchClassPrePackage, String matchClassName, List<SequenceExpression> argumentExpressions, String packageContext)
        {
            PrePackage = prePackage;
            Name = name;
            MatchClassPrePackage = matchClassPrePackage;
            MatchClassName = matchClassName;
            ArgumentExpressions = new SequenceExpression[argumentExpressions.Count];
            Arguments = new object[argumentExpressions.Count];
            for(int i = 0; i < argumentExpressions.Count; ++i)
            {
                ArgumentExpressions[i] = argumentExpressions[i];
                Arguments[i] = null;
            }
            PrePackageContext = packageContext;
        }

        /// <summary>
        /// Instantiates a new FilterCall object for an auto-generated filter
        /// </summary>
        public FilterCall(String prePackage, String name, String matchClassPrePackage, String matchClassName, String[] entities, String packageContext, bool dummy)
        {
            PrePackage = prePackage;
            Name = name;
            MatchClassPrePackage = matchClassPrePackage;
            MatchClassName = matchClassName;
            Entities = entities;
            ArgumentExpressions = new SequenceExpression[0];
            Arguments = new object[0];
            PrePackageContext = packageContext;
        }

        /// <summary>
        /// Instantiates a new FilterCall object for an auto-supplied filter (with sequence expression parameter)
        /// </summary>
        public FilterCall(String prePackage, String name, String matchClassPrePackage, String matchClassName, SequenceExpression argument, String packageContext)
        {
            PrePackage = prePackage;
            Name = name;
            MatchClassPrePackage = matchClassPrePackage;
            MatchClassName = matchClassName;
            IsAutoSupplied = true;
            ArgumentExpressions = new SequenceExpression[1];
            ArgumentExpressions[0] = argument;
            Arguments = new object[1];
            Arguments[0] = null;
            PrePackageContext = packageContext;
        }

        /// <summary>
        /// Copy constructor for filter call object
        /// </summary>
        public FilterCall(FilterCall that)
        {
            Name = that.Name;
            Package = that.Package;
            PackagePrefixedName = that.PackagePrefixedName;
            MatchClassName = that.MatchClassName;
            MatchClassPackage = that.MatchClassPackage;
            MatchClassPackagePrefixedName = that.MatchClassPackagePrefixedName;
            PrePackageContext = that.PrePackageContext;
            PrePackage = that.PrePackage;
            MatchClassPrePackage = that.MatchClassPrePackage;
            Entities = that.Entities;
            IsAutoSupplied = that.IsAutoSupplied;
            ArgumentExpressions = that.ArgumentExpressions;
            Arguments = that.Arguments;
        }

        public bool IsAutoGenerated
        {
            get
            {
                if(Entities != null)
                    return true;
                if(Name == "auto")
                    return true;
                return false;
            }
        }

        public string FullName
        {
            get
            {
                if(Entities != null)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append(Name);
                    sb.Append(EntitySuffix);
                    return sb.ToString();
                }
                else
                    return Name;
            }
        }

        public string PackagePrefixedFullName
        {
            get
            {
                if(Entities != null)
                {
                    StringBuilder sb = new StringBuilder();
                    if(Package != null)
                    {
                        sb.Append(Package);
                        sb.Append("::");
                    }
                    sb.Append(Name);
                    sb.Append(EntitySuffix);
                    return sb.ToString();
                }
                else
                {
                    return PackagePrefixedName;
                }
            }
        }

        private String EntitySuffix
        {
            get
            {
                StringBuilder sb = new StringBuilder();
                sb.Append("<");
                sb.Append(String.Join(",", Entities));
                sb.Append(">");
                return sb.ToString();
            }
        }

        public String EntitySuffixForName
        {
            get
            {
                return String.Join("_", Entities);
            }
        }

        public bool IsContainedIn(List<IFilter> filters)
        {
            return IsContainedIn(filters.ToArray());
        }

        public bool IsContainedIn(IFilter[] filters)
        {
            for(int i = 0; i < filters.Length; ++i)
            {
                if(filters[i] is IFilterAutoGenerated)
                {
                    IFilterAutoGenerated filter = (IFilterAutoGenerated)filters[i];
                    if(filter.PackagePrefixedName == PackagePrefixedName)
                    {
                        if(Name == "auto")
                            return true;
                        else
                        {
                            if(NameSuffixMatches(filter.Entities, Entities))
                                return true;
                        }
                    }
                }
                else //if(filters[i] is IFilterFunction)
                {
                    IFilterFunction filter = (IFilterFunction)filters[i];
                    if(filter.PackagePrefixedName == PackagePrefixedName)
                        return true;
                }
            }
            return false;
        }

        public bool IsContainedInPureNameOnly(List<IFilter> filters)
        {
            return IsContainedInPureNameOnly(filters.ToArray());
        }

        public bool IsContainedInPureNameOnly(IFilter[] filters)
        {
            for(int i = 0; i < filters.Length; ++i)
            {
                if(filters[i] is IFilterAutoGenerated)
                {
                    IFilterAutoGenerated filter = (IFilterAutoGenerated)filters[i];
                    if(filter.PackagePrefixedName == Name)
                    {
                        if(Name == "auto")
                            return true;
                        else
                        {
                            if(NameSuffixMatches(filter.Entities, Entities))
                                return true;
                        }
                    }
                }
                else //if(filters[i] is IFilterFunction)
                {
                    IFilterFunction filter = (IFilterFunction)filters[i];
                    if(filter.PackagePrefixedName == Name)
                        return true;
                }
            }
            return false;
        }

        public bool NameSuffixMatches(List<String> filterEntities, String[] entities)
        {
            if(filterEntities.Count != Entities.Length)
                return false;
            for(int i = 0; i < filterEntities.Count; ++i)
            {
                if(filterEntities[i] != entities[i])
                    return false;
            }
            return true;
        }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            if(MatchClassName != null)
            {
                if(MatchClassPackagePrefixedName != null) // only set after resolving
                    sb.Append(MatchClassPackagePrefixedName + ".");
                else
                    sb.Append(MatchClassName + ".");
            }
            if(PackagePrefixedName != null) // only set after resolving
                sb.Append(PackagePrefixedName);
            else
                sb.Append(Name);
            if(Entities != null)
                sb.Append(EntitySuffix);
            if(Arguments != null)
            {
                sb.Append("(");
                for(int i=0; i<ArgumentExpressions.Length; ++i)
                {
                    if(ArgumentExpressions[i] != null)
                    {
                        sb.Append(ArgumentExpressions[i].Symbol);
                    }
                    else
                    {
                        if(Arguments[i] is Double)
                            sb.Append(((double)Arguments[i]).ToString(System.Globalization.CultureInfo.InvariantCulture));
                        else
                            sb.Append(Arguments[i].ToString());
                    }
                }
                sb.Append(")");
            }
            return sb.ToString();
        }
    }
}
