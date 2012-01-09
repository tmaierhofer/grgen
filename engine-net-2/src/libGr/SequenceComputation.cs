/*
 * GrGen: graph rewrite generator tool -- release GrGen.NET 3.0
 * Copyright (C) 2003-2011 Universitaet Karlsruhe, Institut fuer Programmstrukturen und Datenorganisation, LS Goos
 * licensed under LGPL v3 (see LICENSE.txt included in the packaging of this file)
 * www.grgen.net
 */

using System;
using System.Collections.Generic;
using System.Collections;
using System.Text;
using System.IO;

namespace de.unika.ipd.grGen.libGr
{
    /// <summary>
    /// Specifies the actual subtype of a sequence computation.
    /// A new expression type -> you must add the corresponding class down below 
    /// and adapt the lgspSequenceGenerator.
    /// </summary>
    public enum SequenceComputationType
    {
        Then,
        VFree, VReset,
        ContainerAdd, ContainerRem, ContainerClear,
        Assignment,
        VariableDeclaration,
        Emit, Record,
        AssignmentTarget, // every assignment target (lhs value) is a computation
        Expression // every expression (rhs value) is a computation
    }

    /// <summary>
    /// A sequence computation object with references to child sequence computations.
    /// The computations are basically: visited flags management, container manipulation,
    /// assignments and special functions; they may or may not return values.
    /// They do change things, in contrast to the side-effect free sequence expressions.
    /// </summary>
    public abstract class SequenceComputation : SequenceBase
    {
        /// <summary>
        /// The type of the sequence computation (e.g. Assignment or MethodCall)
        /// </summary>
        public SequenceComputationType SequenceComputationType;

        /// <summary>
        /// Initializes a new SequenceComputation object with the given sequence computation type.
        /// </summary>
        /// <param name="seqCompType">The sequence computation type.</param>
        public SequenceComputation(SequenceComputationType seqCompType)
        {
            SequenceComputationType = seqCompType;

            id = idSource;
            ++idSource;
        }

        /// <summary>
        /// Checks the sequence computation for errors utilizing the given checking environment
        /// reports them by exception
        /// default behavior: check all the children 
        /// </summary>
        public override void Check(SequenceCheckingEnvironment env)
        {
            foreach(SequenceComputation childSeq in Children)
                childSeq.Check(env);
        }

        /// <summary>
        /// Returns the type of the sequence
        /// default behaviour: returns "void"
        /// </summary>
        public override string Type(SequenceCheckingEnvironment env)
        {
            return "void";
        }

        /// <summary>
        /// Copies the sequence computation deeply so that
        /// - the global Variables are kept
        /// - the local Variables are replaced by copies initialized to null
        /// Used for cloning defined sequences before executing them if needed.
        /// Needed if the defined sequence is currently executed to prevent state corruption.
        /// </summary>
        /// <param name="originalToCopy">A map used to ensure that every instance of a variable is mapped to the same copy</param>
        /// <returns>The copy of the sequence computation</returns>
        internal abstract SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy);

