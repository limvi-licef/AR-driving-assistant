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
import com.limvi_licef.ar_driving_assistant.utils.Constants;

public class UserDialogFragment extends DialogFragment {

    private String idPref;

    public static UserDialogFragment newInstance() {
        return new UserDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        idPref = getResources().getString(R.string.user_id_pref);
        final EditText idtext = new EditText(getActivity());
        idtext.setHint(R.string.user_dialog_id_placeholder);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        idtext.setText(settings.getString(idPref, null));

        return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.user_dialog_title)
        .setNegativeButton(R.string.user_dialog_dismiss, null)
        .setPositiveButton(R.string.user_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(idPref, idtext.getText().toString());
                editor.apply();
            }
        })
        .setView(idtext)
        .create();
    }
}
