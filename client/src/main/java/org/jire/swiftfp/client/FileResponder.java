package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public interface FileResponder<RESPONSE extends FileResponse> {
	
	boolean complete(RESPONSE response);
	
}
