package com.example.haotian.tutorial32;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Ryan on 10/24/2015.
 */
public class TitleSnippetDialogFragment extends DialogFragment{
    public String title;
    public String snippet;
    public Marker marker;
    public MapsActivity context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_title_snippet, null);
        builder.setView(v);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()  {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save input from EditTexts
                EditText titleText = (EditText) v.findViewById(R.id.Edit_Title);
                title = titleText.getText().toString();
                EditText snippetText = (EditText) v.findViewById(R.id.Edit_Snippet);
                snippet = snippetText.getText().toString();
                CheckBox deleteCheck = (CheckBox) v.findViewById(R.id.Delete_Check);
                marker.setTitle(title);
                marker.setSnippet(snippet);
                try {context.updateTitleAndSnippet(marker,title,snippet);}
                catch (java.io.IOException e){
                    e.printStackTrace();
                }
                if (deleteCheck.isChecked()){
                    try {context.removeMarker(marker);}
                    catch (java.io.IOException e){
                        e.printStackTrace();
                    }
                    marker.remove();
                }
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

    public static TitleSnippetDialogFragment createFragment(Marker m, MapsActivity context){
        TitleSnippetDialogFragment t = new TitleSnippetDialogFragment();
        t.marker = m;
        t.context = context;
        return t;
    }
}
