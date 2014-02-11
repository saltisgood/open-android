package com.nickstephen.openandroid.components;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.ExpandableListFragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.misc.TypeHelpers;
import com.nickstephen.openandroid.misc.Types;
import com.nickstephen.openandroid.util.BaseExpandableListAdapter;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.ExpandableListView;
import org.holoeverywhere.widget.TextView;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Child fragment class for inspecting member type things. i.e. Fields, Constructors and Methods
 */
public class MemberListFrag extends ExpandableListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.MemberListFrag";

    private Member mMember;
    private Annotation[] mAnnotations;
    private boolean mIsSynthetic;
    private Types mMemberTypeEnum;
    private String mModifiers;
    private int mModifiersInt;
    private Class<?> mMemberType;
    private String mMemberValue;
    private ClassBrowserFrag mParentFrag;

    private LayoutInflater mInflater;

    public MemberListFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentFrag = (ClassBrowserFrag) this.getParentFragment();

        mMember = mParentFrag.getNextMember();

        if (mMember == null) {
            Twig.warning(FRAG_TAG, "mMember null");
            this.popFragment();
            return;
        }

        AccessibleObject accessibleMember;
        if (mMember instanceof AccessibleObject) {
            accessibleMember = (AccessibleObject) mMember;
        } else {
            Twig.debug(FRAG_TAG, "mMember not AccessibleObject");
            this.popFragment();
            return;
        }

        if (accessibleMember instanceof Field) {
            mMemberType = ((Field) accessibleMember).getType();
            mMemberTypeEnum = Types.FIELD;
        } else if (accessibleMember instanceof Method) {
            mMemberType = ((Method) accessibleMember).getReturnType();
            mMemberTypeEnum = Types.METHOD;
        } else if (accessibleMember instanceof Constructor<?>) {
            mMemberType = ((Constructor<?>) accessibleMember).getDeclaringClass();
            mMemberTypeEnum = Types.METHOD;
        }

        mIsSynthetic = mMember.isSynthetic();
        mAnnotations = accessibleMember.getDeclaredAnnotations();
        mModifiers = TypeHelpers.getModifiers(mMember);
        mModifiersInt = mMember.getModifiers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (isExiting()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        mInflater = inflater;

        mParentFrag.setType(mMemberTypeEnum);
        mParentFrag.setHeaderText(TypeHelpers.printMember(mMember));

        View rootView = inflater.inflate(R.layout.subclass_browser_alt);

        this.setListAdapter(new MemberBrowserAdapter(), rootView);

        TextView txt = (TextView) rootView.findViewById(R.id.modifier_text);
        txt.setText(mModifiers);

        txt = (TextView) rootView.findViewById(R.id.type_text);
        txt.setText(mMemberType.getSimpleName());
        txt.setOnClickListener(mMemberTypeClickListener);

        CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.synthetic_checkbox);
        if (mIsSynthetic) {
            checkBox.setChecked(true);
        }

        if (!(mMember instanceof Field)) {
            txt = (TextView) rootView.findViewById(R.id.type_text_prompt);
            txt.setText("Return type: ");
        }

        if (mMember instanceof Field && Modifier.isFinal(mModifiersInt) && Modifier.isStatic(mModifiersInt)) {
            View row = rootView.findViewById(R.id.value_row);
            row.setVisibility(View.VISIBLE);
            txt = (TextView) rootView.findViewById(R.id.value_text);
            try {
                ((Field) mMember).setAccessible(true);
                mMemberValue = ((Field) mMember).get(null).toString();
                txt.setText(mMemberValue);
                txt.setOnClickListener(mValueClickListener);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Twig.warning(FRAG_TAG, "Illegal access to field");
            } catch (SecurityException e) {
                e.printStackTrace();
                Twig.warning(FRAG_TAG, "Unable to add accessibility to field");
            }
        }

        this.getListView().setOnChildClickListener(mListChildClickListener);

        return rootView;
    }

    private class MemberBrowserAdapter extends BaseExpandableListAdapter<String, Annotation> {
        public MemberBrowserAdapter() {
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mAnnotations.length;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.group_expandable_list);

            TextView txt = (TextView) view.findViewById(R.id.text_item);
            txt.setText("Annotations (" + mAnnotations.length + ")");

            return view;
        }

        @Override
        public Annotation getChild(int groupPosition, int childPosition) {
            return mAnnotations[childPosition];
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.child_expandable_list);

            TextView txt = (TextView) view.findViewById(R.id.text_item);
            txt.setText(getChildData(groupPosition, childPosition).toString());

            return view;
        }
    }

    private final View.OnClickListener mMemberTypeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mMemberType.isPrimitive()) {
                StatMethods.hotBread(MemberListFrag.this.getActivity(), "Primitives cannot be explored", Toast.LENGTH_SHORT);
                return;
            }

            SubClassListFrag frag = new SubClassListFrag();
            Bundle args = new Bundle();
            args.putString(Constants.CLASS_KEY, mMemberType.getName());
            frag.setArguments(args);

            MemberListFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.package_browser_frag_container, frag, SubClassListFrag.FRAG_TAG)
                    .addToBackStack(SubClassListFrag.FRAG_TAG).commit();
        }
    };

    private final View.OnClickListener mValueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mMemberValue != null) {
                StatMethods.hotBread(MemberListFrag.this.getActivity(), mMemberValue, Toast.LENGTH_SHORT);
            }
        }
    };

    private final ExpandableListView.OnChildClickListener mListChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            SubClassListFrag frag = new SubClassListFrag();
            Bundle args = new Bundle();
            args.putString(Constants.CLASS_KEY, mAnnotations[childPosition].annotationType().getName());
            frag.setArguments(args);

            MemberListFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.package_browser_frag_container, frag, FRAG_TAG).addToBackStack(FRAG_TAG)
                    .commit();

            return true;
        }
    };
}