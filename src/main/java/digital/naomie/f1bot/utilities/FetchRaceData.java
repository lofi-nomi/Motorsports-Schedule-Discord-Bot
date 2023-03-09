package digital.naomie.f1bot.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchRaceData {
    private static final Logger logger = LoggerFactory.getLogger(FetchRaceData.class);
    private static Map<String, LocalDateTime> lastFetchMap = new HashMap<>();
    private static Map<String, JSONArray> seriesMap = new HashMap<>();
    public static JSONArray getRace(String series, String seriesUrl, int currentYear) throws IOException, ParseException {
        logger.debug(seriesUrl);
        if (lastFetchMap.containsKey(series) && Duration.between(lastFetchMap.get(series), LocalDateTime.now()).toDays() < 1) {
            return seriesMap.get(series);
        }
        logger.debug("Fetching new data");
        String racesUrl = String.format(seriesUrl+"/api/year/%d ", currentYear);
        URL url = new URL(racesUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to fetch races JSON: HTTP error code " + conn.getResponseCode());
        }
        logger.debug("Fetched new data");
        try (InputStream inputStream = conn.getInputStream()) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            seriesMap.put(series, (JSONArray) jsonObject.get("races"));
            lastFetchMap.put(series,LocalDateTime.now());
            logger.debug("Returning new data");
            return (JSONArray) jsonObject.get("races");
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        return null;
    }
}
