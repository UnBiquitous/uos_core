package br.unb.unbiquitous.ubiquitos.network.loopback;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * 
 * This class represents a loopback output stream. It contains a reference of a
 * memory buffer in which the communication will be done.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackOutputStream extends OutputStream{

	/** The reference for the buffer of the channel */	
	private Vector<Byte> buffer;
	
	/**
	 * Constructor. 
	 * @param buffer Reference for the output buffer of the channel.
	 */
	public LoopbackOutputStream(Vector<Byte> buffer){
		this.buffer = buffer;
	}
	
	/**
	 * Writes a byte on the buffer.
	 * @param The byte to be written.
	 */
	public void write(int b) throws IOException {
		synchronized(buffer){
			buffer.add(new Byte((byte) b));
		}
	}
	
}
