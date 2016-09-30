package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import com.limvi_licef.ar_driving_assistant.R;

public class UserDialogFragment extends DialogFragment {

    private final String idPref = getResources().getString(R.string.user_id_pref);

    public static UserDialogFragment newInstance() {
        return new UserDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText idtext = new EditText(getActivity());
        idtext.setHint(R.string.user_dialog_id_placeholder);
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        idtext.setText(settings.getString(idPref, null));
//        if(settings.contains(getResources().getString(R.string.user_id_pref))) {
//            idtext.setText(settings.getString(idPref, ""));
//        }
        return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.user_dialog_title)
        .setNegativeButton(R.string.user_dialog_dismiss, null)
        .setPositiveButton(R.string.user_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(idPref, idtext.getText().toString());
                editor.apply();
            }
        })
        .create();
    }
}
