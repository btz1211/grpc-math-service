package io.grpc.math.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.services.math.MathGrpc;
import io.grpc.services.math.Number;
import io.grpc.stub.StreamObserver;

public class MathImpl extends MathGrpc.MathImplBase{
	private static final Logger logger = Logger.getLogger(MathImpl.class.getName());
	
	@Override
	public void rootOfNumber(Number request, StreamObserver<Number> responseObserver) {
		responseObserver.onNext(Number.newBuilder()
				.setValue(Math.sqrt(request.getValue())).build());
		responseObserver.onCompleted();
	}
	
	@Override
	public StreamObserver<Number> sumOfNumbers(StreamObserver<Number> responseObserver) {
		StreamObserver<Number> observer = new StreamObserver<Number>(){
			double sum  = 0; 
					
			@Override
			public void onCompleted() {
				responseObserver.onNext(Number.newBuilder()
						.setValue(sum).build());
				responseObserver.onCompleted();
			}

			@Override
			public void onError(Throwable error) {
				logger.log(Level.SEVERE, "Error occurred when during sumation", error);
			}

			@Override
			public void onNext(Number number) {
				sum += number.getValue();
			}
		};
		
		return observer;
    }

	@Override
    public StreamObserver<Number> powerOfNumbers(StreamObserver<Number> responseObserver) {
		StreamObserver<Number> observer = new StreamObserver<Number>(){

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}

			@Override
			public void onError(Throwable error) {
				logger.log(Level.SEVERE, "Error occurred when getting power of numbers", error);

			}

			@Override
			public void onNext(Number number) {
				responseObserver.onNext(Number.newBuilder()
						.setValue(Math.pow(number.getValue(), 2))
						.build());
			}
			
		};
		
		return observer;
    }
}
