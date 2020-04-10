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
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;





public class MMIO_Reader {
	
	
// For generating random times
private static Random rndGen = new Random(new Date().getTime());

private static final int MAX_SLEEP_TIME = 10000;



	
	

	private static Semaphore rSem = null;// = new Semaphore(MAX_ALLOWED_IN_QUEUE, true); // added
	private static Semaphore full = null;// = new Semaphore(MAX_ALLOWED_IN_QUEUE, true); // added
	private static Semaphore mutex = null;// = new Semaphore(1, true); // added
	private static Semaphore writerMutex = null;
	private static int NUM_READERS = 0;
	
	
	public static void main(String[] args) {
		// Check if the correct number of command line arguments have been passed.
		if (args.length != 3) {
			System.out.println("usage: java MMIO_Reader binFile numReaders numWriters");
			System.exit(0);
		}
		
		NUM_READERS = 0;
		int NUM_WRITERS = 0;
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
		
		rSem = new Semaphore(NUM_READERS, true);
		full = new Semaphore(NUM_WRITERS, true);
		mutex = new Semaphore(1, true);
		writerMutex = new Semaphore(1, true);
		
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
		
		//Reader readers[] = new Reader[NUM_READERS];
		Thread readers[] = new Thread[NUM_READERS];
		for(int i = 0; i < NUM_READERS; i++) {
			readers[i] = new Thread(new Reader(mbb,size));
			readers[i].start();
		}
		
		//Reader reader = new Reader(mbb,size);
		//reader.initReader(mbb,size);
		//reader.readNode();
		
		
		Thread writers[] = new Thread[NUM_WRITERS];
		for(int i = 0; i < NUM_WRITERS; i++) {
			writers[i] = new Thread(new Writer(mbb,size));
			writers[i].start();
		}
		//Writer writer = new Writer(mbb, size);
		//writer.initWriter(mbb, size);
		//writer.writeNode();
		
			
	}
	
	static class Reader implements Runnable{
		private MappedByteBuffer mbb;
		private int numNodes;
		
		Reader(MappedByteBuffer mbb, long size ) {
			this.mbb = mbb;
			this.numNodes = (int) (size/16);
			//System.out.println("numNodes: "+ numNodes);
		}
		
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
		
		public void run() {
			while(true) {
				
				try {
					// Try to acquire three semaphores from full,
					// means there is minimum number of fans to see
					//full.acquire(3);
					// Acquire mutex lock to make sure only one thread is in the 
					// critical section.
					writerMutex.acquire();
					writerMutex.release();
					rSem.acquire();
					mutex.acquire();
					//mutex.acquire();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				SecureRandom sr = new SecureRandom();
				int sr1 = Math.abs((sr.nextInt() % (numNodes)));
				System.out.println("READER**********************************");
				//System.out.println("Node Index: " + sr1);
				System.out.println("Node Index: " + mbb.getInt(16*sr1));
				System.out.println("Node value[0]: " + (char)mbb.get((16*sr1)+4));
				System.out.println("Node value[1]: " + (char)mbb.get((16*sr1)+5));
				System.out.println("Left Child Index: " + (int)mbb.getInt((16*sr1)+8));
				System.out.println("Right Child Index: " + (int)mbb.getInt((16*sr1)+12));
				System.out.println("**********************************");

				int leftChild = (int)mbb.getInt((16*sr1)+8);
				while(leftChild != -1) {
					System.out.println("READER left**********************************");
					//System.out.println(leftChild + " ");
					System.out.println("Left Child Index: " + mbb.getInt(16*leftChild));
					System.out.println("Node value[0]: " + (char)mbb.get((16*leftChild)+4));
					System.out.println("Node value[1]: " + (char)mbb.get((16*leftChild)+5));
					System.out.println("Left Child Index: " + (int)mbb.getInt((16*leftChild)+8));
					System.out.println("Right Child Index: " + (int)mbb.getInt((16*leftChild)+12));
					System.out.println("**********************************");

					leftChild = (int)mbb.getInt((16*leftChild)+8);
				}
				
				// Release the mutex semaphore and release three semaphores in empty
				mutex.release();				
				rSem.release();

				//empty.release(3);
				
				// Take a break
				try {
					Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME));
				} catch (InterruptedException e) {
					System.err.println(e.toString());
					System.exit(1);
				}
			}
		}

	}
	
	
	
	
	static class Writer implements Runnable{
		private MappedByteBuffer mbb;
		private int numNodes;

		Writer(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size/16);
			//System.out.println("numNodes: "+ numNodes);
			
		}
		
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
			//sleep(100);
		}
		
		public void run() {
			while(true) {
				
				try {
					// Try to get an empty semaphore
					//empty.acquire();
					// Try to get the mutex semaphore so only one thread is 
					// in the critical section at a time.
					//mutex.acquire();
					writerMutex.acquire();
					rSem.acquire(NUM_READERS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				SecureRandom sr = new SecureRandom();
				int sr1 = Math.abs((sr.nextInt() % (numNodes)));
			    int uLimit = 90, lLimit = 65, val = 0;
		        val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1))+ lLimit;
		        //System.out.println("val is: " + val);
		        mbb.put(((16*sr1)+4),(byte)val);
		        val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1))+ lLimit;
		        //System.out.println("val is: " + val);
		        mbb.put(((16*sr1)+5),(byte)val);
				System.out.println("WRITER**********************************");
				//System.out.println("Node Index: " + sr1);
				System.out.println("Node Index: " + mbb.getInt(16*sr1));
				System.out.println("Node value[0]: " + (char)mbb.get((16*sr1)+4));
				System.out.println("Node value[1]: " + (char)mbb.get((16*sr1)+5));
				System.out.println("Left Child Index: " + (int)mbb.getInt((16*sr1)+8));
				System.out.println("Right Child Index: " + (int)mbb.getInt((16*sr1)+12));
				System.out.println("**********************************");
				
				// Release mutex semaphore lock
				//mutex.release();
				writerMutex.release();
				rSem.release(NUM_READERS);
				// Release one to full semaphore
				//full.release();
				
				// Look in the exhibit for a little while
				try {
					Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME));
				} catch (InterruptedException e) {
					System.err.println(e.toString());
					System.exit(1);
				}
				
			}
		}
	}

}
