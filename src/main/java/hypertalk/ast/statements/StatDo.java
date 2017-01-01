/**
 * StatDo.java
 * @author matt.defano@gmail.com
 * 
 * Implementation of the "do" statement (for executing strings)
 */

package hypertalk.ast.statements;

import hypercard.runtime.Interpreter;
import hypertalk.ast.expressions.Expression;
import hypertalk.exception.HtException;

public class StatDo extends Statement {

    public final Expression script;
    
    public StatDo(Expression script) {
        this.script = script;
    }
    
    public void execute () throws HtException {
        Interpreter.executeString(null, script.evaluate().toString());
    }
}
