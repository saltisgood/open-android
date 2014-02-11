package com.nickstephen.openandroid.components;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.gui.IPopChildFragments;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.misc.Types;
import com.nickstephen.openandroid.settings.SettingsAccessor;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import java.lang.reflect.Member;

/**
 * Fragment for browsing inside of Classes. Effectively a container fragment since it contains
 * some children for more detailed view.
 */
public class ClassBrowserFrag extends Fragment implements IPopChildFragments {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.ClassBrowserFrag";

    private String mPackageName;
    private TextView mHeaderText;
    private Types mCurrentType;
    private ImageView mTypeIcon;
    private boolean mIgnoreSynthetics;
    private Member mNextMember;

    public ClassBrowserFrag() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRetainInstance(true);

        mIgnoreSynthetics = SettingsAccessor.getIgnoreSyntheticMethods(this.getActivity());

        Bundle args = this.getArguments();
        if (args != null) {
            mPackageName = args.getString(Constants.PACKAGE_KEY);
        }

        if (mPackageName == null) {
            Twig.warning(FRAG_TAG, "mPackageName null");
            this.popFragment();
            return;
        }

        if (savedInstanceState == null) {
            SubClassListFrag frag = new SubClassListFrag();
            frag.setArguments(args);

            this.getChildFragmentManager().beginTransaction()
                    .add(R.id.package_browser_frag_container, frag, SubClassListFrag.FRAG_TAG)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.isExiting()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        ((LaunchActivity) this.getActivity()).addFragWChildren(this);
        ((LaunchActivity) this.getActivity()).setShouldPopChildren(true);

        View view = inflater.inflate(R.layout.class_browser_frag);
        mHeaderText = (TextView) view.findViewById(R.id.app_name_text);
        mTypeIcon = (ImageView) view.findViewById(R.id.class_type_img);
        if (mCurrentType != null) {
            refreshTypeImg(mCurrentType);
        }
        mTypeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Implement the class icon clicker
                StatMethods.hotBread(ClassBrowserFrag.this.getActivity(), "Not implemented yet", Toast.LENGTH_SHORT);
            }
        });

        return view;
    }

    void setNextMember(Member m) {
        mNextMember = m;
    }

    Member getNextMember() {
        return mNextMember;
    }

    void setHeaderText(String txt) {
        mHeaderText.setText(txt);
    }

    private void refreshTypeImg(Types type) {
        switch (type) {
            case CLASS:
                mTypeIcon.setImageResource(R.drawable.class_img);
                break;
            case INTERFACE:
                mTypeIcon.setImageResource(R.drawable.interface_img);
                break;
            case ENUM:
                mTypeIcon.setImageResource(R.drawable.enum_img);
                break;
            case ANNOTATION:
                mTypeIcon.setImageResource(R.drawable.annotation_img);
                break;
            case FIELD:
                mTypeIcon.setImageResource(R.drawable.field_img);
                break;
            case METHOD:
                mTypeIcon.setImageResource(R.drawable.method_img);
                break;
        }
    }

    void setType(Types type) {
        if (mCurrentType != type) {
            refreshTypeImg(type);
        }
        mCurrentType = type;
    }

    boolean shouldIgnoreSynthetics() {
        return mIgnoreSynthetics;
    }

    @Override
    public void popChildFragment() {
        this.getChildFragmentManager().popBackStack();
    }

    @Override
    public int getChildCount() {
        return this.getChildFragmentManager().getBackStackEntryCount();
    }
}
