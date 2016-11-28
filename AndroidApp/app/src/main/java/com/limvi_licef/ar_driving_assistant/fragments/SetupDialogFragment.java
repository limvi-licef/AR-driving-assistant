package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

/**
 * DialogFragment used to input current user ID and HoloLens Ip address
 */
public class SetupDialogFragment extends DialogFragment {

    public static SetupDialogFragment newInstance() {
        return new SetupDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Add two text fields to the dialog layout
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        //current user id
        final EditText idText = new EditText(getActivity());

        //Unity app ip address
        final EditText ipText = new EditText(getActivity());

        //Set placeholder text
        idText.setHint(R.string.setup_dialog_id_placeholder);
        ipText.setHint(R.string.setup_dialog_ip_placeholder);
        SharedPreferences settings = getActivity().getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        //Display previously entered text if it exists
        idText.setText(settings.getString(Preferences.ID_PREFERENCE, null));
        ipText.setText(settings.getString(Preferences.IP_ADDRESS_PREFERENCE, null));

        layout.addView(idText);
        layout.addView(ipText);

        return new AlertDialog.Builder(getActivity())
        .setNegativeButton(R.string.setup_dialog_dismiss, null)
        .setPositiveButton(R.string.setup_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //Save text field value to shared preferences
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(Preferences.ID_PREFERENCE, idText.getText().toString());
                editor.putString(Preferences.IP_ADDRESS_PREFERENCE, ipText.getText().toString());
                editor.apply();
            }
        })
        .setView(layout)
        .create();
    }
}
