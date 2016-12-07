package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.Event;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.List;

public class DeleteEventsDialogFragment extends DialogFragment {

    private ArrayAdapter<String> arrayAdapter;
    private AlertDialog.Builder builder;
    private Activity hostActivity;

    public static DeleteEventsDialogFragment newInstance() {
        return new DeleteEventsDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        hostActivity = getActivity();
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
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(hostActivity);
                    builderInner.setTitle(hostActivity.getResources().getString(R.string.list_events_inner_dialog_title, item));
                    builderInner.setPositiveButton(R.string.list_events_delete_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(deleteEvent(item)) {
                                        arrayAdapter.remove(item);
                                        if(arrayAdapter.isEmpty()) {
                                            setEmptyView();
                                        }
                                    }
                                    builder.show();
                                }
                            });
                    builderInner.setNegativeButton(R.string.list_events_dialog_dismiss,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    builder.show();
                                }
                            });
                    builderInner.show();
                }
            });
        }

        return builder.create();
    }

    /**
     * Delete an event with the specified label
     * @param label the label of the event to delete
     * @return the success or failure of the delete operation
     */
    private boolean deleteEvent(String label) {
        SQLiteDatabase db = DatabaseHelper.getHelper(getActivity()).getWritableDatabase();
        return db.delete(DatabaseContract.TrainingEvents.TABLE_NAME, DatabaseContract.TrainingEvents.LABEL + " = ?", new String[]{label}) > 0;
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
