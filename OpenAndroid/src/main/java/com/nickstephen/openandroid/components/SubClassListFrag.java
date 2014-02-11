package com.nickstephen.openandroid.components;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.ExpandableListFragment;
import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.misc.TypeHelpers;
import com.nickstephen.openandroid.misc.Types;
import com.nickstephen.openandroid.util.BaseExpandableListAdapter;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ExpandableListView;
import org.holoeverywhere.widget.TextView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Child fragment class that is used to navigate the inside of a class
 */
public class SubClassListFrag extends ExpandableListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.SubClassListFrag";

    private static final String TYPE_KEY = "type_key";

    private Class<?> mClass;
    private String mClassName;
    private Class<?>[] mInterfaces;
    private Annotation[] mAnnotations;
    private Class<?>[] mInnerClasses;
    private Constructor<?>[] mConstructors;
    private Field[] mFields;
    private Method[] mMethods;
    private Class<?> mSuperClass;
    private Types mClassType;
    private ClassBrowserFrag mParentFrag;

    private LayoutInflater mInflater;

    public SubClassListFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentFrag = (ClassBrowserFrag) this.getParentFragment();

        Bundle args = this.getArguments();
        if (args != null) {
            mClassName = args.getString(Constants.CLASS_KEY);
        }

        if (mClassName == null) {
            this.popFragment();
            Twig.debug(FRAG_TAG, "mMemberName null");
            return;
        }

        try {
            //noinspection ConstantConditions
            mClass = ((OpenAndroid) this.getSupportApplication()).getPackageContext().getClassLoader().loadClass(mClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.popFragment();
            return;
        }

        if (mClass.isInterface()) {
            mClassType = Types.INTERFACE;
        } else if (mClass.isEnum()) {
            mClassType = Types.ENUM;
        } else if (mClass.isAnnotation()) {
            mClassType = Types.ANNOTATION;
        } else {
            mClassType = Types.CLASS;
        }

        try {
            mSuperClass = mClass.getSuperclass();
            mInterfaces = mClass.getInterfaces();
            mAnnotations = mClass.getDeclaredAnnotations();
            mInnerClasses = mClass.getDeclaredClasses();
            mConstructors = mClass.getDeclaredConstructors();
            mMethods = mClass.getDeclaredMethods();
            mFields = mClass.getDeclaredFields();
        } catch (SecurityException e) {
            e.printStackTrace();
            this.popFragment();
            return;
        } catch (Exception e) {
            Twig.printStackTrace(e);
        }

        if (mParentFrag.shouldIgnoreSynthetics() && mMethods != null) {
            List<Method> methods = new ArrayList<Method>();
            for (Method m : mMethods) {
                if (!m.isSynthetic()) {
                    methods.add(m);
                }
            }
            mMethods = methods.toArray(new Method[methods.size()]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;

        mParentFrag.setType(mClassType);
        mParentFrag.setHeaderText(TypeHelpers.printClass(mClass));

        View rootView = inflater.inflate(R.layout.subclass_browser_listfrag);

        this.setListAdapter(new ClassBrowserAdapter(), rootView);

        if (mClassType == Types.INTERFACE || mClassType == Types.ENUM || mSuperClass == null) {
            View v = rootView.findViewById(R.id.subclass_header_scrollview);
            v.setVisibility(View.GONE);
            v = rootView.findViewById(R.id.subclass_header_divider);
            v.setVisibility(View.GONE);
        } else {
            TextView superText = (TextView) rootView.findViewById(R.id.superclass_text);
            superText.setText("extends " + mSuperClass.getName());
            superText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SubClassListFrag frag = new SubClassListFrag();
                    Bundle args = new Bundle();
                    args.putString(Constants.CLASS_KEY, mSuperClass.getName());
                    frag.setArguments(args);

                    SubClassListFrag.this.getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                            .replace(R.id.package_browser_frag_container, frag, SubClassListFrag.FRAG_TAG)
                            .addToBackStack(SubClassListFrag.FRAG_TAG).commit();
                }
            });
        }
        this.getListView().setOnChildClickListener(mListChildClickListener);
        this.getListView().setOnChildLongClickListener(mChildLongClickL);

        return rootView;
    }

    private void methodClick(int methodNo) {
        if (mMethods == null || mMethods.length == 0) {
            StatMethods.hotBread(this.getActivity(), "No methods found", Toast.LENGTH_SHORT);
            return;
        }

        if (methodNo >= mMethods.length || methodNo < 0) {
            Twig.info(FRAG_TAG, "methodClick: Parameter error - " + methodNo);
            return;
        }

        if (!Modifier.isStatic(mMethods[methodNo].getModifiers())) {
            StatMethods.hotBread(this.getActivity(), "Non-static methods not supported yet", Toast.LENGTH_SHORT);
            return;
        }


    }

    /**
     * Groups: Interfaces, Annotations, Inner Classes, Fields, Constructors, Methods
     */
    private class ClassBrowserAdapter extends BaseExpandableListAdapter<String, Object> {
        public ClassBrowserAdapter() {
        }

        @Override
        public int getGroupCount() {
            return 6;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case 0:
                    return mInterfaces != null ? mInterfaces.length : 0;
                case 1:
                    return mAnnotations != null ? mAnnotations.length : 0;
                case 2:
                    return mInnerClasses != null ? mInnerClasses.length : 0;
                case 3:
                    return mFields != null ? mFields.length : 0;
                case 4:
                    return mConstructors != null ? mConstructors.length : 0;
                case 5:
                    return mMethods != null ? mMethods.length : 0;
            }
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.group_expandable_list);

            TextView txt = (TextView) view.findViewById(R.id.text_item);
            int childCount = getChildrenCount(groupPosition);

            switch (groupPosition) {
                case 0:
                    if (mClassType == Types.INTERFACE) {
                        txt.setText("Interfaces Extended (" + childCount + ")");
                    } else {
                        txt.setText("Implemented Interfaces (" + childCount + ")");
                    }
                    break;
                case 1:
                    txt.setText("Annotations (" + childCount + ")");
                    break;
                case 2:
                    txt.setText("Inner Classes (" + childCount + ")");
                    break;
                case 3:
                    if (mClassType == Types.INTERFACE) {
                        txt.setText("Constants (" + childCount + ")");
                    } else {
                        txt.setText("Fields (" + childCount + ")");
                    }
                    break;
                case 4:
                    txt.setText("Constructors (" + childCount + ")");
                    break;
                case 5:
                default:
                    txt.setText("Methods (" + childCount + ")");
                    break;
            }

            return view;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case 0:
                    return mInterfaces[childPosition];
                case 1:
                    return mAnnotations[childPosition];
                case 2:
                    return mInnerClasses[childPosition];
                case 3:
                    return mFields[childPosition];
                case 4:
                    return mConstructors[childPosition];
                case 5:
                    return mMethods[childPosition];
                default:
                    return null;
            }
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.child_expandable_list);

            TextView txt = (TextView) view.findViewById(R.id.text_item);
            switch (groupPosition) {
                case 0:
                case 2:
                    txt.setText(TypeHelpers.printClass((Class<?>) getChild(groupPosition, childPosition)));
                    break;
                case 3:
                    txt.setText(TypeHelpers.printField((Field) getChild(groupPosition, childPosition)));
                    break;
                case 4:
                    txt.setText(TypeHelpers.printConstructor((Constructor<?>) getChild(groupPosition, childPosition)));
                    break;
                case 5:
                    txt.setText(TypeHelpers.printMethod((Method) getChild(groupPosition, childPosition)));
                    break;
                default:
                    //noinspection ConstantConditions
                    txt.setText(getChild(groupPosition, childPosition).toString());
                    break;
            }

            return view;
        }
    }

    private final ExpandableListView.OnChildClickListener mListChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (groupPosition >= 0 && groupPosition <= 2) {
                Fragment frag = new SubClassListFrag();
                Bundle args = new Bundle();

                String val = null;
                int num = 0;
                switch (groupPosition) {
                    case 0: // Interfaces
                        val = mInterfaces[childPosition].getName();
                        num = Types.CLASS.ordinal();
                        break;
                    case 1: // Annotations
                        val = mAnnotations[childPosition].annotationType().getName();
                        num = Types.ANNOTATION.ordinal();
                        break;
                    case 2: // Inner Classes
                        val = mInnerClasses[childPosition].getName();
                        num = Types.CLASS.ordinal();
                        break;
                }

                args.putString(Constants.CLASS_KEY, val);
                args.putInt(TYPE_KEY, num);

                frag.setArguments(args);

                SubClassListFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                        .replace(R.id.package_browser_frag_container, frag, SubClassListFrag.FRAG_TAG)
                        .addToBackStack(SubClassListFrag.FRAG_TAG).commit();

                return true;
            } else if (groupPosition >= 3 && groupPosition <= 5) {
                switch (groupPosition) {
                    case 3:
                        mParentFrag.setNextMember(mFields[childPosition]);
                        break;
                    case 4:
                        mParentFrag.setNextMember(mConstructors[childPosition]);
                        break;
                    case 5:
                        mParentFrag.setNextMember(mMethods[childPosition]);
                        break;
                }

                SubClassListFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                        .replace(R.id.package_browser_frag_container, new MemberListFrag(), MemberListFrag.FRAG_TAG)
                        .addToBackStack(MemberListFrag.FRAG_TAG)
                        .commit();

                return true;
            }

            return false;
        }
    };

    private final ExpandableListView.OnChildLongClickListener mChildLongClickL = new ExpandableListView.OnChildLongClickListener() {
        @Override
        public boolean onChildLongClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            switch (groupPosition) {
                case 5:
                    methodClick(childPosition);
                    break;
            }
            return true;
        }
    };
}