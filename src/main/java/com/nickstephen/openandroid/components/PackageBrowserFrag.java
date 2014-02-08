package com.nickstephen.openandroid.components;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nickstephen.lib.Twig;
import com.nickstephen.lib.gui.Fragment;
import com.nickstephen.lib.gui.IPopChildFragments;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;
import com.nickstephen.openandroid.settings.SettingsAccessor;
import com.nickstephen.openandroid.tasks.LazyLogoLoader;
import com.nickstephen.openandroid.util.Constants;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Fragment for browsing inside of app packages.
 */
public class PackageBrowserFrag extends Fragment implements IPopChildFragments {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.components.PackageBrowserFrag";

    private List<String> mClasses;
    private TextView mHeaderText;
    private String mAppLabel;
    private ApplicationInfo mApplicationInfo;
    private PackageManager mPackManager;
    private String mPackageName;
    private Class<?> mResourceClass;

    public PackageBrowserFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRetainInstance(true);

        Bundle args = this.getArguments();
        if (args != null) {
            mPackageName = args.getString(Constants.PACKAGE_KEY);
        } else {
            this.popFragment();
            return;
        }

        if (mPackageName == null) {
            this.popFragment();
            return;
        }

        {
            Context c = ((OpenAndroid) this.getSupportApplication()).getPackageContext();
            if (c == null || c.getPackageName().compareTo(mPackageName) != 0) {
                try {
                    ((OpenAndroid)this.getSupportApplication())
                            .setPackageContext(this.getActivity().createPackageContext(mPackageName,
                                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    StatMethods.hotBread(this.getActivity(), "Package context could not be created", Toast.LENGTH_LONG);
                    this.popFragment();
                    return;
                }
            }
        }

        try {
            ((OpenAndroid) this.getSupportApplication()).setDex(
                    new DexFile(((OpenAndroid) this.getSupportApplication()).getPackageContext().getPackageCodePath()));
        } catch (NullPointerException e) {
            Twig.printStackTrace(e);
            StatMethods.hotBread(this.getActivity(), "Package code path could not be determined", Toast.LENGTH_LONG);
            this.popFragment();
            return;
        } catch (IOException e) {
            Twig.printStackTrace(e);
            StatMethods.hotBread(this.getActivity(), "Package Dex could not be read", Toast.LENGTH_LONG);
            this.popFragment();
            return;
        }

        mClasses = new ArrayList<String>();
        if (SettingsAccessor.getIgnoreAndroidClasses(this.getActivity())) {
            for (Enumeration<String> iter = ((OpenAndroid) this.getSupportApplication()).getDex().entries(); iter.hasMoreElements(); ) {
                String cName = iter.nextElement();
                if (!cName.startsWith("android")) {
                    mClasses.add(cName);
                }
            }
        } else {
            for (Enumeration<String> iter = ((OpenAndroid) this.getSupportApplication()).getDex().entries(); iter.hasMoreElements();) {
                mClasses.add(iter.nextElement());
            }
        }

        mPackManager = this.getActivity().getPackageManager();
        if (mPackManager == null) {
            Twig.warning(FRAG_TAG, "Null package manager");
            this.popFragment();
            return;
        }
        try {
            mApplicationInfo = mPackManager.getApplicationInfo(mPackageName, 0);
            mAppLabel = mPackManager.getApplicationLabel(mApplicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            mAppLabel = mPackageName;
        } catch (NullPointerException e) {
            mAppLabel = mPackageName;
        }

        try {
            mResourceClass = ((OpenAndroid) this.getSupportApplication()).getPackageContext().getClassLoader().loadClass(mPackageName + ".R");
        } catch (ClassNotFoundException e) {
            mResourceClass = null;
            Twig.warning(FRAG_TAG, "Resource class: " + mPackageName + ".R" + ", not found");
        }

        if (savedInstanceState == null) {
            this.getChildFragmentManager().beginTransaction()
                    .add(R.id.package_browser_frag_container, new PackageLandingFrag(), PackageLandingFrag.FRAG_TAG)
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

        View listView = inflater.inflate(R.layout.package_browser_frag);

        mHeaderText = (TextView) listView.findViewById(R.id.app_name_text);
        mHeaderText.setText(mAppLabel);

        ImageView img = (ImageView) listView.findViewById(R.id.logo_img);
        new LazyLogoLoader(img, mApplicationInfo, mPackManager).execute();

        return listView;
    }

    void setHeaderText(String txt) {
        if (txt == null) {
            mHeaderText.setText(mAppLabel);
        } else {
            mHeaderText.setText(mAppLabel + "\n" + txt);
        }
    }

    List<String> getClasses() {
        return mClasses;
    }

    String getPackageName() {
        return mPackageName;
    }

    Class<?> getResourceClass() {
        return mResourceClass;
    }

    PackageManager getPackageManager() {
        return mPackManager;
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
