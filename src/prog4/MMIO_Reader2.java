/**
 * MIMO_Reader.java
 * Kevin Kulda
 * Dr. Fendt
 * Intro to Operating Systems
 * Program 4 Part 2
 * Due: 29-April-2020
 * version 2
 * 
 * Found instructions for doing synchronization with writers preference 
 * at http://jcsites.juniata.edu/faculty/rhodes/os/ch5d.htm.
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
import java.util.concurrent.TimeUnit;

public class MMIO_Reader2 {

	// For generating random times
	private static Random rndGen = new Random(new Date().getTime());
    // Max time for a thread to sleep
	private static final int MAX_SLEEP_TIME = 100;
	//private static Semaphore rSem = null;
	//private static Semaphore mutex = null;
	private static Semaphore randMutex = null;
	//private static Semaphore writerMutex = null;
	private static int NUM_READERS = 0;
	
	private static Semaphore x = null;
	private static Semaphore y = null;
	private static Semaphore z = null;
	private static Semaphore rsem = null;
	private static Semaphore wsem = null;

	
	private static int readcount=0, writecount = 0;

	private static int test = 0;

	public static void main(String[] args) {
		// Check if the correct number of command line arguments have been passed.
		if (args.length != 3) {
			System.out.println("usage: java MMIO_Reader binFile numReaders numWriters");
			System.exit(0);
		}

		NUM_READERS = 0;
		int NUM_WRITERS = 0;
		//Convert program args to integers and check to make sure values are within
		// the acceptable range.
		try {
			NUM_READERS = Integer.parseInt(args[1]);
			System.out.println("NUM_READERS: " + NUM_READERS);
			if (NUM_READERS < 1 || NUM_READERS > 120) {
				System.out.println("Number of readers must be between 1 and 120");
			}
			NUM_WRITERS = Integer.parseInt(args[2]);
			System.out.println("NUM_WRITERS: " + NUM_WRITERS);
			if (NUM_WRITERS < 1 || NUM_WRITERS > 120) {
				System.out.println("Number of writers must be between 1 and 120");
			}
		} catch (NumberFormatException e) {
			System.out.println("program argument must be an integer");
			System.exit(0);
		} catch (NullPointerException e) {
			System.out.println("program argument must be an integer");
			System.exit(0);
		}

		//Initialize semaphores.
		//rSem = new Semaphore(NUM_READERS, true);
		//mutex = new Semaphore(1, true);
		randMutex = new Semaphore(1, true);
		//writerMutex = new Semaphore(1, true);
		
		x = new Semaphore(1, true);
		y = new Semaphore(1);
		z = new Semaphore(1, true);
		rsem = new Semaphore(NUM_READERS, true);
		wsem = new Semaphore(1, true);

		//Create random access file to read and write to binary file.
		RandomAccessFile raFile = null;
		try {
			raFile = new RandomAccessFile(args[0], "rw");
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find file specified");
			e1.printStackTrace();
		}
		//Get the size of the provided file.
		long size = 0;
		try {
			size = raFile.length();
			System.out.println("size of file: " + size);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//Create a mapped byte buffer to read and write the the binary file.
		MappedByteBuffer mbb = null;
		try {
			mbb = raFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
			raFile.close();
			//Access in little endian because this is the format output by c.
			mbb.order(ByteOrder.LITTLE_ENDIAN);
			mbb.load();
		} catch (IOException e) {
			System.out.println("Could not map file for MappedByteBuffer.");
			e.printStackTrace();
		}
		//Create reader threads.
		Thread readers[] = new Thread[NUM_READERS];
		for (int i = 0; i < NUM_READERS; i++) {
			readers[i] = new Thread(new Reader(mbb, size));
			readers[i].start();
		}
		//Create writer threads.
		Thread writers[] = new Thread[NUM_WRITERS];
		for (int i = 0; i < NUM_WRITERS; i++) {
			writers[i] = new Thread(new Writer(mbb, size));
			writers[i].start();
		}

		// for testing only
		
		try {
			TimeUnit.MINUTES.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.exit(0);
		
		 

	}

	/**
	 * 
	 * Reader class
	 * This class uses a thread to read nodes from the binary file.
	 *
	 */
	static class Reader implements Runnable {
		private MappedByteBuffer mbb;
		private int numNodes;

		Reader(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size / 16);
			// System.out.println("numNodes: "+ numNodes);
		}

		void initReader(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size / 16);
			// System.out.println("numNodes: "+ numNodes);
		}

		void readNode() {
			SecureRandom sr = new SecureRandom();
			int sr1 = Math.abs((sr.nextInt() % (numNodes)));
			System.out.println("**********************************");
			// System.out.println("Node Index: " + sr1);
			System.out.println("Node Index: " + mbb.getInt(16 * sr1));
			System.out.println("Node value[0]: " + (char) mbb.get((16 * sr1) + 4));
			System.out.println("Node value[1]: " + (char) mbb.get((16 * sr1) + 5));
			System.out.println("Left Child Index: " + (int) mbb.getInt((16 * sr1) + 8));
			System.out.println("Right Child Index: " + (int) mbb.getInt((16 * sr1) + 12));
			System.out.println("**********************************");

			int leftChild = (int) mbb.getInt((16 * sr1) + 8);
			while (leftChild != -1) {
				System.out.println("**********************************");
				// System.out.println(leftChild + " ");
				System.out.println("Left Child Index: " + mbb.getInt(16 * leftChild));
				System.out.println("Node value[0]: " + (char) mbb.get((16 * leftChild) + 4));
				System.out.println("Node value[1]: " + (char) mbb.get((16 * leftChild) + 5));
				System.out.println("Left Child Index: " + (int) mbb.getInt((16 * leftChild) + 8));
				System.out.println("Right Child Index: " + (int) mbb.getInt((16 * leftChild) + 12));
				System.out.println("**********************************");

				leftChild = (int) mbb.getInt((16 * leftChild) + 8);
			}
		}

		public void run() {
			while (true) {
				try {
					test++;
					//Give the writer preference. If a writer is accessing the file, then wait.
					//writerMutex.acquire();
					//writerMutex.release();
					//Gain access to the file.
					//rSem.acquire();
					
					z.acquire();
					rsem.acquire();
					x.acquire();
					readcount++;
					if(readcount == 1) {
						wsem.acquire();
					}
					x.release();
					rsem.release();
					z.release();
					

					/*
					 * NOTE: A mutex is not required for the readers to read the file because they
					 * do not manipulate the data. However, if I did not have a mutex around this
					 * portion of the code the output could be ordered incorrectly because many
					 * readers could be in this portion of the code at once. For this reason I
					 * included the mutex for the program output.
					 */
					//mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				//Start at a random node and read all the left children.
				SecureRandom sr = new SecureRandom();
				int sr1 = Math.abs((sr.nextInt() % (numNodes)));
				System.out.println("READER**********************************");
				// System.out.println("Node Index: " + sr1);
				System.out.println("Node Index: " + mbb.getInt(16 * sr1));
				System.out.println("Node value[0]: " + (char) mbb.get((16 * sr1) + 4));
				System.out.println("Node value[1]: " + (char) mbb.get((16 * sr1) + 5));
				System.out.println("Left Child Index: " + (int) mbb.getInt((16 * sr1) + 8));
				System.out.println("Right Child Index: " + (int) mbb.getInt((16 * sr1) + 12));
				System.out.println("**********************************");

				int leftChild = (int) mbb.getInt((16 * sr1) + 8);
				while (leftChild != -1) {
					System.out.println("READER left**********************************");
					// System.out.println(leftChild + " ");
					System.out.println("Left Child Index: " + mbb.getInt(16 * leftChild));
					System.out.println("Node value[0]: " + (char) mbb.get((16 * leftChild) + 4));
					System.out.println("Node value[1]: " + (char) mbb.get((16 * leftChild) + 5));
					System.out.println("Left Child Index: " + (int) mbb.getInt((16 * leftChild) + 8));
					System.out.println("Right Child Index: " + (int) mbb.getInt((16 * leftChild) + 12));
					System.out.println("**********************************");

					leftChild = (int) mbb.getInt((16 * leftChild) + 8);
				}

				// Release the mutex semaphore and release the reader semaphore.
				//mutex.release();
				
				
				try {
					x.acquire();
					readcount--;
					if(readcount == 0) {
						wsem.release();
					}
					x.release();

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//rSem.release();
				// Sleep for a while
				try {
					randMutex.acquire();
					Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME));
				    randMutex.release();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	/**
	 * 
	 * Writer class
	 * This class uses threads to write to a binary file.
	 *
	 */
	static class Writer implements Runnable {
		private MappedByteBuffer mbb;
		private int numNodes;

		Writer(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size / 16);
			// System.out.println("numNodes: "+ numNodes);

		}

		void initWriter(MappedByteBuffer mbb, long size) {
			this.mbb = mbb;
			this.numNodes = (int) (size / 16);
			// System.out.println("numNodes: "+ numNodes);
		}

		void writeNode() {
			SecureRandom sr = new SecureRandom();
			int sr1 = Math.abs((sr.nextInt() % (numNodes)));
			int uLimit = 90, lLimit = 65, val = 0;
			val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1)) + lLimit;
			// System.out.println("val is: " + val);
			mbb.put(((16 * sr1) + 4), (byte) val);
			val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1)) + lLimit;
			// System.out.println("val is: " + val);
			mbb.put(((16 * sr1) + 5), (byte) val);
			System.out.println("**********************************");
			// System.out.println("Node Index: " + sr1);
			System.out.println("Node Index: " + mbb.getInt(16 * sr1));
			System.out.println("Node value[0]: " + (char) mbb.get((16 * sr1) + 4));
			System.out.println("Node value[1]: " + (char) mbb.get((16 * sr1) + 5));
			System.out.println("Left Child Index: " + (int) mbb.getInt((16 * sr1) + 8));
			System.out.println("Right Child Index: " + (int) mbb.getInt((16 * sr1) + 12));
			System.out.println("**********************************");
		}

		public void run() {
			while (true) {
				try {
					test--;
					//Allow only one writer in the critical zone at a time.
					//writerMutex.acquire();
					//Wait to enter the critical zone until all the readers 
					// have finished.
					//rSem.acquire(NUM_READERS);
					//writerMutex.release();
					
					y.acquire();
					writecount++;
					if(writecount == 1) {
						rsem.acquire(NUM_READERS);
					}
					y.release();
					wsem.acquire();
					
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//Access a random node and alter the two characters contained in the node.
				SecureRandom sr = new SecureRandom();
				int sr1 = Math.abs((sr.nextInt() % (numNodes)));

				System.out.println("WRITER BEFORE**********************************");
				// System.out.println("Node Index: " + sr1);
				System.out.println("Node Index: " + mbb.getInt(16 * sr1));
				System.out.println("Node value[0]: " + (char) mbb.get((16 * sr1) + 4));
				System.out.println("Node value[1]: " + (char) mbb.get((16 * sr1) + 5));
				System.out.println("Left Child Index: " + (int) mbb.getInt((16 * sr1) + 8));
				System.out.println("Right Child Index: " + (int) mbb.getInt((16 * sr1) + 12));
				System.out.println("**********************************");

				int uLimit = 90, lLimit = 65, val = 0;
				val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1)) + lLimit;
				// System.out.println("val is: " + val);
				mbb.put(((16 * sr1) + 4), (byte) val);
				val = (Math.abs(sr.nextInt()) % (uLimit - lLimit + 1)) + lLimit;
				// System.out.println("val is: " + val);
				mbb.put(((16 * sr1) + 5), (byte) val);
				System.out.println("WRITER AFTER**********************************");
				// System.out.println("Node Index: " + sr1);
				System.out.println("Node Index: " + mbb.getInt(16 * sr1));
				System.out.println("Node value[0]: " + (char) mbb.get((16 * sr1) + 4));
				System.out.println("Node value[1]: " + (char) mbb.get((16 * sr1) + 5));
				System.out.println("Left Child Index: " + (int) mbb.getInt((16 * sr1) + 8));
				System.out.println("Right Child Index: " + (int) mbb.getInt((16 * sr1) + 12));
				System.out.println("**********************************");

				//Release the writer semaphore and reader semaphores.
				//writerMutex.release();
				//rSem.release(NUM_READERS);
				
				
				wsem.release();
				try {
					y.acquire();
					writecount--;
					if(writecount == 0) {
						rsem.release(NUM_READERS);
					}
					y.release();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				// Sleep for a while
				try {
					randMutex.acquire();
					Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME));
				    randMutex.release();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
}
