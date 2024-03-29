/** @author Professor Erika Parsons
 * Edited by Timothy Caole, 2/9/2024
 * MultiLevel Feedback Queue Algorithm
 */
import java.util.*; // Scheduler_mfq.java

public class Scheduler extends Thread
{   @SuppressWarnings({"unchecked","rawtypes"})
private Vector<TCB>[] queue = new Vector[3];
	private int timeSlice;
	private static final int DEFAULT_TIME_SLICE = 1000;

	// New data added to the original algorithm
	private boolean[] tids; // Indicate which ids have been used
	private static final int DEFAULT_MAX_THREADS = 10000;

	// A new feature added to the original algorithm
	// Allocate an ID array, each element indicating if that id has been used
	private int nextId = 0;
	private void initTid( int maxThreads ) {
		tids = new boolean[maxThreads];
		for ( int i = 0; i < maxThreads; i++ )
			tids[i] = false;
	}

	// A new feature added to the original algorithm
	// Search an available thread ID and provide a new thread with this ID
	private int getNewTid( ) {
		for ( int i = 0; i < tids.length; i++ ) {
			int tentative = ( nextId + i ) % tids.length;
			if ( tids[tentative] == false ) {
				tids[tentative] = true;
				nextId = ( tentative + 1 ) % tids.length;
				return tentative;
			}
		}
		return -1;
	}

	// A new feature added to the original algorithm
	// Return the thread ID and set the corresponding tids element to be unused
	private boolean returnTid( int tid ) {
		if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
			tids[tid] = false;
			return true;
		}
		return false;
	}

	// A new feature added to the original algorithm
	// Retrieve the current thread's TCB from the queue
	public TCB getMyTcb( ) {
		Thread myThread = Thread.currentThread( ); // Get my thread object
		synchronized( queue ) {
			for ( int level = 0; level < 3; level++ ) {
				for ( int i = 0; i < queue[level].size( ); i++ ) {
					TCB tcb=queue[level].elementAt( i );
					Thread thread = tcb.getThread( );
					if ( thread == myThread ) // if this is my TCB, return it
						return tcb;
				}
			}
		}
		return null;
	}

	// A new feature added to the original algorithm
	// Return the maximal number of threads to be spawned in the system
	public int getMaxThreads( ) {
		return tids.length;
	}

	public Scheduler( ) {
		timeSlice = DEFAULT_TIME_SLICE;
		initTid( DEFAULT_MAX_THREADS );
		for ( int i = 0; i < 3; i++ ) queue[i] = new Vector<TCB>( );
	}

	public Scheduler( int quantum ) {
		timeSlice = quantum;
		initTid( DEFAULT_MAX_THREADS );
		for ( int i = 0; i < 3; i++ ) queue[i] = new Vector<TCB>( );
	}

	// A new feature added to the original algorithm
	// A constructor to receive the max number of threads to be spawned
	public Scheduler( int quantum, int maxThreads ) {
		timeSlice = quantum;
		initTid( maxThreads );
		for ( int i = 0; i < 3; i++ ) queue[i] = new Vector<TCB>( );
	}

	private void schedulerSleep( ) {
		try {
			Thread.sleep( timeSlice / 2 );
		} catch ( InterruptedException e ) {
		}
	}

	// A modified addThread of the original algorithm
	public TCB addThread( Thread t ) {
		TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
		int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
		int tid = getNewTid( ); // get a new TID
		if ( tid == -1)
			return null;
		TCB tcb = new TCB( t, tid, pid ); // create a new TCB
		queue[0].add( tcb );
		return tcb;
	}

	// A new feature added to the original algorithm
	// Removing the TCB of a terminating thread
	public boolean deleteThread( ) {
		TCB tcb = getMyTcb( );
		if ( tcb!= null ) {
			this.interrupt( );
			return tcb.setTerminated( );
		} else
			return false;
	}

	public void sleepThread( int milliseconds ) {
		try {
			sleep( milliseconds );
		} catch ( InterruptedException e ) { }
	}

	// A modified run of the original algorithm
	public void run( ) {
		Thread current = null;
		TCB currentTCB = null;
		TCB prevTCB = null;
		int slice[] = new int[3];

		for ( int i = 0; i < 3; i++ )
			slice[i] = 0;

		while ( true ) {
			try {
				// get the next TCB and its thread from the highest queue
				int level = 0;
				for ( ; level < 3; level++ ) {
					if ( slice[level] == 0 ) {
						if ( queue[level].size( ) == 0 )
							continue;
						currentTCB = queue[level].firstElement( );
						break;
					}
					else {
						currentTCB = prevTCB;
						break;
					}
				}
				if ( level == 3 )
					continue;
				// ----------- TASK: Implement code that is based on the comments below: ------- //

				if (currentTCB.getTerminated() == true) {
					queue[level].remove(currentTCB); // Remove this thread from queue[level]
					returnTid(currentTCB.getTid()); // Return this thread id
					slice[level] = 0; // slice[level] must be 0
					continue;
				}
				current = currentTCB.getThread();

				if ((current != null)) {
					// If current is alive, resume it otherwise start it.
					// The same logic as Scheduler_rr.java
					// Just copy the logic here.
					if (current.isAlive()) {
						current.resume();
					} else {
					current.start();
				}


				schedulerSleep(); // Scheduler should sleep here.

// If current is alive, suspend it.
// The same logic as Scheduler_rr.java
// Just copy the logic here
				if (current != null && current.isAlive()) {
					current.suspend();
				}

				prevTCB = currentTCB;

// This is the heart of Prog2B!!!!
// Update slice[level].
// if slice[level] returns to 0,
//   currentThread must go to the next level or
//   rotate back in queue[2]

				synchronized (queue) { // synchronize queue to thread safely
					slice[level]++; // increment based on the current level

					if (slice[0] == 1) {// check if timeslice (AKA quantum) is 1
// thread control block (tcb) is moves into the to 2nd queue from 1st queue
						queue[0].remove(currentTCB); // remove from the front of the queue
						queue[1].add(currentTCB); // add at the back of the queue
						slice[0] = 0; // reset timeslice for 1st queue
					}

					if (slice[1] == 2) {// check if timeslice (AKA quantum) is 2
// thread control block (tcb) is moves into the to 3rd queue from 2nd queue
						queue[1].remove(currentTCB);// remove from the front of the queue
						queue[2].add(currentTCB); // add at the back of the queue
						slice[1] = 0; // reset timeslice for 2nd queue
					}

					if (slice[2] == 4) {// check if timeslice (AKA quantum) is 4
// thread control block (tcb) is moved within 3rd queue
						queue[2].remove(currentTCB); // remove from the front of the queue
						queue[2].add(currentTCB); // add at the back of the queue
						slice[2] = 0; // reset timeslice for 3rd queue
					}


					// checks current slice is multiple of 500ms
					if (slice[level] % 2 == 0) {
						for (int higherLevel = level + 1; higherLevel < 3; higherLevel++) {
							if (queue[higherLevel].size() > 0) {
// move current thread to higher priority queue
								queue[level].remove(currentTCB);
								queue[higherLevel].add(currentTCB);
								slice[level] = 0; // reset timeslice in current queue
								break;
							}
						}
					}
				}
				} // end of synchronize queue
			} catch ( NullPointerException e3 ) { };
		}
	}
}
