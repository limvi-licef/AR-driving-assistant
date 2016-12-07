package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.activities.MainActivity;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.Event;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.List;

public class ListEventsDialogFragment extends DialogFragment {

    private ArrayAdapter<String> arrayAdapter;
    private AlertDialog.Builder builder;

    public static ListEventsDialogFragment newInstance() {
        return new ListEventsDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());

        //Create and populate arrayAdapter from events
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_events);
        List<Event> events = Database.getAllEvents(getActivity());
        for(Event e : events) {
            arrayAdapter.add(e.label);
        }

        //set dialog title and buttons
        builder.setTitle(R.string.list_events_dialog_title)
                .setNegativeButton(R.string.list_events_dialog_dismiss, null)
                .setPositiveButton(R.string.list_events_delete_all_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteAllEvents();
                    }
                });

        //show empty message view if no events are found
        if(arrayAdapter.isEmpty()) {
            setEmptyView();
        } else {
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //display delete confirmation dialog on item click
                    final String item = arrayAdapter.getItem(which);
                    ((MainActivity)getActivity()).showDeleteEventDialog(item);
                }
            });
        }
        return builder.create();
    }

    /**
     * Delete all events
     * @return the success or failure of the delete operation
     */
    private boolean deleteAllEvents() {
        SQLiteDatabase db = DatabaseHelper.getHelper(getActivity()).getWritableDatabase();
        return db.delete(DatabaseContract.TrainingEvents.TABLE_NAME, "1", null) > 0;
    }

    /**
     * Replace adapter with a textview when no events are found
     */
    private void setEmptyView() {
        final TextView emptyMsg = new TextView(getActivity());
        emptyMsg.setText(R.string.list_events_empty);
        emptyMsg.setGravity(Gravity.CENTER);
        builder.setView(emptyMsg);
    }
}
