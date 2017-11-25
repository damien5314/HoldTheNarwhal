package com.ddiehl.android.htn.di;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

@Module
public class SharedModule {

    Context mContext;

    public SharedModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesContext() {
        return mContext;
    }

    @Singleton @Provides
    @Nullable Bypass providesBypass(Context context) {
        try {
            Bypass.Options options = new Bypass.Options();
            options.setBlockQuoteColor(
                    ContextCompat.getColor(context, R.color.markdown_quote_block)
            );
            return new Bypass(context, options);
        } catch (UnsatisfiedLinkError error) {
            Timber.w("Bypass is unavailable");
            return null;
        }
    }
}
