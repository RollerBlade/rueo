package com.rueo.android.rueo.history;


import java.util.ArrayList;
import java.util.List;

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

    public void clear ()
    {
        arr.clear();
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
            for (int i = pointer+1; i < arr.size()-1; i++)
            {
                arr.get(i).left = null;
                arr.get(i).right = null;
            }
            checkForDoubles(input);
        }
        pointer = arr.size() - 1;
    }

    public void detete (int n)
    {
        if (arr.get(n).left != null)
        {
            arr.get(n).left.right = arr.get(n).right;
        }
        if (arr.get(n).right != null)
        {
            arr.get(n).right.left = arr.get(n).left;
        }
        arr.remove(n);
    }

    public void checkForDoubles (String input)
    {
        int duplicate = -1;
        for (int i = 0; i < arr.size()-1; i++)
        {
            if (arr.get(i).word.equals(input))
            {
                duplicate = i;
            }
        }
        if (duplicate > -1)
        {
            detete(duplicate);
        }
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

    public List<String> getListArray()
    {
        List<String> out = new ArrayList();
        for (int i=0; i<arr.size(); i++)
        {
            out.add(arr.get(i).toString());
        }
        return out;
    }
}
