package com.ddiehl.android.htn.listings.profile;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ddiehl.android.htn.R;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.Trophy;


public class TrophyCase extends GridLayout {

    private static final @LayoutRes int TROPHY_LAYOUT_RES = R.layout.trophy;

    public TrophyCase(Context context) {
        super(context);
    }

    public TrophyCase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrophyCase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public TrophyCase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            Glide.with(getContext())
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
