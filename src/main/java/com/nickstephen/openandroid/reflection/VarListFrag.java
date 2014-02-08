package com.nickstephen.openandroid.reflection;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.lib.gui.ListFragment;
import com.nickstephen.openandroid.OpenAndroid;

import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import java.lang.reflect.Array;

/**
 * Created by Nick on 14/01/14.
 */
public class VarListFrag extends ListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.reflection.VarListFrag";

    private OpenAndroid.Globals mGlobals;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlobals = ((OpenAndroid) this.getSupportApplication()).getGlobals();
    }



    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.setListAdapter(new VarAdapter(this.getActivity()));

        this.getSupportActionBar().setTitle("Variable List");

        this.getListView().setOnItemClickListener(mListItemClickL);
        this.getListView().setOnItemLongClickListener(mListItemLongClickL);

        if (Build.VERSION.SDK_INT > 11) {
            this.getListView().setLayoutTransition(new LayoutTransition());
        }
    }

    private final AdapterView.OnItemClickListener mListItemClickL = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO: Hook-up variable inspection
            StatMethods.hotBread(VarListFrag.this.getActivity(), "Not implemented yet", Toast.LENGTH_SHORT);
        }
    };

    private void refreshList() {
        ((VarAdapter) this.getListAdapter()).notifyDataSetChanged();
    }

    private final AdapterView.OnItemLongClickListener mListItemLongClickL = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            StatMethods.QuestionBox(VarListFrag.this.getActivity(), "Delete?",
                    "Are you sure you wish to delete this variable?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mGlobals.removeObjectAtPosition(position)) {
                                StatMethods.hotBread(VarListFrag.this.getActivity(), "Variable removed",
                                        Toast.LENGTH_SHORT);
                                refreshList();
                            } else {
                                StatMethods.hotBread(VarListFrag.this.getActivity(),
                                        "Error removing variable", Toast.LENGTH_SHORT);
                            }
                        }
                    }, null);
            return false;
        }
    };

    private class VarAdapter extends ArrayAdapter {
        private int mCurrentItem = -1;
        private Object mCurrentObject;

        public VarAdapter(Context context) {
            super(context, 0);
        }

        private String getTitle(int position) {
            if (mCurrentObject == null || mCurrentItem != position) {
                mCurrentObject = mGlobals.getObject(position);
                mCurrentItem = position;
            }

            return mCurrentObject.getClass().getSimpleName() + " : " + mGlobals.getKeyAtPosition(position);
        }

        private String getValue(int position) {
            if (mCurrentObject == null || mCurrentItem != position) {
                mCurrentObject = mGlobals.getObject(position);
                mCurrentItem = position;
            }

            if (mCurrentObject instanceof Object[]) {
                int len = Array.getLength(mCurrentObject);
                String vals = Array.get(mCurrentObject, 0).toString();
                for (int i = 1; i < len; i++) {
                    vals += ", " + Array.get(mCurrentObject, i);
                }
                return vals;
            } else {
                return mCurrentObject.toString();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = VarListFrag.this.getLayoutInflater().inflate(R.layout.var_listitem);

            TextView txt = (TextView) rootView.findViewById(R.id.title);
            txt.setText(getTitle(position));

            txt = (TextView) rootView.findViewById(R.id.var_value);
            txt.setText(getValue(position));

            return rootView;
        }

        @Override
        public int getCount() {
            return mGlobals.getObjectCount();
        }
    }
}
