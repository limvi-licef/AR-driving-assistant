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
import com.limvi_licef.ar_driving_assistant.tasks.SendEventTask;
import com.limvi_licef.ar_driving_assistant.utils.Events;

/**
 * DialogFragment used to send Event to Unity app
 */
public class SendEventDialogFragment extends DialogFragment {

    public static SendEventDialogFragment newInstance() {
        return new SendEventDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Add radio button group to dialog layout
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final RadioGroup rg = new RadioGroup(getActivity());
        rg.setOrientation(RadioGroup.VERTICAL);

        //Add a radio button for each EventTypes
        for(Events.EventTypes event : Events.EventTypes.values()){
            RadioButton rb = new RadioButton(getActivity());
            rg.addView(rb);
            rb.setText(event.name());
        }
        //Select first radio option
        rg.check(rg.getChildAt(0).getId());
        layout.addView(rg);

        //Add text field for the text to display on the unity app during the event
        final EditText eventText = new EditText(getActivity());
        eventText.setHint(R.string.send_event_task_text_placeholder);
        layout.addView(eventText);

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.send_event_dialog_dismiss, null)
                .setPositiveButton(R.string.send_event_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Get selected radio button text (modulo because the radio button ids are incremented because of each new dialog created)
                        int index = rg.getCheckedRadioButtonId() % rg.getChildCount();
                        RadioButton rb = (RadioButton)rg.getChildAt((index == 0) ? rg.getChildCount()-1 : index-1);
                        //Send event to unity app
                        new SendEventTask(getActivity(), rb.getText().toString(), eventText.getText().toString()).execute();
                    }
                })
                .setView(layout)
                .create();
    }
}
