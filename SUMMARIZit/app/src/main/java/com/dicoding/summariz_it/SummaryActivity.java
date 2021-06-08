package com.dicoding.summariz_it;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dicoding.summariz_it.db.SummaryDBHelper;
import com.dicoding.summariz_it.utils.Grapher;
import com.dicoding.summariz_it.utils.PreProcessor;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity implements SaveDialog.SaveDialogListener {
    private static final String TAG = "SummaryActivity";
    private final PreProcessor preProcessor = HomeActivity.preProcessor;
    private final Grapher grapher = HomeActivity.grapher;
    private Button saveSummaryButton;
    private String summaryText;
    private SummaryDBHelper dbHelper;
    private Intent textIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        dbHelper = new SummaryDBHelper(this);

        saveSummaryButton = findViewById(R.id.saveButton);
        saveSummaryButton.setEnabled(false);
        saveSummaryButton.setOnClickListener(v -> openSaveDialog());

        TextView summaryTextView = findViewById(R.id.summaryTextView);
        textIntent = getIntent();
        if (textIntent.hasExtra("docText")) {
            String documentText = textIntent.getStringExtra("docText");
            Log.i(TAG, "onCreate: DOC TEXT:" + documentText);
            String EMPTY_MESSAGE = "Summary not available";
            if (documentText == null || documentText.isEmpty()) {
                Toast.makeText(this, "Not document text available", Toast.LENGTH_SHORT).show();
                summaryTextView.setText(EMPTY_MESSAGE);
                saveSummaryButton.setEnabled(false);
            } else {
                summaryText = summaryTool(documentText).replaceAll("    ", " ");
            }
            if (summaryText == null || summaryText.isEmpty()) {
                summaryTextView.setText(EMPTY_MESSAGE);
                saveSummaryButton.setEnabled(false);
            } else {
                saveSummaryButton.setEnabled(true);
            }
        } else if (textIntent.hasExtra("open")) {
            summaryText = textIntent.getStringExtra("open");
        }
        summaryTextView.setText(summaryText);

    }

    private String summaryTool(String documentText) {
        StringBuilder text = new StringBuilder();
        String[] sentences = preProcessor.extractSentences(documentText.trim());
        Log.i(TAG, "summarizedDocument: No of sentences: " + sentences.length);
        ArrayList<Grapher.SentenceVertex> sentenceVertices = grapher.sortSentences(sentences, preProcessor.tokenizeSentences(sentences));
        int summarySize = ((sentences.length * 25) / 100);
        int maxLength = 15;
        int counter = 0;
        for (int i = 0; i < summarySize; i++) {
            if (i < maxLength) {
                text.append(sentenceVertices.get(i).getSentence().trim());
                text.append(" ");
                counter++;
            } else
                break;
        }
        Log.i(TAG, "summarizedDocument: Summary length = " + counter + " sentences");
        Log.i(TAG, "\nSUMMARY:\n" + text.toString());
        return text.toString();
    }

    private void openSaveDialog() {
        if (textIntent.hasExtra("open")) {
            Toast.makeText(this, "This has already been saved", Toast.LENGTH_SHORT).show();
        }
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void applyText(String name) {
        Log.i(TAG, "applyText: Save name: " + name);
        Log.i(TAG, "onClick: GOT HERE, SAVING");
        if (name != null && !name.isEmpty()) {
            try {
                saveSummary(name, summaryText);
                saveSummaryButton.setEnabled(false);
                Log.i(TAG, "onClick: Save successful");
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "applyText: ", e);
                Toast.makeText(this, "Could not save summary", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SummaryActivity.this, "Try again, Enter save name", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSummary(String summaryName, String summaryText) throws Exception {
        dbHelper.saveSummary(summaryName, summaryText);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
}
