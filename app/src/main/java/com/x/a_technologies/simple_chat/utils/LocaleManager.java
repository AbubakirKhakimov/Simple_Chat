package com.x.a_technologies.simple_chat.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.orhanobut.hawk.Hawk;
import java.util.Locale;

public class LocaleManager {
    /**
     * set current pref locale
     */
    public static Context setLocale(Context mContext) {
        if (getLanguagePref(mContext).equals("empty")){
            return mContext;
        }
        return updateResources(mContext, getLanguagePref(mContext));
    }
    /**
     * Get saved Locale from SharedPreferences
     *
     * @param mContext current context
     * @return current locale key by default return english locale
     */
    public static String getLanguagePref(Context mContext) {
        return Hawk.get("pref_lang", "empty");
    }
    /**
     * update resource
     */
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();

        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        res.updateConfiguration(config, res.getDisplayMetrics());
        return context;
    }
}