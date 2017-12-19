package qs.projectreactor.play;

import java.util.Date;

import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

public class NormalSubscribe {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Flux<String> flux = Flux.just("foo", "bar", "foobar")
				.map((string) -> {return new Date().toString() + ":" + string;})
				.filter(ele->ele.endsWith("bar"));
		
		//normal subscribe
		flux.subscribe(System.out::println, 
				throwable->{System.out.println("Error occur:" + throwable);}, 
				()->{System.out.println("All done.");});
		
		
		// subscribe with Subscriber
		flux.map(String::toUpperCase)
	      .subscribe(new BaseSubscriber<String>() { 
	              @Override
	              protected void hookOnSubscribe(Subscription subscription) {
	                      System.out.println("Subscribed");
	                      request(2); 
	              }
	              @Override
	              protected void hookOnNext(String value) {
	            	  System.out.println(value);
	                  request(2); 
	              }
	              @Override
	              protected void hookOnComplete() {
	            	  System.out.println("Completed");
	              }
	              
	      });
	}

}
