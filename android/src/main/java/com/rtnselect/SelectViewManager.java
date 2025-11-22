package com.rtnselect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewManagerDelegate;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.viewmanagers.RTNSelectManagerDelegate;
import com.facebook.react.viewmanagers.RTNSelectManagerInterface;

import java.util.ArrayList;
import java.util.Map;

@ReactModule(name = SelectViewManager.REACT_CLASS)
public class SelectViewManager extends SimpleViewManager<SelectView>
		implements RTNSelectManagerInterface<SelectView> {

	public static final String REACT_CLASS = "RTNSelect";
	private final ViewManagerDelegate<SelectView> mDelegate;

	public SelectViewManager(ReactApplicationContext reactContext) {
		mDelegate = new RTNSelectManagerDelegate<>(this);
	}

	@Nullable
	@Override
	protected ViewManagerDelegate<SelectView> getDelegate() {
		return mDelegate;
	}

	@NonNull
	@Override
	public String getName() {
		return REACT_CLASS;
	}

	@Nullable
	@Override
	public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
		// Strict mapping: Native 'topValueChange' -> JS 'onValueChange'
		return MapBuilder.of(
				"topValueChange",
				MapBuilder.of("registrationName", "onValueChange")
		);
	}

	@NonNull
	@Override
	protected SelectView createViewInstance(@NonNull ThemedReactContext reactContext) {
		return new SelectView(reactContext);
	}

	@Override
	@ReactProp(name = "options")
	public void setOptions(SelectView view, @Nullable ReadableArray value) {
		ArrayList<String> list = new ArrayList<>();
		if (value != null) {
			for (int i = 0; i < value.size(); i++) {
				list.add(value.getString(i));
			}
		}
		view.setOptions(list);
	}

	@Override
	@ReactProp(name = "selectedIndex")
	public void setSelectedIndex(SelectView view, int value) {
		view.setSelection(value);
	}

	@Override
	@ReactProp(name = "mode")
	public void setMode(SelectView view, @Nullable String value) {}

	public void setOnValueChange(SelectView view, @Nullable Object value) {}
}
