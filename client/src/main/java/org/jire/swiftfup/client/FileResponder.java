package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileResponder<RESPONSE extends FileResponse> {
	
	boolean complete(RESPONSE response);
	
}
