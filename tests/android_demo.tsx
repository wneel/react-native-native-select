import { useState } from "react";
import { ScrollView, Text, View } from "react-native"
import { Select } from "react-native-native-select";

// Runs on both platforms. Each field is drawn inside its Select, the way an app ships it.
const App = () => {
	const array = [
		"Apple",
		"Banana",
		"Orange",
		"Really really really long text"
	]

	const [selected, setSelected] = useState(0);

	return (
		<ScrollView contentContainerStyle={{
			flexGrow: 1,
			alignItems: "center",
			justifyContent: "space-evenly",
			backgroundColor: "white"
		}}>
			<Text>react-native-native-select</Text>
			<View style={{flexDirection: "row", marginBlock: 25}}>
				{/* A filled field. */}
				<Select
					style={{ marginRight: 12 }}
					options={array}
					selectedIndex={selected}
					onValueChange={(e) => setSelected(e.nativeEvent.index)}
				>
					<View style={{ width: 100, height: 50, backgroundColor: "#2483ff", borderRadius: 12, alignItems: "center", justifyContent: "center" }}>
						<Text style={{color: "white", fontWeight: 700}} numberOfLines={1}>{array[selected]}</Text>
					</View>
				</Select>
				{/* A field with an elevated child: the shadow must not take the tap. */}
				<Select
					style={{ marginRight: 12 }}
					options={array}
					selectedIndex={selected}
					onValueChange={(e) => setSelected(e.nativeEvent.index)}
				>
					<Text style={{ width: 100, height: 50, lineHeight: 50, textAlign: "center", backgroundColor: "white", borderRadius: 12, borderWidth: 2, borderColor: "#2483ff", elevation: 10, shadowOpacity: 0.2, shadowRadius: 8 }}>Click me</Text>
				</Select>
				{/* A wide field against a short list: the list keeps the field's width. */}
				<Select
					options={array.slice(0, 2)}
					selectedIndex={selected % 2}
					onValueChange={(e) => setSelected(e.nativeEvent.index)}
				>
					<View style={{ width: 100, height: 50, backgroundColor: "#000000AA", borderRadius: 12 }} />
				</Select>
			</View>
			<Text>selected : {array[selected]}</Text>
			{/* A field turned off without unmounting it. */}
			<Select
				pointerEvents="none"
				options={array}
				selectedIndex={selected}
			>
				<Text style={{ padding: 12, color: "#999" }}>Disabled: {array[selected]}</Text>
			</Select>
		</ScrollView>
	);
}

export default App;
