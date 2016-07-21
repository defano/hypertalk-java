/**
 * DestinationMsgBox.java
 * @author matt.defano@gmail.com
 * 
 * Representation of the message box as a destination for Value
 */

package hypertalk.ast.containers;

import hypertalk.ast.common.Chunk;
import hypertalk.ast.common.PartType;

public class DestinationMsgBox extends Destination {

	public final Chunk chunk;
	
	public DestinationMsgBox () {
		this.chunk = null;
	}
	
	public DestinationMsgBox (Chunk chunk) {
		this.chunk = chunk;
	}
	
	public Chunk chunk() {
		return chunk;
	}
	
	public PartType type () {
		return PartType.MESSAGEBOX;
	}
}
