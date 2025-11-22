#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import <React/RCTViewManager.h>
#import "RTNSelect.h"

@interface RTNSelectManager : RCTViewManager
@end

@implementation RTNSelectManager

RCT_EXPORT_MODULE(RTNSelect)

- (UIView *)view {
	return [[RTNSelect alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(options, NSArray)
RCT_EXPORT_VIEW_PROPERTY(selectedIndex, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(mode, NSString)
RCT_EXPORT_VIEW_PROPERTY(onValueChange, RCTDirectEventBlock)

@end
