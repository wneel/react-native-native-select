
import { useState } from "react";
import { Text, View } from "react-native"
import { Select } from "react-native-native-select";

const App = () => {
	const array = [
		"Apple",
		"Banana",
		"Orange",
		"Really really really long text"
	]

	const [first, setFirst] = useState(0);
	const [second, setSecond] = useState(0);

	return (
		<View style={{
			flex: 1,
			alignItems: "center",
			justifyContent: "space-evenly",
			backgroundColor: "white"
		}}>
			<Text>react-native-native-select</Text>
			<View>
				<Select
					style={{ width: 300, height: 200 }}
					options={array}
					selectedIndex={first}
					onValueChange={(e) => setFirst(e.nativeEvent.index)}
				/>
				<Text>selected : {array[first]}</Text>
			</View>
			<View>
				<Select
					style={{ width: 190, height: 50 }}
					mode="dropdown"
					options={array}
					selectedIndex={second}
					onValueChange={(e) => setSecond(e.nativeEvent.index)}
				/>
				<Text>selected : {array[second]}</Text>
			</View>
		</View>
	);
}

export default App;
