package com.rtnselect;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.List;

public class SelectView extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {

	private final String TAG = "RTNSelect";

	public SelectView(Context context) {
		super(context);
		init();
	}

	public SelectView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		this.setOnItemSelectedListener(this);
	}

	@Override
	public boolean performClick() {
		boolean result = super.performClick();
		return result;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public void setOptions(List<String> options) {

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		this.setAdapter(adapter);
	}

	@Override
	public void setSelection(int position) {

		this.post(new Runnable() {
			@Override
			public void run() {
				SelectView.super.setSelection(position);
			}
		});

		ReactContext reactContext = (ReactContext) getContext();
		if (reactContext != null) {
			String val = "unknown";
			if (getAdapter() != null && getAdapter().getCount() > position) {
				Object item = getAdapter().getItem(position);
				if (item != null) val = item.toString();
			}

			WritableMap event = Arguments.createMap();
			event.putString("value", val);
			event.putInt("index", position);

			try {
				reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
					getId(),
					"topValueChange",
					event
				);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String val = "null";
		if (parent != null && parent.getItemAtPosition(position) != null) {
			val = parent.getItemAtPosition(position).toString();
		}

		ReactContext reactContext = (ReactContext) getContext();
		if (reactContext != null) {
			WritableMap event = Arguments.createMap();
			event.putString("value", val);
			event.putInt("index", position);

			try {
				reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
					getId(),
					"topValueChange",
					event
				);
			} catch (Exception e) {
			}
		} else {
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}
