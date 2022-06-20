import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:sentiance_flutter/models/mobile_health_data.dart';

export 'package:sentiance_flutter/models/mobile_health_data.dart';

class SentianceFlutter {
  static Map getCredentails(token, sentianceSecret, appId, userLinkUrl,
      crashDetectionUrl, mobileHealthUrl, customerId, userId) {
    return {
      "data": {
        "token": token,
        "sentiance_secret": sentianceSecret,
        "app_id": appId,
        "user_link_url": userLinkUrl,
        "crash_detection_url": crashDetectionUrl,
        "mobile_health_url": mobileHealthUrl,
        "customer_id": customerId,
        "user_id": userId
      }
    };
  }

  // linking user with sentiance id then sdk is initialized

  static const MethodChannel _channel = MethodChannel('sentiance_flutter');
  static const MethodChannel _channel1 =
      MethodChannel('flutter.sentiance/helper');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('intialiseSdk');
    return version;
  }

  // enable the location and physical activity
  static Future<dynamic> enableLocation(Map<dynamic, dynamic> data) async {
    var initSentiance = await _channel.invokeMethod('enableLocation');
    return initSentiance;
  }

  static initialiseSentiance(
      {required String token,
      required String sentianceSecret,
      appId,
      userLinkUrl,
      crashDetectionUrl,
      mobileHealthUrl,
      customerId,
      userId}) async {
    var credentials = getCredentails(token, sentianceSecret, appId, userLinkUrl,
        crashDetectionUrl, mobileHealthUrl, customerId, userId);
    var initSentiance =
        await _channel.invokeMethod('intialiseSdk', credentials);
    return initSentiance;
  }

  // get the sentiance data
  static Future<dynamic> get getSentianceData async {
    var sentianceData = await _channel.invokeMethod('getSentianceData');
    return sentianceData;
  }

  // start the sentiance sdk
  static Future<dynamic> get startSentianceSDK async {
    var startSentiance = await _channel.invokeMethod('startSdk');
    return startSentiance;
  }

  // stop the sentiance sdk
  static Future<String?> get stopSentianceSDK async {
    var stopSentiance = await _channel.invokeMethod('stopSdk');
    return stopSentiance;
  }

  // get the status of the sdk
  static Future<dynamic> get getSentianceStatus async {
    var sentianceStatus = await _channel.invokeMethod('statusSdk');
    return sentianceStatus;
  }

// init dummyCrash
  static Future<dynamic> get dummycrash async {
    var dummycrashdata = await _channel.invokeMethod('dummycrash');
    return dummycrashdata;
  }

 static Future<dynamic> get disableBatteryOptimization async {
    var disableBatteryOptimization = await _channel.invokeMethod('disableBatteryOptimization');
    return disableBatteryOptimization;
  }  

  static Future<dynamic> get getSentianceInitial async {
    var sentianceStatus = await _channel1.invokeMethod('sentiance_initial');
    return sentianceStatus;
  }

  // get the health information about the device
  static Future<MobileHealthData> get getMobileHealthData async {
    var sentianceMobileHealthData =
        await _channel.invokeMethod('getMobileHealthData');
    return MobileHealthData.fromJson(jsonDecode(sentianceMobileHealthData));
  }
}
