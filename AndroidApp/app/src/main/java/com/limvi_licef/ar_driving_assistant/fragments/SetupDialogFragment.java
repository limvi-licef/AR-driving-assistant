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
import com.limvi_licef.ar_driving_assistant.utils.Constants;

public class SetupDialogFragment extends DialogFragment {

    public static SetupDialogFragment newInstance() {
        return new SetupDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText idText = new EditText(getActivity());
        final EditText ipText = new EditText(getActivity());

        idText.setHint(R.string.setup_dialog_id_placeholder);
        ipText.setHint(R.string.setup_dialog_ip_placeholder);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        idText.setText(settings.getString(Constants.ID_PREFERENCE, null));
        ipText.setText(settings.getString(Constants.IP_ADDRESS_PREFERENCE, null));

        layout.addView(idText);
        layout.addView(ipText);

        return new AlertDialog.Builder(getActivity())
        .setNegativeButton(R.string.setup_dialog_dismiss, null)
        .setPositiveButton(R.string.setup_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(Constants.ID_PREFERENCE, idText.getText().toString());
                editor.putString(Constants.IP_ADDRESS_PREFERENCE, ipText.getText().toString());
                editor.apply();
            }
        })
        .setView(layout)
        .create();
    }
}
