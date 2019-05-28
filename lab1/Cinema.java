package com.ve572.l1;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Cinema {
    public Cinema(String path) {
        HashMap<String, Hall> myHalls = new HashMap<>();
        mvToHall = new HashMap<>();
        myOrders = new ArrayList<>();

        // the path is file dir, need to parse
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            System.err.println("invalid dir");
            System.exit(-1);
        }
        try {
            for (File f:listOfFiles) {
                if (f.isFile()) {
                    Hall newHall = new Hall(path + "/" + f.getName());
                    if (myHalls.containsKey(newHall.getHallName())) {
                        System.err.println("Duplicate hall names");
                        System.exit(0);
                    }
                    myHalls.put(newHall.getHallName(), newHall);
                    if (!mvToHall.containsKey(newHall.getMvName())) {
                        List<Hall> mapHalls = new ArrayList<>();
                        mvToHall.put(newHall.getMvName(), mapHalls);
                    }
                    mvToHall.get(newHall.getMvName()).add(newHall);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
        for (Entry<String, List<Hall>> entry :mvToHall.entrySet())
            entry.getValue().sort(compareByName);
    }

    public void serveQuery(String path) {
        File file = new File(path);
        try {	// read the query file
            Scanner inputStream = new Scanner(file);
            inputStream.useDelimiter(System.getProperty("line.separator"));

            String line;
            while(inputStream.hasNext()){	// splits and adds each parsed line
                line = inputStream.next();
                if (line == null)
                    break;
                line = line.replace("\n", "");
                String[] args = line.split(", ");
                Query tmp = new Query();
                tmp.myName = args[0];
                tmp.myMovie = args[1];
                findSeat(tmp, args[2]);
                myOrders.add(tmp);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void findSeat(Query q, String n) {
        n = n.trim();
        int num = Integer.parseInt(n);
        if (!mvToHall.containsKey(q.myMovie))
            return;

        for (Hall h: mvToHall.get(q.myMovie)) {
            if (h.maxLen >= num)
                if (h.setSeat(q, num)) {
                    q.myHall = h.getHallName();
                    break;
                }
        }
    }

    public void printOrders() {
        for (Query q: myOrders) {
            String info = q.myName + "," + q.myMovie;
            if (q.valid) {
                info = info + "," + q.myHall + "," + q.myRow + q.colToString();}
            System.out.println(info);
        }
    }

    Comparator<Hall> compareByName = Comparator.comparing(Hall::getHallName);

    private HashMap<String, List<Hall>> mvToHall;
    private List<Query> myOrders;
}

class Query {
    public Query() {
        myCols = new ArrayList<>();
        valid = false;
    }

    public String colToString() {
        String col = "";
        for (int i: myCols)
            col = col + "," + i;
        return col;
    }

    protected String myName;
    protected String myMovie;
    protected String myHall;
    protected int myRow;
    protected List<Integer> myCols;
    protected Boolean valid;
}
