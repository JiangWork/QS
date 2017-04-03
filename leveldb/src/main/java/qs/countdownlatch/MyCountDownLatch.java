package qs.countdownlatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MyCountDownLatch {

	private AtomicInteger N;
	private List<Object> locks = new ArrayList<Object>();
	
	MyCountDownLatch(int N) {
		this.N = new AtomicInteger(N);
	}
	
	public void await() {
		if (N.get() == 0) return;
		Object lock = new Object();
		locks.add(lock);
		for (;;) {
			if (N.get() == 0) return;
			synchronized(lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void countdown() {
		decrement();
		if (N.get() == 0) {
			for (Object lock: locks) {
				synchronized(lock) {
				
						lock.notify();
					
				}
			}
			locks.clear();
		}
	}
	
	public void decrement() {
		if (N.get() > 0) {
			N.decrementAndGet();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		class Worker extends Thread {
			MyCountDownLatch latch;
			public Worker(MyCountDownLatch latch) {
				this.latch = latch;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println(Thread.currentThread() + ": start to wait" );;
				latch.await();
				System.out.println(Thread.currentThread() + ": start to run." );;
			}
			
		}
		MyCountDownLatch latch = new MyCountDownLatch(1);
		Worker[] workers = new Worker[10];
		for (int i = 0; i < 10; ++i) {
			workers[i] = new Worker(latch);
			workers[i].start();
		}
		Thread.sleep(10000);
		latch.countdown();
		Thread.sleep(10000);
	}
}
