package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.mopub.common.MoPub;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.MoPubShadowTelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
@Config(shadows = {MoPubShadowTelephonyManager.class})
public class NativeUrlGeneratorTest {
    public static final String AD_UNIT_ID = "1234";
    private static final int TEST_SCREEN_WIDTH = 999;
    private static final int TEST_SCREEN_HEIGHT = 888;
    private static final float TEST_DENSITY = 1.0f;
    private Activity context;
    private NativeUrlGenerator subject;
    private MoPubShadowTelephonyManager shadowTelephonyManager;

    @Before
    public void setup() {
        context = spy(Robolectric.buildActivity(Activity.class).create().get());
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
        when(context.getPackageName()).thenReturn("testBundle");
        shadowTelephonyManager = (MoPubShadowTelephonyManager)
                shadowOf((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

        // Set the expected screen dimensions to arbitrary numbers
        final Resources spyResources = spy(context.getResources());
        final DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.widthPixels = TEST_SCREEN_WIDTH;
        mockDisplayMetrics.heightPixels = TEST_SCREEN_HEIGHT;
        mockDisplayMetrics.density = TEST_DENSITY;
        when(spyResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(context.getResources()).thenReturn(spyResources);

        // Only do this on Android 17+ because getRealSize doesn't exist before then.
        // This is the default pathway.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final WindowManager mockWindowManager = mock(WindowManager.class);
            final Display mockDisplay = mock(Display.class);
            doAnswer(new Answer() {
                @Override
                public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                    final Point point = (Point) invocationOnMock.getArguments()[0];
                    point.x = TEST_SCREEN_WIDTH;
                    point.y = TEST_SCREEN_HEIGHT;
                    return null;
                }
            }).when(mockDisplay).getRealSize(any(Point.class));
            when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
            final Context spyApplicationContext = spy(context.getApplicationContext());
            when(spyApplicationContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
            when(context.getApplicationContext()).thenReturn(spyApplicationContext);
        }
    }

    @Test
    public void generateUrlString_shouldIncludeDesiredAssetIfSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID).withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(1);
        assertThat(desiredAssets).contains("title");
    }

