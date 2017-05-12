package com.example.android.rueo.history;

import java.util.ArrayList;

/**
 * Created by RollerBlade on 12.05.2017.
 */

public class Stack
{
    ArrayList<Unit> arr;
    Integer pointer;

    public Stack ()
    {
        arr = new ArrayList<>();
        pointer = null;
    }

    public void push (String input)
    {
        Unit temp = new Unit(input);
        arr.add(temp);
        if (pointer != null)
        {
            arr.get(pointer).right = temp;
            temp.left = arr.get(pointer);
        }
        pointer = arr.size() - 1;
    }

    public String getLeft ()
    {
        if (pointer == null)
        {
            return null;
        }
        else
        {
            if (arr.get(pointer).left == null)
            {
                return null;
            }
            else
            {
                pointer = arr.indexOf(arr.get(pointer).left);
                return arr.get(pointer).toString();
            }
        }
    }

    public String getRight ()
    {
        if (pointer == null)
        {
            return null;
        }
        else
        {
            if (arr.get(pointer).right == null)
            {
                return null;
            }
            else
            {
                pointer = arr.indexOf(arr.get(pointer).right);
                return arr.get(pointer).toString();
            }
        }
    }

    public String[] getStringArray ()
    {
        String[] out = new String[arr.size()];
        for (int i=0; i<arr.size(); i++)
        {
            out[i] = arr.get(i).toString();
        }
        return out;
    }

}
