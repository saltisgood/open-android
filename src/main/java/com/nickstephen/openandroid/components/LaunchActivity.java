package com.nickstephen.openandroid.components;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nickstephen.lib.gui.FragmentUtil;
import com.nickstephen.lib.gui.IPopChildFragments;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.reflection.InstantiatorFrag;
import com.nickstephen.openandroid.reflection.NewVarListFrag;
import com.nickstephen.openandroid.settings.SettingsActivity;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry activity
 */
public class LaunchActivity extends Activity {
    private List<IPopChildFragments> mFragmentsWChildFrags;
    private boolean mShouldPopChildFrags;

    public void addFragWChildren(IPopChildFragments frag) {
        for (IPopChildFragments f : mFragmentsWChildFrags) {
            if (f.equals(frag)) {
                return;
            }
        }
        mFragmentsWChildFrags.add(frag);
    }

    void setShouldPopChildren(boolean b) {
        mShouldPopChildFrags = b;
    }

    @Override
    public void onBackPressed() {
        if (mShouldPopChildFrags && mFragmentsWChildFrags.size() != 0) {
            IPopChildFragments frag = mFragmentsWChildFrags.get(mFragmentsWChildFrags.size() - 1);
            if (frag.getChildCount() == 0) {
                super.onBackPressed();
                mFragmentsWChildFrags.remove(frag);
                return;
            } else {
                frag.popChildFragment();
                return;
            }
        }

        super.onBackPressed();

        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFragmentsWChildFrags = new ArrayList<IPopChildFragments>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        if (savedInstanceState == null) {
            this.getSupportFragmentManager().beginTransaction()
                    .add(FragmentUtil.getContentViewCompat(), new LaunchFrag(), LaunchFrag.FRAG_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            Intent webTent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.APP_URL));
            this.startActivity(webTent);
            return true;
        } else if (id == R.id.action_view_dev) {
            Intent devTent = new Intent(Intent.ACTION_VIEW);
            devTent.setData(Uri.parse(Constants.DEV_URI));
            this.startActivity(devTent);
            return true;
        } else if (id == R.id.test_menu) {
            this.getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                            R.anim.push_right_in, R.anim.push_right_out)
                    .replace(FragmentUtil.getContentViewCompat(), new InstantiatorFrag(), InstantiatorFrag.FRAG_TAG)
                    .addToBackStack(InstantiatorFrag.FRAG_TAG).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (this.getSupportFragmentManager().popBackStackImmediate(ClassBrowserFrag.FRAG_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            mFragmentsWChildFrags.remove(mFragmentsWChildFrags.size() - 1);
            return false;
        } else if (this.getSupportFragmentManager().popBackStackImmediate(PackageBrowserFrag.FRAG_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            mFragmentsWChildFrags.remove(mFragmentsWChildFrags.size() - 1);
            return false;
        } else if (this.getSupportFragmentManager().popBackStackImmediate(AppBrowserListFrag.FRAG_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return false;
        }

        return true;
    }
}