    @Test
    public void generateUrlString_shouldIncludeDesiredAssetsIfSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT, RequestParameters.NativeAdAsset.ICON_IMAGE);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID).withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(3);
        assertThat(desiredAssets).contains("title", "text", "iconimage");
    }

    @Test
    public void generateUrlString_shouldNotIncludeDesiredAssetsIfNotSet() throws Exception {
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(0);
    }

    @Test
    public void generateUrlString_shouldNotIncludeDesiredAssetsIfNoAssetsAreSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.noneOf(RequestParameters.NativeAdAsset.class);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID).withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(0);
    }

    @Test
    public void generateUrlString_needsButDoesNotHaveReadPhoneState_shouldNotContainOperatorName() {
        shadowTelephonyManager.setNeedsReadPhoneState(true);
        shadowTelephonyManager.setReadPhoneStatePermission(false);
        shadowTelephonyManager.setNetworkOperatorName("TEST_CARRIER");

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String requestString = generateMinimumUrlString();

        assertThat(getNetworkOperatorNameFromRequestUrl(requestString)).isNullOrEmpty();
    }

    @Test
    public void generateUrlString_needsAndHasReadPhoneState_shouldContainOperatorName() {
        shadowTelephonyManager.setNeedsReadPhoneState(true);
        shadowTelephonyManager.setReadPhoneStatePermission(true);
        shadowTelephonyManager.setNetworkOperatorName("TEST_CARRIER");

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String requestString = generateMinimumUrlString();

        assertThat(getNetworkOperatorNameFromRequestUrl(requestString)).isEqualTo("TEST_CARRIER");
    }

    @Test
    public void generateUrlString_doesNotNeedReadPhoneState_shouldContainOperatorName() {
        shadowTelephonyManager.setNeedsReadPhoneState(false);
        shadowTelephonyManager.setReadPhoneStatePermission(false);
        shadowTelephonyManager.setNetworkOperatorName("TEST_CARRIER");

        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String requestString = generateMinimumUrlString();

        assertThat(getNetworkOperatorNameFromRequestUrl(requestString)).isEqualTo("TEST_CARRIER");
    }

    @Test
    public void generateUrlString_whenLocationServiceGpsProviderHasMostRecentLocation_shouldUseLocationServiceValue() {
        Location locationFromDeveloper = new Location("");
        locationFromDeveloper.setLatitude(42);
        locationFromDeveloper.setLongitude(-42);
        locationFromDeveloper.setAccuracy(3.5f);
        locationFromDeveloper.setTime(1000);

        // Mock out the LocationManager's last known location to be more recent than the
        // developer-supplied location.
        ShadowLocationManager shadowLocationManager = Robolectric.shadowOf(
                (LocationManager) application.getSystemService(Context.LOCATION_SERVICE));
        Location locationFromSdk = new Location("");
        locationFromSdk.setLatitude(37);
        locationFromSdk.setLongitude(-122);
        locationFromSdk.setAccuracy(5.0f);
        locationFromSdk.setTime(System.currentTimeMillis() - 555555);
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, locationFromSdk);

        RequestParameters requestParameters = new RequestParameters.Builder()
                .location(locationFromDeveloper)
                .build();
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String adUrl = subject.withRequest(requestParameters)
                .generateUrlString("ads.mopub.com");
        assertThat(getParameterFromRequestUrl(adUrl, "ll")).isEqualTo("37.0,-122.0");
        assertThat(getParameterFromRequestUrl(adUrl, "lla")).isEqualTo("5");
        assertThat(getParameterFromRequestUrl(adUrl, "llsdk")).isEqualTo("1");
        // Only test to the full second (as there may be small differences)
        assertThat(getParameterFromRequestUrl(adUrl, "llf")).startsWith("555");
        assertThat(getParameterFromRequestUrl(adUrl, "llf").length()).isEqualTo(6);
    }

    @Test
    public void generateUrlString_whenDeveloperSuppliesMoreRecentLocationThanLocationService_shouldUseDeveloperSuppliedLocation() {
        Location locationFromDeveloper = new Location("");
        locationFromDeveloper.setLatitude(42);
        locationFromDeveloper.setLongitude(-42);
        locationFromDeveloper.setAccuracy(3.5f);
        locationFromDeveloper.setTime(System.currentTimeMillis() - 777777);

        ShadowLocationManager shadowLocationManager = Robolectric.shadowOf(
                (LocationManager) application.getSystemService(Context.LOCATION_SERVICE));

        // Mock out the LocationManager's last known location to be older than the
        // developer-supplied location.
        Location olderLocation = new Location("");
        olderLocation.setLatitude(40);
        olderLocation.setLongitude(-105);
        olderLocation.setAccuracy(8.0f);
        olderLocation.setTime(System.currentTimeMillis() - 888888);
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, olderLocation);

        RequestParameters requestParameters = new RequestParameters.Builder()
                .location(locationFromDeveloper)
                .build();
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String adUrl = subject.withRequest(requestParameters)
                .generateUrlString("ads.mopub.com");
        assertThat(getParameterFromRequestUrl(adUrl, "ll")).isEqualTo("42.0,-42.0");
        assertThat(getParameterFromRequestUrl(adUrl, "lla")).isEqualTo("3");
        assertThat(getParameterFromRequestUrl(adUrl, "llsdk")).isEmpty();
        // Only test to the full second (as there may be small differences)
        assertThat(getParameterFromRequestUrl(adUrl, "llf")).startsWith("777");
        assertThat(getParameterFromRequestUrl(adUrl, "llf").length()).isEqualTo(6);
    }

    @Test
    public void generateUrlString_whenLocationServiceNetworkProviderHasMostRecentLocation_shouldUseLocationServiceValue() {
        Location locationFromDeveloper = new Location("");
        locationFromDeveloper.setLatitude(42);
        locationFromDeveloper.setLongitude(-42);
        locationFromDeveloper.setAccuracy(3.5f);
        locationFromDeveloper.setTime(1000);

        // Mock out the LocationManager's last known location to be more recent than the
        // developer-supplied location.
        ShadowLocationManager shadowLocationManager = Robolectric.shadowOf(
                (LocationManager) application.getSystemService(Context.LOCATION_SERVICE));
        Location locationFromSdk = new Location("");
        locationFromSdk.setLatitude(38);
        locationFromSdk.setLongitude(-123);
        locationFromSdk.setAccuracy(5.0f);
        locationFromSdk.setTime(System.currentTimeMillis() - 123456);
        shadowLocationManager.setLastKnownLocation(LocationManager.NETWORK_PROVIDER,
                locationFromSdk);

        RequestParameters requestParameters = new RequestParameters.Builder()
                .location(locationFromDeveloper)
                .build();
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        String adUrl = subject.withRequest(requestParameters)
                .generateUrlString("ads.mopub.com");
        assertThat(getParameterFromRequestUrl(adUrl, "ll")).isEqualTo("38.0,-123.0");
        assertThat(getParameterFromRequestUrl(adUrl, "lla")).isEqualTo("5");
        assertThat(getParameterFromRequestUrl(adUrl, "llsdk")).isEqualTo("1");
        // Only test to the full second (as there may be small differences)
        assertThat(getParameterFromRequestUrl(adUrl, "llf")).startsWith("123");
        assertThat(getParameterFromRequestUrl(adUrl, "llf").length()).isEqualTo(6);
    }

    @Test
    public void generateUrlString_withOnlyAdUnitSet_shouldReturnMinimumUrl() {
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);
        final String adUrl = generateMinimumUrlString();

        assertThat(adUrl).isEqualTo(
                "http://ads.mopub.com/m/ad?id=" +
                        AD_UNIT_ID +
                        "&nsv=" + MoPub.SDK_VERSION + 
                        "&dn=unknown%2Cunknown%2Cunknown" +
                        "&bundle=testBundle" +
                        "&z=-0700" +
                        "&o=u" +
                        "&w=" +
                        TEST_SCREEN_WIDTH +
                        "&h=" +
                        TEST_SCREEN_HEIGHT +
                        "&sc_a=" +
                        TEST_DENSITY +
                        // maven version
                        "&ct=3&av=1.0" +
                        // Gradle version
                        //"&ct=3&av=" + BuildConfig.VERSION_NAME +
                        "&udid=mp_tmpl_advertising_id&dnt=mp_tmpl_do_not_track");
    }

    @Test
    public void enableLocation_shouldIncludeLocationInUrl() {
        MoPub.setLocationAwareness(MoPub.LocationAwareness.NORMAL);
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);

        String requestString = generateMinimumUrlString();
        assertThat(getParameterFromRequestUrl(requestString, "ll")).isNotNull();
    }

    @Test
    public void disableLocation_shouldNotIncludeLocationInUrl() {
        MoPub.setLocationAwareness(MoPub.LocationAwareness.DISABLED);
        subject = new NativeUrlGenerator(context).withAdUnitId(AD_UNIT_ID);

        String requestString = generateMinimumUrlString();
        assertThat(getParameterFromRequestUrl(requestString, "ll")).isNullOrEmpty();
    }

    @Test
    public void disableLocationCollection_whenLocationServiceHasMostRecentLocation_shouldNotIncludeLocationInUrl() {
        MoPub.setLocationAwareness(MoPub.LocationAwareness.DISABLED);
        subject = new NativeUrlGenerator(context);

        // Mock out the LocationManager's last known location.
        ShadowLocationManager shadowLocationManager = Robolectric.shadowOf(
                (LocationManager) application.getSystemService(Context.LOCATION_SERVICE));
        Location locationFromSdk = new Location("");
        locationFromSdk.setLatitude(37);
        locationFromSdk.setLongitude(-122);
        locationFromSdk.setAccuracy(5.0f);
        locationFromSdk.setTime(2000);
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, locationFromSdk);

        String requestString = generateMinimumUrlString();
        assertThat(getParameterFromRequestUrl(requestString, "ll")).isNullOrEmpty();
    }

    private List<String> getDesiredAssetsListFromRequestUrlString(String requestString) {
        Uri requestUri = Uri.parse(requestString);

        String desiredAssetsString = requestUri.getQueryParameter("assets");
        return (desiredAssetsString == null) ? new ArrayList<String>() : Arrays.asList(desiredAssetsString.split(","));
    }

    private String getNetworkOperatorNameFromRequestUrl(String requestString) {
        Uri requestUri = Uri.parse(requestString);

        String networkOperatorName = requestUri.getQueryParameter("cn");

        if (TextUtils.isEmpty(networkOperatorName)) {
            return "";
        }

        return networkOperatorName;
    }

    private String getParameterFromRequestUrl(String requestString, String key) {
        Uri requestUri = Uri.parse(requestString);
        String parameter = requestUri.getQueryParameter(key);

        if (TextUtils.isEmpty(parameter)) {
            return "";
        }

        return parameter;
    }

    private String generateMinimumUrlString() {
        return subject.generateUrlString("ads.mopub.com");
    }
}
