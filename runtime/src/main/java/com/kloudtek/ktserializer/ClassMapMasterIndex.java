/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;

import java.util.ArrayList;
import java.util.List;

public class ClassMapMasterIndex {
    private final Class<? extends ClassMapList>[] mapsLists;

    public ClassMapMasterIndex(Class<? extends ClassMapList>... mapsLists) {
        this.mapsLists = mapsLists;
    }

    public List<String> mergeMaps() {
        try {
            ArrayList<String> masterList = new ArrayList<String>(100);
            for (Class<? extends ClassMapList> mapsList : mapsLists) {
                masterList.addAll(mapsList.newInstance().getClassNames());
            }
            return masterList;
        } catch (InstantiationException e) {
            throw new UnexpectedException(e);
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
    }
}
