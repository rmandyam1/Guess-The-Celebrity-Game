package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebrities = new ArrayList<String>();
    ArrayList<String> celebImages = new ArrayList<String>();
    ImageView celebImageView;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    int numCelebrities = 0;
    int solutionTag;
    String celeb;

    public void nextQuestion() {
        Random random = new Random();

        int index = random.nextInt(numCelebrities);
        celeb = celebrities.get(index);
        String imageUrl = celebImages.get(index);

        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap image;

        try {
            image = imageDownloader.execute(imageUrl).get();

            celebImageView.setImageBitmap(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] options = new String[4];
        solutionTag = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (i == solutionTag) {
                options[i] = celeb;
            } else {
                options[i] = celebrities.get(random.nextInt(numCelebrities));
                while (options[i].equals(celeb)) {
                    options[i] = celebrities.get(random.nextInt(numCelebrities));
                }
            }
        }

        button1.setText(options[0]);
        button2.setText(options[1]);
        button3.setText(options[2]);
        button4.setText(options[3]);
    }

    public void optionTapped(View view) {
        Button tappedButton = (Button) view;

        int tapped = Integer.parseInt(view.getTag().toString());

        if (tapped == solutionTag) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! It was " + celeb, Toast.LENGTH_SHORT).show();
        }

        nextQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImageView = findViewById(R.id.celebImageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        DownloadTask downloadTask = new DownloadTask();

        String html = null;

        try {
            html = downloadTask.execute("http://www.posh24.se/kandisar").get();
        } catch (Exception e) {
            Log.i("Exception", "Hello");
        }

        String[] split = html.split("<div class=\"col-xs-12 col-sm-6 col-md-4\">");

        html = split[0];

        if (html != null) {
            Pattern pattern = Pattern.compile("img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                celebImages.add(matcher.group(1));
                numCelebrities++;
            }

            Log.i("Num Celeb Images", Integer.toString(numCelebrities));
            numCelebrities = 0;

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(html);
            while (matcher.find()) {
                celebrities.add(matcher.group(1));
                numCelebrities++;
            }

            Log.i("Celeb", celebrities.get(0));

            Log.i("Num Celebs", Integer.toString(numCelebrities));
            nextQuestion();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String html = "";
            URL url;
            HttpURLConnection connection;

            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;

                    html = html + current;
                    data = reader.read();
                }
                return html;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream in = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(in);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
