package com.example.proyecto_microfono;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.Manifest.permission;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.widget.ImageButton;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageButton imageButton;
    EditText editText;
    TextView targetlang;
    Button srclangchoose;
    Button trgtlangchoose;
    Button transbtn;
    int count = 0;
    SpeechRecognizer speechRecognizer;
    Translator translator;
    ProgressDialog progressDialog;

    private Button openInstructionsButton;

    private static final String TAG = "MAIN_TAG";

    private ArrayList<ModelLanguage> languageArrayList = null;

    private String sourceLanguageCode = "es";
    private String targetLanguageCode = "en";

    private TextView appNameTextView;
    private Button copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.button);
        editText = findViewById(R.id.editartexto);
        targetlang = findViewById(R.id.targetlang);
        srclangchoose = findViewById(R.id.srclangchoose);
        trgtlangchoose = findViewById(R.id.trgtlangchoose);
        transbtn = findViewById(R.id.transbtn);
        ImageButton copyButton = findViewById(R.id.copyButton);
// Resto de tu lógica con el ImageButton


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        loadAvailableLanguages();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Obtén una referencia al botón de cerrar
        Button closeButton = findViewById(R.id.closeButton);


        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 0) {
                    imageButton.setImageDrawable(getDrawable(R.drawable.baseline_mic_24));
                    speechRecognizer.startListening(speechRecognizerIntent);
                    count = 1;
                } else {
                    imageButton.setImageDrawable(getDrawable(R.drawable.baseline_mic_off_24));
                    speechRecognizer.stopListening();
                    count = 0;
                }
            }




        });

        // Configura un clic en el botón para cerrar las instrucciones
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oculta el contenedor de instrucciones
                findViewById(R.id.instructionsContainer).setVisibility(View.GONE);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            // ... (otros métodos)
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                // Implement this method
            }

            @Override
            public void onBeginningOfSpeech() {
                // Implement this method
            }

            @Override
            public void onRmsChanged(float v) {
                // Implement this method
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                // Implement this method
            }

            @Override
            public void onEndOfSpeech() {
                // Implement this method
            }

            @Override
            public void onError(int i) {
                // Implement this method
            }

            @Override
            public void onResults(Bundle bundle) {
                // Implement this method
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                validateData();
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                // Implement this method
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                // Implement this method
            }
        });

        srclangchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceLanguageChoose();
            }
        });

        trgtlangchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetLanguageChoose();
            }
        });

        transbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {  // Agregado
            @Override
            public void onClick(View view) {
                copyTextToClipboard();
            }
        });
    }

    private void copyTextToClipboard() {
        String translatedText = targetlang.getText().toString().trim();

        if (!translatedText.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Texto Traducido", translatedText);
            clipboard.setPrimaryClip(clip);

            showToast("Texto copiado al portapapeles");
        }
    }

    private void validateData() {
        String sourceLanguageText = editText.getText().toString().trim();
        Log.d(TAG, "validateData: sourceLanguageText: " + sourceLanguageText);

        if (!sourceLanguageText.isEmpty()) {
            startTranslation();
        }
    }

   /* private void showInstructionsModal() {
        // Inflar el diseño del modal.
        View modalView = getLayoutInflater().inflate(R.layout., null);

        // Construir un AlertDialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modalView);
        builder.setCancelable(true);

        // Crear y mostrar el AlertDialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Configurar el botón de cerrar en el modal.
        Button closeButton = modalView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss(); // Cierra el modal al hacer clic en el botón de cerrar.
            }
        });
    }*/


    private void startTranslation() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor, espere");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Procesando el lenguaje ....");
        progressDialog.show();

        TranslatorOptions translatorOption = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();
        translator = Translation.getClient(translatorOption);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "startTranslation: model is ready, start translation ...");
                    progressDialog.setMessage("Traduciendo ...");

                    translator.translate(editText.getText().toString())
                            .addOnSuccessListener(translatedText -> {
                                Log.d(TAG, "startTranslation: translatedText: " + translatedText);
                                progressDialog.dismiss();
                                targetlang.setText(translatedText);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e(TAG, "startTranslation: ", e);
                                showToast("Error al traducir debido a: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "startTranslation: ", e);
                    showToast("Error al traducir debido a: " + e.getMessage());
                });
    }

    private void loadAvailableLanguages() {
        languageArrayList = new ArrayList<>();
        String[] languageCodeList = TranslateLanguage.getAllLanguages().toArray(new String[0]);

        for (String languageCode : languageCodeList) {
            String languageTitle = new Locale(languageCode).getDisplayLanguage();
            Log.d(TAG, "loadAvailableLanguages: LanguageCode: " + languageCode);
            Log.d(TAG, "loadAvailableLanguages: LanguageTitle: " + languageTitle);

            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }

    private void sourceLanguageChoose() {
        showLanguagePopupMenu(srclangchoose, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();
                sourceLanguageCode = languageArrayList.get(position).getLanguageCode();
                srclangchoose.setText(languageArrayList.get(position).getLanguageTitle());
                Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: " + sourceLanguageCode);
                return true;
            }
        });
    }

    private void targetLanguageChoose() {
        showLanguagePopupMenu(trgtlangchoose, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();
                targetLanguageCode = languageArrayList.get(position).getLanguageCode();
                trgtlangchoose.setText(languageArrayList.get(position).getLanguageTitle());
                Log.d(TAG, "targetLanguageChoose: targetLanguageCode: " + targetLanguageCode);
                return true;
            }
        });
    }

    private void showLanguagePopupMenu(View anchorView, PopupMenu.OnMenuItemClickListener listener) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        for (int i = 0; i < languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).getLanguageTitle());
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();
                listener.onMenuItemClick(menuItem);
                return true;
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
