/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExoneraTorServlet extends HttpServlet {

  private static final long serialVersionUID = -6227541092325776626L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward to the actual ExoneraTor service. */
    response.sendRedirect("https://exonerator.torproject.org");
  }
}
