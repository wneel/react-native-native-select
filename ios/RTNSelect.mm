#import "RTNSelect.h"
#import <React/RCTComponent.h>
#import <React/RCTConversions.h>

#import <react/renderer/components/RTNSelectSpec/ComponentDescriptors.h>
#import <react/renderer/components/RTNSelectSpec/EventEmitters.h>
#import <react/renderer/components/RTNSelectSpec/Props.h>
#import <react/renderer/components/RTNSelectSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface RTNSelect () <RCTRTNSelectViewProtocol>
@end

@implementation RTNSelect {
	UIPickerView *_pickerView;
	UIButton *_button;
	// The React children, drawn inside the button so the button is what the user touches and
	// what the menu grows out of. It takes no touch of its own.
	UIView *_childrenHost;
	BOOL _dropdownMode;

	std::vector<std::string> _options;
	NSInteger _selectedIndex;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
	return concreteComponentDescriptorProvider<RTNSelectComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
	if (self = [super initWithFrame:frame]) {
		static const auto defaultProps = std::make_shared<const RTNSelectProps>();
		_props = defaultProps;
		_selectedIndex = 0;

		_pickerView = [[UIPickerView alloc] initWithFrame:self.bounds];
		_pickerView.dataSource = self;
		_pickerView.delegate = self;
		_pickerView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

		_button = [UIButton buttonWithType:UIButtonTypeCustom];
		_button.frame = self.bounds;
		_button.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		_button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;

		if (@available(iOS 14.0, *)) {
			_button.showsMenuAsPrimaryAction = YES;
		}

		_childrenHost = [[UIView alloc] initWithFrame:_button.bounds];
		_childrenHost.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		_childrenHost.userInteractionEnabled = NO;
		[_button addSubview:_childrenHost];

		_dropdownMode = NO;
		_button.hidden = YES;

		[self addSubview:_pickerView];
		[self addSubview:_button];

		[self applyTextColor:nil];
	}

	return self;
}

- (void)layoutSubviews {
	[super layoutSubviews];
	_pickerView.frame = self.bounds;
	_button.frame = self.bounds;
	// Fabric lays the children out in this view's coordinates, and the host sits at the same
	// origin inside a button that covers this view, so the two spaces are the same.
	_childrenHost.frame = _button.bounds;
}

// ============================================================================
// Children: the field the app draws
// ============================================================================

- (BOOL)hasChildren {
	return _childrenHost.subviews.count > 0;
}

- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
	[_childrenHost insertSubview:childComponentView atIndex:index];
	[self applyPresentation];
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
	[childComponentView removeFromSuperview];
	[self applyPresentation];
}

// A Select with children is a trigger: it opens the menu whatever `mode` says, since a wheel
// cannot be drawn over the app's own field. Without children, `mode` picks the widget.
- (void)applyPresentation {
	BOOL trigger = _dropdownMode || [self hasChildren];
	_pickerView.hidden = trigger;
	_button.hidden = !trigger;
	[self updateButtonMenu];
}

// ============================================================================
// Core Text Color Logic (KVC)
// ============================================================================

- (void)applyTextColor:(UIColor *)color {
	UIColor *resolvedColor = color ?: [UIColor labelColor];

	[_pickerView setValue:resolvedColor forKey:@"textColor"];

	[_button setTitleColor:resolvedColor forState:UIControlStateNormal];

	[_pickerView reloadAllComponents];
	[self updateButtonMenu];
}

// ============================================================================
// Managing the Dropdown Menu
// ============================================================================

- (void)updateButtonMenu {
	if (@available(iOS 14.0, *)) {
		NSMutableArray<UIAction *> *actions = [NSMutableArray array];

		for (int i = 0; i < _options.size(); i++) {
			std::string optionString = _options[i];
			NSString *title = [NSString stringWithUTF8String:optionString.c_str()];

			UIAction *action = [UIAction actionWithTitle:title image:nil identifier:nil handler:^(__kindof UIAction * _Nonnull action) {
					[self selectIndex:i fromSource:@"dropdown"];
			}];

			if (i == _selectedIndex) {
				action.state = UIMenuElementStateOn;
				if ([self hasChildren]) {
					// The app draws the value itself. The button stays readable to VoiceOver.
					[_button setTitle:nil forState:UIControlStateNormal];
					_button.accessibilityLabel = title;
				} else {
					[_button setTitle:title forState:UIControlStateNormal];
					_button.accessibilityLabel = nil;
				}
			}

			[actions addObject:action];
		}

		UIMenu *menu = [UIMenu menuWithTitle:@"" children:actions];
		_button.menu = menu;
	}
}

// ============================================================================
// Unified selection method (Picker or Dropdown)
// ============================================================================

- (void)selectIndex:(NSInteger)index fromSource:(NSString *)source {
	if (index >= _options.size()) return;

	_selectedIndex = index;

	if ([source isEqualToString:@"dropdown"]) {
		[_pickerView selectRow:index inComponent:0 animated:NO];
		[self updateButtonMenu];
	} else {
		[self updateButtonMenu];
	}

	if (_eventEmitter) {
		auto emitter = std::static_pointer_cast<RTNSelectEventEmitter const>(_eventEmitter);
		emitter->onValueChange({
			.value = _options[index],
			.index = (int)index
		});
	}
}

// ============================================================================
// Props
// ============================================================================

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
	const auto &oldViewProps = *std::static_pointer_cast<const RTNSelectProps>(_props);
	const auto &newViewProps = *std::static_pointer_cast<const RTNSelectProps>(props);

	bool optionsChanged = oldViewProps.options != newViewProps.options;
	bool indexChanged = oldViewProps.selectedIndex != newViewProps.selectedIndex;
	bool modeChanged = oldViewProps.mode != newViewProps.mode;
	bool textColorChanged = oldViewProps.textColor != newViewProps.textColor;

	if (optionsChanged) {
		_options = newViewProps.options;
	}

	if (indexChanged) {
		_selectedIndex = newViewProps.selectedIndex;
	}

	// The block runs a turn later, by which time another update may have replaced the props
	// object, so it takes plain values rather than a reference into it.
	UIColor *newColor = textColorChanged ? RCTUIColorFromSharedColor(newViewProps.textColor) : nil;
	NSInteger newIndex = newViewProps.selectedIndex;
	BOOL newDropdownMode = newViewProps.mode == RTNSelectMode::Dropdown;

	[super updateProps:props oldProps:oldProps];

	dispatch_async(dispatch_get_main_queue(), ^{
		if (textColorChanged) {
			[self applyTextColor:newColor];
		}

		if (optionsChanged) {
			[self->_pickerView reloadAllComponents];
			[self updateButtonMenu];
		}

		if (indexChanged && newIndex >= 0 && newIndex < (NSInteger)self->_options.size()) {
			[self->_pickerView selectRow:newIndex inComponent:0 animated:YES];
			[self updateButtonMenu];
		}

		if (modeChanged || optionsChanged) {
			self->_dropdownMode = newDropdownMode;
			[self applyPresentation];
		}
	});
}

// ============================================================================
// 2. UIPickerView Delegate Methods
// ============================================================================

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
	return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
	return _options.size();
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
	if (row < _options.size()) {
		return [NSString stringWithUTF8String:_options[row].c_str()];
	}
	return @"";
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
	[self selectIndex:row fromSource:@"picker"];
}

@end

Class<RCTComponentViewProtocol> RTNSelectCls(void)
{
	return RTNSelect.class;
}
