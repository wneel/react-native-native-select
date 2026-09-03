# react-native-native-select

<p>
	<a href="https://github.com/wneel/react-native-native-select/blob/HEAD/LICENSE">
		<img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="react-native-native-select is released under the MIT license." />
	</a>
	<a href="https://www.npmjs.com/package/react-native-native-select">
		<img src="https://img.shields.io/npm/v/react-native-native-select?color=brightgreen&label=npm%20package" alt="Current npm package version." />
	</a>
	<a href="https://www.npmjs.com/package/react-native-native-select">
		<img src="https://img.shields.io/npm/dm/react-native-native-select" alt="Number of downloads per month." />
	</a>
</p>


A strictly native, performant Select component for React Native built exclusively for the **New Architecture**.

There is no JS rendering logic in this package. When your user opens the list, they are touching `UIMenu`, `UIPickerView` or `AppCompatSpinner` directly: the same widgets the operating system uses for its own pickers, with their animations, their dark mode and their accessibility tree.

## Preview

| iOS (Dropdown Mode) | iOS (Dialog/Wheel Mode) | Android |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/8a65c76b-f0a8-4f89-88d3-49000abe354d" height="300" /> | <img src="https://github.com/user-attachments/assets/71d98f6c-984a-49e8-bc98-abbfb86de535" height="300" /> | <img src="https://github.com/user-attachments/assets/5936637a-e63a-445c-9c9f-3a05940b6f9e" height="300" /> |
| *Native UIMenu (iOS 14+)* | *Classic UIPickerView* | *Native AppCompatSpinner* |

## What it actually renders

| Platform | Mode | Native widget | Source |
| :--- | :--- | :--- | :--- |
| iOS | `dropdown` | A real `UIMenu` built from `UIAction`s, attached to a `UIButton` with `showsMenuAsPrimaryAction = YES` (iOS 14+). The selected row carries `UIMenuElementStateOn`, so the system draws its own checkmark. | `ios/RTNSelect.mm` |
| iOS | `dialog` (default) | `UIPickerView`, the classic wheel, with this view as its own dataSource and delegate. | `ios/RTNSelect.mm` |
| Android | any | `AppCompatSpinner` with an `ArrayAdapter` over the platform `simple_spinner_item` and `simple_spinner_dropdown_item` layouts. | `android/src/main/java/com/rtnselect/SelectView.java` |

On iOS a single `RCTViewComponentView` holds both the picker and the button, and `mode` decides which one is hidden. Nothing is custom-drawn on either platform: no `Modal`, no `FlatList`, no wheel re-implemented in JS.

## How it compares

The popular alternatives are mature, well documented and far more featureful than this one. Here is where each of them sits, with weekly npm downloads so the size difference is on the table (figures pulled from the npm registry for the week of 2026-08-18).

