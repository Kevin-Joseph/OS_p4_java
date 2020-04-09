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

public class MMIO_Reader {
	
	class Reader{
		private RandomAccessFile file;
		private MappedByteBuffer mbb;
		
		
		void initReader(String file, int size) {
			try {
				this.file = new RandomAccessFile(file, "r");
			} catch (FileNotFoundException e) {
				System.out.println("Could not find file specified");
				e.printStackTrace();
			}
			
			try {
				 this.mbb = this.file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, size);
			} catch (IOException e) {
				System.out.println("Could not mape file for MappedByteBuffer.");
				e.printStackTrace();
			}
		}
		
		
		
		
		
		
		
		void closeReader() {
			try {
				file.close();
			} catch (IOException e) {
				System.out.println("Could not close random access file correctly");
				e.printStackTrace();
			}
		}
	}
	
	class Writer{
		private RandomAccessFile file;
		private MappedByteBuffer mbb;

		void initWriter(String file, int size) {
			try {
				this.file = new RandomAccessFile(file, "w");
			} catch (FileNotFoundException e) {
				System.out.println("Could not find file specified");
				e.printStackTrace();
			}
			
			try {
				 this.mbb = this.file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
			} catch (IOException e) {
				System.out.println("Could not mape file for MappedByteBuffer.");
				e.printStackTrace();
			}
			
		}
		
		
		
		
		void closeWriter() {
			try {
				file.close();
			} catch (IOException e) {
				System.out.println("Could not close random access file correctly");
				e.printStackTrace();
			}
		}
	}

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

		int numNodes = (int) (size/16);
		System.out.println("numNodes: "+ numNodes);
		
		
		MappedByteBuffer mbb = null;
		try {
			 mbb = raFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
		} catch (IOException e) {
			System.out.println("Could not mape file for MappedByteBuffer.");
			e.printStackTrace();
		}
		
		for(int i = 0; i < size; i++) {
			System.out.print((char)mbb.get());
		}
		
		SecureRandom sr = new SecureRandom();
		
		int sr1 = (sr.nextInt() % (numNodes - 1 + 1)) + 1;
		System.out.println();
		for(int i = 0; i < 20; i++) {
			sr1 = (sr.nextInt() % (numNodes - 1 + 1)) + 1;
			sr1 = Math.abs(sr1);
			System.out.print(sr1 + " ");
		}

		//mbb.position(16*sr1);
		mbb.order(ByteOrder.LITTLE_ENDIAN);
		mbb.load();
		System.out.println();
		System.out.println(sr1 + " ");
		System.out.println(mbb.getInt(16*sr1));
		System.out.println((char)mbb.getChar((16*sr1)+4));
		System.out.println((char)mbb.getChar((16*sr1)+6));
		System.out.println((int)mbb.getInt((16*sr1)+8));
		System.out.println(mbb.getInt((16*sr1)+12));

		

	}

}
