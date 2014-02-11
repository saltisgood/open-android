package com.nickstephen.openandroid.components;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.gui.FragmentUtil;
import com.nickstephen.lib.gui.widget.AnimTextView;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;

/**
 * The main menu fragment
 */
public class LaunchFrag extends Fragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.LaunchFrag";

    private AnimTextView mAnimFooterText;

    public LaunchFrag() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.launch_frag);

        View button = view.findViewById(R.id.browse_apps_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchFrag.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                AppBrowserListFrag frag = new AppBrowserListFrag();
                Bundle args = new Bundle();
                args.putBoolean(Constants.SYSTEM_LIST_KEY, false);
                frag.setArguments(args);

                LaunchFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                        .replace(FragmentUtil.getContentViewCompat(), frag, AppBrowserListFrag.FRAG_TAG)
                        .addToBackStack(AppBrowserListFrag.FRAG_TAG).commit();
            }
        });

        View button2 = view.findViewById(R.id.browse_system_apps_button);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchFrag.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                AppBrowserListFrag frag = new AppBrowserListFrag();
                Bundle args = new Bundle();
                args.putBoolean(Constants.SYSTEM_LIST_KEY, true);
                frag.setArguments(args);

                LaunchFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                        .replace(FragmentUtil.getContentViewCompat(), frag, AppBrowserListFrag.FRAG_TAG)
                        .addToBackStack(AppBrowserListFrag.FRAG_TAG).commit();
            }
        });

        button = view.findViewById(R.id.help_button);
        button.setOnClickListener(mHelpClickListener);

        mAnimFooterText = (AnimTextView) view.findViewById(R.id.anim_footer_text);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAnimFooterText = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAnimFooterText != null) {
            mAnimFooterText.startAnimation(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAnimFooterText != null) {
            mAnimFooterText.pauseAnimation();
        }
    }

    private final View.OnClickListener mHelpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent webTent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.APP_URL));
            LaunchFrag.this.startActivity(webTent);
        }
    };
}
