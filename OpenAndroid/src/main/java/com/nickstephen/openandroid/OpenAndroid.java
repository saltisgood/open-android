package com.nickstephen.openandroid;

import android.content.Context;

import org.holoeverywhere.app.Application;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * Extension to the HoloEverywhere Application class that keeps some variables in memory for the
 * life of the process.
 */
public class OpenAndroid extends Application {
    private final Globals mGlobals = new Globals();

    public DexFile getDex() {
        return mGlobals.mPackageDex;
    }

    public void setDex(DexFile dex) {
        mGlobals.mPackageDex = dex;
    }

    public Context getPackageContext() {
        return mGlobals.mPackageContext;
    }

    public void setPackageContext(Context c) {
        mGlobals.mPackageContext = c;
    }

    public String getLoadedPackageName() {
        return mGlobals.mPackageContext.getPackageName();
    }

    public Globals getGlobals() {
        return mGlobals;
    }

    public class Globals {
        private DexFile mPackageDex;
        private Context mPackageContext;

        private Map<String, Object> mObjectList = new HashMap<String, Object>();

        private Globals() {

        }

        public void addObject(String key, Object value) {
            if (checkNameIsUnique(key)) {
                mObjectList.put(key, value);
            } else {
                throw new RuntimeException("Variable name not unique");
            }
        }

        public boolean checkNameIsUnique(String key) {
            return !mObjectList.containsKey(key);
        }

        public int getObjectCount() {
            return mObjectList.size();
        }

        @Nullable
        public String getKeyAtPosition(int position) {
            Iterator<String> iter = mObjectList.keySet().iterator();
            int i = 0;
            while (iter.hasNext()) {
                if (i++ == position) {
                    return iter.next();
                }
                iter.next();
            }
            return null;
        }

        @Nullable
        public Object getObject(int position) {
            String k = getKeyAtPosition(position);
            if (k == null) {
                return null;
            }
            return mObjectList.get(k);
        }

        private Object getObject(String key) {
            return mObjectList.get(key);
        }

        public boolean removeObjectAtPosition(int position) {
            String key = getKeyAtPosition(position);
            return key != null && removeObjectWithKey(key);
        }

        public boolean removeObjectWithKey(String key) {
            return mObjectList.remove(key) != null; // TODO: Fix this if null values are allowed
            // Note that the return value from this method is the value being removed from the map.
            // If null values are allowed in the map then this method will return an incorrect value.
        }
    }
}