        /// <summary>
        /// Executes this sequence computation.
        /// </summary>
        /// <param name="procEnv">The graph processing environment on which this sequence computation is to be evaluated.
        ///     Contains especially the graph on which this sequence computation is to be evaluated.</param>
        /// <param name="env">The execution environment giving access to the names and user interface (null if not available)</param>
        /// <returns>The value resulting from computing this sequence computation, 
        ///          null if there is no result value</returns>
        public abstract object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env);

        /// <summary>
        /// Collects all variables of the sequence expression tree into the variables dictionary.
        /// </summary>
        /// <param name="variables">Contains the variables found</param>
        public virtual void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
        }

        /// <summary>
        /// Enumerates all child sequence computation objects
        /// </summary>
        public abstract IEnumerable<SequenceComputation> Children { get; }

        /// <summary>
        /// Tells whether Execute returns a value to be used as a result determining value for a boolean computation sequence.
        /// Only expressions do so, the values returned by plain computations don't bubble up to sequence level, are computation internal only.
        /// </summary>
        public virtual bool ReturnsValue { get { return false; } }
    }

    /// <summary>
    /// A sequence computation on a container object (resulting from a variable or a method call; yielding a container object again)
    /// </summary>
    public abstract class SequenceComputationContainer : SequenceComputation
    {
        public SequenceVariable Container;
        public SequenceComputation MethodCall;

        public SequenceComputationContainer(SequenceComputationType type, SequenceVariable container, SequenceComputation methodCall)
            : base(type)
        {
            Container = container;
            MethodCall = methodCall;
        }

        public string ContainerType(SequenceCheckingEnvironment env)
        {
            if(Container != null) return Container.Type;
            else return MethodCall.Type(env);
        }

        public object ContainerValue(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            if(Container != null) return Container.GetVariableValue(procEnv);
            else return MethodCall.Execute(procEnv, env);
        }

        public override string Type(SequenceCheckingEnvironment env)
        {
            if(Container != null)
                return Container.Type;
            else
                return MethodCall.Type(env);
        }

        public override int Precedence { get { return 8; } }
        public string Name { get { if(Container != null) return Container.Name; else return MethodCall.Symbol; } }
    }


    public class SequenceComputationThen : SequenceComputation
    {
        public SequenceComputation left;
        public SequenceComputation right;

        public SequenceComputationThen(SequenceComputation left, SequenceComputation right)
            : base(SequenceComputationType.Then)
        {
            this.left = left;
            this.right = right;
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationThen copy = (SequenceComputationThen)MemberwiseClone();
            copy.left = left.Copy(originalToCopy);
            copy.right = right.Copy(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            left.Execute(procEnv, env);
            return right.Execute(procEnv, env);
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            left.GetLocalVariables(variables);
            right.GetLocalVariables(variables);
        }

        public override bool ReturnsValue { get { return right.ReturnsValue; } }

        public override IEnumerable<SequenceComputation> Children { get { yield return left; yield return right; } }
        public override int Precedence { get { return 7; } }
        public override string Symbol { get { return left.Symbol + "; " + right.Symbol; } }
    }


    public class SequenceComputationVFree : SequenceComputation
    {
        public SequenceExpression VisitedFlagExpression;

        public SequenceComputationVFree(SequenceExpression visitedFlagExpr)
            : base(SequenceComputationType.VFree)
        {
            VisitedFlagExpression = visitedFlagExpr;
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            VisitedFlagExpression.Check(env);

            if(!TypesHelper.IsSameOrSubtype(VisitedFlagExpression.Type(env), "int", env.Model))
            {
                throw new SequenceParserException(Symbol, "int", VisitedFlagExpression.Type(env));
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationVFree copy = (SequenceComputationVFree)MemberwiseClone();
            copy.VisitedFlagExpression = VisitedFlagExpression.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            int visitedFlag = (int)VisitedFlagExpression.Evaluate(procEnv, env);
            procEnv.Graph.FreeVisitedFlag(visitedFlag);
            return null;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            VisitedFlagExpression.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { yield break; } }
        public override int Precedence { get { return 8; } }
        public override string Symbol { get { return "vfree(" + VisitedFlagExpression.Symbol + ")"; } }
    }

    public class SequenceComputationVReset : SequenceComputation
    {
        public SequenceExpression VisitedFlagExpression;

        public SequenceComputationVReset(SequenceExpression visitedFlagExpr)
            : base(SequenceComputationType.VReset)
        {
            VisitedFlagExpression = visitedFlagExpr;
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            VisitedFlagExpression.Check(env);

            if(!TypesHelper.IsSameOrSubtype(VisitedFlagExpression.Type(env), "int", env.Model))
            {
                throw new SequenceParserException(Symbol, "int", VisitedFlagExpression.Type(env));
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationVReset copy = (SequenceComputationVReset)MemberwiseClone();
            copy.VisitedFlagExpression = VisitedFlagExpression.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            int visitedFlag = (int)VisitedFlagExpression.Evaluate(procEnv, env);
            procEnv.Graph.ResetVisitedFlag(visitedFlag);
            return null;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            VisitedFlagExpression.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { yield break; } }
        public override int Precedence { get { return 8; } }
        public override string Symbol { get { return "vreset(" + VisitedFlagExpression.Symbol + ")"; } }
    }

    public class SequenceComputationContainerAdd : SequenceComputationContainer
    {
        public SequenceExpression Expr;
        public SequenceExpression ExprDst;

        public SequenceComputationContainerAdd(SequenceVariable container, SequenceExpression expr, SequenceExpression exprDst)
            : base(SequenceComputationType.ContainerAdd, container, null)
        {
            Expr = expr;
            ExprDst = exprDst;
        }

        public SequenceComputationContainerAdd(SequenceComputation methodCall, SequenceExpression expr, SequenceExpression exprDst)
            : base(SequenceComputationType.ContainerAdd, null, methodCall)
        {
            Expr = expr;
            ExprDst = exprDst;
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            Expr.Check(env);
            if(ExprDst != null)
                ExprDst.Check(env);

            if(ContainerType(env) == "")
                return; // we can't check further types if the container is untyped, only runtime-check possible

            if(!ContainerType(env).StartsWith("set<") && !ContainerType(env).StartsWith("map<") && !ContainerType(env).StartsWith("array<"))
            {
                throw new SequenceParserException(Symbol, ExprDst == null ? "set or array type" : "map or array type", ContainerType(env));
            }
            if(ExprDst != null && TypesHelper.ExtractDst(ContainerType(env)) == "SetValueType")
            {
                throw new SequenceParserException(Symbol, "map type or array", ContainerType(env));
            }
            if(ContainerType(env).StartsWith("array<"))
            {
                if(!TypesHelper.IsSameOrSubtype(Expr.Type(env), TypesHelper.ExtractSrc(ContainerType(env)), env.Model))
                {
                    throw new SequenceParserException(Symbol, TypesHelper.ExtractSrc(ContainerType(env)), Expr.Type(env));
                }
                if(ExprDst != null && !TypesHelper.IsSameOrSubtype(ExprDst.Type(env), "int", env.Model))
                {
                    throw new SequenceParserException(Symbol, TypesHelper.ExtractDst(ContainerType(env)), ExprDst.Type(env));
                }
            }
            else
            {
                if(!TypesHelper.IsSameOrSubtype(Expr.Type(env), TypesHelper.ExtractSrc(ContainerType(env)), env.Model))
                {
                    throw new SequenceParserException(Symbol, TypesHelper.ExtractSrc(ContainerType(env)), Expr.Type(env));
                }
                if(TypesHelper.ExtractDst(ContainerType(env)) != "SetValueType"
                    && !TypesHelper.IsSameOrSubtype(ExprDst.Type(env), TypesHelper.ExtractDst(ContainerType(env)), env.Model))
                {
                    throw new SequenceParserException(Symbol, TypesHelper.ExtractDst(ContainerType(env)), ExprDst.Type(env));
                }
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationContainerAdd copy = (SequenceComputationContainerAdd)MemberwiseClone();
            if(Container!=null) copy.Container = Container.Copy(originalToCopy);
            if(MethodCall!=null) copy.MethodCall = MethodCall.Copy(originalToCopy);
            copy.Expr = Expr.CopyExpression(originalToCopy);
            if(ExprDst != null) copy.ExprDst = ExprDst.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object container = ContainerValue(procEnv, env);
            if(container is IList)
            {
                IList array = (IList)container;
                if(ExprDst == null)
                    array.Add(Expr.Evaluate(procEnv, env));
                else
                    array.Insert((int)ExprDst.Evaluate(procEnv, env), Expr.Evaluate(procEnv, env));
                return array;
            }
            else
            {
                IDictionary setmap = (IDictionary)container;
                if(setmap.Contains(Expr.Evaluate(procEnv, env)))
                    setmap[Expr.Evaluate(procEnv, env)] = (ExprDst == null ? null : ExprDst.Evaluate(procEnv, env));
                else
                    setmap.Add(Expr.Evaluate(procEnv, env), (ExprDst == null ? null : ExprDst.Evaluate(procEnv, env)));
                return setmap;
            }
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            if(Container != null) Container.GetLocalVariables(variables);
            if(MethodCall != null) MethodCall.GetLocalVariables(variables);
            Expr.GetLocalVariables(variables);
            if(ExprDst != null) ExprDst.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { if(MethodCall == null) yield break; else yield return MethodCall; } }
        public override string Symbol { get { return Name + ".add(" + Expr.Symbol + (ExprDst != null ? "," + ExprDst.Symbol : "") + ")"; } }
    }

    public class SequenceComputationContainerRem : SequenceComputationContainer
    {
        public SequenceExpression Expr;

        public SequenceComputationContainerRem(SequenceVariable container, SequenceExpression expr)
            : base(SequenceComputationType.ContainerRem, container, null)
        {
            Expr = expr;
        }

        public SequenceComputationContainerRem(SequenceComputation methodCall, SequenceExpression expr)
            : base(SequenceComputationType.ContainerRem, null, methodCall)
        {
            Expr = expr;
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            if(Expr != null)
                Expr.Check(env);

            if(ContainerType(env) == "")
                return; // we can't check further types if the variable is untyped, only runtime-check possible

            if(!ContainerType(env).StartsWith("set<") && !ContainerType(env).StartsWith("map<") && !ContainerType(env).StartsWith("array<"))
            {
                throw new SequenceParserException(Symbol, "set or map or array type", ContainerType(env));
            }
            if(ContainerType(env).StartsWith("array<"))
            {
                if(Expr != null && !TypesHelper.IsSameOrSubtype(Expr.Type(env), "int", env.Model))
                {
                    throw new SequenceParserException(Symbol, "int", Expr.Type(env));
                }
            }
            else
            {
                if(!TypesHelper.IsSameOrSubtype(Expr.Type(env), TypesHelper.ExtractSrc(ContainerType(env)), env.Model))
                {
                    throw new SequenceParserException(Symbol, TypesHelper.ExtractSrc(ContainerType(env)), Expr.Type(env));
                }
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationContainerRem copy = (SequenceComputationContainerRem)MemberwiseClone();
            if(Container != null) copy.Container = Container.Copy(originalToCopy);
            if(MethodCall != null) copy.MethodCall = MethodCall.Copy(originalToCopy);
            if(Expr != null) copy.Expr = Expr.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object container = ContainerValue(procEnv, env);
            if(container is IList)
            {
                IList array = (IList)container;
                if(Expr == null)
                    array.RemoveAt(array.Count - 1);
                else
                    array.RemoveAt((int)Expr.Evaluate(procEnv, env));
                return array;
            }
            else
            {
                IDictionary setmap = (IDictionary)container;
                setmap.Remove(Expr.Evaluate(procEnv, env));
                return setmap;
            }
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            if(Container != null) Container.GetLocalVariables(variables);
            if(MethodCall != null) MethodCall.GetLocalVariables(variables);
            if(Expr != null) Expr.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { if(MethodCall == null) yield break; else yield return MethodCall; } }
        public override string Symbol { get { return Name + ".rem(" + (Expr != null ? Expr.Symbol : "") + ")"; } }
    }

    public class SequenceComputationContainerClear : SequenceComputationContainer
    {
        public SequenceComputationContainerClear(SequenceVariable container)
            : base(SequenceComputationType.ContainerClear, container, null)
        {
        }

        public SequenceComputationContainerClear(SequenceComputation methodCall)
            : base(SequenceComputationType.ContainerClear, null, methodCall)
        {
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            if(ContainerType(env) == "")
                return; // we can't check further types if the variable is untyped, only runtime-check possible

            if(!ContainerType(env).StartsWith("set<") && !ContainerType(env).StartsWith("map<") && !ContainerType(env).StartsWith("array<"))
            {
                throw new SequenceParserException(Symbol, "set or map or array type", ContainerType(env));
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationContainerClear copy = (SequenceComputationContainerClear)MemberwiseClone();
            if(Container != null) copy.Container = Container.Copy(originalToCopy);
            if(MethodCall != null) copy.MethodCall = MethodCall.Copy(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            if(Container.GetVariableValue(procEnv) is IList)
            {
                IList array = (IList)Container.GetVariableValue(procEnv);
                array.Clear();
                return array;
            }
            else
            {
                IDictionary setmap = (IDictionary)Container.GetVariableValue(procEnv);
                setmap.Clear();
                return setmap;
            }
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            if(Container != null) Container.GetLocalVariables(variables);
            if(MethodCall != null) MethodCall.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { if(MethodCall == null) yield break; else yield return MethodCall; } }
        public override string Symbol { get { return Name + ".clear()"; } }
    }

    public class SequenceComputationAssignment : SequenceComputation
    {
        public AssignmentTarget Target;
        public SequenceComputation SourceValueProvider;

        public SequenceComputationAssignment(AssignmentTarget tgt, SequenceComputation srcValueProvider)
            : base(SequenceComputationType.Assignment)
        {
            Target = tgt;
            SourceValueProvider = srcValueProvider;
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            base.Check(env);
            SourceValueProvider.Check(env);

            // the assignment of an untyped variable to a typed variable is ok, cause we want access to persistency
            // which is only offered by the untyped variables; it is checked at runtime / causes an invalid cast exception
            if(!TypesHelper.IsSameOrSubtype(SourceValueProvider.Type(env), Target.Type(env), env.Model))
            {
                throw new SequenceParserException(Symbol, Target.Type(env), SourceValueProvider.Type(env));
            }
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationAssignment copy = (SequenceComputationAssignment)MemberwiseClone();
            copy.Target = Target.CopyTarget(originalToCopy);
            copy.SourceValueProvider = SourceValueProvider.Copy(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object value = SourceValueProvider.Execute(procEnv, env);
            Target.Assign(value, procEnv, env);
            return value;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            Target.GetLocalVariables(variables);
            SourceValueProvider.GetLocalVariables(variables);
        }

        public override string Symbol { get { return Target.Symbol + "=" + SourceValueProvider.Symbol; } }
        public override IEnumerable<SequenceComputation> Children { get { yield return Target; yield return SourceValueProvider; } }
        public override int Precedence { get { return 8; } } // always a top prio assignment factor
    }

    public class SequenceComputationVariableDeclaration : SequenceComputation
    {
        public SequenceVariable Target;

        public SequenceComputationVariableDeclaration(SequenceVariable tgt)
            : base(SequenceComputationType.VariableDeclaration)
        {
            Target = tgt;
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationVariableDeclaration copy = (SequenceComputationVariableDeclaration)MemberwiseClone();
            copy.Target = Target.Copy(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object value = TypesHelper.DefaultValue(Target.Type, procEnv.Graph.Model);
            Target.SetVariableValue(value, procEnv);
            return value;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            Target.GetLocalVariables(variables);
        }

        public override string Symbol { get { return Target.Name; } }
        public override IEnumerable<SequenceComputation> Children { get { yield break; } }
        public override int Precedence { get { return 8; } } // always a top prio assignment factor
    }

    public class SequenceComputationEmit : SequenceComputation
    {
        public SequenceExpression Expression;

        public SequenceComputationEmit(SequenceExpression expr)
            : base(SequenceComputationType.Emit)
        {
            Expression = expr;
            if(Expression is SequenceExpressionConstant)
            {
                SequenceExpressionConstant constant = (SequenceExpressionConstant)Expression;
                if(constant.Constant is string)
                {
                    constant.Constant = ((string)constant.Constant).Replace("\\n", "\n");
                    constant.Constant = ((string)constant.Constant).Replace("\\r", "\r");
                    constant.Constant = ((string)constant.Constant).Replace("\\t", "\t");
                    constant.Constant = ((string)constant.Constant).Replace("\\#", "#");
                }
            }
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            Expression.Check(env);
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationEmit copy = (SequenceComputationEmit)MemberwiseClone();
            copy.Expression = Expression.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object value = Expression.Evaluate(procEnv, env);
            if(value != null)
            {
                if(value is IDictionary)
                    procEnv.EmitWriter.Write(DictionaryListHelper.ToString((IDictionary)value, procEnv.Graph));
                else if(value is IList)
                    procEnv.EmitWriter.Write(DictionaryListHelper.ToString((IList)value, procEnv.Graph));
                else
                    procEnv.EmitWriter.Write(DictionaryListHelper.ToString(value, procEnv.Graph));
            }
            return value;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            Expression.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { yield return Expression; } }
        public override int Precedence { get { return 8; } }
        public override string Symbol { get { return "emit(" + Expression.Symbol + ")"; } }
    }

    public class SequenceComputationRecord : SequenceComputation
    {
        public SequenceExpression Expression;

        public SequenceComputationRecord(SequenceExpression expr)
            : base(SequenceComputationType.Record)
        {
            Expression = expr;
            if(Expression is SequenceExpressionConstant)
            {
                SequenceExpressionConstant constant = (SequenceExpressionConstant)Expression;
                if(constant.Constant is string)
                {
                    constant.Constant = ((string)constant.Constant).Replace("\\n", "\n");
                    constant.Constant = ((string)constant.Constant).Replace("\\r", "\r");
                    constant.Constant = ((string)constant.Constant).Replace("\\t", "\t");
                    constant.Constant = ((string)constant.Constant).Replace("\\#", "#");
                }
            }
        }

        public override void Check(SequenceCheckingEnvironment env)
        {
            Expression.Check(env);
        }

        internal override SequenceComputation Copy(Dictionary<SequenceVariable, SequenceVariable> originalToCopy)
        {
            SequenceComputationRecord copy = (SequenceComputationRecord)MemberwiseClone();
            copy.Expression = Expression.CopyExpression(originalToCopy);
            return copy;
        }

        public override object Execute(IGraphProcessingEnvironment procEnv, SequenceExecutionEnvironment env)
        {
            object value = Expression.Evaluate(procEnv, env);
            if(value != null)
            {
                if(value is IDictionary)
                    procEnv.Recorder.Write(DictionaryListHelper.ToString((IDictionary)value, procEnv.Graph));
                else if(value is IList)
                    procEnv.Recorder.Write(DictionaryListHelper.ToString((IList)value, procEnv.Graph));
                else
                    procEnv.Recorder.Write(DictionaryListHelper.ToString(value, procEnv.Graph));
            }
            return value;
        }

        public override void GetLocalVariables(Dictionary<SequenceVariable, SetValueType> variables)
        {
            Expression.GetLocalVariables(variables);
        }

        public override IEnumerable<SequenceComputation> Children { get { yield return Expression; } }
        public override int Precedence { get { return 8; } }
        public override string Symbol { get { return "record(" + Expression.Symbol + ")"; } }
    }
}
