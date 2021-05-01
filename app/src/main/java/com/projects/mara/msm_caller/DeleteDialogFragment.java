package com.projects.mara.msm_caller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Yashasvi on 07-06-2017.
 */

public class DeleteDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder theDialog = new AlertDialog.Builder(getActivity());
        theDialog.setTitle("Delete Entry");
        theDialog.setMessage(R.string.delete_question);
        theDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((CallLog)getActivity()).delete();
            }
        });
        theDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().cancel();
            }
        });
        return theDialog.create();
    }
}
