package qs.snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * A wrapper of {@link RandomAccessFile}, provides function to read certain size of data.
 * 
 * 
 * @author Jiang
 *
 */
public class ArchiveInputStream extends InputStream {

	private RandomAccessFile raf;
	private long size;
	private long readBytesCount = 0;


	public ArchiveInputStream(RandomAccessFile raf, long size) {
		this.raf = raf;
		this.size = size;
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		throw new IOException("Oops, this should never happed.");
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (readBytesCount >= size) {
			return -1;
		}
		int byteSlots = Math.min(len, b.length - off);
		int shouldRead = Math.min(byteSlots, (int)(size - readBytesCount));
		int bytesNum = raf.read(b, off, shouldRead);
		readBytesCount += bytesNum;
		return bytesNum;
	}
	
	public void close() throws IOException {
		raf.close();
	}
}
