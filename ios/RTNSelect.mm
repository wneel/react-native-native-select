#import "RTNSelect.h"
#import <React/RCTComponent.h>

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

	std::vector<std::string> _options;
	NSInteger _selectedIndex;
	RCTDirectEventBlock _onValueChangeLegacy;
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

		_button = [UIButton buttonWithType:UIButtonTypeSystem];
		_button.frame = self.bounds;
		_button.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

		// Style du bouton (alignement à gauche, couleur noire pour le texte)
		_button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
		[_button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];

		if (@available(iOS 14.0, *)) {
			_button.showsMenuAsPrimaryAction = YES;
		}

		_button.hidden = YES;

		[self addSubview:_pickerView];
		[self addSubview:_button];
	}

	return self;
}

- (void)layoutSubviews {
	[super layoutSubviews];
	_pickerView.frame = self.bounds;
	_button.frame = self.bounds;
}

// ============================================================================
// Gestion du Menu Flottant (Dropdown)
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
				[_button setTitle:title forState:UIControlStateNormal];
			}

			[actions addObject:action];
		}

		UIMenu *menu = [UIMenu menuWithTitle:@"" children:actions];
		_button.menu = menu;
	}
}

// ============================================================================
// Méthode unifiée de sélection (appelée par Picker ou Dropdown)
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

	if (_onValueChangeLegacy) {
		_onValueChangeLegacy(@{
			@"value": [NSString stringWithUTF8String:_options[index].c_str()],
			@"index": @(index)
		});
	}
}

// ============================================================================
// 1. FABRIC (New Architecture) Implementation
// ============================================================================

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
	const auto &oldViewProps = *std::static_pointer_cast<const RTNSelectProps>(_props);
	const auto &newViewProps = *std::static_pointer_cast<const RTNSelectProps>(props);

	bool optionsChanged = oldViewProps.options != newViewProps.options;
	bool indexChanged = oldViewProps.selectedIndex != newViewProps.selectedIndex;
	bool modeChanged = oldViewProps.mode != newViewProps.mode;

	if (optionsChanged) {
		_options = newViewProps.options;
	}

	if (indexChanged) {
		_selectedIndex = newViewProps.selectedIndex;
	}

	[super updateProps:props oldProps:oldProps];

	dispatch_async(dispatch_get_main_queue(), ^{
		if (optionsChanged) {
			[self->_pickerView reloadAllComponents];
			[self updateButtonMenu];
		}

		if (indexChanged && newViewProps.selectedIndex < self->_options.size()) {
			[self->_pickerView selectRow:newViewProps.selectedIndex inComponent:0 animated:YES];
			[self updateButtonMenu];
		}

		if (modeChanged || optionsChanged) {
			if (newViewProps.mode == RTNSelectMode::Dropdown) {
				self->_pickerView.hidden = YES;
				self->_button.hidden = NO;
			} else {
				self->_pickerView.hidden = NO;
				self->_button.hidden = YES;
			}
		}
	});
}

// ============================================================================
// 2. LEGACY / INTEROP Implementation
// ============================================================================

- (void)setOptions:(NSArray<NSString *> *)options {
	_options.clear();
	for (NSString *option in options) {
		_options.push_back([option UTF8String]);
	}
	dispatch_async(dispatch_get_main_queue(), ^{
		[self->_pickerView reloadAllComponents];
		[self updateButtonMenu];
	});
}

- (void)setSelectedIndex:(NSInteger)selectedIndex {
	_selectedIndex = selectedIndex;
	dispatch_async(dispatch_get_main_queue(), ^{
		if (selectedIndex < self->_options.size()) {
			[self->_pickerView selectRow:selectedIndex inComponent:0 animated:YES];
			[self updateButtonMenu];
		}
	});
}

- (void)setMode:(NSString *)mode {
	dispatch_async(dispatch_get_main_queue(), ^{
		if ([mode isEqualToString:@"dropdown"]) {
			self->_pickerView.hidden = YES;
			self->_button.hidden = NO;
		} else {
			self->_pickerView.hidden = NO;
			self->_button.hidden = YES;
		}
	});
}

- (void)setOnValueChange:(RCTDirectEventBlock)onValueChange {
	_onValueChangeLegacy = onValueChange;
}

// ============================================================================
// 3. UIPickerView Delegate Methods
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
