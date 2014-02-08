package com.nickstephen.openandroid.tasks;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.nickstephen.lib.Twig;

/**
 * Lazy load a drawable resource
 */
public class LazyDrawableLoader extends AsyncTask<Integer, Void, Drawable> {
    private final ImageView mImageView;
    private final Resources mResources;

    public LazyDrawableLoader(ImageView img, Resources res) {
        mImageView = img;
        mResources = res;
    }

    @Override
    protected Drawable doInBackground(Integer... integers) {
        try {
            return mResources.getDrawable(integers[0]);
        } catch (Resources.NotFoundException e) {
            Twig.warning("LazyDrawableLoader", "Resource not found!");
            return null;
        }
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (drawable != null) {
            mImageView.setImageDrawable(drawable);
            if (mImageView.getVisibility() != View.VISIBLE) {
                mImageView.setVisibility(View.VISIBLE);
            }
        }
    }
}
