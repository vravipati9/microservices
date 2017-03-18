package com.restclient_supplier;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

	private Client client;
    private WebTarget tut;
    private WebTarget processor;

    @Before
    public void init() {
        this.client = ClientBuilder.newClient();
        //client.property(ClientProperties.CONNECT_TIMEOUT, 100);
        //client.property(ClientProperties.READ_TIMEOUT, 500);
        this.tut = this.client.target("http://localhost:8080/restservice_supplier/webapi/messages");
        this.processor = this.client.target("http://localhost:8080/restservice_supplier/webapi/processor");
    }
    
    @Test
	public void fetchMessage() throws InterruptedException, ExecutionException, TimeoutException {
         //String message = this.tut.request().get(String.class);
    	 //System.out.println("message::"+message);
         Supplier<String> messageSupplier = () -> this.tut.request().get(String.class);
         CompletableFuture.supplyAsync(messageSupplier).thenAccept(this::consume).get();
	}
    
    @Test
	public void fetchMessage1() throws InterruptedException, ExecutionException, TimeoutException {
    	ExecutorService pool = Executors.newFixedThreadPool(1);
    	Supplier<String> messageSupplier = () -> this.tut.request().get(String.class);
    	CompletableFuture.supplyAsync(messageSupplier, pool).
    	thenApply(this::process).
    	exceptionally(this::handle).
    	thenAccept(this::consume).get();
	}

    // If any exception comes this calls
    String handle(Throwable t) {
    	return "We are overloaded!";
    }
    
    String process(String input) {
    	Response response = this.processor.request().post(Entity.text(input));
    	return response.readEntity(String.class);
    }
    
    void consume(String message) {
    	System.out.println("message::"+message);
    	this.tut.request().post(Entity.text(message));
    }

}
