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
		if (current.isAlive())
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