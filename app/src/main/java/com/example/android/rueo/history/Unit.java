package com.example.android.rueo.history;

/**
 * Created by RollerBlade on 12.05.2017.
 */

public class Unit
{
    public String word;
    public Unit left, right;

    public Unit(String input)
    {
        word = input;
        left = right = null;
    }

    @Override
    public String toString()
    {
        return word;
    }
}
