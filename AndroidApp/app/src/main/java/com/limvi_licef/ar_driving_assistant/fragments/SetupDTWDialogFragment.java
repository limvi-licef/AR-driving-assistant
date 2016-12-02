package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.models.sensors.AccelerationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.RotationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.SpeedSensor;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

public class SetupDTWDialogFragment extends DialogFragment {

    public static SetupDTWDialogFragment newInstance() {
        return new SetupDTWDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        SharedPreferences settings = getActivity().getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        final CheckBox cbAcceleration = new CheckBox(getActivity());
        cbAcceleration.setText(AccelerationSensor.class.getSimpleName());
        cbAcceleration.setChecked(settings.getBoolean(AccelerationSensor.class.getSimpleName(), false));

        final CheckBox cbRotation = new CheckBox(getActivity());
        cbRotation.setText(RotationSensor.class.getSimpleName());
        cbRotation.setChecked(settings.getBoolean(RotationSensor.class.getSimpleName(), false));

        final CheckBox cbSpeed = new CheckBox(getActivity());
        cbSpeed.setText(SpeedSensor.class.getSimpleName());
        cbSpeed.setChecked(settings.getBoolean(SpeedSensor.class.getSimpleName(), false));

        //add checkboxes to layout
        layout.addView(cbAcceleration);
        layout.addView(cbRotation);
        layout.addView(cbSpeed);

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.setup_dialog_dismiss, null)
                .setPositiveButton(R.string.setup_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Save checkbox values to shared preferences
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                        editor.putBoolean(cbAcceleration.getText().toString(), cbAcceleration.isChecked());
                        editor.putBoolean(cbRotation.getText().toString(), cbRotation.isChecked());
                        editor.putBoolean(cbSpeed.getText().toString(), cbSpeed.isChecked());
                        editor.apply();
                    }
                })
                .setView(layout)
                .create();
    }
}
