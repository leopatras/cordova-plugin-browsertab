/*! @file CBTBrowserTab.h
    @brief Browser tab plugin for Cordova
    @copyright
        Copyright 2016 Google Inc. All Rights Reserved.
    @copydetails
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */

#import "Cordova/CDVPlugin.h"
#import <SafariServices/SafariServices.h>

@interface CBTBrowserTab : CDVPlugin <SFSafariViewControllerDelegate>

- (void)isAvailable:(CDVInvokedUrlCommand *)command;
- (void)openUrl:(CDVInvokedUrlCommand *)command;
- (void)close:(CDVInvokedUrlCommand *)command;

@end
