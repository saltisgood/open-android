package com.nickstephen.openandroid.components;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.ListFragment;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.tasks.LazyDrawableLoader;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ListFragment for displaying the drawable resources in a package to the user
 */
public class DrawablePreviewListFrag extends ListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.DrawablePreviewListFrag";

    private LayoutInflater mInflater;
    private Class<?> mResourceClass;
    private Field[] mDrawableFields;
    private List<String> mResNames;
    private Map<String, Integer> mResValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String drawClassName = null;

        Bundle args = this.getArguments();
        if (args != null) {
            drawClassName = args.getString(Constants.CLASS_KEY);
        }
        if (drawClassName == null) {
            this.popFragment();
            Twig.warning(FRAG_TAG, "Null args passed to fragment");
            return;
        }

        mResNames = new ArrayList<String>();
        mResValues = new HashMap<String, Integer>();

        try {
            //noinspection ConstantConditions
            mResourceClass = ((OpenAndroid) this.getSupportApplication()).getPackageContext().getClassLoader().loadClass(drawClassName);
            mDrawableFields = mResourceClass.getDeclaredFields();

            for (Field f : mDrawableFields) {
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
        mInflater = inflater;

        this.setListAdapter(new DrawableAdapter());

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ImageView img = (ImageView) mInflater.inflate(R.layout.drawable_preview);

        try {
            img.setImageDrawable(((OpenAndroid) this.getSupportApplication()).getPackageContext().getResources().getDrawable(mResValues.get(mResNames.get(position))));
        } catch (Resources.NotFoundException e) {
            Twig.printStackTrace(e);
            Twig.warning(FRAG_TAG, "Drawable not found for preview!");
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
        dialog.setTitle(mResNames.get(position)).setView(img).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private class DrawableAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDrawableFields == null ? 0 : mDrawableFields.length;
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
            ImageView img;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_list_layout);
                img = (ImageView) convertView.findViewById(R.id.logo_img);
            } else {
                img = (ImageView) convertView.findViewById(R.id.logo_img);
                img.setVisibility(View.INVISIBLE);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.app_name_text);
            String resName = mResNames.get(position);
            txt.setText(resName);

            new LazyDrawableLoader(img, ((OpenAndroid) DrawablePreviewListFrag.this.getSupportApplication())
                    .getPackageContext().getResources()).execute(mResValues.get(resName));

            return convertView;
        }
    }
}
