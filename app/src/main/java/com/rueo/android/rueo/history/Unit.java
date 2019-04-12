package com.rueo.android.rueo.history;

public class Unit {
    public String word;
    public Unit left, right;

    public Unit(String input) {
        word = input;
        left = right = null;
    }

    @Override
    public String toString() {
        return word;
    }
}
