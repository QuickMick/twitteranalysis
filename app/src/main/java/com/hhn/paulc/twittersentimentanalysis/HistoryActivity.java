package com.hhn.paulc.twittersentimentanalysis;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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

import com.hhn.graphs.BarChartActivity;
import com.hhn.graphs.HistoryTimelineActivity;
import com.hhn.mick.emotionanalizer.AnalizationHelper;
import com.hhn.mick.emotionanalizer.AnalizationResult;
import com.hhn.mick.service.Constants;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 */
public class HistoryActivity extends AppCompatActivity implements View.OnClickListener{


    private ListView filesLv;

    private File[] listedFilesX = new File[0];
    private synchronized File[] getListedFiles(){
        synchronized (lock){
            return this.listedFilesX;
        }
    }

    private Button showHistoryTimelineBtn;

    private TextView folderTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        this.filesLv = (ListView)findViewById(R.id.fileslv);
        this.showHistoryTimelineBtn = (Button)findViewById(R.id.showhistorytimelinebtn);
        this.showHistoryTimelineBtn.setOnClickListener(this);

        this.folderTv = (TextView)findViewById(R.id.folder);

        this.filesLv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                File clickedFile = HistoryActivity.this.getListedFiles()[position];

                HistoryActivity.this.showSelectDialog(clickedFile);
                //Toast.makeText(HistoryActivity.this, clickedFile.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    protected void onResume(){
        super.onResume();
        this.rebuildView();
    }

    private void rebuildView(){
        AnalizationHelper.INSTANCE().loadSettings(this);

        this.folderTv.setText("/"+AnalizationHelper.INSTANCE().getAnalyzation_folder()+"/");
        final ProgressDialog dialog = ProgressDialog.show(HistoryActivity.this, "","Loading History. Please wait...", true);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(!HistoryActivity.this.requistPermission()){
                    return false;
                }
                HistoryActivity.this.listFiles();
                return true;
            }

            @Override
            public void onPostExecute(Boolean result){
                if(!result){
                    Toast.makeText(HistoryActivity.this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter adapter = new ArrayAdapter<File>(HistoryActivity.this, R.layout.simple_list_item_colored /*android.R.layout.simple_list_item_2*/, R.id.text1lbl,HistoryActivity.this.getListedFiles()  ) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(R.id.text1lbl);
                        TextView text2 = (TextView) view.findViewById(R.id.text2lbl);

                        text1.setText(HistoryActivity.this.getListedFiles() [position].getName());
                        text2.setText(HistoryActivity.this.getListedFiles() [position].getAbsolutePath());
                        return view;
                    }
                };

                HistoryActivity.this.filesLv.setAdapter(adapter);
                dialog.dismiss();
            }
        }.execute();
    }

    private Object lock = new Object();
    private synchronized void listFiles(){

            File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.INSTANCE().getAnalyzation_folder());
            if (!mydir.exists()) {
                return;
            }

            File[] tmpFiles = mydir.listFiles();
            ArrayList<File> cur = new ArrayList<File>();
            for (File f : tmpFiles) {
                if (f.getAbsolutePath().endsWith(".json")) {
                    cur.add(f);
                }
            }

            //sort the entries depending on the date, newest first
            Collections.sort(cur, new Comparator<File>() {
                public int compare(File o1, File o2) {

                    if (o1.lastModified() > o2.lastModified()) {
                        return -1;
                    } else if (o1.lastModified() < o2.lastModified()) {
                        return 1;
                    }
                    return 0;
                }

            });
            // Collections.reverse(cur);

        synchronized (lock) {
            this.listedFilesX = cur.toArray(new File[cur.size()]);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private boolean requistPermission(){
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
           // Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //Toast.makeText(this, "Error: external storage is read only.",Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.d("myAppName", "External storage is not read only or unavailable");

        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
           // Toast.makeText(this, "permission:WRITE_EXTERNAL_STORAGE: NOT granted!",Toast.LENGTH_SHORT).show();
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

            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if(v==this.showHistoryTimelineBtn){
            int len = this.getListedFiles().length;
            if( len <= 2){
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

      /*  DialogFragment d= new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {*/
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("What do you want do?")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                switch(options[which]){
                                    case Constants.HISORY.OPEN:

                                        // Load the History
                                        if(!AnalizationHelper.INSTANCE().isRunning()) {
                                            // load the history file in async task
                                            final ProgressDialog progressDialog = ProgressDialog.show(HistoryActivity.this, "","Saving. Please wait...", true);
                                            new AsyncTask<Void,Void,Boolean>(){


                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    try {
                                                        AnalizationHelper.INSTANCE().setFinalResult(AnalizationResult.createFromJSON(HistoryActivity.this.loadJSONFromFolder(file)));
                                                        AnalizationHelper.INSTANCE().setSaved(true);
                                                        return true;
                                                    } catch (JSONException e) {

                                                        return false;
                                                    }
                                                }

                                                @Override
                                                protected void onPostExecute(Boolean result) {
                                                    progressDialog.dismiss();
                                                    if(result){
                                                        Intent i = new Intent(HistoryActivity.this, BarChartActivity.class);
                                                        i.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_HISTORY);

                                                        HistoryActivity.this.startActivity(i);
                                                        Toast.makeText(HistoryActivity.this, file.getName()+" sucessfully loaded", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(HistoryActivity.this, "Error while loading file "+file.getName(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            }.execute();

                                        }else {
                                            Toast.makeText(HistoryActivity.this, "Cannot load Archived Analysis, because Analysis is running. Please stop it first.", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case Constants.HISORY.DELETE:
                                        // show "do you really want to delte " dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                                        builder.setTitle("Do you really want to delete "+file.getName());
                                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //delete
                                                file.delete();
                                                Toast.makeText(HistoryActivity.this, "File"+file.getName()+" successfully deleted", Toast.LENGTH_SHORT).show();
                                                HistoryActivity.this.rebuildView();
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
              //  return builder.create();
       /*     }
        };*/
                                builder.create().show();
       // d.show(this.getFragmentManager(),"SELECT_HISTORY_OPTION");
    }


}


/*


 */