| Library | Downloads / week | What draws the list | New Architecture | Runtime deps | Multi-select |
| :--- | ---: | :--- | :--- | :--- | :--- |
| **react-native-native-select** | see the badge above | The OS widget itself: `UIMenu`, `UIPickerView`, `AppCompatSpinner` | Required, old bridge unsupported | none | no |
| [`@react-native-picker/picker`](https://www.npmjs.com/package/@react-native-picker/picker) | 959k | Native pickers too (`RNCPicker` on iOS, dialog and dropdown pickers on Android), plus macOS and Windows | Supported, and it still ships the old-architecture code path | none | no, one `selectedValue` |
| [`react-native-picker-select`](https://www.npmjs.com/package/react-native-picker-select) | 175k | A JS wrapper that puts `@react-native-picker/picker` inside a React `Modal` on iOS | Inherits whatever its peer picker supports | `lodash.isequal`, `lodash.isobject`, and a peer on `@react-native-picker/picker` | no |
| [`react-native-element-dropdown`](https://www.npmjs.com/package/react-native-element-dropdown) | 167k | Pure JS: a `Modal` with a `FlatList` of rows | Works, it is JS only | `lodash` | yes, a `MultiSelect` component |
| [`react-native-dropdown-picker`](https://www.npmjs.com/package/react-native-dropdown-picker) | 157k | Pure JS, no native code in the package at all | Works, it is JS only | none | yes, single and multiple |

Read that table as a positioning statement, not a verdict. Those four libraries give you searchable lists, `{label, value}` items, icons, badges, themes, RTL, placeholders, and multi-select. This one gives you none of that.

What it gives you instead is narrow and specific:

* **The real widget.** Not an imitation of the platform look, the platform widget. It ages with the OS instead of against it.
* **The interaction never touches JS.** Opening the menu, scrolling the wheel and highlighting a row are UIKit and Android View work. JS hears about it once, when the value changes.
* **A long list costs nothing.** A `UIMenu` of 250 rows is 250 `UIAction`s handed to UIKit, not 250 React elements virtualised by a `FlatList`.
* **Nothing to keep in sync.** No dependencies, and the whole surface is five props.

If you need any of the features in the right-hand columns, use one of those libraries. If you want the OS widget and a component you can read end to end in ten minutes, keep reading.

## Requirements

* **React Native:** `>= 0.80.0` (declared as a peer dependency, along with `react`). The codegen spec imports `codegenNativeComponent` and the `CodegenTypes` namespace from the `react-native` root, which only re-exports them from 0.80 onward. On 0.71-0.79, use `1.2.1`.
* **Architecture:** the New Architecture, enabled. `RCT_NEW_ARCH_ENABLED=1` must have been set when you ran `pod install`.
* **iOS:** 14.0+ for `dropdown` mode, since `UIMenu` and `showsMenuAsPrimaryAction` are gated behind an availability check. The podspec deployment target is 11.0, and `dialog` mode works all the way down.
* **Android:** `compileSdkVersion` and `targetSdkVersion` default to 31 and `minSdkVersion` to 21, each read through `safeExtGet` so your root `ext` values win.

The old bridge is genuinely not supported, and that is a compile-time fact rather than a policy: `ios/RTNSelect.mm` imports the codegen-generated `RTNSelectSpec` headers unconditionally, and `SelectViewManager` implements the generated `RTNSelectManagerInterface`. Without codegen output, neither side builds.

## Installation

```bash
npm install react-native-native-select
# or
yarn add react-native-native-select
```

### iOS

```bash
cd ios && pod install
```

### Android

Nothing to do. Autolinking picks up the library's own `SelectViewPackage`, and the shipped `AndroidManifest.xml` declares nothing but the package name: no permissions, no activities, no `MainApplication` edit, no Proguard rule.

## Quick start

```tsx
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Select } from 'react-native-native-select';

const FRUITS = ['Apple', 'Banana', 'Orange', 'Mango'];

export default function App() {
	const [index, setIndex] = useState(0);

	return (
		<View style={styles.container}>
			<Text style={styles.label}>Choose a fruit:</Text>

			<Select
				style={styles.select}
				mode="dropdown"
				options={FRUITS}
				selectedIndex={index}
				onValueChange={(e) => {
					setIndex(e.nativeEvent.index); // 0
					console.log(e.nativeEvent.value); // "Apple"
				}}
			/>

			<Text>Selected: {FRUITS[index]}</Text>
		</View>
	);
}

const styles = StyleSheet.create({
	container: { flex: 1, justifyContent: 'center', padding: 20 },
	label: { marginBottom: 10, fontSize: 16 },
	select: {
		width: '100%',
		height: 50, // width and height are load-bearing, see Troubleshooting
	},
});
```

Two runnable screens live in [`tests/ios_demo.tsx`](./tests/ios_demo.tsx) and [`tests/android_demo.tsx`](./tests/android_demo.tsx). Drop either one in as your `App` component.

## The overlay pattern

This is the part worth reading twice, and the recommended way to ship this component inside a real design system.

The native widget draws itself. You cannot give `UIMenu` your border radius or teach `AppCompatSpinner` your font, and on Android the spinner does not reliably reflect a controlled `selectedIndex` (the visual state can lag behind the prop). So do not fight it: **render your own label, and stretch the `Select` over it invisibly.** The tap opens the real OS widget, the state comes back through `onValueChange`, and the box the user actually looks at is 100% yours.

Since v1.2.0 the Android view calls `setBackground(null)` on init, so the spinner contributes no background of its own underneath your styling.

### The 0.02 constant

```tsx
opacity: 0.02 // never 0
```

That number is the whole trick. iOS `hitTest:withEvent:` ignores views whose alpha is at or below 0.01, so an overlay at `opacity: 0` is invisible **and** untappable: you get a beautiful button that does nothing. `0.02` clears that threshold with a margin to spare and is imperceptible on screen. Android is more forgiving (the demo screen uses `0` and works), so use `0.02` everywhere and stop thinking about it.

### A complete field component

```tsx
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Select } from 'react-native-native-select';

type Props = {
	label: string;
	options: string[];
	selectedIndex: number;
	onSelect: (index: number, value: string) => void;
};

export function SelectField({ label, options, selectedIndex, onSelect }: Props) {
	return (
		<View style={styles.field}>
			<Text style={styles.label} accessible={false}>
				{label}
			</Text>

			<View
				accessible={true}
				accessibilityRole="button"
				accessibilityLabel={`${label}, ${options[selectedIndex]}`}
				accessibilityHint="Opens the list of options"
			>
				{/* Your design system draws this. */}
				<Text style={styles.value} accessible={false}>
					{options[selectedIndex]}
				</Text>

				{/* The real widget, stretched over it, all but invisible. */}
				<Select
					style={styles.overlay}
					mode="dropdown"
					options={options}
					selectedIndex={selectedIndex}
					onValueChange={(e) => onSelect(e.nativeEvent.index, e.nativeEvent.value)}
				/>
			</View>
		</View>
	);
}

const styles = StyleSheet.create({
	field: { marginBottom: 16 },
	label: { fontSize: 13, marginBottom: 6, color: '#444' },
	value: {
		height: 48,
		lineHeight: 48,
		paddingHorizontal: 14,
		borderRadius: 12,
		borderWidth: 1,
		borderColor: '#2483ff',
		fontWeight: '600',
	},
	overlay: {
		position: 'absolute',
		top: 0,
		bottom: 0,
		left: 0,
		right: 0,
		opacity: 0.02, // never 0: iOS hitTest drops taps at alpha <= 0.01
	},
});
```

Usage:

```tsx
const [size, setSize] = useState(0);

<SelectField
	label="Size"
	options={['Small', 'Medium', 'Large']}
	selectedIndex={size}
	onSelect={(index) => setSize(index)}
/>
```

Three details make the difference between this working and nearly working:

1. `position: 'absolute'` with all four edges pinned, so the overlay covers exactly the box you drew, no more.
2. `opacity: 0.02`, for the reason above.
3. `mode="dropdown"`, so iOS anchors a `UIMenu` to the invisible button rather than showing a wheel where your field is.

## Props

`NativeProps` extends `ViewProps`, so every standard view prop (`style`, `testID`, `pointerEvents`, the accessibility props) is accepted on top of these.

| Prop | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| **`options`** | `ReadonlyArray<string>` | **Yes** | The rows to display. Strings only. |
| **`selectedIndex`** | `number` | No | Index of the selected row. The native views start at `0`. Behaves as a controlled value on iOS; see the Android caveat below. |
| **`mode`** | `'dialog' \| 'dropdown'` | No | **iOS only.** `dialog` (the codegen default) uses `UIPickerView`, the classic wheel. `dropdown` uses the iOS 14+ `UIMenu` pull-down. Ignored on Android, which always draws its spinner. |
| **`textColor`** | `ColorValue` | No | **iOS only.** Overrides the text color of both the wheel and the button title. Defaults to `UIColor.labelColor`, which already follows light and dark mode, so set this only when you need to override the system. |
| **`onValueChange`** | `(event) => void` | No | Fired on selection. Read `event.nativeEvent.value` (`string`) and `event.nativeEvent.index` (`number`). |
| **`style`** | `ViewStyle` | No | Standard style prop. Give it a `width` and a `height`, or Flexbox constraints that produce them. |

```tsx
onValueChange={(e) => {
	e.nativeEvent.value; // "Banana"
	e.nativeEvent.index; // 1
}}
```

## Platform differences

Nothing here is a bug to be fixed later, it is what wrapping two different OS widgets behind one component costs. Better said out loud.

* **`mode` and `textColor` do nothing on Android.** In `SelectViewManager`, `setMode` has an empty body and `setTextColor` carries a comment saying so. They exist only because the codegen-generated `RTNSelectManagerInterface` requires them, and a missing method there is a build failure (that is exactly what v1.1.1 fixed). Style Android text through the overlay pattern instead.
* **Android does not reliably reflect a controlled `selectedIndex`.** `AppCompatSpinner` owns its own selection state and the manager pushes the prop through `setSelection`, which posts the update to the next frame. Treat the Android widget as uncontrolled and render the current value yourself, which is the overlay pattern again.
* **Writing `selectedIndex` on Android emits a change event.** `setSelection` reports back through `topValueChange` as well as the adapter's own `onItemSelected`, so a state update driven from JS can echo. If your handler does more than `setState`, guard it against the value it already holds.
* **Below iOS 14, `dropdown` degrades to an inert button.** Both `showsMenuAsPrimaryAction` and the menu construction sit behind `@available(iOS 14.0, *)`, so on iOS 11 to 13 the button renders with no menu attached. Use `dialog` if you still support those versions.
* **iOS applies props on the main queue.** `updateProps` diffs `options`, `selectedIndex`, `mode` and `textColor`, then dispatches the UIKit work asynchronously. A `selectedIndex` change animates the wheel; expect the visual update one turn later, not synchronously with your `setState`.

## Limitations

* **Single-select only.** One `selectedIndex` in, one `{value, index}` out, and iOS pins `numberOfComponentsInPickerView` to `1`. There is no multiple, no chips, no checkbox list.
* **`options` is `string[]`.** No `{label, value}` objects, no icons, no per-row styling, no section headers, no disabled rows. Map to labels before rendering and map back through the index you get in the event.
* **No search or filter.** A 250-row `UIMenu` scrolls, it does not filter.
* **No Expo Go, and no config plugin.** The package ships native code and requires codegen, so it needs bare React Native or an Expo prebuild.
* **`mode` and `textColor` are iOS only** (see above).
* **No accessibility props of its own** (see the next section).
* **No placeholder or empty state.** With an empty `options` array the widget renders empty; there is no "Select an option" row unless you put one in `options` yourself.

### If you need multi-select

Compose it in JS on top of the component. The library will not do it for you, and it is a dozen lines:

```tsx
const ALL = ['Cat', 'Dog', 'Rabbit', 'Ferret'];

const [picked, setPicked] = useState<string[]>([]);
const remaining = ALL.filter((option) => !picked.includes(option));

return (
	<>
		{picked.map((value) => (
			<Chip key={value} label={value} onRemove={() => setPicked(picked.filter((p) => p !== value))} />
		))}

		{remaining.length > 0 && (
			<Select
				style={{ width: 200, height: 48 }}
				mode="dropdown"
				options={remaining}
				onValueChange={(e) => setPicked([...picked, e.nativeEvent.value])}
			/>
		)}
	</>
);
```

The `Select` stays uncontrolled here (no `selectedIndex`), and shrinking `options` after each pick is what stops the same row being chosen twice.

## Accessibility

The library sets no `accessibilityRole` and no `accessibilityLabel` of its own. It does not need to for the widget itself, and it cannot for your field.

What comes free, because the rendered thing genuinely is the platform control:

* **VoiceOver** reads a `UIMenu` as a menu and a `UIPickerView` as an adjustable picker, with the announcements and gestures users already know from the rest of iOS. **TalkBack** reads `AppCompatSpinner` as a spinner.
* **Dark mode** works with no code from you: the iOS default text color is `UIColor.labelColor`, and the Android spinner takes your app theme.
* **Dynamic Type and font scale** apply, since the OS is drawing the text.
* **Reduce Motion, Switch Control, Voice Control, external keyboards:** the platform's job, and already done.

What you own is the wrapper. When you use the overlay pattern, the visual field is your own views, so give the group the semantics of one control:

```tsx
<View
	accessible={true}
	accessibilityRole="button"
	accessibilityLabel={`${label}, ${options[selectedIndex]}`}
	accessibilityHint="Opens the list of options"
	accessibilityState={{ disabled }}
>
	<Text accessible={false}>{options[selectedIndex]}</Text>
	<Select style={styles.overlay} mode="dropdown" options={options} selectedIndex={selectedIndex} onValueChange={handleChange} />
</View>
```

Three things to get right there:

1. `accessible={true}` on the wrapper, `accessible={false}` on the visual children, so the screen reader announces one control instead of a label, a value and an unnamed native view.
2. An `accessibilityLabel` that carries both the field name and the current value. "Size, Medium" tells a blind user where they are; "Medium" alone does not.
3. `accessibilityRole="button"`, because from the outside that is what your field is: a thing you press to open a list.

## Under the hood

Worth knowing before you depend on it, since the whole implementation is short enough to audit in one sitting.

* **A true Fabric component via codegen.** `src/RTNSelectNativeComponent.ts` is a single `codegenNativeComponent<NativeProps>('RTNSelect')` call, and `codegenConfig` in `package.json` names the spec `RTNSelectSpec`. The rest of the implementation is Objective-C++ and Java.
* **The package ships raw TypeScript.** `main`, `react-native`, `types` and `source` all point at `src/index.ts`. There is no build step and no `dist/`, so the code you read on GitHub is exactly the code your bundler compiles.
* **Two TypeScript files, three iOS source files, three Java files.** No dependencies, no vendored assets, no generated code checked in.
* **iOS event path:** selection funnels through one `selectIndex:fromSource:` method that updates state, re-marks the `UIMenu` checkmark, and emits through the Fabric `RTNSelectEventEmitter`.
* **Android event path:** `SelectView` dispatches `topValueChange`, which `getExportedCustomDirectEventTypeConstants` maps to the JS prop `onValueChange`.
* **One thing to know about `textColor`:** on iOS it reaches the wheel through `setValue:forKey:@"textColor"`, key-value coding against a `UIPickerView` key that Apple does not document. It has worked for years and it is how everyone colors that widget, but it is not public API, and it is the one line in this package that could break on a future iOS release. The dropdown button's title color goes through the documented `setTitleColor:forState:`.

## Used in production

This component ships in [Animalert](https://animalert.app), a live lost-pet reporting platform, on iOS and Android. It is on the [App Store](https://apps.apple.com/app/id6480419312) and [Google Play](https://play.google.com/store/apps/details?id=com.animalert), so what follows is checkable rather than asserted.

It backs a dozen direct call sites there plus a shared form field, itself reused a dozen more times, so essentially every select in the app is this component. The most demanding case is the country dial-code picker on the phone-number field: roughly 250 countries, rendered as a native `UIMenu` behind an invisible overlay rather than as a 250-row JS list. That field is the reason the overlay pattern and the `0.02` constant are documented above rather than rediscovered by everyone.

## Troubleshooting

**The component takes up space but shows nothing.**
Give the `style` an explicit `width` and `height`, or Flexbox constraints that resolve to real numbers. A native view with a zero-sized frame lays its subviews out into nothing.

**The app crashes on launch, or the iOS build cannot find `RTNSelectSpec/Props.h`.**
`RCT_NEW_ARCH_ENABLED=1` was not set when you ran `pod install`. Set it, re-run `pod install`, and rebuild. There is no old-bridge fallback.

**An Android build fails on a missing method in `RTNSelectManagerInterface`.**
Update to 1.1.1 or later, where the `setTextColor` stub the generated interface expects was added.

**My invisible overlay does not respond to taps on iOS.**
Its `opacity` is `0` (or below `0.02`). See the overlay pattern section.

**Android ignores `mode` or `textColor`.**
Expected, they are iOS only. See Platform differences.

**Android fires `onValueChange` when I set `selectedIndex` from JS.**
Expected too, and documented in Platform differences. Guard your handler against a value that has not changed.

## Contributing

Bug reports and pull requests are welcome. [`CONTRIBUTING.md`](./CONTRIBUTING.md) covers how to run the component against the demo screens, and what to include in a report so it can be reproduced. Release history is in [`CHANGELOG.md`](./CHANGELOG.md).

## License

MIT, Wayan NEEL. See [LICENSE](./LICENSE).
