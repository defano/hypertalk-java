/*
 * Interpreter
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.runtime;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypercard.paint.ToolMode;
import com.defano.hypercard.paint.ToolsContext;
import com.defano.hypertalk.ast.common.*;
import com.defano.hypertalk.ast.statements.ExpressionStatement;
import com.defano.hypertalk.ast.statements.StatementList;
import com.defano.hypertalk.exception.HtSemanticException;
import com.google.common.base.Function;
import com.google.common.util.concurrent.*;
import com.defano.hypertalk.HyperTalkTreeVisitor;
import com.defano.hypertalk.HyperTalkErrorListener;
import com.defano.hypertalk.ast.containers.PartSpecifier;
import com.defano.hypertalk.ast.functions.UserFunction;
import com.defano.hypertalk.ast.statements.Statement;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtParseError;
import com.defano.hypertalk.exception.HtSyntaxException;
import com.defano.hypertalk.parser.HyperTalkLexer;
import com.defano.hypertalk.parser.HyperTalkParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

/**
 * A facade and thread model for executing HyperTalk scripts. All script compilation and execution should flow through
 * this class to assure proper threading.
 */
public class Interpreter {

    private final static int MAX_COMPILE_THREADS = 4;           // Simultaneous background compile tasks
    private final static int MAX_EXECUTOR_THREADS = 12;         // Simultaneous scripts executing
    private final static int MAX_LISTENER_THREADS = 12;         // Simultaneous listeners waiting for handler completion

    private final static int IDLE_PERIOD_MS = 200;              // Frequency that 'idle' message is sent to card
    private final static int IDLE_DEFERRAL_CYCLES = 50;         // Number of cycles we defer if error is encountered

    private static final Executor messageExecutor;
    private static final ThreadPoolExecutor backgroundCompileExecutor;
    private static final ThreadPoolExecutor scriptExecutor;
    private static final ThreadPoolExecutor completionListenerExecutor;

    private static final ListeningExecutorService listeningScriptExecutor;
    private static final ScheduledExecutorService idleTimeExecutor;
    private static int deferIdleCycles = 0;

