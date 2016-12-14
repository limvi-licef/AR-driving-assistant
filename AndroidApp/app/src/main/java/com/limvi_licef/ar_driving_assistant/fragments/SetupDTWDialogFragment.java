package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
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

        final TextView accelLabel = new TextView(getActivity());
        accelLabel.setText(R.string.setup_dtw_accel_label);
        final EditText accelText = new EditText(getActivity());
        accelText.setText(String.valueOf(Preferences.getDouble(settings, Preferences.ACCEL_DISTANCE_CUTOFF, DynamicTimeWarping.DEFAULT_ACCELERATION_DISTANCE_CUTOFF)));

        final TextView rotationLabel = new TextView(getActivity());
        rotationLabel.setText(R.string.setup_dtw_rotation_label);
        final EditText rotationText = new EditText(getActivity());
        rotationText.setText(String.valueOf(Preferences.getDouble(settings, Preferences.ROTATION_DISTANCE_CUTOFF, DynamicTimeWarping.DEFAULT_ROTATION_DISTANCE_CUTOFF)));

        final TextView speedLabel = new TextView(getActivity());
        speedLabel.setText(R.string.setup_dtw_speed_label);
        final EditText speedText = new EditText(getActivity());
        speedText.setText(String.valueOf(Preferences.getDouble(settings, Preferences.SPEED_DISTANCE_CUTOFF, DynamicTimeWarping.DEFAULT_SPEED_DISTANCE_CUTOFF)));

        //add checkboxes to layout
        layout.addView(cbAcceleration);
        layout.addView(cbRotation);
        layout.addView(cbSpeed);
        layout.addView(accelLabel);
        layout.addView(accelText);
        layout.addView(rotationLabel);
        layout.addView(rotationText);
        layout.addView(speedLabel);
        layout.addView(speedText);

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

                        //save distance cutoff values to shared preferences
                        try {
                            Preferences.putDouble(editor, Preferences.ACCEL_DISTANCE_CUTOFF, Double.parseDouble(accelText.getText().toString()));
                        } catch (NumberFormatException e) {
                            //do not save if not a number
                        }
                        try {
                            Preferences.putDouble(editor, Preferences.ROTATION_DISTANCE_CUTOFF, Double.parseDouble(rotationText.getText().toString()));
                        } catch (NumberFormatException e) {
                            //do not save if not a number
                        }
                        try {
                            Preferences.putDouble(editor, Preferences.SPEED_DISTANCE_CUTOFF, Double.parseDouble(speedText.getText().toString()));
                        } catch (NumberFormatException e) {
                            //do not save if not a number
                        }
                        editor.apply();
                    }
                })
                .setView(layout)
                .create();
    }
}
