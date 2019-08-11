package com.ve572.l1;

import org.apache.commons.cli.*;

public class Main {
    static public void main(String[] args) {

        ParseArg paths = new ParseArg(args);

        try {
            Cinema test = new Cinema(paths.getMyHallPath());
            test.serveQuery(paths.getMyQueries());
            test.printOrders();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }

}

class ParseArg {
    public ParseArg(String[] args) {
        // runs the program
        Options options = new Options();

        Option help = new Option("h", "help", false, "print this message");
        help.setRequired(false);
        options.addOption(help);

        Option hall = new Option(null, "hall", true, "path of the hall config directory");
        hall.setRequired(true);
        options.addOption(hall);

        Option query = new Option(null, "query", true, "query of customer orders");
        query.setRequired(true);
        options.addOption(query);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                formatter.printHelp("cinema", options);
                System.exit(0);
            }

            myHallPath = cmd.getOptionValue("hall");
            myQueries = cmd.getOptionValue("query");

        } catch (ParseException e) {
            formatter.printHelp("cinema", options);
            System.exit(0);
        }
    }

    public String getMyHallPath() {
        return myHallPath;
    }

    public String getMyQueries() {
        return myQueries;
    }

    private String myHallPath;
    private String myQueries;
}
