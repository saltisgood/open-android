package com.nickstephen.openandroid.components;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.ListFragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.util.SparseArray;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Fragment for viewing the list of resources in an App
 */
public class ResourceListFrag extends ListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.ResourceListFrag";

    private Class<?> mResourceClass;
    private PackageBrowserFrag mParentFrag;
    private Class<?>[] mSubResourceClasses;
    private LayoutInflater mInflater;
    private SparseArray<Class<?>> mSubResourceMap;

    public ResourceListFrag() {}

    @Override
    protected View getEmptyView() {
        return mInflater.inflate(R.layout.empty_resourcelistfrag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentFrag = (PackageBrowserFrag) this.getParentFragment();

        if (mParentFrag.getResourceClass() == null) {
            Twig.warning(FRAG_TAG, "Resource class not found in sub-frag");
            this.popFragment();
            return;
        }

        try {
            mSubResourceClasses = mParentFrag.getResourceClass().getDeclaredClasses();
        } catch (SecurityException e) {
            Twig.printStackTrace(e);
            this.popFragment();
            return;
        }

        Arrays.sort(mSubResourceClasses, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> class1, Class<?> class2) {
                String name1 = class1.getSimpleName(), name2 = class2.getSimpleName();
                if (StatMethods.IsStringNullOrEmpty(name1)) {
                    name1 = class1.getName();
                }
                if (StatMethods.IsStringNullOrEmpty(name2)) {
                    name2 = class2.getName();
                }

                return name1.toLowerCase().compareTo(name2.toLowerCase());
            }
        });

        mSubResourceMap = new SparseArray<Class<?>>();
        for (int i = 0; i < mSubResourceClasses.length; i++) {
            mSubResourceMap.put(i, mSubResourceClasses[i]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.isExiting()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        mInflater = inflater;

        return inflater.inflate(R.layout.simple_list_layout);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.setListAdapter(new ResourceListAdapter());
        if (mSubResourceClasses == null || mSubResourceClasses.length == 0) {
            this.getListView().setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.empty_list_text).setVisibility(View.GONE);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        Class<?> cls = mSubResourceMap.get(position);
        if (cls.getSimpleName().compareTo("drawable") == 0) {
            DrawablePreviewListFrag frag = new DrawablePreviewListFrag();
            Bundle args = new Bundle();
            args.putString(Constants.CLASS_KEY, cls.getName());
            frag.setArguments(args);

            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.package_browser_frag_container, frag, DrawablePreviewListFrag.FRAG_TAG)
                    .addToBackStack(DrawablePreviewListFrag.FRAG_TAG).commit();
        } else if (cls.getSimpleName().compareTo("string") == 0) {
            SubResourceListFrag frag = new SubResourceListFrag();
            Bundle args = new Bundle();
            args.putString(Constants.CLASS_KEY, cls.getName());
            frag.setArguments(args);

            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.package_browser_frag_container, frag, SubResourceListFrag.FRAG_TAG)
                    .addToBackStack(SubResourceListFrag.FRAG_TAG).commit();

        } else {
            StatMethods.hotBread(this.getActivity(), "Only drawable and string resources support at this stage", Toast.LENGTH_SHORT);
        }
    }

    private class ResourceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubResourceClasses == null ? 0 : mSubResourceClasses.length;
        }

        @Override
        public Object getItem(int position) {
            return mSubResourceClasses[position].getSimpleName();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.child_expandable_list);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.text_item);
            txt.setText((String) getItem(position));

            return convertView;
        }
    }
}
