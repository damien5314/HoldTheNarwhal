package com.ddiehl.android.htn.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedModule {

    Context mContext;

    public SharedModule(Context context) {
        mContext = context.getApplicationContext();
    }

    @Provides
    Context providesContext() {
        return mContext;
    }

//    @Singleton @Provides
//    @Nullable Bypass providesBypass(Context context) {
//        try {
//            Bypass.Options options = new Bypass.Options();
//            options.setBlockQuoteColor(
//                    ContextCompat.getColor(context, R.color.markdown_quote_block)
//            );
//            return new Bypass(context, options);
//        } catch (UnsatisfiedLinkError error) {
//            Timber.w("Bypass is unavailable");
//            return null;
//        }
//    }
}
