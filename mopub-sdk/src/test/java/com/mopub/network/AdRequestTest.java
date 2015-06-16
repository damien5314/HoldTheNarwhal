package com.mopub.network;

import android.app.Activity;
import android.location.Location;

import com.mopub.common.AdFormat;
import com.mopub.common.AdType;
import com.mopub.common.DataKeys;
import com.mopub.common.event.BaseEvent;
import com.mopub.common.event.EventDispatcher;
import com.mopub.common.event.MoPubEvents;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.ResponseHeader;
import com.mopub.volley.NetworkResponse;
import com.mopub.volley.Response;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class AdRequestTest {

    @Mock private AdRequest.Listener mockListener;
    @Mock private AdResponse mockAdResponse;
    @Mock private EventDispatcher mockEventDispatcher;

    private AdRequest subject;
    private HashMap<String, String> defaultHeaders;
    private Activity activity;
    private String adUnitId;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
        adUnitId = "testAdUnitId";
        subject = new AdRequest("testUrl", AdFormat.NATIVE, adUnitId, activity, mockListener);
        defaultHeaders = new HashMap<String, String>();
        defaultHeaders.put(ResponseHeader.SCROLLABLE.getKey(), "0");
        defaultHeaders.put(ResponseHeader.REDIRECT_URL.getKey(), "redirect");
        defaultHeaders.put(ResponseHeader.CLICK_TRACKING_URL.getKey(), "click_tracking");
        defaultHeaders.put(ResponseHeader.IMPRESSION_URL.getKey(), "impression");
        defaultHeaders.put(ResponseHeader.FAIL_URL.getKey(), "fail_url");
        defaultHeaders.put(ResponseHeader.REFRESH_TIME.getKey(), "30");

        MoPubEvents.setEventDispatcher(mockEventDispatcher);
    }

    @After
    public void teardown() {
        MoPubEvents.setEventDispatcher(null);
    }

    @Test
    public void parseNetworkResponse_stringBody_shouldSucceed() throws Exception {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.HTML);
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        assertThat(response.result).isNotNull();
        assertThat(response.result.getStringBody()).isEqualTo("abc");
    }

    @Test
    public void parseNetworkResponse_withStringBody_shouldLogScribeEvent() throws Exception {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.HTML);
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        subject.parseNetworkResponse(testResponse);

        verify(mockEventDispatcher).dispatch(any(BaseEvent.class));
    }

    @Test
    public void parseNetworkResponse_withServerExtrasInResponseBody_shouldSucceed() throws Exception {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.HTML);
        defaultHeaders.put(ResponseHeader.FULL_AD_TYPE.getKey(), "anything");
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        // Check the server extras
        final Map<String, String> serverExtras = response.result.getServerExtras();
        assertThat(serverExtras).isNotNull();
        assertThat(serverExtras).isNotEmpty();
        assertThat(serverExtras.get(DataKeys.SCROLLABLE_KEY)).isEqualToIgnoringCase("false");
        assertThat(serverExtras.get(DataKeys.REDIRECT_URL_KEY)).isEqualToIgnoringCase("redirect");
        assertThat(serverExtras.get(DataKeys.CLICKTHROUGH_URL_KEY)).isEqualToIgnoringCase("click_tracking");
    }

    @Test
    public void parseNetworkResponse_nonJsonStringBodyForNative_jsonParseShouldFail() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.NATIVE);
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        assertThat(response.error).isNotNull();
        assertThat(response.error).isExactlyInstanceOf(MoPubNetworkError.class);
        assertThat(((MoPubNetworkError) response.error).getReason()).isEqualTo(MoPubNetworkError.Reason.BAD_BODY);
    }

    @Test
    public void parseNetworkResponse_nonJsonStringBodyForNative_shouldNotLogScribeEvent() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.NATIVE);
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        subject.parseNetworkResponse(testResponse);

        verify(mockEventDispatcher, never()).dispatch(any(BaseEvent.class));
    }

    @Test
    public void parseNetworkResponse_withWarmupHeaderTrue_shouldError() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.NATIVE);
        defaultHeaders.put(ResponseHeader.WARMUP.getKey(), "1");
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        assertThat(response.error).isNotNull();
        assertThat(response.error).isInstanceOf(MoPubNetworkError.class);
        assertThat(((MoPubNetworkError) response.error).getReason()).isEqualTo(MoPubNetworkError.Reason.WARMING_UP);
    }

    @Test
    public void parseNetworkResponse_withWarmupHeaderTrue_shouldNotLogScribeEvent() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.NATIVE);
        defaultHeaders.put(ResponseHeader.WARMUP.getKey(), "1");
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        subject.parseNetworkResponse(testResponse);

        verify(mockEventDispatcher, never()).dispatch(any(BaseEvent.class));
    }

    @Test
    public void parseNetworkResponse_withRefreshTime_shouldIncludeRefreshTimeInResult() {
        defaultHeaders.put(ResponseHeader.REFRESH_TIME.getKey(), "13");
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);
        assertThat(response.result.getRefreshTimeMillis()).isEqualTo(13000);
    }

    @Test
    public void parseNetworkResponse_withoutRefreshTime_shouldNotIncludeRefreshTime() {
        defaultHeaders.remove(ResponseHeader.REFRESH_TIME.getKey());
        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);

        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);
        assertThat(response.result.getRefreshTimeMillis()).isNull();
    }
    
    @Test
    public void parseNetworkResponse_withClearAdType_withRefreshTimeHeader_shouldErrorAndIncludeRefreshTime() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.CLEAR);

        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        assertThat(response.error).isNotNull();
        assertThat(response.error).isInstanceOf(MoPubNetworkError.class);
        final MoPubNetworkError moPubNetworkError = (MoPubNetworkError) response.error;
        assertThat(moPubNetworkError.getReason()).isEqualTo(MoPubNetworkError.Reason.NO_FILL);
        assertThat(moPubNetworkError.getRefreshTimeMillis()).isEqualTo(30000);
    }

    @Test
    public void parseNetworkResponse_withClearAdType_withNoRefreshTimeHeader_shouldErrorAndNotIncludeRefreshTime() {
        defaultHeaders.remove(ResponseHeader.REFRESH_TIME.getKey());
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.CLEAR);

        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        final Response<AdResponse> response = subject.parseNetworkResponse(testResponse);

        assertThat(response.error).isNotNull();
        assertThat(response.error).isInstanceOf(MoPubNetworkError.class);
        final MoPubNetworkError moPubNetworkError = (MoPubNetworkError) response.error;
        assertThat(moPubNetworkError.getReason()).isEqualTo(MoPubNetworkError.Reason.NO_FILL);
        assertThat(moPubNetworkError.getRefreshTimeMillis()).isNull();
    }

    @Test
    public void parseNetworkResponse_withClearAdType_shouldLogScribeEvent() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.CLEAR);

        NetworkResponse testResponse =
                new NetworkResponse(200, "abc".getBytes(Charset.defaultCharset()), defaultHeaders, false);
        subject.parseNetworkResponse(testResponse);

        verify(mockEventDispatcher).dispatch(any(BaseEvent.class));
    }

    @Test
    public void parseNetworkResponse_withBadJSON_shouldReturnError() {
        defaultHeaders.put(ResponseHeader.AD_TYPE.getKey(), AdType.NATIVE);
        NetworkResponse badNativeNetworkResponse = new NetworkResponse(200,
                "{[abc}".getBytes(Charset.defaultCharset()),
                defaultHeaders, false);
        subject = new AdRequest("testUrl", AdFormat.NATIVE, "testAdUnitId", activity, mockListener);

        final Response<AdResponse> response = subject.parseNetworkResponse(badNativeNetworkResponse);

        assertThat(response.error).isNotNull();
        assertThat(response.error.getCause()).isExactlyInstanceOf(JSONException.class);
    }

    @Test
    public void deliverResponse_shouldCallListenerOnSuccess() throws Exception {
        subject.deliverResponse(mockAdResponse);
        verify(mockListener).onSuccess(mockAdResponse);
    }

    @Test
    public void getRequestId_shouldParseAndReturnRequestIdFromFailUrl() throws Exception {
        String requestId = subject.getRequestId("http://ads.mopub.com/m/ad?id=8cf00598d3664adaaeccd800e46afaca&exclude=043fde1fe2f9470c9aa67fec262a0596&request_id=7fd6dd3bf1c84f87876b4740c1dd7baa&fail=1");

        assertThat(requestId).isEqualTo("7fd6dd3bf1c84f87876b4740c1dd7baa");
    }

    @Test
    public void getRequestId_withNullFailUrl_shouldReturnNull() throws Exception {
        assertThat(subject.getRequestId(null)).isNull();
    }

    @Test
    public void getRequestId_withUrlWithNoRequestIdParam_shouldReturnNull() throws Exception {
        assertThat(subject.getRequestId("http://ads.mopub.com/m/ad?id=8cf00598d3664adaaeccd800e46afaca")).isNull();
    }

    @Test
    public void getHeaders_withDefaultLocale_shouldReturnDefaultLanguageCode() throws Exception {
        Map<String, String> expectedHeaders = new TreeMap<String, String>();
        expectedHeaders.put(ResponseHeader.ACCEPT_LANGUAGE.getKey(), "en");

        assertThat(subject.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    public void getHeaders_withUserPreferredLocale_shouldReturnUserPreferredLanguageCode() throws Exception {
        Map<String, String> expectedHeaders = new TreeMap<String, String>();
        expectedHeaders.put(ResponseHeader.ACCEPT_LANGUAGE.getKey(), "fr");

        // Assume user-preferred locale is fr_CA
        activity.getResources().getConfiguration().locale = Locale.CANADA_FRENCH;

        assertThat(subject.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    public void getHeaders_withUserPreferredLocaleAsNull_shouldReturnDefaultLanguageCode() throws Exception {
        Map<String, String> expectedHeaders = new TreeMap<String, String>();
        expectedHeaders.put(ResponseHeader.ACCEPT_LANGUAGE.getKey(), "en");

        // Assume user-preferred locale is null
        activity.getResources().getConfiguration().locale = null;

        assertThat(subject.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    public void getHeaders_withUserPreferredLanguageAsEmptyString_shouldReturnDefaultLanguageCode() throws Exception {
        Map<String, String> expectedHeaders = new TreeMap<String, String>();
        expectedHeaders.put(ResponseHeader.ACCEPT_LANGUAGE.getKey(), "en");

        // Assume user-preferred locale's language code is empty string after trimming
        activity.getResources().getConfiguration().locale = new Locale(" ");

        assertThat(subject.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    public void getHeaders_withLocaleLanguageAsEmptyString_shouldNotAddLanguageHeader() throws Exception {
        Map<String, String> expectedHeaders = Collections.emptyMap();

        // Assume default locale's language code is empty string
        Locale.setDefault(new Locale(""));

        // Assume user-preferred locale's language code is empty string after trimming
        activity.getResources().getConfiguration().locale = new Locale(" ");

        assertThat(subject.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    public void logScribeEvent_shouldLogEvent() throws Exception {
        AdResponse mockAdResponse = mock(AdResponse.class);
        when(mockAdResponse.getDspCreativeId()).thenReturn("dsp_creative_id");
        when(mockAdResponse.getAdType()).thenReturn("html");
        when(mockAdResponse.getNetworkType()).thenReturn("network_type");
        when(mockAdResponse.getWidth()).thenReturn(320);
        when(mockAdResponse.getHeight()).thenReturn(50);
        when(mockAdResponse.getRequestId()).thenReturn("ac298c522b0e412b85ff81e4b9b51f03");

        NetworkResponse networkResponse = new NetworkResponse(200, null, null, false, 300);

        Location mockLocation = mock(Location.class);
        when(mockLocation.getLatitude()).thenReturn(37.7833);
        when(mockLocation.getLongitude()).thenReturn(-122.4167);
        when(mockLocation.getAccuracy()).thenReturn((float) 2000.0);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BaseEvent baseEvent = (BaseEvent) args[0];
                assertThat(baseEvent.getName()).isEqualTo(BaseEvent.Name.AD_REQUEST);
                assertThat(baseEvent.getCategory()).isEqualTo(BaseEvent.Category.REQUESTS);
                assertThat(baseEvent.getSamplingRate()).isEqualTo(0.1);
                assertThat(baseEvent.getAdUnitId()).isEqualTo(adUnitId);
                assertThat(baseEvent.getAdCreativeId()).isEqualTo("dsp_creative_id");
                assertThat(baseEvent.getAdType()).isEqualTo("html");
                assertThat(baseEvent.getAdNetworkType()).isEqualTo("network_type");
                assertThat(baseEvent.getAdWidthPx()).isEqualTo(320);
                assertThat(baseEvent.getAdHeightPx()).isEqualTo(50);
                assertThat(baseEvent.getGeoLat()).isEqualTo(37.7833);
                assertThat(baseEvent.getGeoLon()).isEqualTo(-122.4167);
                assertThat(baseEvent.getGeoAccuracy()).isEqualTo(2000.0);
                assertThat(baseEvent.getPerformanceDurationMs()).isEqualTo(300);
                assertThat(baseEvent.getRequestId()).isEqualTo("ac298c522b0e412b85ff81e4b9b51f03");
                assertThat(baseEvent.getRequestStatusCode()).isEqualTo(200);
                assertThat(baseEvent.getRequestUri()).isEqualTo("testUrl");
                return null;
            }
        }).when(mockEventDispatcher).dispatch(any(BaseEvent.class));

        subject.logScribeEvent(mockAdResponse, networkResponse, mockLocation);

        verify(mockEventDispatcher).dispatch(any(BaseEvent.class));
    }
}
