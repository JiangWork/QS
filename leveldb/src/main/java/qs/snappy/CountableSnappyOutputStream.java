package qs.snappy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyCodec;
import org.xerial.snappy.SnappyOutputStream;
import org.xerial.snappy.buffer.BufferAllocatorFactory;
import org.xerial.snappy.buffer.BufferAllocator;
import org.xerial.snappy.buffer.CachedBufferAllocator;



/**
 * Modify from {@link SnappyOutputStream}.
 * Add a field to count the number of bytes written to output stream.
 * 
 * @author Jiang
 *
 */
public class CountableSnappyOutputStream  extends OutputStream
{
    static final int MIN_BLOCK_SIZE = 1 * 1024;
    static final int DEFAULT_BLOCK_SIZE = 32 * 1024; // Use 32kb for the default block size

    protected final OutputStream out;
    private final int blockSize;

    private final BufferAllocator inputBufferAllocator;
    private final BufferAllocator outputBufferAllocator;

    // The input and output buffer fields are set to null when closing this stream:
    protected byte[] inputBuffer;
    protected byte[] outputBuffer;
    private int inputCursor = 0;
    private int outputCursor = 0;
    private boolean closed;
    
	private long writtenBytesCount = 0;
	

    public CountableSnappyOutputStream(OutputStream out)
    {
        this(out, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param out
     * @param blockSize byte size of the internal buffer size
     * @throws IOException
     */
    public CountableSnappyOutputStream(OutputStream out, int blockSize)
    {
        this(out, blockSize, CachedBufferAllocator.getBufferAllocatorFactory());
    }

    public CountableSnappyOutputStream(OutputStream out, int blockSize, BufferAllocatorFactory bufferAllocatorFactory)
    {
        this.out = out;
        this.blockSize = Math.max(MIN_BLOCK_SIZE, blockSize);
        int inputSize = blockSize;
        int outputSize = SnappyCodec.HEADER_SIZE + 4 + Snappy.maxCompressedLength(blockSize);

        this.inputBufferAllocator = bufferAllocatorFactory.getBufferAllocator(inputSize);
        this.outputBufferAllocator = bufferAllocatorFactory.getBufferAllocator(outputSize);

        inputBuffer = inputBufferAllocator.allocate(inputSize);
        outputBuffer = outputBufferAllocator.allocate(outputSize);

        outputCursor = SnappyCodec.currentHeader.writeHeader(outputBuffer, 0);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int byteOffset, int byteLength)
            throws IOException
    {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        int cursor = 0;
        while (cursor < byteLength) {
            int readLen = Math.min(byteLength - cursor, blockSize - inputCursor);
            // copy the input data to uncompressed buffer
            if (readLen > 0) {
                System.arraycopy(b, byteOffset + cursor, inputBuffer, inputCursor, readLen);
                inputCursor += readLen;
            }
            if (inputCursor < blockSize) {
                return;
            }

            compressInput();
            cursor += readLen;
        }
    }

    /**
     * Compress the input long array data
     *
     * @param d input array
     * @param off offset in the array
     * @param len the number of elements in the array to copy
     * @throws IOException
     */
    public void write(long[] d, int off, int len)
            throws IOException
    {
        rawWrite(d, off * 8, len * 8);
    }

    /**
     * Compress the input double array data
     *
     * @param f input array
     * @param off offset in the array
     * @param len the number of elements in the array to copy
     * @throws IOException
     */
    public void write(double[] f, int off, int len)
            throws IOException
    {
        rawWrite(f, off * 8, len * 8);
    }

    /**
     * Compress the input float array data
     *
     * @param f input array
     * @param off offset in the array
     * @param len the number of elements in the array to copy
     * @throws IOException
     */
    public void write(float[] f, int off, int len)
            throws IOException
    {
        rawWrite(f, off * 4, len * 4);
    }

    /**
     * Compress the input int array data
     *
     * @param f input array
     * @param off offset in the array
     * @param len the number of elements in the array to copy
     * @throws IOException
     */
    public void write(int[] f, int off, int len)
            throws IOException
    {
        rawWrite(f, off * 4, len * 4);
    }

    /**
     * Compress the input short array data
     *
     * @param f input array
     * @param off offset in the array
     * @param len the number of elements in the array to copy
     * @throws IOException
     */
    public void write(short[] f, int off, int len)
            throws IOException
    {
        rawWrite(f, off * 2, len * 2);
    }

    /**
     * Compress the input array data
     *
     * @param d
     * @throws IOException
     */
    public void write(long[] d)
            throws IOException
    {
        write(d, 0, d.length);
    }

    /**
     * Compress the input array data
     *
     * @param f
     * @throws IOException
     */
    public void write(double[] f)
            throws IOException
    {
        write(f, 0, f.length);
    }

    /**
     * Compress the input array data
     *
     * @param f
     * @throws IOException
     */
    public void write(float[] f)
            throws IOException
    {
        write(f, 0, f.length);
    }

    /**
     * Compress the input array data
     *
     * @param f
     * @throws IOException
     */
    public void write(int[] f)
            throws IOException
    {
        write(f, 0, f.length);
    }

    /**
     * Compress the input array data
     *
     * @param f
     * @throws IOException
     */
    public void write(short[] f)
            throws IOException
    {
        write(f, 0, f.length);
    }

    private boolean hasSufficientOutputBufferFor(int inputSize)
    {
        int maxCompressedSize = Snappy.maxCompressedLength(inputSize);
        return maxCompressedSize < outputBuffer.length - outputCursor - 4;
    }

    /**
     * Compress the raw byte array data.
     *
     * @param array array data of any type (e.g., byte[], float[], long[], ...)
     * @param byteOffset
     * @param byteLength
     * @throws IOException
     */
    public void rawWrite(Object array, int byteOffset, int byteLength)
            throws IOException
    {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        int cursor = 0;
        while (cursor < byteLength) {
            int readLen = Math.min(byteLength - cursor, blockSize - inputCursor);
            // copy the input data to uncompressed buffer
            if (readLen > 0) {
                Snappy.arrayCopy(array, byteOffset + cursor, readLen, inputBuffer, inputCursor);
                inputCursor += readLen;
            }
            if (inputCursor < blockSize) {
                return;
            }

            compressInput();
            cursor += readLen;
        }
    }

    /**
     * Writes the specified byte to this output stream. The general contract for
     * write is that one byte is written to the output stream. The byte to be
     * written is the eight low-order bits of the argument b. The 24 high-order
     * bits of b are ignored.
     */
    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b)
            throws IOException
    {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        if (inputCursor >= inputBuffer.length) {
            compressInput();
        }
        inputBuffer[inputCursor++] = (byte) b;
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush()
            throws IOException
    {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        compressInput();
        dumpOutput();
        out.flush();
    }

    static void writeInt(byte[] dst, int offset, int v)
    {
        dst[offset] = (byte) ((v >> 24) & 0xFF);
        dst[offset + 1] = (byte) ((v >> 16) & 0xFF);
        dst[offset + 2] = (byte) ((v >> 8) & 0xFF);
        dst[offset + 3] = (byte) ((v >> 0) & 0xFF);
    }

    static int readInt(byte[] buffer, int pos)
    {
        int b1 = (buffer[pos] & 0xFF) << 24;
        int b2 = (buffer[pos + 1] & 0xFF) << 16;
        int b3 = (buffer[pos + 2] & 0xFF) << 8;
        int b4 = buffer[pos + 3] & 0xFF;
        return b1 | b2 | b3 | b4;
    }

    protected void dumpOutput()
            throws IOException
    {
        if (outputCursor > 0) {
            out.write(outputBuffer, 0, outputCursor);
            writtenBytesCount += outputCursor;
            outputCursor = 0;
        }
    }

    protected void compressInput()
            throws IOException
    {
        if (inputCursor <= 0) {
            return; // no need to dump
        }

        // Compress and dump the buffer content
        if (!hasSufficientOutputBufferFor(inputCursor)) {
            dumpOutput();
        }
        int compressedSize = Snappy.compress(inputBuffer, 0, inputCursor, outputBuffer, outputCursor + 4);
        // Write compressed data size
        writeInt(outputBuffer, outputCursor, compressedSize);
        outputCursor += 4 + compressedSize;
        inputCursor = 0;
    }

    /**
     * close the stream
     */
    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close()
            throws IOException
    {
        if (closed) {
            return;
        }
        try {
            flush();
            out.close();
        }
        finally {
            closed = true;
            inputBufferAllocator.release(inputBuffer);
            outputBufferAllocator.release(outputBuffer);
            inputBuffer = null;
            outputBuffer = null;
        }
    }
    
	public long getWrittenBytesCount() {
		return writtenBytesCount;
	}


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int bufferSize =  4 * 1024 * 1024;
		FileOutputStream fos = new FileOutputStream(new File("snappy.out"));
        CountableSnappyOutputStream csos = new CountableSnappyOutputStream(fos, bufferSize);
        FileInputStream fis = new FileInputStream(new File("/Users/Miller/Downloads/ProgramminginScala.pdf"));
        byte[] buffer = new byte[bufferSize];
        int readCount = 0;
        while((readCount = fis.read(buffer)) != -1) {
        	csos.write(buffer, 0, readCount);
        }
        csos.close();
        fis.close();
        System.out.println("Writting " + csos.writtenBytesCount);
	}
}

