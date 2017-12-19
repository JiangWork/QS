package qs.projectreactor.play;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ThreadingFlux {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Flux.range(1, 10) 
	    .publishOn(Schedulers.single()) 
	    .subscribe();
		
		Flux.generate(
			    () -> 0, 
			    (state, sink) -> {
			      sink.next("3 x " + state + " = " + 3*state); 
			      if (state == 10) sink.complete(); 
			      return state + 1; 
			    }).subscribe(System.out::println);
	}

}
