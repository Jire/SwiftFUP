package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public final class FileChecksumsResponse {
	
	private final int size;
	
	private final int[] pairs;
	private final int[] checksums;
	
	public FileChecksumsResponse(int size, int[] pairs, int[] checksums) {
		this.size = size;
		this.pairs = pairs;
		this.checksums = checksums;
	}
	
	public int getSize() {
		return size;
	}
	
	public int[] getPairs() {
		return pairs;
	}
	
	public int[] getChecksums() {
		return checksums;
	}
	
	public static final class Builder {
		
		private final int size;
		
		private final int[] pairs;
		private final int[] checksums;
		
		private int cursor;
		
		public Builder(int size) {
			this.size = size;
			
			pairs = new int[size];
			checksums = new int[size];
		}
		
		public Builder add(int pair, int checksum) {
			pairs[cursor] = pair;
			checksums[cursor] = checksum;
			
			cursor++;
			
			return this;
		}
		
		public FileChecksumsResponse build() {
			return new FileChecksumsResponse(size, pairs, checksums);
		}
		
	}
	
}
