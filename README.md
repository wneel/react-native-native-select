# react-native-native-select

A strictly native, performant Select component for React Native built exclusively for the **New Architecture**.

It leverages actual OS primitives: `UIMenu` (iOS 14+), `UIPickerView` (iOS Wheel), and `AppCompatSpinner` (Android), offering a truly native look and feel that JS-based libraries cannot match.

## Preview

| iOS (Dropdown Mode) | iOS (Dialog/Wheel Mode) | Android |
|:---:|:---:|:---:|
| *[Insert Screenshot of iOS Select with open Menu]* | *[Insert Screenshot of iOS Bottom Wheel picker]* | *[Insert Screenshot of Android Dropdown]* |
| *Native UIMenu (iOS 14+)* | *Classic UIPickerView* | *Native AppCompatSpinner* |

## Why this library?

Most Select/Picker libraries in the React Native ecosystem fall into two categories:
1.  **JS-based Simulations:** They render a Modal with a FlatList. They are customizable but feel "off" compared to the OS native UI and often lack accessibility standards like [`react-native-picker-select`](https://www.npmjs.com/package/react-native-picker-select).
2.  **Legacy Wrappers:** Libraries like [`@react-native-picker/picker`](https://www.npmjs.com/package/@react-native-picker/picker) are excellent but often rely on the old bridge or split functionality across different components.

**react-native-native-select** is designed for modern apps:
* **Zero JS Thread Overhead:** Fully native implementation using Fabric.
* **Modern iOS UI:** Supports the iOS 14+ `pull-down` menu style out of the box.
* **Native Performance:** Interactions run on the UI thread.

## Requirements

* **React Native:** >= 0.71.0
* **Architecture:** New Architecture **Enabled**
* **iOS:** 14.0+ (for Dropdown mode), 11.0+ (for Dialog mode)

## Installation

```bash
npm install react-native-native-select
# or
yarn add react-native-native-select
```

### iOS Setup

Since this library uses native modules, you must run pod install:

```bash
cd ios && pod install
```

## Usage

```tsx
import { StyleSheet, View, Text } from 'react-native';
import { Select } from 'react-native-native-select';

export default function App() {
	return (
		<View style={styles.container}>
			<Text style={styles.label}>Choose a fruit:</Text>

			<Select
				style={styles.select}
				mode="dropdown"
				options={["Apple", "Banana", "Orange", "Mango"]}
				selectedIndex={0}
				onValueChange={(e) => {
					console.log("Selected Item:", e.nativeEvent.value); // Ex: "Apple"
					console.log("Selected Index:", e.nativeEvent.index); // Ex: 0
				}}
			/>
		</View>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		padding: 20,
	},
	label: {
		marginBottom: 10,
		fontSize: 16,
	},
	select: {
		width: '100%',
		height: 50, // Height is required for layout calculation
	},
});
```

### Android Usage

Please take a look at this [Android example usage](./tests/android_demo.tsx) because the native component is not updating the visual state at the moment

## Props

| Prop | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| **`options`** | `string[]` | **Yes** | An array of strings to display in the list. |
| **`selectedIndex`** | `number` | No | The index of the currently selected item. Defaults to `0`. |
| **`mode`** | `'dropdown' \| 'dialog'` | No | **iOS Only.** <br>`dropdown`: Uses `UIMenu` (Modern iOS 14+). <br>`dialog`: Uses `UIPickerView` (Classic Wheel). <br> *On Android, this prop is ignored as it always uses the native Spinner.* |
| **`onValueChange`** | `function` | No | Callback fired when an item is selected. Returns `{ value: string, index: number }`. |
| **`style`** | `ViewStyle` | No | Standard style prop. **Note:** You must define `width` and `height` for the view to render correctly. |

## Troubleshooting

**The component is visible but empty (Empty space)**
Ensure you have defined a `width` and `height` in the `style` prop. Native views on iOS require explicit dimensions or Flexbox constraints to render their subviews correctly.

**App crashes on launch**
Ensure `RCT_NEW_ARCH_ENABLED=1` was present when you ran `pod install`. This library does not support the old React Native Bridge.
