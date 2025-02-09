package com.example.chromcastaudiodemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class MyServer extends NanoHTTPD {

	private final static int PORT = 8080;

	public MyServer() throws IOException {
		super(PORT);
		start();
		System.err.println("\nRunning! Point your browers to http://localhost:8080/ \n");
	}

	@Override
	public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parameters, Map<String, String> files) {

		FileInputStream fis = null;

		File file = new File(uri);

		try {

			fis = new FileInputStream(file.getAbsoluteFile());

			return newFixedLengthResponse(Status.OK, "audio/mp3", fis, file.length());

		} catch (FileNotFoundException e) {

			e.printStackTrace();

			return newFixedLengthResponse(Status.NOT_FOUND, "audio/mp3", fis, file.length());
		}

	}
}
