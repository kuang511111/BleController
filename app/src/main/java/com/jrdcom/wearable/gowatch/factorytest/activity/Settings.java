
package com.jrdcom.wearable.gowatch.factorytest.activity;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.jrdcom.wearable.mb12m.minitest.R;

public class Settings extends Activity
{


    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle icicle)
        {
            super.onCreate(icicle);
            addPreferencesFromResource(R.xml.preferences);
            ListPreference listPreference = (ListPreference) findPreference("key_max_connect_value");


            listPreference.setOnPreferenceChangeListener((preference, newValue) ->
            {
                preference.setSummary("Current selected max is:" + newValue);
                return true;
            });


//            EditTextPreference mEditTextSetRom = (EditTextPreference) findPreference("set_rom_version");
//            EditTextPreference mEditTextSetCu = (EditTextPreference) findPreference("set_curef_version");
//
//            mEditTextSetRom.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
//            {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object obj)
//                {
//                    String rom = obj.toString();
//                    boolean isOK = MyUtils.match(MyUtils.romReg, rom);
//                    if ("V1.0.0".equals(rom))
//                    {
//                        isOK = false;
//                    }
//                    if (isOK)
//                    {
//                        return true;
//                    } else
//                    {
//                        Toast.makeText(SettingsFragment.this.getActivity(), "版本号格式不对，请参考：V1.0.1", Toast.LENGTH_LONG).show();
//                        return false;
//                    }
//                }
//            });
//
//            mEditTextSetCu.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
//            {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object obj)
//                {
//                    String cu = obj.toString();
//                    boolean isOK = MyUtils.match(MyUtils.regCu, cu);
//                    if (isOK)
//                    {
//                        return true;
//                    } else
//                    {
//                        Toast.makeText(SettingsFragment.this.getActivity(), "Curef格式不对，请参考：MB12-2AALEU0", Toast.LENGTH_LONG).show();
//                        return false;
//                    }
//                }
//            });
        }


    }
}
