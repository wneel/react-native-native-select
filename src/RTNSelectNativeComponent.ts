import { codegenNativeComponent } from 'react-native';

import type { CodegenTypes, ColorValue, HostComponent, ViewProps } from 'react-native';

type OnChangeEvent = Readonly<{
	value: string;
	index: CodegenTypes.Int32;
}>;

export interface NativeProps extends ViewProps {
	options: ReadonlyArray<string>;
	selectedIndex?: CodegenTypes.Int32;
	mode?: CodegenTypes.WithDefault<'dialog' | 'dropdown', 'dialog'>; // iOS only
	textColor?: ColorValue; // iOS only
	onValueChange?: CodegenTypes.DirectEventHandler<OnChangeEvent>;
}

export default codegenNativeComponent<NativeProps>(
	'RTNSelect'
) as HostComponent<NativeProps>;
