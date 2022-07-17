/**  @author LiveWallpaper by Yoko Equilibrium (Yo Equilibrium)
             Undertale/Sans by Toby "Radiation" Fox
    Made for fan Fun and getting some EXP =)
    Send me message if you got bugs / requests / advices =)
    yoequilibrium @ gmail.com
 */
package com.yoequilibrium.sans;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class LiveWallpaperSettings extends PreferenceActivity 
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(
                LiveWallpaperService.PREFERENCES);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("MY pref","Changed in prefs act");//срабатывает в LiveWallpaperService
    }

}