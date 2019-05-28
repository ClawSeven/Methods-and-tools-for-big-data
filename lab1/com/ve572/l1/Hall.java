package com.ve572.l1;

import java.io.*;
import java.util.*;

public class Hall {
    public Hall(String fileName) {
        File file= new File(fileName);

        mySeats = new ArrayList<>();
        numEmpty = new ArrayList<>();
        myBound = new ArrayList<>();

        try {	// read the configuration file
            Scanner inputStream = new Scanner(file);
            inputStream.useDelimiter(System.getProperty("line.separator"));
            hallName = inputStream.next();  // System.out.println(hallName);
            mvName = inputStream.next();  // System.out.println(mvName);
            String line;

            while(inputStream.hasNext()){	// splits and adds each parsed line
                line = inputStream.next();
                if (line == null)
                    break;
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
        for (int i = 0; i < numRows; i++) {
            Bound b = new Bound();
            b.l = (int) centerLine;
            b.r = b.l + 1;
//            b.r = b.l + adds;
            b.row = i + 1;
            myBound.add(b);
        }
    }

    public String getHallName() {
        return hallName;
    }

    public String getMvName() {
        return mvName;
    }

    public Boolean setSeat(Query q, int num) {
        if (num > maxLen)
            return q.valid = false;

        TreeSet<Center> myStore = new TreeSet<>(compareSeat);
        for (Bound b: myBound) {
            if (b.l < 1 && b.r > (int) maxLen)
                continue;
            if (Math.abs(b.l - centerLine) < 1 && mySeats.get(b.row-1).get(b.l-1)) {
                Center tmp = new Center(b.row, b.l- (num-1)/2, false);
                tmp.dx = 0;
                tmp.dy = numRows - b.row;
                myStore.add(tmp);
            } else { // the line is not empty
                if (b.l>0 && mySeats.get(b.row-1).get(b.l-1) && b.l >= num) {
                    Center tmp = new Center(b.row, b.l, false);
                    tmp.dx = centerLine - (float)b.l + (float)(num-1)/2;
                    tmp.dy = numRows - b.row;
                    myStore.add(tmp);
                }
                if (b.r < maxLen && mySeats.get(b.row-1).get(b.r-1) && maxLen - b.r >= num-1) {
                    Center tmp = new Center(b.row, b.r, true);
                    tmp.dx = (float)b.r - centerLine  + (float)(num-1)/2;
                    tmp.dy = numRows - b.row;
                    myStore.add(tmp);
                }
            }
        }
        if (myStore.isEmpty())
            return q.valid = false;
        else {
            Center tmp = myStore.first();
            if (tmp.dx == 0) { //System.out.println(tmp.c);
                if (maxLen %2 != 0 && num%2 == 0)
                    tmp.c -= 1;
                for (int i = tmp.c - 1; i < tmp.c + num - 1; i++) {
                    mySeats.get(tmp.r - 1).set(i, false);
                    q.myCols.add(i+1);
                }
                myBound.get(tmp.r-1).l = tmp.c - 1;
                myBound.get(tmp.r-1).r = tmp.c + num;
            } else if (tmp.dir) {
                // if right
                for (int i = tmp.c - 1; i < tmp.c - 1 + num; i++) {
                    mySeats.get(tmp.r - 1).set(i, false);
                    q.myCols.add(i+1);
                }
                myBound.get(tmp.r-1).r += num;
            } else {
                for (int i = tmp.c - num; i < tmp.c; i++) {
                    mySeats.get(tmp.r - 1).set(i, false);
                    q.myCols.add(i+1);
                }
                myBound.get(tmp.r-1).l -= num;
            }
            q.myRow = tmp.r;
        }
        return q.valid = true;
    }

    // compare
    Comparator<Center> compareSeat = new Comparator<Center>() {
        @Override
        public int compare(Center s1, Center s2) {
            if (((int)s1.dx + s1.dy) != ((int)s2.dx + s2.dy))
                return Math.round(((int)s1.dx + s1.dy) - ((int)s2.dx + s2.dy));
            else if (s1.dy != s2.dy)
                return s1.dy - s2.dy;
            else return (int)s1.dx - (int)s2.dx; // only defers in x
        }
    };

    private List<List<Boolean>> mySeats;
    private List<Integer> numEmpty; // record num of empty seats in each row
    private String hallName;
    private String mvName;
    private float centerLine;
    private int numRows = 0;
    protected float maxLen = 0;
    private List<Bound> myBound;
}

class Bound {
    public int l;
    public int r;
    public int row;
}

class Center {
    public Center(int x, int y, boolean right) {
        r = x;
        c = y;
        dir = right; // left = 0; right = 1;
    }
    public int r;
    public int c;
    public float dx;
    public int dy;
    public boolean dir;
}
