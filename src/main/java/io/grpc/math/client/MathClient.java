package io.grpc.math.client;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.services.math.MathGrpc;
import io.grpc.services.math.Number;
import io.grpc.services.math.Number.Builder;
import io.grpc.stub.StreamObserver;

public class MathClient {
	private static final Logger logger = Logger.getLogger(MathClient.class.getName());
	private final ManagedChannel channel;
	private final MathGrpc.MathBlockingStub stub;
	private final MathGrpc.MathStub asyncStub;
	
	public MathClient(String host, int port){
		channel = ManagedChannelBuilder.forAddress(host, port)
				// Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
				// needing certificates.
				.usePlaintext(true)
				.build();
		stub = MathGrpc.newBlockingStub(channel);
		asyncStub = MathGrpc.newStub(channel);
	}
	
	public double getRoot(Double num){
		Number result = stub.rootOfNumber(Number.newBuilder().setValue(num).build());
		return result.getValue();
	}
	
	public double getSum(Double[] nums) throws InterruptedException{
		final Builder builder = Number.newBuilder();
	    final CountDownLatch finishLatch = new CountDownLatch(1);

		StreamObserver<Number> responseObserver = new StreamObserver<Number>(){

			@Override
			public void onCompleted() {
				finishLatch.countDown();
			}

			@Override
			public void onError(Throwable error) {
				logger.log(Level.SEVERE, "failed to get sum", error);
				finishLatch.countDown();
			}

			@Override
			public void onNext(Number result) {
				builder.setValue(result.getValue());
			}
		};
		
		StreamObserver<Number> requestObserver = asyncStub.sumOfNumbers(responseObserver);
		
		Arrays.asList(nums)
			  .forEach(num -> requestObserver.onNext(Number.newBuilder().setValue(num).build()));

		requestObserver.onCompleted();
		finishLatch.await();
		return builder.build().getValue();
	}
	
	public Double[] getSquared(Double[] nums) throws InterruptedException{
		final Double[] poweredNums = new Double[nums.length];
	    final CountDownLatch finishLatch = new CountDownLatch(1);

	    StreamObserver<Number> responseObserver = new StreamObserver<Number>(){
	    	int counter = 0;
			@Override
			public void onCompleted() {
				finishLatch.countDown();
			}

			@Override
			public void onError(Throwable error) {
				logger.log(Level.SEVERE, "failed to get sum", error);
				finishLatch.countDown();
			}

			@Override
			public void onNext(Number result) {
				poweredNums[counter++] = result.getValue();
			}
		};
		
		StreamObserver<Number> requestObserver = asyncStub.powerOfNumbers(responseObserver);
		
		for(Double num: nums){
			requestObserver.onNext(Number.newBuilder().setValue(num).build());
		}
		
		requestObserver.onCompleted();
		finishLatch.await();
		return poweredNums;
	}
	
	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args){
		MathClient client = new MathClient("localhost", 8009);
		Double[] nums = {2.0, 3.0, 4.0, 5.0, 6.0};
		try {
			System.out.println("==========ROOT============");
			System.out.println("root: " + client.getRoot(16.0));
			System.out.println("==========SUM============");
			System.out.println("sum:" + client.getSum(nums));
			System.out.println("==========SQUARED============");
			Arrays.asList(client.getSquared(nums))
			.forEach(System.out::println);
			
			client.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
