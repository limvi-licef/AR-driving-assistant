package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.activities.MainActivity;
import com.limvi_licef.ar_driving_assistant.models.Event;

public class CreateTrainingEventDialogFragment extends DialogFragment {

    public static CreateTrainingEventDialogFragment newInstance() {
        return new CreateTrainingEventDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create alert dialog layout
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        //Add textfield for the label of the training event to be created
        final EditText labelField = new EditText(getActivity());
        labelField.setHint(getResources().getString(R.string.training_task_hint_label));
        layout.addView(labelField);

        //The type of the event to be created
        final RadioGroup rg = new RadioGroup(getActivity());
        rg.setOrientation(RadioGroup.VERTICAL);
        for(Event.EventTypes event : Event.EventTypes.values()){
            RadioButton rb = new RadioButton(getActivity());
            rg.addView(rb);
            rb.setText(event.name());
        }
        //Select first option
        rg.check(rg.getChildAt(0).getId());
        layout.addView(rg);

        //Add textfield for the message associated with the training event
        final EditText eventText = new EditText(getActivity());
        eventText.setHint(getResources().getString(R.string.training_task_hint_message));
        layout.addView(eventText);

        return new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.training_task_dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Send all values back to MainActivity
                        long startTimestamp = System.currentTimeMillis();
                        String label = labelField.getText().toString();
                        int index = rg.getCheckedRadioButtonId() % rg.getChildCount();
                        RadioButton rb = (RadioButton)rg.getChildAt((index == 0) ? rg.getChildCount()-1 : index-1);
                        String type = rb.getText().toString();
                        String message = eventText.getText().toString();
                        MainActivity activity = (MainActivity)getActivity();
                        activity.setTrainingData(startTimestamp, label, message, type);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.training_task_dialog_dismiss), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Untoggle button
                        MainActivity activity = (MainActivity)getActivity();
                        activity.trainToggle.setChecked(false);
                    }
                })
                .show();
    }
}
