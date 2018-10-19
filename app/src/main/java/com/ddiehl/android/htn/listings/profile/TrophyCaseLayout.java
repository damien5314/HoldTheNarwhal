package com.ddiehl.android.htn.listings.profile;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.glide.GlideApp;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;
import rxreddit.model.Listing;
import rxreddit.model.Trophy;


public class TrophyCaseLayout extends GridLayout {

    private static final @LayoutRes int TROPHY_LAYOUT_RES = R.layout.trophy;

    public TrophyCaseLayout(Context context) {
        super(context);
    }

    public TrophyCaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrophyCaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public TrophyCaseLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void bind(List<? extends Trophy> trophies) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Listing listing : trophies) {
            Trophy trophy = (Trophy) listing;
            String name = trophy.getName();

            // Inflate trophy layout
            ImageView imageView = (ImageView) inflater.inflate(R.layout.trophy, this, false);

            // Set accessible description
            imageView.setContentDescription(name);

            // Show toast on click
            imageView.setOnClickListener(
                    view -> Toast.makeText(view.getContext(), name, Toast.LENGTH_SHORT).show()
            );

            // Load image
            GlideApp.with(getContext())
                    .load(trophy.getIcon70())
                    .into(imageView);

            // Add view to GridLayout
            addView(imageView);
        }

        // Calculate and set number of columns
        GridLayout.LayoutParams params = (GridLayout.LayoutParams) getChildAt(0).getLayoutParams();
        final int columns = getWidth() / (params.width + params.rightMargin);
        setColumnCount(columns);
    }
}
