package com.elneo.elneoscan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class EditQuantityDialogFragment extends DialogFragment
                                        implements TextView.OnEditorActionListener {
    private EditText editText;
    static private MainActivity mainActivity;

    public interface EditQuantityDialogListener {
        void onFinishEditDialog(String input);
    }

    public EditQuantityDialogFragment() {}

    public static EditQuantityDialogFragment newInstance(String title, MainActivity main) {
        mainActivity = main;
        EditQuantityDialogFragment fragment = new EditQuantityDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_qty, null);

        editText = (EditText) view.findViewById(R.id.frag_ed_edit_qty);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString("title", "Set quantity");
        dialogBuilder.setTitle(title);
        dialogBuilder.setView(view);
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.onFinishEditDialog(editText.getText().toString());
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        editText.requestFocus();
        return dialog;
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            EditQuantityDialogListener listener = (EditQuantityDialogListener) getActivity();
            listener.onFinishEditDialog(editText.getText().toString());
            dismiss();
            return true;
        }
        return false;
    }
}