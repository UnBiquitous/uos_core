package br.unb.unbiquitous.ubiquitos.network.loopback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * 
 * This class represents a loopback input stream. It contains a reference of a memory buffer in which the communication will be done.
 * 
 * @author Lucas Paranhos Quintella
 * @author Tales Porto
 * 
 */
public class LoopbackInputStream extends InputStream {

	/** The reference for the buffer of the channel */
	private Vector<Byte> vector;
	volatile int counter = 0;
	private byte[] buffer = new byte[0];

	/**
	 * Constructor.
	 * 
	 * @param buffer
	 *            Reference for the input buffer of the channel
	 */
	public LoopbackInputStream(Vector<Byte> buffer) {
		this.vector = buffer;
		this.buffer = toByteArray(buffer);
		new Thread(new LookupGarbageCollector(this));
	}

	public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }
	
	/**
	 * Reads a byte from the buffer, if buffer's size is greater than 0.
	 * 
	 * @return A byte from the buffer. -1 if buffer's size is 0 or less.
	 */
	public int read() throws IOException {
		synchronized (this.buffer) {
			refreshByteArray();
			if (buffer.length > 0) {
				return buffer[counter++];
			} else {
				return -1;
			}
		}
	}

	private void refreshByteArray() {
		synchronized (this.buffer) {
			synchronized (this.vector) {
				if (this.vector.size() != this.buffer.length)
					this.buffer = toByteArray(vector);
			}
		}
	}

	private byte[] toByteArray(Vector<Byte> buffer) {
		synchronized (this.buffer) {
			synchronized (this.vector) {
				int size = buffer.size();
				byte[] byteArray = new byte[size];
				for (int i = 0; i < size; i++) {
					byteArray[i] = buffer.get(i);

				}
				return byteArray;
			}
		}
	}

	/**
	 * Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of a method for this input stream.
	 * 
	 * @throws IOException
	 * @return Numbers of bytes that can be read.
	 */
	public int available() throws IOException {
		synchronized (this.buffer) {
			refreshByteArray();
			return buffer.length;
		}
	}

	private class LookupGarbageCollector implements Runnable {
		
		private LoopbackInputStream in;
		
		public LookupGarbageCollector(LoopbackInputStream in) {
			this.in = in;
		}
		
		@Override
		public void run() {
			while (vector.size() > 0) {
				synchronized (buffer) {
					synchronized (vector) {
						if (counter != 0) {
							vector.removeElementAt(0);
							counter--;
							in.toByteArray(vector);
						}
					}
				}
			}
		}
	}
}
