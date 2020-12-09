package ru.handy.android.wm;
/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Locale;

import ru.handy.android.wm.learning.Learning;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class GlobApp extends Application {
    private DB db;
    private TextToSpeech tts;
    private Tracker mTracker;
    private Learning learning;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    /**
     * Gets open {@link DB}
     * @return DB
     */
    synchronized public DB getDb() {
        if (db == null || !db.isOpen()) {
            db = new DB(this.getApplicationContext());
            db.open();
            Log.d("myLogs", "create new DB");
        }
        return db;
    }

    /**
     * Close {@link DB}
     */
    synchronized public void closeDb() {
        if (db != null) db.close();
    }

    /**
     * Gets the default {@link TextToSpeech} for this {@link Application}.
     * @return TextToSpeech
     */
    synchronized public TextToSpeech speak(final String text) {
        if (tts == null || tts.speak(text, TextToSpeech.QUEUE_ADD, null) == TextToSpeech.ERROR) {
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        String pronun = db.getValueByVariable(DB.PRONUNCIATION_USUK);
                        int pronunc = (pronun == null || pronun.equals("0") ? 0 : 1);
                        int result = tts.setLanguage(pronunc == 0 ? Locale.US : Locale.UK);
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("myLogs", "This Language Locale.US is not supported");
                        } else
                            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                    } else
                        Log.e("myLogs", "TextToSpeech initilization was failed!");
                }
            });
        }
        return tts;
    }

    synchronized public void shutdownTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /**
     * Sets {@link Learning} for this {@link Application}.
     */
    synchronized public void setLearning(Learning learning) {
        this.learning = learning;
    }

    /**
     * Gets {@link Learning} for this {@link Application}.
     * @return Learning
     */
    synchronized public Learning getLearning() {
        return learning;
    }

}