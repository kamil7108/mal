package pl.polsl.km.mal.services;/*
 * [y] hybris Platform
 *
 * Copyright (c) 2021 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import io.netty.handler.codec.http.HttpStatusClass;
import lombok.extern.slf4j.Slf4j;
import pl.polsl.km.mal.exception.UnableToRunStreamDatabase;

@Slf4j
@Service
public class RecordProducerService
{
	private final HttpClient client;
	private final static String HOST = "http://localhost:8081";
	private final static String RUN_PATH = "runStreamDatabase";
	private final static String CLEAN_PATH = "cleanStreamDatabase";
	private final static String STOP_PATH = "stopProcess";
	private final static String FILL_PATH = "fill";
	private final static String REQUEST_FORMAT = "%s/%s";

	public RecordProducerService()
	{
		this.client = HttpClient.newBuilder()//
				.version(HttpClient.Version.HTTP_1_1)//
				.build();
	}

	public void runRecordProducer(final LocalDateTime startTime)
	{
		log.info("Sending request to start record producer with start date {}.", startTime);
		var uri = URI.create(String.format(REQUEST_FORMAT, HOST, RUN_PATH));
		var json = new JSONObject();
		json.put("startDate", startTime);
		var request = HttpRequest.newBuilder()//
				.header(HttpHeaders.CONTENT_TYPE, "application/json")//
				.uri(uri)//
				.PUT(HttpRequest.BodyPublishers.ofString(json.toString()))//
				.build();

		final HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
		if (!isSuccess(response.statusCode()))
		{
			var msg = "Unable to run stream database response={}";
			log.error(msg, response.body());
			throw new UnableToRunStreamDatabase(msg + response.body());
		}
	}

	public void cleanRecordInRecordProducerDatabase()
	{
		log.info("Sending request to clean record producer database.");
		var uri = URI.create(String.format(REQUEST_FORMAT, HOST, CLEAN_PATH));
		var request = HttpRequest.newBuilder()//
				.uri(uri)//
				.PUT(HttpRequest.BodyPublishers.noBody())//
				.build();

		final HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
		if (!isSuccess(response.statusCode()))
		{
			var msg = "Unable to clean stream database response={}";
			log.error(msg, response.body());
			throw new UnableToRunStreamDatabase(msg + response.body());
		}
	}

	public void stopRecordProducer()
	{
		log.info("Sending request to stop record producer process.");
		var uri = URI.create(String.format(REQUEST_FORMAT, HOST, STOP_PATH));
		var request = HttpRequest.newBuilder()//
				.uri(uri)//
				.PUT(HttpRequest.BodyPublishers.noBody())//
				.build();

		final HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
		if (!isSuccess(response.statusCode()))
		{
			var msg = "Unable to stop process in record producer response={}";
			log.error(msg, response.body());
			throw new UnableToRunStreamDatabase(msg + response.body());
		}
	}

	public void produceRecordBetweenTwoDates(final LocalDateTime startTime, final LocalDateTime endtime)
	{
		log.info("Sending request to start record producer with start date {} to endtime {}.", startTime, endtime);
		var uri = URI.create(String.format(REQUEST_FORMAT, HOST, FILL_PATH));
		var json = new JSONObject();
		json.put("startDate", startTime);
		json.put("endDate", endtime);
		var request = HttpRequest.newBuilder()//
				.header(HttpHeaders.CONTENT_TYPE, "application/json")//
				.uri(uri)//
				.PUT(HttpRequest.BodyPublishers.ofString(json.toString()))//
				.build();

		final HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
		if (!isSuccess(response.statusCode()))
		{
			var msg = "Unable to run stream database response={}";
			log.error(msg, response.body());
			throw new UnableToRunStreamDatabase(msg + response.body());
		}
	}

	private boolean isSuccess(int responseCode)
	{
		return HttpStatusClass.SUCCESS.contains(responseCode);
	}
}
