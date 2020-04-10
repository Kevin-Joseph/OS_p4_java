/**
 * MIMO_Reader.java
 * Kevin Kulda
 * Dr. Fendt
 * Intro to Operating Systems
 * Program 4 Part 2
 * Due: 29-April-2020
 * version 1
 */

package prog4;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.concurrent.Semaphore;

public class MMIO_Reader {
	
	static class Reader{
		private MappedByteBuffer mbb;
		private int numNodes;
		
		
		void initReader(MappedByteBuffer mbb, long size ) {
			this.mbb = mbb;
			this.numNodes = (int) (size/16);
			//System.out.println("numNodes: "+ numNodes);
		}
		
		void readNode() {
			SecureRandom sr = new SecureRandom();
			
			int sr1 = Math.abs((sr.nextInt() % (numNodes)));
			System.out.println("**********************************");
			//System.out.println("Node Index: " + sr1);
			System.out.println("Node Index: " + mbb.getInt(16*sr1));
			System.out.println("Node value[0]: " + (char)mbb.get((16*sr1)+4));
			System.out.println("Node value[1]: " + (char)mbb.get((16*sr1)+5));
			System.out.println("Left Child Index: " + (int)mbb.getInt((16*sr1)+8));
			System.out.println("Right Child Index: " + (int)mbb.getInt((16*sr1)+12));
			System.out.println("**********************************");

			int leftChild = (int)mbb.getInt((16*sr1)+8);
			while(leftChild != -1) {
				System.out.println("**********************************");
				//System.out.println(leftChild + " ");
				System.out.println("Left Child Index: " + mbb.getInt(16*leftChild));
				System.out.println("Node value[0]: " + (char)mbb.get((16*leftChild)+4));
				System.out.println("Node value[1]: " + (char)mbb.get((16*leftChild)+5));
				System.out.println("Left Child Index: " + (int)mbb.getInt((16*leftChild)+8));
				System.out.println("Right Child Index: " + (int)mbb.getInt((16*leftChild)+12));
				System.out.println("**********************************");

				leftChild = (int)mbb.getInt((16*leftChild)+8);
			}
			
			//sleep(100); add later
		}

	}
	
	
	
	
	static class Writer{
		private MappedByteBuffer mbb;
		private int numNodes;


		void initWriter(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size/16);
			//System.out.println("numNodes: "+ numNodes);
			
		}
		
		void writeNode() {
			SecureRandom sr = new SecureRandom();
			int sr1 = Math.abs((sr.nextInt() % (numNodes)));
		    int uLimit = 90, lLimit = 65, val = 0;
	        val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1))+ lLimit;
	        //System.out.println("val is: " + val);
	        mbb.put(((16*sr1)+4),(byte)val);
	        val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1))+ lLimit;
	        //System.out.println("val is: " + val);
	        mbb.put(((16*sr1)+5),(byte)val);
			System.out.println("**********************************");
			//System.out.println("Node Index: " + sr1);
			System.out.println("Node Index: " + mbb.getInt(16*sr1));
			System.out.println("Node value[0]: " + (char)mbb.get((16*sr1)+4));
			System.out.println("Node value[1]: " + (char)mbb.get((16*sr1)+5));
			System.out.println("Left Child Index: " + (int)mbb.getInt((16*sr1)+8));
			System.out.println("Right Child Index: " + (int)mbb.getInt((16*sr1)+12));
			System.out.println("**********************************");

		}
		
		//sleep(100);
	}

	//private final Semaphore empty = new Semaphore(MAX_ALLOWED_IN_QUEUE, true); // added
	//private final Semaphore full = new Semaphore(MAX_ALLOWED_IN_QUEUE, true); // added
	//private final Semaphore mutex = new Semaphore(1, true); // added
	
	public static void main(String[] args) {
		// Check if the correct number of command line arguments have been passed.
		if (args.length != 3) {
			System.out.println("usage: java MMIO_Reader binFile numReaders numWriters");
			System.exit(0);
		}
		
		int NUM_READERS = 0, NUM_WRITERS = 0;
		try {
			NUM_READERS = Integer.parseInt(args[1]);
			System.out.println("NUM_READERS: " + NUM_READERS);
			NUM_WRITERS = Integer.parseInt(args[2]);
			System.out.println("NUM_WRITERS: " + NUM_WRITERS);
	    } catch(NumberFormatException e) { 
	    	System.out.println("program argument must be an integer");
	    	System.exit(0);
	    } catch(NullPointerException e) {
	    	System.out.println("program argument must be an integer");
	    	System.exit(0);
	    }
		
 
		
		
		RandomAccessFile raFile = null;
		try {
			raFile = new RandomAccessFile(args[0], "rw");
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find file specified");
			e1.printStackTrace();
		}
		
		long size = 0;
		try {
			size = raFile.length();
			System.out.println("size of file: "+ size);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		MappedByteBuffer mbb = null;
		try {
			mbb = raFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
			raFile.close();
			mbb.order(ByteOrder.LITTLE_ENDIAN);
			mbb.load();
		} catch (IOException e) {
			System.out.println("Could not mape file for MappedByteBuffer.");
			e.printStackTrace();
		}

		
		Reader reader = new Reader();
		reader.initReader(mbb,size);
		reader.readNode();
		
		
		
		Writer writer = new Writer();
		writer.initWriter(mbb, size);
		writer.writeNode();
		
			
	}

}
