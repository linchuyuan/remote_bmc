package supermicro.chu.redfish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Chu on 3/25/2016.
 */
public class splash extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int index = getIntent().getIntExtra("index",-1);
        if(index == -1) {
            setContentView(R.layout.splash);
        }
        else { setContentView(R.layout.splash2);}

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(1000) ;
                } catch (Exception e) {

                } finally {
                    if (index == -1) {
                        Intent i = new Intent(splash.this, splash.class);
                        i.putExtra("index",index + 1);
                        startActivity(i);
                        finish();
                    }
                    else {
                        Intent i = new Intent(splash.this, MainActivity.class);
                        i.putExtra("index",index + 1);
                        startActivity(i);
                        finish();
                    }
                }
            }
        };
        welcomeThread.start();
    }

}
