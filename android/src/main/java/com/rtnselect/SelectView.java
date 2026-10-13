package com.rtnselect;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.PointerEvents;
import com.facebook.react.uimanager.UIManagerHelper;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A container that opens the platform dropdown list when it is tapped.
 *
 * Whatever JS renders inside the Select is the field the user sees and touches, and this view is
 * its parent. That is the whole point of 3.0: an invisible spinner laid over app-drawn chrome
 * competes with its siblings for the touch, and Android hands a touch to the sibling with the
 * highest elevation first, whatever the order in JS. A parent cannot lose that race, because it
 * sees the touch before any of its children.
 *
 * The list is a ListPopupWindow, the same window an AppCompatSpinner opens in dropdown mode, so it
 * keeps the look the spinner had. It is anchored under this view and flips above it when there is
 * no room below.
 */
public class SelectView extends ReactViewGroup {

	/** No index yet. Distinct from 0, which is a legitimate selection. */
	private static final int NO_SELECTION = -1;

	/** How many rows are measured to size the list, the same cap Spinner uses. */
	private static final int MAX_ITEMS_MEASURED = 15;

	/** Keeps a wide list off the screen edges, in dp. */
	private static final int SCREEN_MARGIN_DP = 16;

	private final List<String> options = new ArrayList<>();
	private ArrayAdapter<String> adapter;
	private int selectedIndex = NO_SELECTION;
	private ListPopupWindow popup;

	/**
	 * A tap, not a press: a drag that starts on the field belongs to the scroll view around it,
	 * which takes the gesture over and sends this view a cancel, so the list does not open.
	 */
	private final GestureDetector tapDetector;

	public SelectView(Context context) {
		super(context);
		setClickable(true);
		tapDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDown(@NonNull MotionEvent event) {
				return true;
			}

			@Override
			public boolean onSingleTapUp(@NonNull MotionEvent event) {
				performClick();
				return true;
			}
		});
		// A slow press still opens the list; a long press would otherwise swallow the tap.
		tapDetector.setIsLongpressEnabled(false);
	}

	// ============================================================================
	// Touch: the container takes every touch inside it
	// ============================================================================

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// The children are the drawing of the field, not controls of their own, so they never get
		// a native touch. JS still sees the touch through the root view, as for any React view.
		if (!PointerEvents.canBeTouchTarget(getPointerEvents())) {
			return super.onInterceptTouchEvent(event);
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!PointerEvents.canBeTouchTarget(getPointerEvents())) {
			return super.onTouchEvent(event);
		}
		tapDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean performClick() {
		// super sends the accessibility click event; TalkBack's double tap also lands here.
		super.performClick();
		open();
		return true;
	}

	@Override
	public CharSequence getAccessibilityClassName() {
		// Read as a drop-down list unless the app gives the Select an accessibilityRole.
		return Spinner.class.getName();
	}

	// ============================================================================
	// Props
	// ============================================================================

	public void setOptions(List<String> next) {
		// Fabric re-applies every prop whenever any one of them changes, and callers routinely
		// build this array inline, so an identical list arrives on most renders.
		if (options.equals(next)) {
			return;
		}
		options.clear();
		options.addAll(next);
		adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(options));
		// The rows on screen no longer match the indices JS would receive.
		dismissPopup();
	}

	public void setSelectedIndex(int index) {
		// A -1 is how callers spell "nothing selected yet" after an indexOf. Out-of-range values
		// are kept as asked and simply mark no row, so the order Fabric writes `options` and
		// `selectedIndex` in does not matter.
		selectedIndex = index < 0 ? NO_SELECTION : index;
	}

	// ============================================================================
	// The list
	// ============================================================================

	private void open() {
		if (adapter == null || options.isEmpty() || getWindowToken() == null) {
			return;
		}
		if (popup != null && popup.isShowing()) {
			return;
		}

		ListPopupWindow window = new ListPopupWindow(getContext());
		window.setAnchorView(this);
		window.setAdapter(adapter);
		window.setModal(true);
		window.setContentWidth(measureContentWidth(window));
		window.setOnItemClickListener((parent, row, position, id) -> {
			window.dismiss();
			pick(position);
		});
		window.setOnDismissListener(() -> {
			if (popup == window) {
				popup = null;
			}
		});
		popup = window;
		window.show();

		// Mark the current row and scroll it into view, as the spinner's own popup does.
		ListView list = window.getListView();
		if (list != null && selectedIndex >= 0 && selectedIndex < options.size()) {
			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			list.setItemChecked(selectedIndex, true);
			window.setSelection(selectedIndex);
		}
	}

	/**
	 * As wide as the widest row, never narrower than the field and never wider than the screen.
	 * Measures a window of rows around the selection, the way Spinner sizes its own dropdown, so a
	 * 250-row list costs fifteen measures.
	 */
	private int measureContentWidth(ListPopupWindow window) {
		int widest = 0;
		int start = Math.max(0, selectedIndex - (MAX_ITEMS_MEASURED - 1));
		int end = Math.min(options.size(), start + MAX_ITEMS_MEASURED);
		start = Math.max(0, end - MAX_ITEMS_MEASURED);

		FrameLayout parent = new FrameLayout(getContext());
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		View row = null;
		for (int i = start; i < end; i++) {
			row = adapter.getView(i, row, parent);
			if (row.getLayoutParams() == null) {
				row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			row.measure(spec, spec);
			widest = Math.max(widest, row.getMeasuredWidth());
		}

		Rect frame = new Rect();
		Drawable background = window.getBackground();
		if (background != null) {
			background.getPadding(frame);
		}
		int dressing = frame.left + frame.right;
		int margin = Math.round(SCREEN_MARGIN_DP * getResources().getDisplayMetrics().density);
		int limit = getResources().getDisplayMetrics().widthPixels - 2 * margin - dressing;

		return Math.max(Math.min(widest, limit), getWidth() - dressing);
	}

	private void pick(int position) {
		if (position < 0 || position >= options.size()) {
			return;
		}
		selectedIndex = position;
		emitValueChange(position);
	}

	private void dismissPopup() {
		if (popup != null) {
			ListPopupWindow window = popup;
			popup = null;
			window.dismiss();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		// A popup outliving its anchor leaks the window and crashes on the next touch.
		dismissPopup();
		super.onDetachedFromWindow();
	}

	private void emitValueChange(int index) {
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
