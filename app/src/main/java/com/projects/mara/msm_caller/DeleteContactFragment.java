package com.projects.mara.msm_caller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Yashasvi on 13-06-2017.
 */

public class DeleteContactFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder theDialog = new AlertDialog.Builder(getActivity());
        theDialog.setTitle("Delete Contact");
        theDialog.setMessage(R.string.delete_contact);
        theDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MainActivity)getActivity()).deleteContact();
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
