package com.rtnselect;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.AppCompatSpinner;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.UIManagerHelper;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.util.ArrayList;
import java.util.List;

public class SelectView extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {

	/** No index yet. Distinct from 0, which is a legitimate selection. */
	private static final int NO_SELECTION = -1;

	private final List<String> options = new ArrayList<>();

	/**
	 * The index JS asked for. Fabric writes props in whatever order RawProps yields, so
	 * `selectedIndex` regularly lands before `options` and there is no adapter to apply it to
	 * yet. Holding it here and replaying it once the adapter exists makes prop order irrelevant.
	 */
	private int requestedIndex = NO_SELECTION;

	/** The last index announced to JS, so an unchanged selection stays quiet. */
	private int announcedIndex = NO_SELECTION;

	/**
	 * Set when the user opens the list. It is what tells a person's pick apart from a prop write,
	 * and the two are otherwise indistinguishable: the spinner's popup reports the row the user
	 * chose by calling this view's own `setSelection`, the very method the `selectedIndex` prop
	 * arrives through. Emitting from `setSelection` unconditionally is why writing the prop used
	 * to echo an onValueChange straight back to JS.
	 */
	private boolean openedByUser = false;

	/**
	 * React Native drives layout from the shadow tree and swallows a requestLayout() raised by a
	 * child, so AbsSpinner.setAdapter's own request never produces a measure pass and the spinner
	 * keeps drawing its previous row. Re-running measure and layout on our own frame is the
	 * standard workaround on this platform; ReactPicker and ReactSwitch both carry it.
	 */
	private final Runnable measureAndLayout = new Runnable() {
		@Override
		public void run() {
			measure(
				MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY)
			);
			layout(getLeft(), getTop(), getRight(), getBottom());
		}
	};

	public SelectView(Context context) {
		// MODE_DROPDOWN is pinned rather than inherited. An AppCompatSpinner fixes its mode at
		// construction, and the mode it would otherwise take comes from the host app theme's
		// spinnerStyle: AppCompat's own sets `dropdown`, but a vendor theme can hand back
		// `dialog`, where the popup is an AlertDialog built on the React context. Pinning it makes
		// the widget behave the same whatever theme it is dropped into.
		super(context, null, androidx.appcompat.R.attr.spinnerStyle, Spinner.MODE_DROPDOWN);
		setOnItemSelectedListener(this);
		// The overlay pattern this component is built for puts an invisible Select on top of
		// app-drawn chrome, so the platform spinner background and its arrow must not show through.
		setBackground(null);
	}

	@Override
	public void requestLayout() {
		super.requestLayout();
		post(measureAndLayout);
	}

	public void setOptions(List<String> next) {
		// Fabric re-applies every prop whenever any one of them changes, and callers routinely
		// build this array inline, so an identical list arrives on most renders. Rebuilding the
		// adapter for it would reset the spinner's own selection for nothing.
		if (options.equals(next)) {
			return;
		}
		options.clear();
		options.addAll(next);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setAdapter(adapter);

		applyRequestedSelection();
	}

	@Override
	public void setSelection(int position) {
		boolean userPick = openedByUser;
		openedByUser = false;

		if (!userPick && requestedIndex == position) {
			return;
		}
		requestedIndex = position;
		applyRequestedSelection();

		if (position < 0 || position == announcedIndex) {
			return;
		}
		announcedIndex = position;
		if (userPick) {
			emitValueChange(position);
		}
	}

	@Override
	public boolean performClick() {
		openedByUser = true;
		return super.performClick();
	}

	private void applyRequestedSelection() {
		// A -1 is how callers spell "nothing selected yet" after an indexOf, and a spinner has no
		// empty state. Forwarding it used to reach ArrayAdapter.getItem and take the host app down
		// with IndexOutOfBoundsException.
		if (requestedIndex < 0 || getAdapter() == null || requestedIndex >= getAdapter().getCount()) {
			return;
		}
		if (getSelectedItemPosition() != requestedIndex) {
			super.setSelection(requestedIndex);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Deliberately silent, and it leaves announcedIndex alone. Every selection that matters to
		// JS already came through setSelection above, while this callback also fires for the row 0
		// an attaching adapter picks on its own, which iOS never reports.
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void emitValueChange(int index) {
		if (index < 0 || index >= options.size()) {
			return;
		}
		Context context = getContext();
		if (!(context instanceof ReactContext)) {
			return;
		}
		EventDispatcher dispatcher = UIManagerHelper.getEventDispatcher((ReactContext) context);
		if (dispatcher == null) {
			return;
		}
		dispatcher.dispatchEvent(
			new ValueChangeEvent(UIManagerHelper.getSurfaceId(this), getId(), options.get(index), index)
		);
	}
}
