package com.mopub.common.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.Surface;

import com.mopub.common.CreativeOrientation;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.nativeads.test.support.MoPubShadowDisplay;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class DeviceUtilsTest {

    private Activity testActivity;
    private Resources testResources;

    @Before
    public void setup() {
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
        testResources = testActivity.getResources();
    }

    @Test
    public void getOrientation_whenReverseLandscape_shouldReturnReverseLandscape() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_270);

        assertThat(DeviceUtils.getScreenOrientation(testActivity)).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Test
    public void getOrientation_whenLandscape_shouldReturnLandscape() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_90);

        assertThat(DeviceUtils.getScreenOrientation(testActivity)).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Test
    public void getOrientation_whenPortrait_shouldReturnPortrait() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_0);

        assertThat(DeviceUtils.getScreenOrientation(testActivity)).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void getOrientation_whenReversePortrait_shouldReturnReversePortrait() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_180);

        assertThat(DeviceUtils.getScreenOrientation(testActivity)).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }
    
    @Test
    public void lockOrientation_toLandscapeWhenLandscape_shouldRemainTheSame() throws Exception {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_90);

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.LANDSCAPE);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Test
    public void lockOrientation_toLandscapeWhenReverseLandscape_shouldBeReverseLandscape() {

        testResources.getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_180);  // Reverse landscape

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.LANDSCAPE);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Test
    public void lockOrientation_toLandscapeWhenPortrait_shouldBeLandscape() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_180);  // Reverse portrait

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.LANDSCAPE);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Test
    public void lockOrientation_toPortraitWhenPortrait_shouldRemainPortrait() {

        testResources.getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_0);

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.PORTRAIT);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void lockOrientation_toPortraitWhenReversePortrait_shouldRemainReversePortrait() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_180);

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.PORTRAIT);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }

    @Test
    public void lockOrientation_toPortraitWhenLandscape_shouldBeLandscape() {
        testResources.getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        MoPubShadowDisplay.setStaticRotation(Surface.ROTATION_0);  // Reverse landscape

        DeviceUtils.lockOrientation(testActivity, CreativeOrientation.PORTRAIT);
        assertThat(testActivity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
