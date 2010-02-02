import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

public class TorperfProcessor {
  public TorperfProcessor(String statsDirectory,
      String torperfDirectory) {
    Logger logger = Logger.getLogger(TorperfProcessor.class.getName());
    File rawFile = new File(statsDirectory + "/torperf-raw");
    File statsFile = new File(statsDirectory + "/torperf-stats");
    File torperfDir = new File(torperfDirectory);
    SortedMap<String, String> rawObs = new TreeMap<String, String>();
    SortedMap<String, String> stats = new TreeMap<String, String>();
    try {
      if (rawFile.exists()) {
        logger.info("Reading file " + statsDirectory + "/torperf-raw...");
        BufferedReader br = new BufferedReader(new FileReader(rawFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          String key = line.substring(0, line.lastIndexOf(","));
          rawObs.put(key, line);
        }
        br.close();
        logger.info("Finished reading file " + statsDirectory
            + "/torperf-raw.");
      }
      if (statsFile.exists()) {
        logger.info("Reading file " + statsDirectory
            + "/torperf-stats...");
        BufferedReader br = new BufferedReader(new FileReader(statsFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          String key = line.split(",")[0] + "," + line.split(",")[1];
          stats.put(key, line);
        }
        br.close();
        logger.info("Finished reading file " + statsDirectory
            + "/torperf-stats.");
      }
      if (torperfDir.exists()) {
        logger.info("Importing files in " + torperfDirectory + "/...");
        Stack<File> filesInInputDir = new Stack<File>();
        filesInInputDir.add(torperfDir);
        while (!filesInInputDir.isEmpty()) {
          File pop = filesInInputDir.pop();
          if (pop.isDirectory()) {
            for (File f : pop.listFiles()) {
              filesInInputDir.add(f);
            } 
          } else {
            String source = pop.getName().substring(0,
                pop.getName().indexOf("."));
            BufferedReader br = new BufferedReader(new FileReader(pop));
            String line = null;
            Format formatter = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
            while ((line = br.readLine()) != null) {
              String[] parts = line.split(" ");
              // remove defective lines as they occurred on gabelmoo as well
              // as and incomplete downloads 
              if (parts.length == 20 && parts[0].length() == 10
                  && !parts[16].equals("0")) {
                long startSec = Long.parseLong(parts[0]);
                String dateTime = formatter.format(
                    new Date(startSec * 1000L));
                long completeMillis = Long.parseLong(parts[16].substring(5))
                    * 1000L + Long.parseLong(parts[17]) / 1000L
                    - Long.parseLong(parts[0].substring(5)) * 1000L
                    + Long.parseLong(parts[1]) / 1000L;
                String key = source + "," + dateTime;
                String value = key + "," + completeMillis;
                if (!rawObs.containsKey(key)) {
                  rawObs.put(key, value);
                  // TODO if torperf-stats generation takes long, compile
                  // list of dates that have changes for torperf-stats and
                  // only re-generate those
                }
              }
            }
            br.close();
          }
        }
        logger.info("Finished importing files in " + torperfDirectory
            + "/.");
      }
      if (rawObs.size() > 0) {
        logger.info("Writing file " + statsDirectory + "/torperf-raw...");
        new File(statsDirectory).mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(rawFile));
        bw.append("source,date,start,completemillis\n");
        String tempSourceDate = null;
        Iterator<Map.Entry<String, String>> it =
            rawObs.entrySet().iterator();
        List<Long> dlTimes = new ArrayList<Long>();
        boolean haveWrittenFinalLine = false;
        while (it.hasNext() || !haveWrittenFinalLine) {
          Map.Entry<String, String> next = it.hasNext() ? it.next() : null;
          if (tempSourceDate != null
              && (next == null || !(next.getValue().split(",")[0] + ","
              + next.getValue().split(",")[1]).equals(tempSourceDate))) {
            if (dlTimes.size() > 4) {
              Collections.sort(dlTimes);
              long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
              long md = dlTimes.get(dlTimes.size() / 2 - 1);
              long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
              stats.put(tempSourceDate, tempSourceDate + "," + q1 + ","
                  + md + "," + q3);
            }
            dlTimes.clear();
            if (next == null) {
              haveWrittenFinalLine = true;
            }
          }
          if (next != null) {
            bw.append(next.getValue() + "\n");
            String[] parts = next.getValue().split(",");
            tempSourceDate = parts[0] + "," + parts[1];
            dlTimes.add(Long.parseLong(parts[3]));
          }
        }
        bw.close();
        logger.info("Finished writing file " + statsDirectory
            + "/torperf-raw.");
      }
      if (stats.size() > 0) {
        logger.info("Writing file " + statsDirectory
            + "/torperf-stats...");
        new File(statsDirectory).mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile));
        bw.append("source,date,q1,md,q3\n");
        // TODO should we handle missing days?
        for (String s : stats.values()) {
          bw.append(s + "\n");
        }
        bw.close();
        logger.info("Finished writing file " + statsDirectory
            + "/torperf-stats.");
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed writing " + statsDirectory
          + "/torperf-{raw|stats}!", e);
    }
  }
}

