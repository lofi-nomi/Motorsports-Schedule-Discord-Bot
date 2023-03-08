package digital.naomie.f1bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import digital.naomie.f1bot.utilities.FetchRaceData;
import digital.naomie.f1bot.commands.ScheduleCommand;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Bot extends ListenerAdapter {
        public final static String versionName = "Dubawnt";
        private static final Logger logger = LoggerFactory.getLogger(Bot.class);

        public static void main(String[] args) throws LoginException, InterruptedException {
                List<CommandData> globalCommandList = new ArrayList<CommandData>();

                String token = readToken();

                ScheduleCommand scheduleCommand = new ScheduleCommand();

                JDA jda = JDABuilder.createLight(token).build();
                jda.addEventListener(scheduleCommand);

                globalCommandList.add(new CommandData("race", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The F1 Round Number or F1 Race name, leave blank for next F1 race"));
                globalCommandList.add(new CommandData("f1", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The F1 Round Number or F1 Race name, leave blank for next F1 race"));
                globalCommandList.add(new CommandData("f2", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The F2 Round Number or F2 Race name, leave blank for next F2 race"));
                globalCommandList.add(new CommandData("f3", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The F3 Round Number or F3 Race name, leave blank for next F3 race"));
                globalCommandList.add(new CommandData("fe", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The FE Round Number or FE Race name, leave blank for next FE race"));
                globalCommandList.add(new CommandData("wseries", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The W Series Round Number or W Series Race name, leave blank for next W Series race"));
                globalCommandList.add(new CommandData("exe", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The Extreme E Round Number or Extreme E Race name, leave blank for next Extreme E race"));
                globalCommandList.add(new CommandData("indy", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The IndyCar Round Number or IndyCar Race name, leave blank for next IndyCar race"));
                globalCommandList.add(new CommandData("moto", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The MotoGP Round Number or MotoGP Race name, leave blank for next MotoGP race"));
                globalCommandList.add(new CommandData("f1a", "Display the session times for a given race")
                                .addOption(OptionType.STRING, "round",
                                                "The F1 Academy Round Number or F1 Academy Race name, leave blank for next F1 Academy race"));

                globalCommandList.add(new CommandData("version", "Displays debug information"));

                jda.awaitReady();
                jda.updateCommands().addCommands(globalCommandList).queue();
               logger.debug("F1 Bot Loaded");

        }

        private static String readToken() {
                String token = null;
                try {
                        token = Files.readString(Path.of("token.txt").toAbsolutePath());
                } catch (IOException e) {
                        logger.error("FATAL - Failed to load Token");
                        System.exit(2);
                }
                if (token == null) {
                        logger.error("FATAL - Failed to load Token");
                        System.exit(5);
                }
                return token;
        }
}
