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
import com.limvi_licef.ar_driving_assistant.utils.Enums;

public class SendEventDialogFragment extends DialogFragment {

    public static SendEventDialogFragment newInstance() {
        return new SendEventDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final RadioGroup rg = new RadioGroup(getActivity());
        rg.setOrientation(RadioGroup.VERTICAL);
        for(Enums.EventTypes event : Enums.EventTypes.values()){
            RadioButton rb = new RadioButton(getActivity());
            rg.addView(rb);
            rb.setText(event.name());
        }
        rg.check(rg.getChildAt(0).getId());
        layout.addView(rg);

        final EditText eventText = new EditText(getActivity());
        eventText.setHint(R.string.send_event_task_text_placeholder);
        layout.addView(eventText);

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.send_event_dialog_dismiss, null)
                .setPositiveButton(R.string.send_event_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        int index = rg.getCheckedRadioButtonId() % rg.getChildCount();
                        RadioButton rb = (RadioButton)rg.getChildAt((index == 0) ? rg.getChildCount()-1 : index-1);
                        new SendEventTask(getActivity(), rb.getText().toString(), eventText.getText().toString()).execute();
                    }
                })
                .setView(layout)
                .create();
    }
}
