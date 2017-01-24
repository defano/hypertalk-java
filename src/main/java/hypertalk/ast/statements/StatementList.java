/**
 * StatementList.java
 * @author matt.defano@gmail.com
 * 
 * Encapsulation of a list of statements (e.g., the body of a function or handler)
 */

package hypertalk.ast.statements;

import hypertalk.exception.HtException;

import java.util.Vector;

public class StatementList {

    public final Vector<Statement> list;
    
    public StatementList () {
        list = new Vector<>();
    }
    
    public StatementList (Statement s) {
        list = new Vector<>();
        append(s);
    }

    public StatementList append (Statement s) {
        list.add(s);
        return this;
    }
    
    public void execute() throws HtException {
        for (Statement s : list) {
            s.execute();
            
            if (s.breakExecution)
                break;
        }
    }
}
