package com.blogspot.developersu.nowol.nowol;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;

public class popUp extends DialogFragment {

    private EditText hostName;
    private Button submit;

    public interface pupUpRetuningValueListener{
        void onFinishEdit(String hostNameReSet);
    }

    public static popUp newInstance(CharSequence ipRecieved){
        popUp f = new popUp();
        Bundle myBundle = new Bundle();
        myBundle.putCharSequence("ipRec", ipRecieved);
        f.setArguments(myBundle);
        return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //getDialog().setTitle(R.string.hostChBntMain);

        View v = inflater.inflate(R.layout.popup, container, false);
        submit = (Button) v.findViewById(R.id.popupHostBtn);
        hostName = (EditText) v.findViewById(R.id.popUpHostText);


        hostName.setText(getArguments().getCharSequence("ipRec"));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pupUpRetuningValueListener retLs = (pupUpRetuningValueListener)getActivity();
                retLs.onFinishEdit(hostName.getText().toString());
                dismiss();
            }
        });

        return v;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


}