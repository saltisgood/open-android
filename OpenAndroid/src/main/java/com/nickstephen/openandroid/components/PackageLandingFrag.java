package com.nickstephen.openandroid.components;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

/**
 * The Fragment reached by clicking on an app in the AppBrowserListFrag. Asks the user where they
 * want to go from here.
 */
public class PackageLandingFrag extends Fragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.PackageLandingFrag";

    private PackageBrowserFrag mParentFrag;

    public PackageLandingFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentFrag = (PackageBrowserFrag) this.getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.isExiting()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        View view = inflater.inflate(R.layout.package_landing_frag);

        View button = view.findViewById(R.id.explore_package_button);
        button.setOnClickListener(mClassExplorerClickListener);

        button = view.findViewById(R.id.explore_res_button);
        button.setOnClickListener(mResExplorerClickListener);

        button = view.findViewById(R.id.launch_app_button);
        button.setOnClickListener(mLaunchAppListener);

        TextView txt = (TextView) view.findViewById(R.id.package_name_text);
        txt.setText("Package Name: " + mParentFrag.getPackageName());
        txt.setOnClickListener(mPackageClickListener);

        txt = (TextView) view.findViewById(R.id.class_count_text);
        txt.setText("Total No. of Classes: " + String.valueOf(mParentFrag.getClasses().size()));

        return view;
    }

    private final View.OnClickListener mClassExplorerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PackageLandingFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.package_browser_frag_container, new SubPackageListFrag(), SubPackageListFrag.FRAG_TAG)
                    .addToBackStack(SubPackageListFrag.FRAG_TAG).commit();
        }
    };

    private final View.OnClickListener mResExplorerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mParentFrag.getResourceClass() == null) {
                StatMethods.hotBread(PackageLandingFrag.this.getActivity(), "Resource class not found :(", Toast.LENGTH_SHORT);
            } else {
                PackageLandingFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                        .replace(R.id.package_browser_frag_container, new ResourceListFrag(), ResourceListFrag.FRAG_TAG)
                        .addToBackStack(SubPackageListFrag.FRAG_TAG).commit();
            }
        }
    };

    private final View.OnClickListener mPackageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StatMethods.hotBread(PackageLandingFrag.this.getActivity(), mParentFrag.getPackageName(), Toast.LENGTH_SHORT);
        }
    };

    private final View.OnClickListener mLaunchAppListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = null;
            try {
                intent = mParentFrag.getPackageManager().getLaunchIntentForPackage(mParentFrag.getPackageName());
            } catch (Exception e) {
                // Catching generic exception because apparently the compiler doesn't realise it can
                // throw a PackageManager.NameNotFoundException
                Twig.printStackTrace(e);
                Twig.warning(FRAG_TAG, "Error getting launch intent!");
                StatMethods.hotBread(PackageLandingFrag.this.getActivity(), "An error occurred", Toast.LENGTH_SHORT);
                view.setEnabled(false);
                return;
            }
            if (intent == null) {
                StatMethods.hotBread(PackageLandingFrag.this.getActivity(), "No launch intent found", Toast.LENGTH_SHORT);
                view.setEnabled(false);
            } else {
                PackageLandingFrag.this.getActivity().startActivity(intent);
            }
        }
    };
}
