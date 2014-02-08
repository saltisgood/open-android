package com.nickstephen.openandroid.tasks;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

/**
 * Lazy loader for loading images from the Package Manager into an ImageView
 */
public class LazyLogoLoader extends AsyncTask<Void, Void, Drawable> {
    private final ImageView mImg;
    private final ApplicationInfo mAppInfo;
    private final PackageManager mPackManager;

    public LazyLogoLoader(ImageView img, ApplicationInfo info, PackageManager pm) {
        mImg = img;
        mAppInfo = info;
        mPackManager = pm;
    }

    @Override
    protected Drawable doInBackground(Void... voids) {
        return mPackManager.getApplicationIcon(mAppInfo);
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (drawable != null) {
            mImg.setImageDrawable(drawable);
            if (mImg.getVisibility() != View.VISIBLE) {
                mImg.setVisibility(View.VISIBLE);
            }
        }
    }
}
