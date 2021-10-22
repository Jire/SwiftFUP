package org.jire.swiftfup.client;

import java.util.zip.Inflater;

/**
 * @author Jire
 */
public final class GZIPDecompressor {
	
	private final Inflater inflater = new Inflater(true);
	private final byte[] buffer;
	
	public GZIPDecompressor(byte[] buffer) {
		this.buffer = buffer;
	}
	
	public GZIPDecompressor() {
		this(new byte[999999]);
	}
	
	public byte[] decompress(byte[] input) {
		int outputLength = decompress(input, buffer);
		byte[] output = new byte[outputLength];
		System.arraycopy(buffer, 0, output, 0, outputLength);
		return output;
	}
	
	public int decompress(byte[] input, byte[] output) {
		int offset = calcOffset(input);
		
		int uncompressedBytes = -1;
		try {
			inflater.setInput(input, offset, input.length - offset - 8); //Trim GZIP footer 8 bytes
			uncompressedBytes = inflater.inflate(output);
		} catch (final Exception exception) {
			inflater.reset();
			throw new RuntimeException("Invalid GZIP compressed data!");
		}
		inflater.reset();
		
		return uncompressedBytes;
	}
	
	public int calcOffset(byte[] input) {
		if (input[0] != 31 || input[1] != -117)
			throw new RuntimeException("invalid gzip header");
		
		int flags = input[3];
		
		int offset = 10;
		
		if ((flags & (1 << 1)) != 0) {
			offset += 2;
		}
		
		if ((flags & (1 << 2)) != 0) {
			offset += 2;
		}
		
		if ((flags & (1 << 3)) != 0) {
			while (input[offset++] != '\0') ;
		}
		
		if ((flags & (1 << 4)) != 0) {
			while (input[offset++] != '\0') ;
		}
		
		if ((flags & (1 << 5)) != 0) {
			offset += 12;
		}
		
		return offset;
	}
	
	private static final ThreadLocal<GZIPDecompressor> threadLocal = ThreadLocal.withInitial(GZIPDecompressor::new);
	
	public static GZIPDecompressor getInstance() {
		return threadLocal.get();
	}
	
}
