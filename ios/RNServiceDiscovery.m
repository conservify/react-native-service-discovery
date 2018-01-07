
#import "RNServiceDiscovery.h"
#import <React/RCTLog.h>

@implementation RNServiceDiscovery

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(ServiceDiscovery)

- (NSArray<NSString *> *)supportedEvents {
    return @[@"udp-discovery", @"service-resolved"];
}

RCT_EXPORT_METHOD(start:(int) port)
{
    RCTLogInfo(@"Started ServiceDiscovery");
    // [self sendEventWithName:@"udp-discovery" body: @{@"address": @"192.168.0.12", @"port": @54321 }];
}

@end
  
