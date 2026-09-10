package com.rtnselect;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;

/**
 * The `onValueChange` payload, dispatched through the EventDispatcher so it reaches both
 * architectures. The legacy `RCTEventEmitter` path this replaces only still worked under
 * bridgeless through the `useFabricInterop` shim, which logs a soft exception per event and
 * dispatches with a surface id of -1.
 */
public class ValueChangeEvent extends Event<ValueChangeEvent> {

	public static final String EVENT_NAME = "topValueChange";

	private final String value;
	private final int index;

	public ValueChangeEvent(int surfaceId, int viewId, String value, int index) {
		super(surfaceId, viewId);
		this.value = value;
		this.index = index;
	}

	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	@Nullable
	@Override
	protected WritableMap getEventData() {
		WritableMap event = Arguments.createMap();
		event.putString("value", value);
		event.putInt("index", index);
		return event;
	}
}
