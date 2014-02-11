package com.nickstephen.openandroid.components;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.nickstephen.lib.gui.ExpandableListFragment;
import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.gui.FragmentUtil;
import com.nickstephen.lib.misc.Comparators;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.util.BaseExpandableListAdapter;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ExpandableListView;
import org.holoeverywhere.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The child fragment of the PackageBrowserFrag. Used for navigating through a package.
 */
public class SubPackageListFrag extends ExpandableListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.SubPackageListFrag";
    public static final String SUBPACKAGE_KEY = "subpackage_key";

    private String mPackagePrefix;
    private PackageBrowserFrag mParentFrag;
    private LayoutInflater mInflater;
    private PackageBrowserAdapter mAdapter;

    public SubPackageListFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentFrag = (PackageBrowserFrag) this.getParentFragment();

        Bundle args = this.getArguments();
        if (args != null) {
            mPackagePrefix = args.getString(SUBPACKAGE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;

        mParentFrag.setHeaderText(mPackagePrefix);

        View rootView = inflater.inflate(R.layout.subpackage_browser_listfrag);
        if (mAdapter == null) {
            this.setListAdapter(mAdapter = new PackageBrowserAdapter(), rootView);
            this.getListView().expandGroup(0, false);
            this.getListView().expandGroup(1, false);
            this.getListView().smoothScrollToPosition(0);
        } else {
            this.setListAdapter(mAdapter, rootView);
        }

        this.getListView().setOnChildClickListener(mChildClickListener);

        return rootView;
    }

    private class PackageBrowserAdapter extends BaseExpandableListAdapter<String, String> {
        private final List<String> mSubPackageList;
        private final List<String> mClassList;
        private final int mSubEntryCount;
        private final int mClassCount;

        public PackageBrowserAdapter() {
            List<String> packs;
            int subPackageDepth;

            if (mPackagePrefix == null) {
                packs = mParentFrag.getClasses();
                subPackageDepth = 0;
            } else {
                packs = new ArrayList<String>();
                for (String c : mParentFrag.getClasses()) {
                    if (c.startsWith(mPackagePrefix)) {
                        packs.add(c);
                    }
                }

                subPackageDepth = mPackagePrefix.split("\\.").length;
            }

            mSubPackageList = new ArrayList<String>();
            mClassList = new ArrayList<String>();
            for (String c : packs) {
                String[] split = c.split("\\.");
                if (split.length <= subPackageDepth) {
                    continue;
                }
                if (split.length == subPackageDepth + 1) {
                    if (!split[subPackageDepth].contains("$")) {
                        mClassList.add(split[subPackageDepth]);
                    }
                    continue;
                }
                String pack = split[subPackageDepth];

                boolean found = false;
                for (String p : mSubPackageList) {
                    if (p.compareTo(pack) == 0) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mSubPackageList.add(pack);
                }
            }
            mSubEntryCount = mSubPackageList.size();
            mClassCount = mClassList.size();

            Collections.sort(mSubPackageList, new Comparators.StringCompareCaseIgnore());
            Collections.sort(mClassList, new Comparators.StringCompareCaseIgnore());
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case 0:
                    return mSubEntryCount;
                case 1:
                    return mClassCount;
                default:
                    return 0;
            }
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getId() != R.id.group_layout) {
                convertView = mInflater.inflate(R.layout.group_expandable_list);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.text_item);
            switch (groupPosition) {
                case 0:
                    txt.setText("Sub-Packages (" + mSubEntryCount + ")");
                    break;
                case 1:
                default:
                    txt.setText("Classes (" + mClassCount + ")");
                    break;
            }

            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case 0:
                    return mSubPackageList.get(childPosition);
                case 1:
                    return mClassList.get(childPosition);
                default:
                    return null;
            }
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getId() != R.id.child_layout) {
                convertView = mInflater.inflate(R.layout.child_expandable_list);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.text_item);
            txt.setText(getChildData(groupPosition, childPosition));

            return convertView;
        }
    }

    private final ExpandableListView.OnChildClickListener mChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            switch (groupPosition) {
                case 0:
                    Fragment frag = new SubPackageListFrag();
                    Bundle args = new Bundle();
                    String selectedPack = ((PackageBrowserAdapter) SubPackageListFrag.this.getAdapter())
                            .getChildData(groupPosition, childPosition);

                    args.putString(SUBPACKAGE_KEY, (mPackagePrefix != null) ?
                            mPackagePrefix + "." + selectedPack : selectedPack);
                    frag.setArguments(args);

                    SubPackageListFrag.this.getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                            .replace(R.id.package_browser_frag_container, frag, SubPackageListFrag.FRAG_TAG)
                            .addToBackStack(SubPackageListFrag.FRAG_TAG).commit();

                    return true;
                case 1:
                    frag = new ClassBrowserFrag();
                    args = new Bundle();
                    args.putString(Constants.PACKAGE_KEY, ((OpenAndroid) SubPackageListFrag.this.getSupportApplication()).getLoadedPackageName());
                    String selectedClass = ((PackageBrowserAdapter) SubPackageListFrag.this.getAdapter())
                            .getChildData(groupPosition, childPosition);
                    selectedClass = (mPackagePrefix != null) ? mPackagePrefix + "." + selectedClass
                            : selectedClass;
                    args.putString(Constants.CLASS_KEY, selectedClass);

                    frag.setArguments(args);

                    SubPackageListFrag.this.getParentFragment().getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                            .replace(FragmentUtil.getContentViewCompat(), frag, ClassBrowserFrag.FRAG_TAG)
                            .addToBackStack(ClassBrowserFrag.FRAG_TAG)
                            .commit();

                    ((LaunchActivity) SubPackageListFrag.this.getActivity()).setShouldPopChildren(false);

                    return true;
                default:
                    return false;
            }
        }
    };
}