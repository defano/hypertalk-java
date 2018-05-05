package com.defano.wyldcard.runtime.interpreter;

import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.model.NamedBlock;
import com.defano.hypertalk.ast.model.Script;
import com.defano.hypertalk.ast.model.UserFunction;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.ast.statements.ExpressionStatement;
import com.defano.hypertalk.ast.statements.Statement;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.debug.message.HandlerInvocation;
import com.defano.wyldcard.debug.message.HandlerInvocationBridge;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.util.ThreadUtils;
import com.google.common.util.concurrent.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A facade and thread model for executing HyperTalk scripts. All script compilation and execution should flow through
 * this class to assure proper threading.
 */
public class Interpreter {

    private final static int MAX_COMPILE_THREADS = 6;          // Simultaneous background parse tasks
    private final static int MAX_EXECUTOR_THREADS = 8;         // Simultaneous scripts executing

    private static final ThreadPoolExecutor bestEffortCompileExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("beasync-compiler-%d").build());
    private static final ThreadPoolExecutor asyncCompileExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_COMPILE_THREADS, new ThreadFactoryBuilder().setNameFormat("async-compiler-%d").build());
    private static final ThreadPoolExecutor scriptExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS, new ThreadFactoryBuilder().setNameFormat("script-executor-%d").build());
    private static final ListeningExecutorService listeningScriptExecutor = MoreExecutors.listeningDecorator(scriptExecutor);

    /**
     * Preemptively compiles the given script text on a background thread and invokes the CompileCompletionObserver
     * (on the background thread) when complete.
     * <p>
     * Note that this method cancels any previously requested compilation except the currently executing one (if one is
     * executing). Thus, invocation of the completion observer is not guaranteed; some jobs will be canceled
     * before they run and thus never complete.
     * <p>
     * This method is primarily useful for parse-as-you-type syntax checking.
     *
     * @param scriptText The script to parse.
     * @param observer   A non-null callback to fire when compilation is complete.
     */
    public static void asyncBestEffortCompile(CompilationUnit compilationUnit, String scriptText, CompileCompletionObserver observer) {

        // Preempt any previously enqueued parse jobs
        bestEffortCompileExecutor.getQueue().clear();
        bestEffortCompileExecutor.submit(getCompileTask(compilationUnit, scriptText, observer));
    }

    public static void asyncCompile(CompilationUnit compilationUnit, String scriptText, CompileCompletionObserver observer) {
        asyncCompileExecutor.submit(getCompileTask(compilationUnit, scriptText, observer));
    }

    private static Runnable getCompileTask(CompilationUnit compilationUnit, String scriptText, CompileCompletionObserver observer) {
        return () -> {
            HtException generatedError = null;
            Object compiledScript = null;

            try {
                compiledScript = TwoPhaseParser.parseScript(compilationUnit, scriptText);
            } catch (HtException e) {
                generatedError = e;
            }

            observer.onCompileCompleted(scriptText, compiledScript, generatedError);
        };
    }

    /**
     * Compiles the given {@link CompilationUnit#SCRIPTLET} on the current thread.
     *
     * @param scriptText The script text to parse.
     * @return The compiled Script object (the root of the abstract syntax tree)
     * @throws HtException Thrown if an error (i.e., syntax error) occurs when compiling.
     */
    public static Script blockingCompileScriptlet(String scriptText) throws HtException {
        return (Script) TwoPhaseParser.parseScript(CompilationUnit.SCRIPTLET, scriptText);
    }

    /**
     * Compiles the given {@link CompilationUnit#SCRIPT} on the current thread.
     *
     * @param scriptText The script text to parse.
     * @return The compiled Script object (the root of the abstract syntax tree)
     * @throws HtException Thrown if an error (i.e., syntax error) occurs when compiling.
     */
    public static Script blockingCompileScript(String scriptText) throws HtException {
        return (Script) TwoPhaseParser.parseScript(CompilationUnit.SCRIPT, scriptText);
    }

    /**
     * Evaluates a string as a HyperTalk expression on the current thread.
     *
     * @param expression The value of the evaluated text; based on HyperTalk language semantics, text that cannot be
     *                   evaluated or is not a legal expression evaluates to itself.
     * @param context    The execution context
     * @return The Value of the evaluated expression.
     */
    public static Value blockingEvaluate(String expression, ExecutionContext context) {
        try {
            Statement statement = blockingCompileScriptlet(expression).getStatements().list.get(0);
            if (statement instanceof ExpressionStatement) {
                return ((ExpressionStatement) statement).expression.evaluate(context);
            }
        } catch (Exception e) {
            // Nothing to do; okay to evaluate bogus text
        }

        // Value of a non-expression is itself
        return new Value(expression);
    }

    /**
     * Attempts to evaluate the given value as an AST node identified by klass on the current thread.
     * <p>
     * The given value is compiled as a HyperTalk scriptlet and the first statement in the script is coerced to the
     * requested type. Returns null if the value is not a valid HyperTalk script or contains a script fragment that
     * cannot be coerced to the requested type.
     * <p>
     * For example, if value contains the text 'card field id 1', and klass is PartExp.class then an instance of
     * PartIdExp will be returned referring to the requested card field part.
     *
     * @param value The value to dereference; may be any non-null value, but only Values containing valid HyperTalk
     *              can successfully be de-referenced.
     * @param klass The Class to coerce/dereference the value into (may return a subtype of this class).
     * @param <T>   The type of the requested class.
     * @return Null if dereference fails for any reason, otherwise an instance of the requested class representing
     * the dereferenced value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T blockingEvaluate(Value value, Class<T> klass) {
        try {
            Statement statement = Interpreter.blockingCompileScriptlet(value.stringValue()).getStatements().list.get(0);

            // Simple case; statement matches requested type
            if (statement.getClass().isAssignableFrom(klass)) {
                return (T) statement;
            } else if (Expression.class.isAssignableFrom(klass)) {
                Expression expression = ((ExpressionStatement) statement).expression;
                if (klass.isAssignableFrom(expression.getClass())) {
                    return (T) expression;
                }
            }

        } catch (Exception e) {
            // Nothing to do; value was either not valid HyperTalk or not coercible to requested type
        }

        return null;
    }

    /**
     * Executes a user-defined function on the current thread and returns the result; may not be invoked from the Swing
     * dispatch thread.
     *
     * @param context   The execution context
     * @param me        The part that the 'me' keyword refers to.
     * @param function  The compiled UserFunction
     * @param arguments The arguments to be passed to the function
     * @return The value returned by the function (an empty string if the function does not invoke 'return')
     * @throws HtSemanticException Thrown if an error occurs executing the function.
     */
    public static Value blockingExecuteFunction(ExecutionContext context, PartSpecifier me, UserFunction function, Expression arguments) throws HtException {
        ThreadUtils.assertWorkerThread();
        return new FunctionExecutionTask(context, me, function, arguments).call();
    }

    /**
     * Executes a script handler on a background thread and notifies an observer when complete.
     * <p>
     * Any handler that does not 'pass' the handler name traps its behavior and prevents other scripts (or WyldCard)
     * from acting upon it. A script that does not implement a handler for a given message is assumed to 'pass' it.
     *
     * @param context            The execution context
     * @param me                 The part whose script is being executed (for the purposes of the 'me' keyword).
     * @param script             The script of the part
     * @param message            The command handler name.
     * @param arguments          A list of expressions representing arguments passed with the message
     * @param completionObserver Invoked after the handler has executed on the same thread on which the handler ran.
     */
    public static void asyncExecuteHandler(ExecutionContext context, PartSpecifier me, Script script, String message, ListExp arguments, HandlerCompletionObserver completionObserver) {
        NamedBlock handler = script == null ? null : script.getHandler(message);
        CheckedFuture<Boolean, HtException> future;

        // Execute implemented handler in the script
        if (handler != null) {
            future = getFutureForHandlerExecutionTask(new DefaultHandlerExecutionTask(context, me, handler, arguments));
        }

        // Special case: No handler in the script for this message; produce a "no-op" execution
        else {
            future = getFutureForHandlerExecutionTask(() -> {
                // Synthesize handler invocation for message watcher
                HandlerInvocation invocation = new HandlerInvocation(Thread.currentThread().getName(), message, new ArrayList<>(), me, context.getStackDepth(), false);
                HandlerInvocationBridge.getInstance().notifyMessageHandled(invocation);

                // Unimplemented handlers don't trap message
                return false;
            });
        }

        Futures.addCallback(future, new HandlerExecutionFutureCallback(me, script, message, completionObserver));
    }

    /**
     * Evaluates text using a specified context on a background thread and notifies an observer of the result when
     * complete. This is primarily useful for message window text evaluation (also used in the "Evaluate Expression"
     * feature of the debugger).
     * <p>
     * Message evaluation is a special case of script execution:
     * <p>
     * 1. All messages entered into the message window share a single stack frame so that symbols created in one message
     * are available to the next. For example, 'put 10 into x' followed by 'put x' should result in 10. To achieve this,
     * all message evaluations must occur in the same execution context and a special execution task is required to
     * prevent pushing a new stack frame during each evaluation.
     * <p>
     * 2. When evaluating from the message window, 'the target' returns the current card, not the message box (for
     * whatever reason). This results in a special case where the target is not the base of the 'me' stack.
     * <p>
     * 3. When multiple statements are evaluated using this method, the last statement is interpreted as an expression
     * and the evaluation of that expression is returned as the result. This has the otherwise unusual behavior of
     * treating a literal symbol value as the last statement as a request to evaluate the symbol as a variable. For
     * example, if the last statement evaluated is "doSomething", HyperTalk will typically interpret that as a message
     * to be sent; but in this case, it will be sent as a message, and then evalauted as a variable returning to the
     * observer whatever symbolic value "doSomething" has been assigned, or the literal "doSomething" if no value is
     * bound to it.
     *
     * @param staticContext      The execution context under which the text should be evaluated
     * @param message            The message text to evaluate.
     * @param evaluationObserver A set of observer callbacks that fire (on the Swing dispatch thread) when evaluation is
     *                           complete.
     */
    public static void asyncEvaluateMessage(ExecutionContext staticContext, String message, MessageEvaluationObserver evaluationObserver) {

        Futures.addCallback(Futures.makeChecked(listeningScriptExecutor.submit(new MessageEvaluationTask(staticContext, message)), new CheckedFutureExceptionMapper()), new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                SwingUtilities.invokeLater(() -> evaluationObserver.onMessageEvaluated(result));
            }

            @Override
            public void onFailure(Throwable t) {
                SwingUtilities.invokeLater(() -> evaluationObserver.onEvaluationError(new CheckedFutureExceptionMapper().apply((Exception) t)));
            }
        });
    }

    /**
     * Compiles a list of HyperTalk statements on the current thread, then executes the compiled script om a background
     * thread. Produces a CheckFuture providing the name of the message passed by the script, or null, if no message
     * is passed.
     *
     * @param context       The execution context
     * @param me            The part that the 'me' keyword refers to.
     * @param statementList The list of statements.
     * @return A CheckedFuture to the name passed from the script or null if no name was passed, throwing an
     * HtException if an error occurs while executing the script.
     * @throws HtException Thrown if an error occurs compiling the statements.
     */
    public static CheckedFuture<Boolean, HtException> asyncExecuteString(ExecutionContext context, PartSpecifier me, String statementList) throws HtException {
        return getFutureForHandlerExecutionTask(new DefaultHandlerExecutionTask(context, me, NamedBlock.anonymousBlock(blockingCompileScriptlet(statementList).getStatements()), new ListExp(null)));
    }

    /**
     * Gets the number of scripts that are either actively executing or waiting to be executed. Returns 0 when HyperCard
     * is "idle". Does not include any script evaluation done via the message box.
     *
     * @return The number of active or pending scripts
     */
    public static int getPendingScriptCount() {
        return scriptExecutor.getActiveCount() + scriptExecutor.getQueue().size();
    }

    /**
     * Determines if the current thread is a known script execution thread. That is, one of the eight threads used for
     * normal script execution.
     *
     * @return True if the current thread is a script-executor thread.
     */
    private static boolean isScriptExecutorThread() {
        return Thread.currentThread().getName().startsWith("script-executor-");
    }

    /**
     * Gets a CheckedFuture representing the future result of executing a script handler (a boolean value indicating
     * whether the handler trapped the message, or, an {@link HtException} if the script failed to complete executing).
     *
     * If this thread is a script execution thread, the handler is executed synchronously on this thread and the result
     * is returned as an immediate future.
     *
     * If this thread is not a script execution thread (e.g., the Swing dispatch thread), then the handler task is
     * submitted for execution by one of the available script executor threads.
     *
     * @param handlerTask The handler task to execute
     * @return A future (placeholder) for the result of executing the handler (which may occur after some delay). The
     * success disposition provides a boolean indicating whether the handler "trapped" the message; the failure
     * disposition provides a {@link HtException} describing why the handler failed to execute.
     */
    private static CheckedFuture<Boolean, HtException> getFutureForHandlerExecutionTask(HandlerExecutionTask handlerTask) {
        if (isScriptExecutorThread()) {
            try {
                return Futures.makeChecked(Futures.immediateFuture(handlerTask.call()), new CheckedFutureExceptionMapper());
            } catch (HtException e) {
                return Futures.makeChecked(Futures.immediateFailedCheckedFuture(e), new CheckedFutureExceptionMapper());
            }
        } else {
            return Futures.makeChecked(listeningScriptExecutor.submit(handlerTask), new CheckedFutureExceptionMapper());
        }
    }
}