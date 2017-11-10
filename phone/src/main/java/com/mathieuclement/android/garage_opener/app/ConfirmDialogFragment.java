package com.mathieuclement.android.garage_opener.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author Mathieu Clément
 * @since 29.07.2013
 */
abstract class ConfirmDialogFragment extends DialogFragment {
    private String titleStr;
    private String messageStr;

    private int titleId = -1;
    private int messageId = -1;

    public ConfirmDialogFragment(String title, String message) {
        super();
        this.titleStr = title;
        this.messageStr = message;
    }

    public ConfirmDialogFragment(int title, int message) {
        super();
        this.titleId = title;
        this.messageId = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (titleId != -1) {
            builder.setTitle(titleId);
            builder.setMessage(messageId);
        } else {
            builder.setTitle(titleStr);
            builder.setMessage(messageStr);
        }
        builder.setPositiveButton(R.string.confirm_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                onPositiveButtonClicked();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.confirm_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                onNegativeButtonClicked();
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    protected abstract void onPositiveButtonClicked();

    protected abstract void onNegativeButtonClicked();
}
