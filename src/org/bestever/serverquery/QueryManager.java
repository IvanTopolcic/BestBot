// --------------------------------------------------------------------------
// Copyright (C) 2012-2013 Best-Ever
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// --------------------------------------------------------------------------

package org.bestever.serverquery;

import java.util.concurrent.LinkedBlockingQueue;

import org.bestever.bebot.Bot;

/**
 * Runs on its own thread and handles incoming requests for servery querying <br>
 * This is on it's own thread so it can control any bottlenecking/spam safely
 * via a queue
 */
public class QueryManager extends Thread {
	
	/**
	 * Contains a list of requests that it will process in order
	 */
	private LinkedBlockingQueue<ServerQueryRequest> queryRequests;
	
	/**
	 * Tells us if the thread is to be terminated or not
	 */
	private boolean threadTerminate = false;
	
	/**
	 * Indicates if this thread is processing a query or not
	 */
	private boolean processingQuery = false;
	
	/**
	 * Reference to the bot
	 */
	private Bot bot;
	
	/**
	 * We should not exceed four requests at the same time, this would
	 * indicate we are getting flooded
	 */
	public static final int MAX_REQUESTS = 4;
	
	/**
	 * Initializes the QueryManager object, does not run it (must be done manually)
	 */
	public QueryManager(Bot bot) {
		this.queryRequests = new LinkedBlockingQueue<>(MAX_REQUESTS);
		this.bot = bot;
	}
	
	/**
	 * Adds the given query to the list to be processed
	 * @param query The serverquery we want to make
	 * @return True if it was added, false if the queue is full or it could not be added
	 */
	public boolean addRequest(ServerQueryRequest query) {
		if (queryRequests.size() >= MAX_REQUESTS)
			return false;
		return queryRequests.add(query);
	}
	
	/**
	 * Prepares the thread to terminate, will probably finish any request it is in
	 */
	public void kill() {
		threadTerminate = true;
	}
	
	/**
	 * Allows an external thread (socket handler) to tell this thread it is done
	 */
	public void signalProcessQueryComplete() {
		processingQuery = false;
	}
	
	/**
	 * Begins processing a query in the queue
	 */
	private void processQuery() {
		// Prevent our thread from trying to do two queries at once
		processingQuery = true;
		
		// This should never be null because we check in run() if the size is > 0
		ServerQueryRequest targetQuery = queryRequests.poll();
		if (targetQuery != null) {
			QueryHandler query = new QueryHandler(targetQuery, bot, this); // Must give it a reference to ourselves to signal query completion
			query.run();
		} else {
			bot.sendMessageToChannel("targetQuery was somehow null, aborting query.");
			signalProcessQueryComplete();
		}
	}
	
	/**
	 * Runs the main thread which loops while solving all query requests <br>
	 */
	public void run() {
		while (!threadTerminate) {
			// If we are not processing a request and we have a query, handle it
			if (!processingQuery && queryRequests.size() > 0)
				processQuery();
			
			// This thread should only check requests every few seconds or so
			// The thread should not be demanding at all
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}
	}
}
