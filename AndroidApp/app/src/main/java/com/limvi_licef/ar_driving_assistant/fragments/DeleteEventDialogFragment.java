package com.limvi_licef.ar_driving_assistant.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.activities.MainActivity;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class DeleteEventDialogFragment extends DialogFragment {

    public static DeleteEventDialogFragment newInstance(String item) {
        DeleteEventDialogFragment fragment = new DeleteEventDialogFragment();
        Bundle args = new Bundle();
        args.putString("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String item = getArguments().getString("item");
        AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
        builderInner.setTitle(getActivity().getResources().getString(R.string.list_events_inner_dialog_title, item));
        builderInner.setPositiveButton(R.string.list_events_delete_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEvent(item);
                        ((MainActivity)getActivity()).showListEventDialog();
                    }
                });
        builderInner.setNegativeButton(R.string.list_events_dialog_dismiss,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity)getActivity()).showListEventDialog();
                    }
                });
        return builderInner.create();
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
}
