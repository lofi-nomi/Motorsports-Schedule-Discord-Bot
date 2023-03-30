package digital.naomie.f1bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import digital.naomie.f1bot.utilities.FetchRaceData;
import digital.naomie.f1bot.utilities.Series;

import static digital.naomie.f1bot.utilities.CommonUtilities.isNumeric;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.awt.Color;


public class ScheduleCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommand.class);
    private static final Map<String, Series> seriesMap = new HashMap<>();

    public ScheduleCommand() {
        seriesMap.put("f1", new Series("https://f1calendar.com", "Formula 1", "0xee0000", "f1.png"));
        seriesMap.put("race", new Series("https://f1calendar.com", "Formula 1", "0xee0000", "f1.png"));
        seriesMap.put("f2", new Series("https://f2cal.com", "Formula 2", "0x0090d0", "f2.png"));
        seriesMap.put("f3", new Series("https://f3calendar.com", "Formula 3", "0xe90300", "f3.png"));
        seriesMap.put("fe", new Series("https://formulaecal.com", "Formula E", "0x14b7ed", "fe.png"));
        seriesMap.put("wseries", new Series("https://wseriescal.com", "W Series", "0x451b94", "wseries.png"));
        seriesMap.put("exe", new Series("https://extremeecalendar.com", "Extreme E", "0x00ff9c", "exe.png"));
        seriesMap.put("indy", new Series("https://indycarcalendar.com", "IndyCar", "0xe2221c", "indy.png"));
        seriesMap.put("moto", new Series("https://motogpcal.com", "MotoGP", "0xd90042", "moto.png"));
        seriesMap.put("f1a", new Series("https://f1academycalendar.com", "F1 Academy", "0x9e2d99", "f1a.png"));
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (seriesMap.containsKey(event.getName())) {
            logger.debug("Schedule command %s".formatted(event.getName()));
            event.deferReply().queue();
            processCommand(event);
        }
    }
    public void processCommand(SlashCommandEvent event) {
        JSONArray seriesArray;
        try {
            seriesArray = FetchRaceData.getRace(event.getName(), seriesMap.get(event.getName()).getUrl(), LocalDate.now().getYear());
            logger.debug("Series array: %s".formatted(seriesArray.toString()));
            calculateRound(event,seriesArray, event.getOptions().size() > 0 ? event.getOptions().get(0).getAsString() : "current");

        } catch (IOException e) {
            logger.error(e.toString());
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error(e.toString());
            e.printStackTrace();

        }
    }

    @SuppressWarnings("unchecked")
    public void calculateRound(SlashCommandEvent event, JSONArray series, String optionString){
        if (optionString.toLowerCase().equals("current")) {
            logger.debug("Current race");
            Instant currentInstant = Instant.now();
            long closest = Long.MAX_VALUE;
            JSONObject current = null;
            for (Object raceObject : series) {
                JSONObject raceJSONObject = (JSONObject) raceObject;
                JSONObject session = (JSONObject) raceJSONObject.get("sessions");
                Iterator<String> keys = session.keySet().iterator();
                while(keys.hasNext()){
                    String key = keys.next();
                    String value = session.get(key).toString();
                    Instant sessionInstant = Instant.parse(value);
                    if (sessionInstant.isAfter(currentInstant) && sessionInstant.toEpochMilli() - currentInstant.toEpochMilli() < closest) {
                        closest = sessionInstant.toEpochMilli() - currentInstant.toEpochMilli();
                        current = raceJSONObject;
                    }
                }
            }
            scheduleReply(event, current, series.size());
            return;
        }
        int roundNumber = -99;
        if (isNumeric(optionString)) {
            try {
                roundNumber = Integer.parseInt(optionString);
            } catch (NumberFormatException e) {
                logger.error(e.toString());
                event.getHook().sendMessage(optionString + " is not a valid round number").queue();
            }
        }
        JSONObject raceJSONObject = null;
        for (Object raceObject : series) {
            raceJSONObject = (JSONObject) raceObject;
            if (roundNumber == -99) {
                if (raceJSONObject.containsKey("name") || raceJSONObject.containsKey("location")) {
                    if (raceJSONObject.get("name").toString().toLowerCase().contains(optionString.toLowerCase())
                            || raceJSONObject.get("location").toString().toLowerCase()
                                    .contains(optionString.toLowerCase())) {
                        scheduleReply(event, raceJSONObject, series.size());
                        return;
                    }
                }
            } else {
                if (raceJSONObject.containsKey("round")) {
                    if (Integer.parseInt(raceJSONObject.get("round").toString()) == roundNumber) {
                        scheduleReply(event, raceJSONObject, series.size());
                        return;
                    }
                }
            }
        }
        event.getHook().sendMessage(String.format("%s is not a valid round selection", optionString)).queue();
    }

    public static void scheduleReply(SlashCommandEvent event, JSONObject raceJSONObject, int totalRaces) {
        EmbedBuilder replyBuilder = new EmbedBuilder();
        totalRaces++;
        JSONObject session = (JSONObject) raceJSONObject.get("sessions");
        logger.debug("Schedule Reply Method");
        if (raceJSONObject.containsKey("sessions")) {
            replyBuilder.setTitle(String.format("**%s %s Grand Prix**", seriesMap.get(event.getName()).getName(), raceJSONObject.get("name")));
            replyBuilder.addField("Location", raceJSONObject.get("location").toString(), true);
            File seriesLogo = new File("images/" + seriesMap.get(event.getName()).getLogo());
            replyBuilder.setThumbnail("attachment://" + seriesMap.get(event.getName()).getLogo());
            replyBuilder.setColor(Color.decode(seriesMap.get(event.getName()).getColor()));
            List<Object> sessionList = new ArrayList<>(session.keySet());

            Collections.sort(sessionList, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    Instant sessionDate1 = Instant.parse((CharSequence) session.get(o1.toString()));
                    Instant sessionDate2 = Instant.parse((CharSequence) session.get(o2.toString()));
                    return sessionDate1.compareTo(sessionDate2);
                }
            });            
            for (Object sessionObject : sessionList) {
                String sessionName = sessionObject.toString();
                Instant sessionDate = Instant.parse((CharSequence) session.get(sessionName));
                String sessionTimestamp = String.format("<t:%d:F>", sessionDate.toEpochMilli() / 1000);
                String sessionRelative = String.format("<t:%d:R>", sessionDate.toEpochMilli() / 1000);
                replyBuilder.addField(sessionName.toUpperCase(), String.format("%s (%s)", sessionTimestamp, sessionRelative),
                        false);
            }

            int roundInteger = Integer.parseInt(raceJSONObject.get("round").toString());
            switch (roundInteger % 20) {
                case 1 ->
                    replyBuilder.setFooter(String.format("This is the %dst Grand Prix of %d total this season", roundInteger, totalRaces));
                case 2 ->
                    replyBuilder.setFooter(String.format("This is the %dnd Grand Prix of %d total this season", roundInteger, totalRaces));
                case 3 ->
                    replyBuilder.setFooter(String.format("This is the %drd Grand Prix of %d total this season", roundInteger, totalRaces));
                default ->
                    replyBuilder.setFooter(String.format("This is the %dth Grand Prix of %d total this season", roundInteger, totalRaces));
            }
            event.getHook().sendMessageEmbeds(replyBuilder.build()).addFile(seriesLogo, seriesMap.get(event.getName()).getLogo()).queue();
            return;
        }
    }
}
