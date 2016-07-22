package com.example.acbingo.myfinaltest10.activitys;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.Particlesys.ParticleSystemRenderer;

/**
 * Created by tfuty on 2016-05-23.
 */
public class FirstActivity extends AppCompatActivity {
    GLSurfaceView mGlSurfaceView;
    Button send_button;
    Button receive_button;
    Button more_button;
    ImageView imageView;

    static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);

        imageView = (ImageView) findViewById(R.id.image);


        fragmentManager = getFragmentManager();

        //背景星空绘制
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGlSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            ParticleSystemRenderer mRenderer = new ParticleSystemRenderer(mGlSurfaceView);
            mGlSurfaceView.setRenderer(mRenderer);
            mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        } else {
            throw new UnsupportedOperationException();
        }

        send_button = (Button) findViewById(R.id.send);
        receive_button = (Button) findViewById(R.id.receive);
        more_button = (Button) findViewById(R.id.more);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,SendSignalActivity.class));
            }
        });



        receive_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,ReceiveSignalActivity.class));

            }
        });
        more_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startActivity(new Intent(FirstActivity.this,BluetoothChat.class));


                First_More_Fragment first_more_fragment = new First_More_Fragment();

                FragmentTransaction transaction = fragmentManager.beginTransaction();

                First_Fragment first_fragment = (First_Fragment) fragmentManager.findFragmentById(R.id.fragment_first);
                transaction.hide(first_fragment);

                /*transaction.setCustomAnimations(R.animator.fragment_slide_upward,
                        0,
                        R.animator.fragment_slide_upward,
                        0);*/

                //transaction.replace(R.id.framelayout,first_more_fragment);
                transaction.add(R.id.framelayout,first_more_fragment);

                transaction.show(first_more_fragment);

                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }


}
