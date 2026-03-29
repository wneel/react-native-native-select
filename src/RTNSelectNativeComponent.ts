import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

import type { ColorValue, HostComponent } from 'react-native';
import type { ViewProps } from 'react-native/Libraries/Components/View/ViewPropTypes';
import type { Int32, DirectEventHandler, WithDefault } from 'react-native/Libraries/Types/CodegenTypes';

type OnChangeEvent = Readonly<{
	value: string;
	index: Int32;
}>;

export interface NativeProps extends ViewProps {
	options: ReadonlyArray<string>;
	selectedIndex?: Int32;
	mode?: WithDefault<'dialog' | 'dropdown', 'dialog'>; // iOS only
	textColor?: ColorValue; // iOS only
	onValueChange?: DirectEventHandler<OnChangeEvent>;
}

export default codegenNativeComponent<NativeProps>(
	'RTNSelect'
) as HostComponent<NativeProps>;
