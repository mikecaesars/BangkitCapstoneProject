package com.dicoding.summariz_it;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import org.jetbrains.annotations.NotNull;

public class SaveDialog extends AppCompatDialogFragment {
    private EditText summaryName;
    private SaveDialogListener saveDialogListener;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view)
                .setTitle("Save Summary")
                .setCancelable(true)
                .setNegativeButton("cancel", (dialog, which) -> {
                })
                .setPositiveButton("save", (dialog, which) -> {
                    summaryName.getText().toString();
                    String saveName = summaryName.getText().toString();
                    saveDialogListener.applyText(saveName);
                });
        summaryName = view.findViewById(R.id.saveEditText);
        return builder.create();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            saveDialogListener = (SaveDialogListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement SaveDialogListener");
        }
    }

    public interface SaveDialogListener {
        void applyText(String name);
    }
}
