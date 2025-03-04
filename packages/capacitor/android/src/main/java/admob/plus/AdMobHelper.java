package admob.plus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AdMobHelper {
    private final Adapter mAdapter;

    public AdMobHelper(Adapter adapter) {
        mAdapter = adapter;
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return String.format("%32s", bigInt.toString(16)).replace(' ', '0');
        } catch (NoSuchAlgorithmException ignore) {
        }
        return "";
    }

    @NonNull
    public String getDeviceId() {
        // This will request test ads on the emulator and device by passing this hashed device ID.
        @SuppressLint("HardwareIds") String ANDROID_ID = Settings.Secure.getString(mAdapter.getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        return md5(ANDROID_ID).toUpperCase();
    }

    public boolean isRunningInTestLab() {
        String testLabSetting = Settings.System.getString(mAdapter.getActivity().getContentResolver(), "firebase.test.lab");
        return "true".equals(testLabSetting);
    }

    public RequestConfiguration buildRequestConfiguration(JSONObject cfg) {
        return buildRequestConfiguration(cfg, false);
    }

    public RequestConfiguration buildRequestConfiguration(JSONObject cfg, boolean fromExisting) {
        RequestConfiguration.Builder builder = fromExisting ? MobileAds.getRequestConfiguration().toBuilder() : new RequestConfiguration.Builder();
        if (cfg.has("maxAdContentRating")) {
            builder.setMaxAdContentRating(cfg.optString("maxAdContentRating"));
        }
        Integer tagForChildDirectedTreatment = intFromBool(cfg, "tagForChildDirectedTreatment",
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED,
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE,
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE);
        if (tagForChildDirectedTreatment != null) {
            builder.setTagForChildDirectedTreatment(tagForChildDirectedTreatment);
        }
        Integer tagForUnderAgeOfConsent = intFromBool(cfg, "tagForUnderAgeOfConsent",
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED,
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE,
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE);
        if (tagForUnderAgeOfConsent != null) {
            builder.setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent);
        }
        if (cfg.has("testDeviceIds")) {
            List<String> testDeviceIds = new ArrayList<String>();
            JSONArray ids = cfg.optJSONArray("testDeviceIds");
            for (int i = 0; i < ids.length(); i++) {
                String testDeviceId = ids.optString(i);
                if (testDeviceId != null) {
                    testDeviceIds.add(testDeviceId);
                }
            }
            builder.setTestDeviceIds(testDeviceIds);
        }
        return builder.build();

    }

    @Nullable
    private Integer intFromBool(JSONObject cfg, String name, int vNull, int vTrue, int vFalse) {
        if (!cfg.has(name)) {
            return null;
        }
        if (cfg.opt(name) == null) {
            return vNull;
        }
        if (cfg.optBoolean(name)) {
            return vTrue;
        }
        return vFalse;
    }

    public interface Adapter {
        Activity getActivity();
    }
}
