package com.project.ocrreader;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;

import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


import java.io.IOException;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

	SurfaceView mCameraView;
	TextView mTextView;
	CameraSource mCameraSource;
	TextToSpeech t1;
	private TextToSpeech tts;

	private static final String TAG = "MainActivity";
	private static final int requestPermissionID = 101;




	private void speak(String text) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
		} else {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	@Override
	public void onDestroy() {
		if (tts != null) {

			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}



//    private void listen() {
//        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
//
//        try {
//            startActivityForResult(i, 100);
//        } catch (ActivityNotFoundException a) {
//            Toast.makeText(LogInActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
//        }
//    }





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		t1=new TextToSpeech(this,this);

		mCameraView = findViewById(R.id.surfaceView);
		mTextView = findViewById(R.id.text_view);


		t1.speak("hellooo", TextToSpeech.QUEUE_FLUSH, null);

		startCameraSource();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode != requestPermissionID) {
			Log.d(TAG, "Got unexpected permission result: " + requestCode);
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}

		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			try {
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
					return;
				}
				mCameraSource.start(mCameraView.getHolder());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void startCameraSource() {

		//Create the TextRecognizer
		final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

		if (!textRecognizer.isOperational()) {
			Log.w(TAG, "Detector dependencies not loaded yet");
		} else {

			//Initialize camerasource to use high resolution and set Autofocus on.
			mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
					.setFacing(CameraSource.CAMERA_FACING_BACK)
					.setRequestedPreviewSize(1280, 1024)
					.setAutoFocusEnabled(true)
					.setRequestedFps(2.0f)
					.build();

			/**
			 * Add call back to SurfaceView and check if camera permission is granted.
			 * If permission is granted we can start our cameraSource and pass it to surfaceView
			 */
			mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					try {

						if (ActivityCompat.checkSelfPermission(getApplicationContext(),
								Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

							ActivityCompat.requestPermissions(MainActivity.this,
									new String[]{Manifest.permission.CAMERA},
									requestPermissionID);
							return;
						}
						mCameraSource.start(mCameraView.getHolder());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					mCameraSource.stop();
				}
			});

			//Set the TextRecognizer's Processor.
			textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
				@Override
				public void release() {
				}

				/**
				 * Detect all the text from camera using TextBlock and the values into a stringBuilder
				 * which will then be set to the textView.
				 * */
				@Override
				public void receiveDetections(Detector.Detections<TextBlock> detections) {
					final SparseArray<TextBlock> items = detections.getDetectedItems();
					if (items.size() != 0 ){

						mTextView.post(new Runnable() {
							@Override
							public void run() {
								StringBuilder stringBuilder = new StringBuilder();
								for(int i=0;i<items.size();i++){
									TextBlock item = items.valueAt(i);
									stringBuilder.append(item.getValue());
									stringBuilder.append("\n");
								}
								mTextView.setText(stringBuilder.toString());

								Log.d("Text",stringBuilder.toString());

								// speak("Cancelled!" + stringBuilder.toString());


								//  MaryLink.load(getApplicationContext());


							}
						});
					}
				}
			});
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {

			int result = t1.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				//    t1.speak(contents.toString(), TextToSpeech.QUEUE_FLUSH, null);
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}
}
