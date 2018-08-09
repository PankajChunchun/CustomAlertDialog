package pankaj.com.customdialogexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.kloojj.customdialog.CustomAlertDialog;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.one_button).setOnClickListener(this);
        findViewById(R.id.two_button).setOnClickListener(this);
        findViewById(R.id.three_button).setOnClickListener(this);
        findViewById(R.id.full_screen).setOnClickListener(this);
        findViewById(R.id.without_title).setOnClickListener(this);
        findViewById(R.id.only_content).setOnClickListener(this);
        findViewById(R.id.scrollable_content).setOnClickListener(this);
        findViewById(R.id.not_cancelable).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.one_button:
                oneButton();
                break;
            case R.id.two_button:
                twoButton();
                break;
            case R.id.three_button:
                threeButton();
                break;
            case R.id.full_screen:
                fullScreen();
                break;
            case R.id.without_title:
                withoutTitle();
                break;
            case R.id.only_content:
                onlyContent();
                break;
            case R.id.scrollable_content:
                scrollableContent();
                break;
            case R.id.not_cancelable:
                notCancelable();
                break;
        }
    }

    private void oneButton() {
        new CustomAlertDialog()
                .setTitle("Single Button Dialog")
                .setMessage("This is sample message of dialog")
                .setPositiveButton("Proceed", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(MainActivity.this);
    }

    private void twoButton() {
        new CustomAlertDialog()
                .setTitle("Yes/No Dialog")
                .setMessage("This is sample message of dialog")
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show(MainActivity.this);
    }

    private void threeButton() {
        new CustomAlertDialog()
                .setTitle("Yes/No with Neutral")
                .setMessage("This is sample message of dialog")
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setNeutralButton("Can't say", null)
                .show(MainActivity.this);
    }

    private void fullScreen() {
        new CustomAlertDialog()
                .setTitle("Full Screen Modal")
                .setMessage(getResources().getString(R.string.long_text))
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setFullscreenModal(true)
                .setNegativeButton("No", null)
                .setNeutralButton("Can't say", null)
                .show(MainActivity.this);
    }

    private void withoutTitle() {
        new CustomAlertDialog()
                .setMessage("This is sample message of dialog without title")
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(MainActivity.this);
    }

    private void onlyContent() {
        new CustomAlertDialog()
                .setMessage("This is sample message of dialog with only content")
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(MainActivity.this);
    }

    private void scrollableContent() {
        new CustomAlertDialog()
                .setTitle("Scrollable Content")
                .setMessage(getResources().getString(R.string.long_text))
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(MainActivity.this);
    }

    private void notCancelable() {
        new CustomAlertDialog()
                .setTitle("Yes/No/Neutral with Not Cancelable")
                .setMessage("This is sample message of dialog")
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getApplicationContext(), "Yes Button Clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .setNeutralButton("Can't say", null)
                .setCanCancelable(false)
                .show(MainActivity.this);
    }
}