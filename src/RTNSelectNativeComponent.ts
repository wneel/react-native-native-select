import { codegenNativeComponent } from 'react-native';

import type { CodegenTypes, ColorValue, HostComponent, ViewProps } from 'react-native';

type OnChangeEvent = Readonly<{
	value: string;
	index: CodegenTypes.Int32;
}>;

/**
 * Wrap your own field in the Select: its children are what the user sees, and a tap anywhere on
 * them opens the native list (a UIMenu on iOS, a dropdown list on Android). With children, iOS
 * always uses the menu, whatever `mode` says. Without children, iOS draws its own widget (the
 * wheel, or a menu button) and Android draws nothing.
 */
export interface NativeProps extends ViewProps {
	options: ReadonlyArray<string>;
	selectedIndex?: CodegenTypes.Int32;
	mode?: CodegenTypes.WithDefault<'dialog' | 'dropdown', 'dialog'>; // iOS only, and only without children
	textColor?: ColorValue; // iOS only
	onValueChange?: CodegenTypes.DirectEventHandler<OnChangeEvent>;
}

export default codegenNativeComponent<NativeProps>(
	'RTNSelect'
) as HostComponent<NativeProps>;
