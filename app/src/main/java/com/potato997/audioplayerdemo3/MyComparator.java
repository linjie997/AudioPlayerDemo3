package com.potato997.audioplayerdemo3;

import java.util.Comparator;

/**
 * Created by linjie on 15/10/2017.
 */

public class MyComparator implements Comparator<Music> {

    @Override
    public int compare(Music o1, Music o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}