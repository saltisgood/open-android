package com.nickstephen.openandroid.reflection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.gui.FragmentUtil;
import com.nickstephen.openandroid.R;

/**
 * Created by Nick on 14/01/14.
 */
public class InstantiatorFrag extends Fragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.reflection.InstantiatorFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.instant_frag, null);

        View button = rootView.findViewById(R.id.new_primitive);
        button.setOnClickListener(newPrimClick);

        button = rootView.findViewById(R.id.new_object);
        button.setOnClickListener(newObjClick);

        button = rootView.findViewById(R.id.view_objs);
        button.setOnClickListener(viewVarsClick);

        return rootView;
    }

    private final View.OnClickListener newPrimClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NewVarListFrag frag = new NewVarListFrag();
            Bundle args = new Bundle();
            args.putBoolean(NewVarListFrag.KEY_IS_PRIMITIVE, true);
            frag.setArguments(args);

            InstantiatorFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                            R.anim.push_right_in, R.anim.push_right_out)
                    .replace(FragmentUtil.getContentViewCompat(), frag, NewVarListFrag.FRAG_TAG)
                    .addToBackStack(NewVarListFrag.FRAG_TAG).commit();
        }
    };

    private final View.OnClickListener newObjClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NewVarListFrag frag = new NewVarListFrag();
            Bundle args = new Bundle();
            args.putBoolean(NewVarListFrag.KEY_IS_PRIMITIVE, false);
            frag.setArguments(args);

            InstantiatorFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                            R.anim.push_right_in, R.anim.push_right_out)
                    .replace(FragmentUtil.getContentViewCompat(), frag, NewVarListFrag.FRAG_TAG)
                    .addToBackStack(NewVarListFrag.FRAG_TAG).commit();
        }
    };

    private final View.OnClickListener viewVarsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InstantiatorFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                            R.anim.push_right_in, R.anim.push_right_out)
                    .replace(FragmentUtil.getContentViewCompat(), new VarListFrag(), VarListFrag.FRAG_TAG)
                    .addToBackStack(VarListFrag.FRAG_TAG).commit();
        }
    };
}
