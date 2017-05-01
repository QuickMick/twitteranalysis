package com.example.paulc.twittersentimentanalysis;

import android.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.graphs.BarChartActivity;
import com.example.graphs.DetailGraphActivity;
import com.example.graphs.HistoryTimelineActivity;
import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.service.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class HistoryActivity extends AppCompatActivity implements View.OnClickListener{


    private ListView filesLv;

    private File[] listedFiles;

    private Button showHistoryTimelineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        this.filesLv = (ListView)findViewById(R.id.fileslv);
        this.showHistoryTimelineBtn = (Button)findViewById(R.id.showhistorytimelinebtn);
        this.showHistoryTimelineBtn.setOnClickListener(this);



        final ProgressDialog dialog = ProgressDialog.show(HistoryActivity.this, "","Loading History. Please wait...", true);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HistoryActivity.this.requistPermission();
                HistoryActivity.this.listFiles();
                return "";
            }

            @Override
            public void onPostExecute(String result){
                ArrayAdapter adapter = new ArrayAdapter<File>(HistoryActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1,HistoryActivity.this.listedFiles  ) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        text1.setText(HistoryActivity.this.listedFiles [position].getName());
                        text2.setText(HistoryActivity.this.listedFiles [position].getAbsolutePath());
                        return view;
                    }
                };

                HistoryActivity.this.filesLv.setAdapter(adapter);
                dialog.dismiss();
            }
        }.execute();




        this.filesLv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                File clickedFile = HistoryActivity.this.listedFiles[position];

                HistoryActivity.this.showSelectDialog(clickedFile);
                //Toast.makeText(HistoryActivity.this, clickedFile.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listFiles(){
        File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.getAnalyzation_folder());
        if(!mydir.exists()) {
            return;
        }

        File[] tmpFiles = mydir.listFiles();
        ArrayList<File> cur = new ArrayList<File>();
        for(File f :tmpFiles){
            if(f.getAbsolutePath().endsWith(".json")) {
                cur.add(f);
            }
        }

        Collections.sort(cur);
        Collections.reverse(cur);


        this.listedFiles = cur.toArray(new File[cur.size()]);
    }

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private void requistPermission(){
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
            return;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(this, "Error: external storage is read only.",Toast.LENGTH_SHORT).show();
            return ;
        }
        Log.d("myAppName", "External storage is not read only or unavailable");

        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission:WRITE_EXTERNAL_STORAGE: NOT granted!",Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v==this.showHistoryTimelineBtn){
            if(this.listedFiles.length <= 2){
                Toast.makeText(HistoryActivity.this, "Not enough stored analysis to show a usefull graph. provide at least 2 analysises", Toast.LENGTH_SHORT).show();
            }else{
                startActivity(new Intent(this, HistoryTimelineActivity.class));
            }
        }
    }

    public String loadJSONFromFolder(File file) {
        String json = null;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    private void showSelectDialog(final File file){

        final String[] options = Constants.historyCommandsAsArray();

        DialogFragment d= new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("What do you want do?")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                switch(options[which]){
                                    case Constants.HISORY.OPEN:
                                        if(!AnalizationHelper.INSTANCE().isRunning()) {
                                            //TODO:load in asyncTask
                                            try {
                                                AnalizationHelper.INSTANCE().setFinalResult(AnalizationResult.createFromJSON(HistoryActivity.this.loadJSONFromFolder(file)));
                                            } catch (JSONException e) {
                                                Toast.makeText(HistoryActivity.this, "Error while loading file "+file.getName(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            Intent i = new Intent(HistoryActivity.this, BarChartActivity.class);
                                            i.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_HISTORY);
                                            startActivity(i);
                                            Toast.makeText(HistoryActivity.this, file.getName()+" sucessfully loaded", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(HistoryActivity.this, "Cannot load Archived Analysis, because Analysis is running. Please stop it first.", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case Constants.HISORY.DELETE:
                                        // show "do you really want to delte " dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Do you really want to delete "+file.getName());
                                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //delete
                                                file.delete();
                                                Toast.makeText(HistoryActivity.this, "File"+file.getName()+" successfully deleted", Toast.LENGTH_SHORT).show();
                                                HistoryActivity.this.listFiles();
                                                dialog.dismiss();
                                            }
                                        });

                                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //cancel
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.create().show();
                                        break;
                                    case Constants.HISORY.SHARE:
                                        Uri path = Uri.fromFile(file);
                                        Intent sendFileIntent = new Intent(Intent.ACTION_SEND);

                                        sendFileIntent.putExtra(Intent.EXTRA_STREAM, path);
                                        sendFileIntent.setType("application/json"); //sendFileIntent.setType("text/plain");
                                      /*  String to[] = {"asd@gmail.com"};
                                        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
                                        // the mail subject
                                        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");*/
                                        startActivity(Intent.createChooser(sendFileIntent , "Send file..."));

                                        break;
                                }

                            }
                        });
                return builder.create();
            }
        };

        d.show(this.getFragmentManager(),"SELECT_HISTORY_OPTION");
    }


}


/*


 */