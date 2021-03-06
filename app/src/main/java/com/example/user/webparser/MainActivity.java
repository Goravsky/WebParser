
package com.example.user.webparser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Приложение для вывода текстового содержимого веб-страницы по известному URL адресу
 *
 *@author  Goravsky
 */
public class MainActivity extends AppCompatActivity {

    private Button parseButton;
    private Button scanButton;
    private TextView resultView;
    private EditText urlInput;
    private String url;
    private String pageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseButton = findViewById(R.id.parse_button);
        resultView = findViewById(R.id.result_view);
        urlInput = findViewById(R.id.url_input);
        scanButton = findViewById(R.id.scan_button);

        resultView.setMovementMethod(new ScrollingMovementMethod());

        parseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                url = urlInput.getText().toString();

                MyParser myParser = new MyParser();             //создание фонового потока
                myParser.execute();

                resultView.setText(pageContent);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanCode();
            }
        });
    }

    /*
    Фоновый поток, в котором будет производится парсинг веб-страницы
     */
    class MyParser extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            parseWebPage();

            return null;
        }
    }

    /*
    Метод для парсинга веб-страницы и записи текстового содержимого страницы
     */
    public void parseWebPage(){
        Document webPage = null;     //сюда будет записываться DOM дерево HTML документа
        Elements pageText;           //сюда будут записываться контент из тегов параграфов

        try {
            webPage = Jsoup.connect(url).get();         //получение HTML кода веб-страницы
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (webPage != null) {
            pageText = webPage.select("p");
            pageContent = webPage.title();              //запись заголовка веб-страницы
            for(int i = 0; i < pageText.size(); i++){
                pageContent = pageContent + "\n" + pageText.get(i).text() + "\n";      //получение содержимого тегов параграфов
            }
        } else {
            pageContent = "Ошибка. Код страницы не найден.";
        }
    }

    public void scanCode(){
        Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.qr);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE).build();

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

        Barcode thisCode = barcodes.valueAt(0);
        urlInput.setText(thisCode.rawValue);

    }
}