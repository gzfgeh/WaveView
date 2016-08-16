package com.gzfgeh.waveview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button dialogBtn;
    private CustomProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dialogBtn = (Button) findViewById(R.id.dialog);
        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        if (!subscriber.isUnsubscribed()) {
                            for (int i = 0; i < 1000; i++) {
                                if (i % 10 == 0) {
                                    subscriber.onNext(i);
                                }

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            subscriber.onCompleted();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Integer>() {
                            @Override
                            public void onCompleted() {
                                dialog.setProgress(1.0f);
                                dialog = null;
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(Integer o) {
                                float percent = o/1000f;

                                if (dialog == null)
                                    dialog = CustomProgressDialog.show(MainActivity.this);

                                dialog.setProgress(percent);
                            }
                        });
            }
        });

    }
}
