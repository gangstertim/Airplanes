package airplane.g0;

import java.util.Comparator;

import airplane.classes.LocationList;

public class FlightComparator implements Comparator<Integer>
{
    private final LocationList[] array;

    public FlightComparator(LocationList[] array)
    {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // Autoboxing
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
         // Autounbox from Integer to int to use as array indexes
        return array[index1].compareTo(array[index2]);
    }
}