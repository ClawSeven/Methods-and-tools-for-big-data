package com.ve572.l1;

import java.io.*;
import java.util.*;

public class Hall {
    public Hall(String fileName) {
        File file= new File(fileName);

        mySeats = new ArrayList<>();
        numEmpty = new ArrayList<>();

        try {	// read the configuration file
            Scanner inputStream = new Scanner(file);
            inputStream.useDelimiter(System.getProperty("line.separator"));
            hallName = inputStream.next();
            mvName = inputStream.next();
            String line;

            while(inputStream.hasNext()){	// splits and adds each parsed line
                line = inputStream.next();
                if (line == null)
                    break;
                // line = line.replace("\n", "");
                line = line.trim();
                String[] row = line.split(" ");
                mySeats.add(new ArrayList<>());
                numEmpty.add(line.length());
                for (String i:row)
                    mySeats.get(mySeats.size() - 1).add(i.equals("1"));
                numRows++;
                maxLen = row.length;
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        centerLine = (maxLen + 1) / 2;
    }

    public String getHallName() {
        return hallName;
    }

    public String getMvName() {
        return mvName;
    }

    public Boolean setSeat(Query q, int num) {
        if ((double)num > maxLen)
            return q.valid = false;

        TreeSet<Center> myStore = new TreeSet<>(compareSeat);
        double center;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < maxLen; col++) {
                if (available(row, col, num)) {
                    center = (double)(num+1)/2 + col;
                    int d_y = (int)numRows - 1 - row;
                    d_y *= d_y;
                    double d = Math.abs(center - centerLine);
                    d *= d;
                    d += (double)d_y;
                    myStore.add(new Center(row, col, d));
                    if (q.myName.equals("Brinda Sedore")) {
                    }
                }
            }
        }
        if (myStore.isEmpty())
            return q.valid = false;
        Center tmp = myStore.first();
        for (int x = tmp.c; x < tmp.c+num; x++) {
            mySeats.get(tmp.r).set(x, false);
            q.myCols.add(x+1);
        }
        q.myRow = tmp.r + 1;
        return q.valid = true;
    }

    // compare
    Comparator<Center> compareSeat = new Comparator<Center>() {
        @Override
        public int compare(Center s1, Center s2) {
            if (Double.compare(s1.d, s2.d) != 0)
                return Double.compare(s1.d, s2.d);
            else if (s1.r != s2.r)
                return s2.r - s1.r;
            else return s1.c - s2.c; // only defers in x
        }
    };

    private boolean available(int row, int start, int num) {
        if (start + num > maxLen)
            return false;
        for (int i = start; i < start + num; i++) {
            if (!mySeats.get(row).get(i))
                return false;
        }
        return true;
    }

    private List<List<Boolean>> mySeats;
    private List<Integer> numEmpty; // record num of empty seats in each row
    private String hallName;
    private String mvName;
    private double centerLine;
    private int numRows = 0;
    protected double maxLen = 0;
}

class Center {
    public Center(int x, int y, double dis) {
        r = x;
        c = y;
        d = dis;
    }
    public int r;
    public int c;
    public double d;
}
