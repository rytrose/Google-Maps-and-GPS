package com.example.haotian.tutorial32;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Ryan on 10/24/2015.
 */
public class TitleSnippetDialogFragment extends DialogFragment{
    public String title;
    public String snippet;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_title_snippet, null));

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()  {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save input from EditTexts
                EditText titleText = (EditText) getView().findViewById(R.id.Edit_Title);
                title = titleText.getText().toString();
                EditText snippetText = (EditText) getView().findViewById(R.id.Edit_Snippet);
                snippet = snippetText.getText().toString();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TitleSnippetDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
