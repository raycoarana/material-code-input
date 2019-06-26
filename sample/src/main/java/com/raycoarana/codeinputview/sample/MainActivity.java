package com.raycoarana.codeinputview.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;
import com.raycoarana.codeinputview.OnDigitInputListener;

public class MainActivity extends AppCompatActivity {

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final CodeInputView otherCodeInput = (CodeInputView) findViewById(R.id.pairing);
		otherCodeInput.addOnCompleteListener(new OnCodeCompleteListener() {
			@Override
			public void onCompleted(String code) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//Make the input enable again so the user can change it
						otherCodeInput.setEditable(true);

						//Show error
						otherCodeInput.setError("Your code is incorrect");
					}
				}, 1000);
			}
		});

		final CodeInputView codeInputView = (CodeInputView) findViewById(R.id.with_complete_callback);

		//Default value
		codeInputView.setCode("23");
		codeInputView.setInPasswordMode(true);

		//Action to do when completed
		codeInputView.addOnCompleteListener(new OnCodeCompleteListener() {
			@Override
			public void onCompleted(String code) {
				Toast.makeText(MainActivity.this, "Your code: " + code, Toast.LENGTH_SHORT).show();

				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//Make the input enable again so the user can change it
						codeInputView.setEditable(true);

						//Show error
						codeInputView.setError("Your code is incorrect");
					}
				}, 1000);
			}
		});
		codeInputView.addOnDigitInputListener(new OnDigitInputListener() {
			@Override
			public void onInput(char inputDigit) {
				Log.i("CondeInputView", "Digit input: " + inputDigit);
			}

			@Override
			public void onDelete() {
				Log.i("CondeInputView", "Digit deleted");
			}
		});

		final CodeInputView gravityView = findViewById(R.id.gravity);
		gravityView.setError("Really big error message that will need to be wrapped in several lines so we need to properly measure and layout it.");
	}
}
