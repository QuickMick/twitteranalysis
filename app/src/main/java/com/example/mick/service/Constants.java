package com.example.mick.service;

/**
 * Created by Mick on 27.04.2017.
 */
public class Constants {
    public interface ACTION {
        public static String ANALIZATION = "ANALIZATION";
       /* public static String MAIN_ACTION = "com.truiton.foregroundservice.action.main";
        public static String PREV_ACTION = "com.truiton.foregroundservice.action.prev";
        public static String PLAY_ACTION = "com.truiton.foregroundservice.action.play";
        public static String NEXT_ACTION = "com.truiton.foregroundservice.action.next";
        public static String STARTFOREGROUND_ACTION = "com.truiton.foregroundservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.truiton.foregroundservice.action.stopforeground";*/
    }

    public interface HISORY{
        public static String OPEN ="Open Archived Analysis";
        public static String DELETE ="Delete File";
        public static String SHARE = "Share File";
    }

    public static String[] historyCommandsAsArray(){
        return new String[]{HISORY.OPEN,HISORY.SHARE,HISORY.DELETE};
    }

    public interface DETAIL_GRAPH{
        public static String EMOTION_NAME ="EMOTION_NAME";

        public static String EMOTION_NAME_ANGER ="ANGER";
        public static String EMOTION_NAME_ANTICIPATION ="ANTICIPATION";
        public static String EMOTION_NAME_DISGUST ="DISGUST";
        public static String EMOTION_NAME_FEAR ="FEAR";
        public static String EMOTION_NAME_JOY ="JOY";
        public static String EMOTION_NAME_SADNESS ="SADNESS";
        public static String EMOTION_NAME_SURPRISE ="SURPRISE";
        public static String EMOTION_NAME_TRUST ="TRUST";
        public static String EMOTION_NAME_POSITIVE ="POSITIVE";
        public static String EMOTION_NAME_NEGATIVE ="NEGATIVE";

        public static String ALL_WORDS = "All words";
    }
    public static String[] emotionCommandsAsArray(){
        return new String[]{
                DETAIL_GRAPH.ALL_WORDS,
                DETAIL_GRAPH.EMOTION_NAME_ANGER,
                DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION,
                DETAIL_GRAPH.EMOTION_NAME_DISGUST,
                DETAIL_GRAPH.EMOTION_NAME_FEAR,
                DETAIL_GRAPH.EMOTION_NAME_JOY,
                DETAIL_GRAPH.EMOTION_NAME_SADNESS,
                DETAIL_GRAPH.EMOTION_NAME_SURPRISE,
                DETAIL_GRAPH.EMOTION_NAME_TRUST,
                DETAIL_GRAPH.EMOTION_NAME_POSITIVE,
                DETAIL_GRAPH.EMOTION_NAME_NEGATIVE
        };
    }

    public interface ANALIZATION{

        public static String BROADCAST_ANALIZATION_STOPPED = "BROADCAST_ANALIZATION_STOPPED";
        /**
         * Key for intend, when creating a new diagramm activity
         */
        public static String DIAGRAM_MODE = "DIAGRAM_MODE";

        public static String DIAGRAM_MODE_FIND = "DIAGRAM_MODE_FIND";

        /**
         * Value for the diagram mode - means that the analization is currently running
         */
        public static String MODE_ANALIZATION_RUNNING = "MODE_ANALIZATION_RUNNING";

        /**
         * Value for diagramm mode -> meaning is, that the result of the last analisation should be shown,
         * and the stop button should be hidden
         */
        public static String MODE_ANALIZATION_STOPPED = "MODE_ANALIZATION_STOPPED";

        /**
         * Value for the diagramm mode - show history, additional value (MODE_HISTORY_DATE) is needed.
         * and the stop button should be hidden
         */
        public static String MODE_HISTORY = "MODE_HISTORY";

        /**
         * should be used to put the date (used as id) to the diagramm intent
         */
        public static String MODE_HISTORY_DATE = "MODE_HISTORY_DATE";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}