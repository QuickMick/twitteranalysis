package com.example.graphs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HistoryTimelineActivity extends AppCompatActivity  implements View.OnClickListener{


    public static final String SEPERATOR= ";";


    LineGraphSeries<DataPoint> anger_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> anticipation_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> disgust_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> fear_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> joy_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> sadness_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> surprise_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> trust_series = new LineGraphSeries<DataPoint>();

    LineGraphSeries<DataPoint> positive_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> negative_series = new LineGraphSeries<DataPoint>();

    private GraphView graph;
    private boolean showSentiment=false;

    private Button exportCsvButton;

    private DisplayValue[] displayValues;

    private TextView folderLbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_timeline);
        this.graph = (GraphView) findViewById(R.id.graph);
        this.exportCsvButton = (Button)findViewById(R.id.exportcsvbtn);
        this.folderLbl = (TextView)findViewById(R.id.folderlbl);
        this.exportCsvButton.setOnClickListener(this);
        this.initGraph();


        final ProgressDialog dialog = ProgressDialog.show(HistoryTimelineActivity.this, "","Loading History Data. Please wait...", true);
        new AsyncTask<Void, Void, DisplayValue[]>() {
            @Override
            protected DisplayValue[] doInBackground(Void... params) {
                return HistoryTimelineActivity.this.init();
            }

            @Override
            protected void onPostExecute(DisplayValue[] r){
                if(r == null){
                    Toast.makeText(HistoryTimelineActivity.this, "Error while loading files",Toast.LENGTH_SHORT).show();
                    finish();
                }
                HistoryTimelineActivity.this.displayValues = r;
                HistoryTimelineActivity.this.updateGraph(r);
                HistoryTimelineActivity.this.folderLbl.setText("/"+AnalizationHelper.INSTANCE().getAnalyzation_folder()+"/");
                dialog.dismiss();
            }
        }.execute();

    }

    private DisplayValue[] init(){
        File[] files = this.listFiles();

        if(files.length <=0) return null;

        ArrayList<DisplayValue> result = new ArrayList<DisplayValue>();
        try {
            for(File f :files){

                AnalizationResult ar = AnalizationResult.createFromJSON(this.loadJSONFromFolder(f));
                //   public DisplayValue(EmotionWeighting w, Date d, int words, int sentences, int tweets, int analizedWords){
                result.add(new DisplayValue(ar));
            }

        } catch (JSONException e) {
            e.printStackTrace();

            //finish();
            return null;
        }

        Collections.sort(result, new Comparator<DisplayValue>() {
            public int compare(DisplayValue o1, DisplayValue o2) {
                return o1.startDate.compareTo(o2.startDate);
            }
        });

        return result.toArray(new DisplayValue[result.size()]);

    }



    private void initGraph(){
        this.graph = (GraphView) findViewById(R.id.graph);
        this.graph.setOnClickListener(this);
        //  this.updateGraph();

        anger_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_ANGER);
        anticipation_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION);
        disgust_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_DISGUST);
        fear_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_FEAR);
        joy_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_JOY);
        sadness_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_SADNESS);
        surprise_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_SURPRISE);
        trust_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_TRUST);

        positive_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_POSITIVE);
        negative_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_NEGATIVE);


        anger_series.setColor(Color.parseColor("#d62032"));
        anticipation_series.setColor(Color.parseColor("#f46314"));
        disgust_series.setColor(Color.parseColor("#bf1899"));
        fear_series.setColor(Color.parseColor("#009661"));
        joy_series.setColor(Color.parseColor("#f8b313"));
        sadness_series.setColor(Color.parseColor("#3e7fc7"));
        surprise_series.setColor(Color.parseColor("#25c0ce"));
        trust_series.setColor(Color.parseColor("#99bc43"));

        positive_series.setColor(Color.parseColor("#f8b313"));
        negative_series.setColor(Color.parseColor("#999999"));


        this.addSeries();

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setBackgroundColor(Color.argb(30,0,0,0));
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss")));

/*
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1);
        graph.getViewport().setXAxisBoundsManual(true);*/
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);

        graph.getViewport().setScalable(false);
        graph.getViewport().setScrollable(false);
        graph.getViewport().setScalableY(false);

