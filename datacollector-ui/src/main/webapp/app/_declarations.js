/**
 * Main Module
 */

angular.module('dataCollectorApp', [
  'ngRoute',
  'ngCookies',
  'tmh.dynamicLocale',
  'pascalprecht.translate',
  'templates-app',
  'templates-common',
  'dataCollectorApp.common',
  'dataCollectorApp.home',
  'dataCollectorApp.sdcConfiguration',
  'dataCollectorApp.jvmMetrics',
  'dataCollectorApp.logs',
  'ngStorage',
  'angular-google-analytics'
]);