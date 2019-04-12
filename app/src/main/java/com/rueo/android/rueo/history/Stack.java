package com.rueo.android.rueo.history;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    private ArrayList<HistoryElement> arr;
    private Integer pointer;

    public Stack() {
        arr = new ArrayList<>();
        pointer = null;
    }

    public void clear() {
        arr.clear();
        pointer = null;
    }

    public void push(String input) {
        HistoryElement temp = new HistoryElement(input);
        arr.add(temp);
        if (pointer != null) {

            arr.get(pointer).setRight(temp);
            temp.setLeft(arr.get(pointer));
            for (int i = pointer + 1; i < arr.size() - 1; i++) {
                arr.get(i).setLeft(null);
                arr.get(i).setRight(null);
            }
            checkForDoubles(input);
        }
        pointer = arr.size() - 1;
    }

    private void delete(int n) {
        if (arr.get(n).getLeft() != null) {
            arr.get(n).getLeft().setRight(arr.get(n).getRight());
        }
        if (arr.get(n).getRight() != null) {
            arr.get(n).setLeft(arr.get(n).getLeft());
        }
        arr.remove(n);
    }

    private void checkForDoubles(String input) {
        int duplicate = -1;
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i).getWord().equals(input)) {
                duplicate = i;
            }
        }
        if (duplicate > -1) {
            delete(duplicate);
        }
    }

    public String getLeft() {
        if (pointer == null) {
            return null;
        } else {
            if (arr.get(pointer).getLeft() == null) {
                return null;
            } else {
                pointer = arr.indexOf(arr.get(pointer).getLeft());
                return arr.get(pointer).toString();
            }
        }
    }

    public String getRight() {
        if (pointer == null) {
            return null;
        } else {
            if (arr.get(pointer).getRight() == null) {
                return null;
            } else {
                pointer = arr.indexOf(arr.get(pointer).getRight());
                return arr.get(pointer).toString();
            }
        }
    }

    public String[] getStringArray() {
        String[] out = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            out[i] = arr.get(i).toString();
        }
        return out;
    }

    public List<String> getListArray() {
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            out.add(arr.get(i).toString());
        }
        return out;
    }
}