/*
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);*/

        //  graph.getViewport().setScalableY(false);
    }


    private void addSeries(){
        graph.removeAllSeries();

        if(!this.showSentiment) {
            graph.addSeries(anger_series);
            graph.addSeries(anticipation_series);
            graph.addSeries(disgust_series);
            graph.addSeries(fear_series);
            graph.addSeries(joy_series);
            graph.addSeries(sadness_series);
            graph.addSeries(surprise_series);
            graph.addSeries(trust_series);
        }else{
            graph.addSeries(positive_series);
            graph.addSeries(negative_series);
        }
    }

    private void updateGraph(DisplayValue[] steps){

        //  this.graph.removeAllSeries();

        // Log.d("analize_result",steps ==null?"UPDATE NO DATA":"UPDATE "+steps.length);
        if(steps == null || steps.length <2) return; //no data available

        DataPoint[] anger = new DataPoint[steps.length];
        DataPoint[] anticipation = new DataPoint[steps.length];
        DataPoint[] disgust = new DataPoint[steps.length];
        DataPoint[] fear = new DataPoint[steps.length];
        DataPoint[] joy = new DataPoint[steps.length];
        DataPoint[] sadness = new DataPoint[steps.length];
        DataPoint[] surprise = new DataPoint[steps.length];
        DataPoint[] trust = new DataPoint[steps.length];

        DataPoint[] positive = new DataPoint[steps.length];
        DataPoint[] negative = new DataPoint[steps.length];

        for(int i=0; i<steps.length;i++){
            //  for(int i=steps.length-1; i>=0;i--){
            float total = steps[i].weigthing.getTotalEmotion();
            if(total ==0)total=1;
            anger[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.anger)/total)*100f);
            anticipation[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.anticipation)/total)*100f);
            disgust[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.disgust)/total)*100f);
            fear[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.fear)/total)*100f);
            joy[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.joy)/total)*100f);
            sadness[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sadness)/total)*100f);
            surprise[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.surprise)/total)*100f);
            trust[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.trust)/total)*100f);

            float total_s = steps[i].weigthing.getTotalSentiment();
            positive[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sentiment_positive)/total_s)*100f);
            negative[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sentiment_negative)/total_s)*100f);
        }

      //  graph.removeAllSeries();

        anger_series.resetData(anger);
        anticipation_series.resetData(anticipation);
        disgust_series.resetData(disgust);
        fear_series.resetData(fear);
        joy_series.resetData(joy);
        sadness_series.resetData(sadness);
        surprise_series.resetData(surprise);
        trust_series.resetData(trust);

        positive_series.resetData(positive);
        negative_series.resetData(negative);

        graph.getViewport().setMinX(anger_series.getLowestValueX());
        graph.getViewport().setMaxX(anger_series.getHighestValueX());

    }


    private File[] listFiles(){
        File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.INSTANCE().getAnalyzation_folder());
        if(!mydir.exists()) {
            return new File[0];
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

        return cur.toArray(new File[cur.size()]);
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

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private boolean requistPermission(){
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(this, "Error: external storage is read only.",Toast.LENGTH_SHORT).show();
            return false;
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
            //    return false;

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
        return true;

    }

    @Override
    public void onClick(View v) {
        if(v == this.graph){
            this.showSentiment = !this.showSentiment;
            this.addSeries();

        }else if(v == this.exportCsvButton){
            this.exportAsCSV();
        }
    }


    private void exportAsCSV(){
        final DateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        final String fileName = AnalizationHelper.INSTANCE().getAnalyzation_folder()+"_"+format.format(HistoryTimelineActivity.this.displayValues[0].startDate)+"_-_" + format.format(HistoryTimelineActivity.this.displayValues[HistoryTimelineActivity.this.displayValues.length-1].startDate) + ".csv";

        final File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.TWITTER_EXPORTS_FOLDER); // AnalizationHelper.INSTANCE().getAnalyzation_folder());
        final File myFile = new File(mydir, fileName);

        if(myFile.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("*.CSV already exists. Do you want to overwrite it?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //delete
                    HistoryTimelineActivity.this.createCSV(mydir,myFile);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    HistoryTimelineActivity.this.shareFile(myFile,"Do you Want to share the existing *.CSV");
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }else{
            HistoryTimelineActivity.this.createCSV(mydir,myFile);

        }
    }

    private void createCSV(final File mydir, final File myFile){
        final ProgressDialog dialog = ProgressDialog.show(HistoryTimelineActivity.this, "","Saving. Please wait...", true);
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground (Void...params){

                if(!HistoryTimelineActivity.this.requistPermission()){
                    return "External Storage unavailable";
                }

                String time="Time";
                String timecode="Millis";

                String keywords="Keyowrds";
                String keywordsProhibited="Prohibited Keywords";

                String anger="Anger";
                String anticipation="Anticipation";
                String disgust="Disgust";
                String fear="Fear";
                String joy="Joy";
                String sadness="Sadness";
                String surprise="Surprise";
                String trust="Trust";

                String sentiment_negative="Negative";
                String sentiment_positive="Positive";



                String anger_p="Anger";
                String anticipation_p="Anticipation";
                String disgust_p="Disgust";
                String fear_p="Fear";
                String joy_p="Joy";
                String sadness_p="Sadness";
                String surprise_p="Surprise";
                String trust_p="Trust";

                String sentiment_negative_p="Negative";
                String sentiment_positive_p="Positive";

                String totalTweets = "Amount of Tweet";
                String totalSentences = "Amount of Sentences";
                String totalWords = "Amount of Word";
                String analizedWords = "Analized Words";
                String differentWords = "Different Words";


                String overall_top="Overall Top-List";
                String anger_top="Anger Top-List";
                String anticipation_top="Anticipation Top-List";
                String disgust_top="Disgust Top-List";
                String fear_top="Fear Top-List";
                String joy_top="Joy Top-List";
                String sadness_top="Sadness Top-List";
                String surprise_top="Surprise Top-List";
                String trust_top="Trust Top-List";

                String sentiment_negative_top="Negative Top-List";
                String sentiment_positive_top="Positive Top-List";


                final DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                for(DisplayValue dv : HistoryTimelineActivity.this.displayValues) {

                    time+=SEPERATOR+format.format(dv.startDate);
                    timecode+=SEPERATOR+dv.startDate.getTime();

                    totalTweets += SEPERATOR+dv.tweets;
                    totalSentences += SEPERATOR+dv.sentences;
                    totalWords += SEPERATOR+dv.words;
                    analizedWords += SEPERATOR+dv.analizedWords;
                    differentWords += SEPERATOR+dv.different_words_count;

                    anger+=SEPERATOR+dv.weigthing.anger;
                    anticipation+=SEPERATOR+dv.weigthing.anticipation;
                    disgust+=SEPERATOR+dv.weigthing.disgust;
                    fear+=SEPERATOR+dv.weigthing.fear;
                    joy+=SEPERATOR+dv.weigthing.joy;
                    sadness+=SEPERATOR+dv.weigthing.sadness;
                    surprise+=SEPERATOR+dv.weigthing.surprise;
                    trust+=SEPERATOR+dv.weigthing.trust;

                    sentiment_negative+=SEPERATOR+dv.weigthing.sentiment_negative;
                    sentiment_positive+=SEPERATOR+dv.weigthing.sentiment_positive;

                    double emotionTotal = dv.weigthing.getTotalEmotion();
                    double sentimentTotal = dv.weigthing.getTotalSentiment();

                    if(emotionTotal==0)emotionTotal=1;
                    if(sentimentTotal==0)sentimentTotal=1;

                    anger_p+=SEPERATOR+((((double)dv.weigthing.anger)/emotionTotal) *100d);
                    anticipation_p+=SEPERATOR+((((double)dv.weigthing.anticipation)/emotionTotal) *100d);
                    disgust_p+=SEPERATOR+((((double)dv.weigthing.disgust)/emotionTotal) *100d);
                    fear_p+=SEPERATOR+((((double)dv.weigthing.fear)/emotionTotal) *100d);
                    joy_p+=SEPERATOR+((((double)dv.weigthing.joy)/emotionTotal) *100d);
                    sadness_p+=SEPERATOR+((((double)dv.weigthing.sadness)/emotionTotal) *100d);
                    surprise_p+=SEPERATOR+((((double)dv.weigthing.surprise)/emotionTotal) *100d);
                    trust_p+=SEPERATOR+((((double)dv.weigthing.trust)/emotionTotal) *100d);

                    sentiment_negative_p+=SEPERATOR+((((double)dv.weigthing.sentiment_negative)/sentimentTotal) *100d);
                    sentiment_positive_p+=SEPERATOR+((((double)dv.weigthing.sentiment_positive)/sentimentTotal) *100d);




                    int TOPLIST_ELEMENTS = 10;
                    overall_top     +=SEPERATOR+dv.overall_top;
                    anger_top       +=SEPERATOR+dv.anger_top;
                    anticipation_top+=SEPERATOR+dv.anticipation_top;
                    disgust_top     +=SEPERATOR+dv.disgust_top;
                    fear_top        +=SEPERATOR+dv.fear_top;
                    joy_top         +=SEPERATOR+dv.joy_top;
                    sadness_top     +=SEPERATOR+dv.sadness_top;
                    surprise_top    +=SEPERATOR+dv.surprise_top;
                    trust_top       +=SEPERATOR+dv.trust_top;

                    sentiment_negative_top+=SEPERATOR+dv.sentiment_negative_top;
                    sentiment_positive_top+=SEPERATOR+dv.sentiment_positive_top;

                    keywords+=SEPERATOR+dv.keywords;
                    keywordsProhibited+=SEPERATOR+dv.keywords_prohibited;


                }

               /* String csv ="Total\n"+timecode+"\n"+time+"\n"+anger+"\n"+anticipation+"\n"+disgust+"\n"+fear+"\n"+joy+"\n"+sadness+"\n"+surprise+"\n"+trust+"\n"+sentiment_negative+"\n"+sentiment_positive;
                csv +="\n\nPercent\n"+timecode+"\n"+time+"\n"+anger_p+"\n"+anticipation_p+"\n"+disgust_p+"\n"+fear_p+"\n"+joy_p+"\n"+sadness_p+"\n"+surprise_p+"\n"+trust_p+"\n"+sentiment_negative_p+"\n"+sentiment_positive_p;
                csv +="\n\nStatistic\n"+totalTweets+"\n"+totalSentences+"\n"+totalWords+"\n"+analizedWords+"\n";
                csv=csv.replace(".",",");*/
                String totalCaption="TIME\n"+timecode+"\n"+time;

                String keys="\n\nFilters\n"+keywords+"\n"+keywordsProhibited+"";

                String totalData ="\n\nTotal Emotion Values\n"+anger+"\n"+anticipation+"\n"+disgust+"\n"+fear+"\n"+joy+"\n"+sadness+"\n"+surprise+"\n"+trust+"\n\nTotal Sentiment Values\n"+sentiment_negative+"\n"+sentiment_positive;
                String percentCaption="\n\nPercent Emotion Values\n";//+timecode+"\n"+time+"\n";

                String percentData =anger_p+"\n"+anticipation_p+"\n"+disgust_p+"\n"+fear_p+"\n"+joy_p+"\n"+sadness_p+"\n"+surprise_p+"\n"+trust_p+"\n\nPercent Sentiment Values\n"+sentiment_negative_p+"\n"+sentiment_positive_p;
                String statistic="\n\nStatistic\n"+totalTweets+"\n"+totalSentences+"\n"+totalWords+"\n"+analizedWords+"\n"+differentWords+"\n";

                String topLists= "\n\nOverall Top-List\n"+overall_top+"\n\nEmotion: Word Top-Lists\n"+anger_top+"\n"+anticipation_top+"\n"+disgust_top+"\n"+fear_top+"\n"+joy_top+"\n"+sadness_top+"\n"+surprise_top+"\n"+trust_top+"\n\nSentiment: Word Top-List\n"+sentiment_negative_top+"\n"+sentiment_positive_top;

                String csv=totalCaption+keys+totalData.replace(".",",")+percentCaption+percentData.replace(".",",")+statistic+topLists;


                try {
                  //  File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.INSTANCE().getAnalyzation_folder());
                    if (!mydir.exists()) {
                        mydir.mkdirs();
                    }

                  //  File myFile = new File(mydir, fileName);

                    Log.d("AppD", "save file: " + myFile.getAbsolutePath());

                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(csv);
                    myOutWriter.close();
                    fOut.close();


                    return null;    // saving successfull

                } catch (Exception e) {

                    return e.getMessage();   // saving not sucessful
                }
            }

            protected void onPostExecute (String result){

                dialog.dismiss();
                if(result==null) {
                    Toast.makeText(HistoryTimelineActivity.this, "Done writing Summary *.CSV to SD Card Folder (SD/"+AnalizationHelper.TWITTER_EXPORTS_FOLDER+") Filename "+myFile.getName(), Toast.LENGTH_LONG).show();
                    HistoryTimelineActivity.this.exportCsvButton.setVisibility(Button.INVISIBLE);
                    HistoryTimelineActivity.this.shareFile(myFile,"Do you want to share the generated *.CSV?");
                }else{
                    Toast.makeText(HistoryTimelineActivity.this, "Error while saving: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }




    private void shareFile(final File myFile,final String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryTimelineActivity.this);
        builder.setTitle(msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //delete
                Uri path = Uri.fromFile(myFile);
                Intent sendFileIntent = new Intent(Intent.ACTION_SEND);

                sendFileIntent.putExtra(Intent.EXTRA_STREAM, path);
                sendFileIntent.setType("application/csv");
                startActivity(Intent.createChooser(sendFileIntent , "Share file..."));

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
    }

    class DisplayValue{
        public static final int TOPLIST_ELEMENT_COUNT = 10;
        EmotionWeighting weigthing;
        int words;
        int sentences;
        int analizedWords;
        int tweets;
        Date startDate;
        int different_words_count=0;

        String keywords="";
        String keywords_prohibited = "";

        String overall_top="";
        String anger_top="";
        String anticipation_top="";
        String disgust_top="";
        String fear_top="";
        String joy_top="";
        String sadness_top="";
        String surprise_top="";
        String trust_top="";

        String sentiment_negative_top="";
        String sentiment_positive_top="";

        public DisplayValue(AnalizationResult ar){
            this.weigthing = ar.weigthing;
            this.startDate = ar.startDate;
            this.words = ar.wordCount;
            this.sentences = ar.sentenceCount;
            this.tweets = ar.tweetCount;
            this.analizedWords = ar.wordCountAnalized;

            this.different_words_count=ar.wordStatistic_all.size();

            this.overall_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_all),SEPERATOR);
            this.anger_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_anger),SEPERATOR);
            this.anticipation_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_anticipation),SEPERATOR);
            this.disgust_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_disgust),SEPERATOR);
            this.fear_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_fear),SEPERATOR);
            this.joy_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_joy),SEPERATOR);
            this.sadness_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_sadness),SEPERATOR);
            this.surprise_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_surprise),SEPERATOR);
            this.trust_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_trust),SEPERATOR);

            this.sentiment_negative_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_sentiment_negative),SEPERATOR);
            this.sentiment_positive_top+=this.wordListToString(TOPLIST_ELEMENT_COUNT,AnalizationResult.getTopList(ar.wordStatistic_sentiment_positive),SEPERATOR);

            this.keywords = this.arrayToString(ar.getKeywords());
            this.keywords_prohibited = this.arrayToString(ar.getKeywordsProhibited());

        }

        private String wordListToString(int n, List<Map.Entry<String,Integer>> list, String separator){
            if(list.size() <=0)return "\"\"";
            String result = "\"";
            List<Map.Entry<String,Integer>> subItems = list.subList(0, Math.min(list.size(), n));

            for(Map.Entry<String,Integer> e : subItems){
                result+=e.getKey()+":"+e.getValue()+"\n";//+separator+" ";  //TODO: wozld it be better to remove the newline and replace it again with seperator+whitespace? but excel works fine with this
            }

            result = result.substring(0, result.length() - 2);  //remove last separator and newLine

            result+="\"";

            return result;
        }

        public String arrayToString(String[] keywords){
            String result = "\"";

            if(keywords==null)return "\"\"";

            for(String e:keywords){
                result = result.concat(e+", ");
            }

            if(result.length() >1) {
                result = result.substring(0, result.length() - 2);
            }

            result = result.concat("\"");

            return result;
        }



    }
}
