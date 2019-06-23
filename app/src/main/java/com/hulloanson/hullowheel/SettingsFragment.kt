package com.hulloanson.hullowheel


import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        val addressPref = findPreference<EditTextPreference>("address")
        addressPref?.setOnPreferenceChangeListener { _, newValue ->
            val valid = matches(newValue as String)
            if (!valid) {
                Handler().post {
                    addressPref.performClick()
                    addressPref.text = newValue
                    Toast.makeText(context,
                            "Not a correct address. Try again!",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
            valid
        }
    }
    /**
     * Called during [.onCreate] to supply the preferences for this fragment.
     * Subclasses are expected to call [.setPreferenceScreen] either
     * directly or via helper methods such as [.addPreferencesFromResource].
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     * [PreferenceScreen] with this key.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)
    }

}