    static {
        messageExecutor = Executors.newSingleThreadExecutor();
        completionListenerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_LISTENER_THREADS, new ThreadFactoryBuilder().setNameFormat("completion-listener-%d").build());
        backgroundCompileExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_COMPILE_THREADS, new ThreadFactoryBuilder().setNameFormat("compiler-%d").build());
        scriptExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS, new ThreadFactoryBuilder().setNameFormat("script-executor-%d").build());

        listeningScriptExecutor = MoreExecutors.listeningDecorator(scriptExecutor);
        idleTimeExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("idle-executor-%d").build());

        idleTimeExecutor.scheduleAtFixedRate(() -> {
            int pendingHandlers = scriptExecutor.getActiveCount() + scriptExecutor.getQueue().size();
            if (pendingHandlers == 0) {
                ExecutionContext.getContext().getGlobalProperties().resetProperties();

                if (ToolsContext.getInstance().getToolMode() == ToolMode.BROWSE && deferIdleCycles < 1) {
                    ExecutionContext.getContext().getCurrentCard().getCardModel().receiveMessage(SystemMessage.IDLE.messageName, new ExpressionList(), (command, wasTrapped, error) -> {
                        if (error != null) {
                            deferIdleCycles = IDLE_DEFERRAL_CYCLES;
                        }
                    });
                }

                if (deferIdleCycles > 0) {
                    --deferIdleCycles;
                }
            }

        }, 0, IDLE_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Compiles the given script on a background compile thread and invokes the CompileCompletionObserver when complete.
     *
     * @param scriptText The script to compile.
     * @param observer A non-null callback to fire when compilation is complete.
     */
    public static void compileInBackground(String scriptText, CompileCompletionObserver observer) {
        backgroundCompileExecutor.submit(() -> {
            HtException generatedError = null;
            Script compiledScript = null;

            try {
                compiledScript = compile(scriptText);
            } catch (HtException e) {
                generatedError = e;
            }

            observer.onCompileCompleted(scriptText, compiledScript, generatedError);
        });
    }

    /**
     * Compiles the given script on the current thread.
     *
     * @param scriptText The script text to compile
     * @return The compiled Script object
     * @throws HtException Thrown if an error (i.e., syntax error) occurs when compiling.
     */
    public static Script compile(String scriptText) throws HtException {
        HyperTalkErrorListener errors = new HyperTalkErrorListener();

        HyperTalkLexer lexer = new HyperTalkLexer(new CaseInsensitiveInputStream(scriptText));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HyperTalkParser parser = new HyperTalkParser(tokens);
        parser.removeErrorListeners();        // don't log to console
        parser.addErrorListener(errors);

        try {
            ParseTree tree = parser.script();

            if (!errors.errors.isEmpty()) {
                throw errors.errors.get(0);
            }

            return (Script) new HyperTalkTreeVisitor().visit(tree);

        } catch (HtParseError e) {
            throw new HtSyntaxException("Didn't understand that.", e.lineNumber, e.columnNumber);
        } catch (Throwable e) {
            throw new HtException("Didn't understand that.", e);
        }
    }

    /**
     * Evaluates a string as a HyperTalk expression on the current thread.
     *
     * @param expression The value of the evaluated text; based on HyperTalk language semantics, text that cannot be
     *                   evaluated or is not a legal expression evaluates to itself.
     * @return The Value of the evaluated expression.
     */
    public static Value evaluate(String expression) {
        try {
            Statement statement = compile(expression).getStatements().list.get(0);
            if (statement instanceof ExpressionStatement) {
                return ((ExpressionStatement) statement).expression.evaluate();
            }
        } catch (Exception e) {
            // Nothing to do; okay to evaluate bogus text
        }

        // Value of a non-expression is itself
        return new Value(expression);
    }

    /**
     * Determines if the given Script text represents a valid HyperTalk expression on the current thread.
     *
     * @param statement Text to evaluate
     * @return True if the statement is a valid expression; false if it is not.
     * @throws HtException Thrown if the statement cannot be compiled (due to a syntax/semantic error).
     */
    public static boolean isExpressionStatement(String statement) throws HtException {
        return compile(statement).getStatements().list.get(0) instanceof ExpressionStatement;
    }

    /**
     * Executes a handler in the given script on a background thread, returning a future indicating whether or not the
     * script trapped the message.
     *
     * Any handler that does not 'pass' the command traps its behavior and prevents other scripts (or HyperCard) from
     * acting upon it. A script that does not implement the handler is assumed to 'pass' it.
     *
     * @param me The part whose script is being executed.
     * @param script The script of the part
     * @param command The command handler name.
     * @return A future containing a boolean indicating if the handler has "trapped" the message. Returns null if the
     * scripts attempts to pass a message other than the message being handled.
     */
    public static CheckedFuture<Boolean,HtException> executeHandler(PartSpecifier me, Script script, String command, ExpressionList arguments) throws HtSemanticException {
        NamedBlock handler = script.getHandler(command);

        if (handler == null) {
            return Futures.makeChecked(Futures.immediateFuture(false), HtException::new);
        } else {
            return Futures.makeChecked(Futures.transform(executeNamedBlock(me, handler, arguments), (Function<String, Boolean>) passedMessage -> {

                // Did not invoke pass: handler trapped message
                if (passedMessage == null || passedMessage.isEmpty()) {
                    return true;
                }

                // Invoked pass; did not trap message
                if (passedMessage.equalsIgnoreCase(command)) {
                    return false;
                }

                // Semantic error: Passing a message other than the handled message is disallowed.
                HyperCard.getInstance().showErrorDialog(new HtSemanticException("Cannot pass a message other than the one being handled."));
                return true;
            }), HtException::new);
        }
    }

    /**
     * Executes a list of HyperTalk statements on a background thread and returns the name of message passed (if any).
     *
     * @param me The part that the 'me' keyword refers to.
     * @param statementList The list of statements.
     * @return A future to the name passed from the script or null if no name was passed.
     * @throws HtException Thrown if an error occurs compiling the statements.
     */
    public static Future<String> executeString(PartSpecifier me, String statementList) throws HtException  {
        return executeNamedBlock(me, getAnonymousBlock(compile(statementList).getStatements()), new ExpressionList());
    }

    /**
     * Synchronously executes a compiled user function (blocks the current thread until execution is complete).
     *
     * Executes the function on the current thread, unless the current thread is the dispatch thread (in which case the
     * function executes on a new thread, but the current thread is blocked pending its completion).
     *
     * @param me The part that the 'me' keyword refers to.
     * @param function The compiled UserFunction
     * @param arguments The arguments to be passed to the function
     * @return The value returned by the function (an empty string if the function does not invoke 'return')
     * @throws HtSemanticException Thrown if an error occurs executing the function.
     */
    public static Value executeFunction(PartSpecifier me, UserFunction function, ExpressionList arguments) throws HtSemanticException {
        FunctionExecutionTask functionTask = new FunctionExecutionTask(me, function, arguments);

        try {
            // Not normally possible, since user functions are always executed in the context of a handler
            if (SwingUtilities.isEventDispatchThread())
                return scriptExecutor.submit(functionTask).get();
            else
                return functionTask.call();
        } catch (Exception e) {
            throw new HtSemanticException(e.getMessage(), e);
        }
    }

    /**
     * Gets the executor used to execute scripts handling a HyperCard system message (i.e., when 'mouseUp' or 'idle'
     * is sent to a part).
     *
     * @return The message handler executor.
     */
    public static Executor getCompletionListenerExecutor() {
        return completionListenerExecutor;
    }

    /**
     * Gets the executor used to evaluate the contents of the message box.
     * @return The message box executor.
     */
    public static Executor getMessageExecutor() {
        return messageExecutor;
    }

    /**
     * Executes a named block and returns a future to the name of the message passed from the block (if any).
     *
     * Executes on the current thread if the current thread is not the dispatch thread. If the current thread is the
     * dispatch thread, then submits execution to the scriptExecutor.
     *
     * @param me The part that the 'me' keyword refers to.
     * @param handler The block to execute
     * @param arguments The arguments to be passed to the block
     * @return A future to the name of the message passed from the block or null if no message was passed.
     * @throws HtSemanticException Thrown if an error occurs executing the block
     */
    private static CheckedFuture<String, HtException> executeNamedBlock(PartSpecifier me, NamedBlock handler, ExpressionList arguments) throws HtSemanticException {
        HandlerExecutionTask handlerTask = new HandlerExecutionTask(me, handler, arguments);

        try {
            if (SwingUtilities.isEventDispatchThread())
                return Futures.makeChecked(listeningScriptExecutor.submit(handlerTask), HtException::new);
            else {
                return Futures.makeChecked(Futures.immediateFuture(handlerTask.call()), HtException::new);
            }
        } catch (Exception e) {
            throw new HtSemanticException(e.getMessage(), e);
        }
    }

    /**
     * Wraps a list of statements in an anonymous NamedBlock object.
     *
     * @param statementList The list of statements
     * @return A NamedBlock representing the
     */
    private static NamedBlock getAnonymousBlock(StatementList statementList) {
        return new NamedBlock("", "", new ParameterList(), statementList);
    }

}
