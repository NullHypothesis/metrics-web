<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Data</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Data</h2>
        <br>
        <p><font color="red"><b>Notice:</b> The files linked on this site
        are now available on:
        <a href="https://collector.torproject.org/">https://collector.torproject.org/</a>.
        This page will be automatically redirected there after August 3,
        2014.</font>
        </p>
        <p>One of the main goals of the Tor Metrics Project is to make all
        gathered data available to the public. This approach enables
        privacy researchers to perform their own analyses using real data
        on the Tor network, and it acts as a safeguard to not gather data
        that are too sensitive to publish. The following data are
        available (see the <a href="tools.html">Tools</a> section for
        details on processing the files):</p>
        <ul>
          <li><a href="#relaydesc">Relay descriptor archives</a></li>
          <li><a href="#bridgedesc">Bridge descriptor archives</a></li>
          <li><a href="#bridgeassignments">Bridge pool assignments</a></li>
          <li><a href="#performance">Performance data</a></li>
          <li><a href="#exitlist">Exit lists</a></li>
        </ul>
        <p>The tarballs listed on this page and the raw files that were
        published on the last three days are also available via
        "rsync metrics.torproject.org::".</p>
        <p>Note that most descriptors compress really well. You should
        expect tarballs to decompress to 20 times the compressed size or
        even more.</p>
        <br>
        <a name="relaydesc"></a>
        <h3><a href="#relaydesc" class="anchor">Relay descriptor
        archives</a></h3>
        <br>
        <p>The relay descriptor archives contain all documents that the
        directory authorities make available about the network of relays.
        These documents include network statuses, server (relay)
        descriptors, and extra-info descriptors.
        The data formats are described <a href="formats.html">here</a>.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${relayDescriptors}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>${longDate}</td>
              <td>
                <c:if test="${item.value['tor'] ne null}" >
                  <a href="${item.value['tor'][0]}">v1 directories</a>
                  <c:if test="${item.value['tor'][1] ne null}">
                    (<a href="${item.value['tor'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['statuses'] ne null}" >
                  <a href="${item.value['statuses'][0]}">v2 statuses</a>
                  <c:if test="${item.value['statuses'][1] ne null}">
                    (<a href="${item.value['statuses'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['server-descriptors'] ne null}" >
                  <a href="${item.value['server-descriptors'][0]}">server descriptors</a>
                  <c:if test="${item.value['server-descriptors'][1] ne null}">
                    (<a href="${item.value['server-descriptors'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['extra-infos'] ne null}" >
                  <a href="${item.value['extra-infos'][0]}">extra-infos</a>
                  <c:if test="${item.value['extra-infos'][1] ne null}">
                    (<a href="${item.value['extra-infos'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['votes'] ne null}" >
                  <a href="${item.value['votes'][0]}">v3 votes</a>
                  <c:if test="${item.value['votes'][1] ne null}">
                    (<a href="${item.value['votes'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['consensuses'] ne null}" >
                  <a href="${item.value['consensuses'][0]}">v3 consensuses</a>
                  <c:if test="${item.value['consensuses'][1] ne null}">
                    (<a href="${item.value['consensuses'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['microdescs'] ne null}" >
                  <a href="${item.value['microdescs'][0]}">microdescriptors</a>
                  <c:if test="${item.value['microdescs'][1] ne null}">
                    (<a href="${item.value['microdescs'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <c:if test="${certs[0] ne null}">
          <br>
          <p>In order to verify the v3 votes and v3 consensuses, download
          the tarball of <a href="${certs[0]}">v3 certificates</a>
          <c:if test="${certs[1] ne null}">
            (<a href="${certs[1]}">sig</a>)
          </c:if>
          which is updated whenever new v3 certificates become available.</p>
        </c:if>
        <c:if test="${relayStatistics[0] ne null}">
          <br>
          <p>Some of the relays are configured to gather statistics on the
          number of requests or connecting clients, the number of
          processed cells per queue, or the number of exiting bytes per
          port. Relays running version 0.2.2.4-alpha or higher can include
          these statistics in extra-info descriptors, so that they are
          included in the relay descriptor archives. This
          <a href="${relayStatistics[0]}">archive</a>
          <c:if test="${relayStatistics[1] ne null}">
            (<a href="${relayStatistics[1]}">sig</a>)
          </c:if>
          contains the statistics produced by relays running earlier
          versions.</p>
        </c:if>
        <br>
        <a name="bridgedesc"></a>
        <h3><a href="#bridgedesc" class="anchor">Bridge descriptor
        archives</a></h3>
        <br>
        <p>The bridge descriptor archives contain similar documents as the
        relay descriptor archives, but for the non-public bridges. The
        descriptors have been sanitized before publication to remove all
        information that could otherwise be used to locate bridges. The
        files below contain all documents of a given month, including
        bridge network statuses, bridge server descriptors, and bridge
        extra-info descriptors. The sanitizing process is described
        <a href="formats.html#bridgedesc">here</a>.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${bridgeDescriptors}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
                <c:if test="${item.value[1] ne null}">
                    (<a href="${item.value[1]}">sig</a>)
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <p></p>
        <br>
        <a name="bridgeassignments"></a>
        <h3><a href="#bridgeassignments" class="anchor">Bridge pool
        assignments</a></h3>
        <br>
        <p>BridgeDB periodically dumps the list of running bridges with
        information about the rings, subrings, and file buckets to which
        they are assigned to a local file.  We are archiving sanitized
        versions of these files here to analyze how the pool assignment
        affects a bridge's usage.</p>
        The data format and sanitizing process is described
        <a href="formats.html#bridgepool">here</a>.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${bridgePoolAssignments}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <a name="performance"></a>
        <h3><a href="#performance" class="anchor">Performance
        data</a></h3>
        <br>
        <p>We are continuously measuring the performance of the Tor
        network by periodically requesting files of different sizes and
        recording the time needed to do so. These measurements take place
        on moria, siv, and torperf and use an unmodified Tor client.
        The files below contain the output of the torperf application.
        The data format is described
        <a href="formats.html#torperf">here</a>.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${torperfTarballs}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <p>The output above is the result of combining torperf request
        data with information about used paths.
        The raw files are also available below.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${torperfData}" >
            <tr>
              <td>${item.key}</td>
              <td>
                <c:if test="${item.value['50kb'] ne null}" >
                  <c:if test="${item.value['50kb'][0] ne null}" >
                    <a href="${item.value['50kb'][0]}">50 KiB requests</a>
                  </c:if>
                  <c:if test="${item.value['50kb'][1] ne null}" >
                    <a href="${item.value['50kb'][1]}">50 KiB path info</a>
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['1mb'] ne null}" >
                  <c:if test="${item.value['1mb'][0] ne null}" >
                    <a href="${item.value['1mb'][0]}">1 MiB requests</a>
                  </c:if>
                  <c:if test="${item.value['1mb'][1] ne null}" >
                    <a href="${item.value['1mb'][1]}">1 MiB path info</a>
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['5mb'] ne null}" >
                  <c:if test="${item.value['5mb'][0] ne null}" >
                    <a href="${item.value['5mb'][0]}">5 MiB requests</a>
                  </c:if>
                  <c:if test="${item.value['5mb'][1] ne null}" >
                    <a href="${item.value['5mb'][1]}">5 MiB path info</a>
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <p>We further conducted additional experiments with Torperf in the
        past by modifying the guard node selection strategies or circuit
        build timeouts.  The modified guard node selection strategies are
        to pick guard nodes from sets of the a) absolute fastest, b)
        absolute slowest, c) best rated vs. advertised ratio or d) worst
        rated vs. advertised ratio nodes. The ratio mechanisms provide a
        way to select the nodes that the bandwidth authorities think stand
        out in their measurement.  Experiments are listed by the date when
        they ended.  Details about the experiment setup are contained in a
        README file in the tarballs.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${torperfExperiments}" >
            <fmt:formatDate var="endDate" pattern="MMMM dd, yyyy"
                value="${item.key}"/>
            <tr><td><a href="${item.value[0]}">${endDate}</a></td></tr>
          </c:forEach>
        </table>
        <br>
        <a name="exitlist"></a>
        <h3><a href="#exitlist" class="anchor">Exit lists</a></h3>
        <br>
        <p>We are archiving the bulk exit lists used by
        <a href="https://check.torproject.org/">Tor Check</a>
        containing the IP addresses that exit relays exit from.
        The data format is described in
        <a href="https://www.torproject.org/tordnsel/exitlist-spec.txt">exitlist-spec.txt</a>
        and <a href="formats.html#exitlist">here</a>.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${exitLists}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
              </td>
            </tr>
          </c:forEach>
        </table>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>