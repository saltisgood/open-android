package com.nickstephen.openandroid.components;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.ListFragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ListFragment for viewing general resource values.
 */
public class SubResourceListFrag extends ListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.SubResourceListFrag";

    private List<String> mResNames;
    private Map<String, Integer> mResValues;
    private Class<?> mResourceClass;
    private Field[] mResourceFields;
    private LayoutInflater mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String className = null;

        Bundle args = this.getArguments();
        if (args != null) {
            className = args.getString(Constants.CLASS_KEY);
        }
        if (className == null) {
            this.popFragment();
            Twig.warning(FRAG_TAG, "Null args passed to fragment");
            return;
        }

        mResNames = new ArrayList<String>();
        mResValues = new HashMap<String, Integer>();

        try {
            //noinspection ConstantConditions
            mResourceClass = ((OpenAndroid) this.getSupportApplication()).getPackageContext().getClassLoader().loadClass(className);
            mResourceFields = mResourceClass.getDeclaredFields();

            for (Field f : mResourceFields) {
                int resId = f.getInt(null);
                mResNames.add(f.getName());
                mResValues.put(f.getName(), resId);
            }
        } catch (ClassNotFoundException e) {
            Twig.printStackTrace(e);
            Twig.warning(FRAG_TAG, "Error loading class");
            this.popFragment();
        } catch (IllegalAccessException e) {
            Twig.printStackTrace(e);
            Twig.warning(FRAG_TAG, "Error getting value");
            this.popFragment();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.isExiting()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        mInflater = inflater;

        this.setListAdapter(new DrawableAdapter());

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        try {
            StatMethods.hotBread(this.getActivity(),
                    ((OpenAndroid) SubResourceListFrag.this.getSupportApplication()).getPackageContext()
                            .getResources().getString(mResValues.get(mResNames.get(position))),
                    Toast.LENGTH_SHORT);
        } catch (Resources.NotFoundException e) {
            StatMethods.hotBread(this.getActivity(), "Resource not found", Toast.LENGTH_SHORT);
        }
    }

    private class DrawableAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mResourceFields == null ? 0 : mResourceFields.length;
        }

        @Override
        public Object getItem(int position) {
            return mResNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.subres_list_item);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.res_name_text);
            String resName = mResNames.get(position);
            txt.setText(resName);

            txt = (TextView) convertView.findViewById(R.id.res_value_text);
            try {
                txt.setText(((OpenAndroid) SubResourceListFrag.this.getSupportApplication()).getPackageContext().getResources().getString(mResValues.get(resName)));
            } catch (Resources.NotFoundException e) {
                txt.setText("**String not found**");
            }

            return convertView;
        }
    }
}
