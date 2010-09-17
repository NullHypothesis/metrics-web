package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.torproject.ernie.util.*;

import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;

public class DescriptorServlet extends HttpServlet {

  private Connection conn = null;

  public DescriptorServlet() {

    /* Try to load the database driver. */
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      /* Don't initialize conn and always reply to all requests with
       * "500 internal server error". */
      return;
    }

    /* Read JDBC URL from property file. */
    ErnieProperties props = new ErnieProperties();
    String connectionURL = props.getProperty("jdbc.url");

    /* Try to connect to database. */
    try {
      conn = DriverManager.getConnection(connectionURL);
    } catch (SQLException e) {
      conn = null;
    }
  }

  private void writeHeader(PrintWriter out) throws IOException {
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
          + "Transitional//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
        + "  <head>\n"
        + "    <meta content=\"text/html; charset=ISO-8859-1\"\n"
        + "          http-equiv=\"content-type\" />\n"
        + "    <title>Relay Descriptor</title>\n"
        + "    <meta http-equiv=Content-Type content=\"text/html; "
          + "charset=iso-8859-1\">\n"
        + "    <link href=\"http://www.torproject.org/"
          + "stylesheet-ltr.css\" type=text/css rel=stylesheet>\n"
        + "    <link href=\"http://www.torproject.org/favicon.ico\" "
          + "type=image/x-icon rel=\"shortcut icon\">\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div class=\"center\">\n"
        + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
          + "cellspacing=\"0\" summary=\"\">\n"
        + "        <tr>\n"
        + "          <td class=\"banner-left\"><a "
          + "href=\"https://www.torproject.org/\"><img "
          + "src=\"http://www.torproject.org/images/top-left.png\" "
          + "alt=\"Click to go to home page\" width=\"193\" "
          + "height=\"79\"></a></td>\n"
        + "          <td class=\"banner-middle\">\n"
        + "            <a href=\"/\">Home</a>\n"
        + "            <a href=\"graphs.html\">Graphs</a>\n"
        + "            <a href=\"research.html\">Research</a>\n"
        + "            <a href=\"status.html\">Status</a>\n"
        + "            <br/>\n"
        + "            <font size=\"2\">\n"
        + "              <a href=\"exonerator.html\">ExoneraTor</a>\n"
        + "              <a class=\"current\">Relay Search</a>\n"
        + "              <a href=\"consensus-health.html\">Consensus "
          + "Health</a>\n"
        + "              <a href=\"log.html\">Last Log</a>\n"
        + "            </font>\n"
        + "          </td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\" style=\"margin:5; "
          + "Padding:0;\">\n"
        + "        <h2>Relay Descriptor</h2>\n");
  }

  private void writeFooter(PrintWriter out) throws IOException {
    out.println("        <br/>\n"
        + "      </div>\n"
        + "    </div>\n"
        + "    <div class=\"bottom\" id=\"bottom\">\n"
        + "      <p>This material is supported in part by the National "
          + "Science Foundation under Grant No. CNS-0959138. Any "
          + "opinions, finding, and conclusions or recommendations "
          + "expressed in this material are those of the author(s) and "
          + "do not necessarily reflect the views of the National "
          + "Science Foundation.</p>\n"
        + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
          + "href=\"https://www.torproject.org/trademark-faq.html.en\">"
          + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
        + "      <p>Data on this site is freely available under a <a "
          + "href=\"http://creativecommons.org/publicdomain/zero/1.0/\">"
          + "CC0 no copyright declaration</a>: To the extent possible "
          + "under law, the Tor Project has waived all copyright and "
          + "related or neighboring rights in the data. Graphs are "
          + "licensed under a <a "
          + "href=\"http://creativecommons.org/licenses/by/3.0/us/\">"
          + "Creative Commons Attribution 3.0 United States "
          + "License</a>.</p>\n"
        + "    </div>\n"
        + "  </body>\n"
        + "</html>");
    out.close();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Measure how long it takes to process this request. */
    long started = System.currentTimeMillis();

    /* Get print writer and start writing response. */
    PrintWriter out = response.getWriter();
    writeHeader(out);

    /* Check if we have a database connection. */
    if (conn == null) {
      out.println("<br/><p><font color=\"red\"><b>Warning: </b></font>"
          + "This server doesn't have any relay descriptors available. "
          + "If this problem persists, please "
          + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
          + "know</a>!</p>\n");
      writeFooter(out);
      return;
    }

    /* Check desc-id parameter. */
    String descIdParameter = request.getParameter("desc-id");
    String descId = null;
    if (descIdParameter != null && descIdParameter.length() >= 8 &&
        descIdParameter.length() <= 40) {
      Pattern descIdPattern = Pattern.compile("^[0-9a-f]{8,40}$");
      if (descIdPattern.matcher(descIdParameter.toLowerCase()).
          matches()) {
        descId = descIdParameter.toLowerCase();
      }
    }
    if (descId == null) {
      out.write("    <br/><p>Sorry, \"" + descIdParameter + "\" is not a "
          + "valid descriptor identifier. Please provide at least the "
          + "first 8 hex characters of a descriptor identifier.</p>\n");
      writeFooter(out);
      return;
    }

    /* Look up descriptor in the database. */
    String descriptor = null, nickname = null, published = null,
        extrainfo = null;
    byte[] rawDescriptor = null, rawExtrainfo = null;
    try {
      Statement statement = conn.createStatement();
      String query = "SELECT descriptor, nickname, published, extrainfo, "
          + "rawdesc FROM descriptor WHERE descriptor LIKE '" + descId
          + "%'";
      ResultSet rs = statement.executeQuery(query);
      if (rs.next()) {
        descriptor = rs.getString(1);
        nickname = rs.getString(2);
        published = rs.getTimestamp(3).toString().substring(0, 19);
        extrainfo = rs.getString(4);
        rawDescriptor = rs.getBytes(5);
      }
      query = "SELECT rawdesc FROM extrainfo WHERE extrainfo = '"
          + extrainfo + "'";
      rs = statement.executeQuery(query);
      if (rs.next()) {
        rawExtrainfo = rs.getBytes(1);
      }
    } catch (SQLException e) {
      out.write("<br/><p><font color=\"red\"><b>Warning: </b></font>"
          + "Internal server error when looking up descriptor. If this "
          + "problem persists, please "
          + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
          + "know</a>!</p>\n");
      writeFooter(out);
      return;
    }

    /* If no descriptor was found, stop here. */
    if (descriptor == null) {
      out.write("<p>No descriptor found " + (descId.length() < 40
          ? "starting " : "") + "with identifier " + descId + ".</p>");
      writeFooter(out);
      return;
    }

    /* Print out both server and extra-info descriptor. */
    out.write("<br/><p>The following server descriptor was published by "
        + "relay " + nickname + " at " + published + " UTC:</p>");
    BufferedReader br = new BufferedReader(new StringReader(new String(
        rawDescriptor, "US-ASCII")));
    String line = null;
    while ((line = br.readLine()) != null) {
      out.println("        <tt>" + line + "</tt><br/>");
    }
    br.close();
    if (rawExtrainfo != null) {
      out.println("<br/><p>Together with this server descriptor, the "
          + "relay published the following extra-info descriptor:</p>");
      br = new BufferedReader(new StringReader(new String(rawExtrainfo,
          "US-ASCII")));
      line = null;
      while ((line = br.readLine()) != null) {
        out.println("        <tt>" + line + "</tt><br/>");
      }
    }

    /* Print out in which consensuses this descriptor is referenced. */
    try {
      Statement statement = conn.createStatement();
      String query = "SELECT validafter, rawdesc FROM statusentry "
          + "WHERE descriptor = '" + descriptor + "' ORDER BY validafter "
          + "DESC";
      ResultSet rs = statement.executeQuery(query);
      boolean printedDescription = false;
      while (rs.next()) {
        if (!printedDescription) {
          out.println("<br/><p>This server descriptor is referenced from "
              + "the following network status consensuses:</p>");
          printedDescription = true;
        }
        String validAfter = rs.getTimestamp(1).toString().
            substring(0, 19);
        out.println("        <br/><tt>valid-after "
            + "<a href=\"consensus?valid-after="
            + validAfter.replaceAll(":", "-").replaceAll(" ", "-")
            + "\" target=\"_blank\">" + validAfter + "</a></tt><br/>");
        byte[] rawStatusEntry = rs.getBytes(2);
        br = new BufferedReader(new StringReader(new String(
            rawStatusEntry, "US-ASCII")));
        line = null;
        while ((line = br.readLine()) != null) {
          out.println("        <tt>" + line + "</tt><br/>");
        }
      }
    } catch (SQLException e) {
      // TODO handle?
    }

    /* Provide links to raw descriptors, too. */
    out.println("<br/><p>Note that the descriptor" + (rawExtrainfo != null
        ? "s have" : " has") + " been converted to ASCII and reformatted "
        + "for display purposes. You may also download the raw "
        + "<a href=\"serverdesc?desc-id=" + descriptor
        + "\" target=\"_blank\">server " + "descriptor</a>"
        + (extrainfo != null ? " and <a href=\"extrainfodesc?desc-id="
        + extrainfo + "\" target=\"_blank\">extra-info descriptor</a>"
        : "") + " as " + (extrainfo != null ? "they were" : "it was")
        + " published to the directory authorities.</p>");

    /* Display total lookup time on the results page. */
    long searchTime = System.currentTimeMillis() - started;
    out.write("        <br/><p>Looking up this descriptor took us "
        + String.format("%d.%03d", searchTime / 1000, searchTime % 1000)
        + " seconds.</p>\n");

    /* Finish writing response. */
    writeFooter(out);
  }
}

