package io.grpc.math.server;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class MathServer {
	private static final Logger logger = Logger.getLogger(MathServer.class.getName());

	private int port = 8009;
	private Server server;
	
	public void start() throws IOException{
		server = ServerBuilder.forPort(port)
				.addService(new MathImpl())
				.build()
				.start();
		
		logger.info("Server started, listening to: " + port);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				MathServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
		
	}
	
	public void stop(){
		if(server != null){
			server.shutdown();
		}
	}
	
	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final MathServer server = new MathServer();
		server.start();
		server.blockUntilShutdown();
	}
}
