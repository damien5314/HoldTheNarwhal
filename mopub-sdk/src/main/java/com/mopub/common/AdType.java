package com.mopub.common;

/**
 * Valid values for the "X-Adtype" header from the MoPub ad server. The value of this header
 * controls the custom event loading behavior.
 */
public class AdType {
    public static final String HTML = "html";
    public static final String MRAID = "mraid";
    public static final String INTERSTITIAL = "interstitial";
    public static final String NATIVE = "json";
    public static final String CUSTOM = "custom";
    public static final String CLEAR = "clear";
}
