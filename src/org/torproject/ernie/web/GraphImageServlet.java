/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that reads an HTTP request for a graph image, asks the
 * RObjectGenerator to generate this graph if it's not in the cache, and
 * returns the image bytes to the client.
 */
public class GraphImageServlet extends HttpServlet {

  private static final long serialVersionUID = -7356818641689744288L;

  private RObjectGenerator rObjectGenerator;

  public void init() {

    /* Get a reference to the R object generator that we need to generate
     * graph images. */
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Find out which graph type was requested and make sure we know this
     * graph type. */
    String requestedGraph = request.getRequestURI();
    if (requestedGraph.endsWith(".png")) {
      requestedGraph = requestedGraph.substring(0, requestedGraph.length()
          - ".png".length());
    }
    if (requestedGraph.contains("/")) {
      requestedGraph = requestedGraph.substring(requestedGraph.
          lastIndexOf("/") + 1);
    }

    /* Request graph from R object generator, which either returns it from
     * its cache or asks Rserve to generate it. */
    byte[] graphBytes = rObjectGenerator.generateGraph(requestedGraph,
        request.getParameterMap());

    /* Make sure that we have a graph to return. */
    if (graphBytes == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write graph bytes to response. */
    BufferedOutputStream output = null;
    response.setContentType("image/png");
    response.setHeader("Content-Length",
        String.valueOf(graphBytes.length));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + requestedGraph + ".png\"");
    output = new BufferedOutputStream(response.getOutputStream(), 1024);
    output.write(graphBytes, 0, graphBytes.length);
    output.flush();
    output.close();
  }
}

