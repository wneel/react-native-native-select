
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
				<Text>style it as you want :</Text>
				<View style={{flexDirection: "row", marginBlock: 25}}>
					<View style={{ width: 100, height: 50, backgroundColor: "#2483ff", marginRight: 12, borderRadius: 12, alignItems: "center", justifyContent: "center" }}>
						<Text style={{color: "white", fontWeight: 700}}>{array[second]}</Text>
						<Select
							style={{ width: 100, height: 50, opacity: 0, position: "absolute" }}
							mode="dropdown"
							options={array}
							selectedIndex={second}
							onValueChange={(e) => setSecond(e.nativeEvent.index)}
						/>
					</View>
					<View style={{ width: 100, height: 50, backgroundColor: "#000000AA", marginRight: 12, borderRadius: 12, alignItems: "center", justifyContent: "center" }}>
						<Text style={{color: "white", fontWeight: 700}}>Click me !</Text>
						<Select
							style={{ width: 100, height: 50, opacity: 0, position: "absolute" }}
							mode="dropdown"
							options={array}
							selectedIndex={second}
							onValueChange={(e) => setSecond(e.nativeEvent.index)}
						/>
					</View>
					<View style={{ width: 100, height: 50, backgroundColor: "#000000AA" }}>
						<Select
							style={{ flex: 1, opacity: 0 }}
							mode="dropdown"
							options={array}
							selectedIndex={second}
							onValueChange={(e) => setSecond(e.nativeEvent.index)}
						/>
					</View>
				</View>
				<Text>selected : {array[second]}</Text>
			</View>
			<View>
				<Text>because it's not working properly with the native style :</Text>
				<Select
					style={{ width: 300, height: 100 }}
					options={array}
					selectedIndex={first}
					onValueChange={(e) => { console.d(e.nativeEvent);setFirst(e.nativeEvent.index) }}
				/>
				<Text>selected : {array[first]}</Text>
			</View>
		</View>
	);
}

export default App;
