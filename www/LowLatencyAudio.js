cordova.define("com.phonegap.LowLatencyAudio", function(require, exports, module) {

	var exec = require('cordova/exec');

	var LowLatencyAudio = {
  
		preloadFX: function ( id, assetPath, success, fail) {
			return exec(success, fail, "com.phonegap.LowLatencyAudio", "preloadFX", [id, assetPath]);
		},    
	
		preloadAudio: function ( id, assetPath, voices, success, fail) {
			return cordova.exec(success, fail, "com.phonegap.LowLatencyAudio", "preloadAudio", [id, assetPath, voices]);
		},
	
		play: function (id, success, fail) {
			return cordova.exec(success, fail, "com.phonegap.LowLatencyAudio", "play", [id]);
		},
	
		stop: function (id, success, fail) {
			return cordova.exec(success, fail, "com.phonegap.LowLatencyAudio", "stop", [id]);
		},
	
		loop: function (id, success, fail) {
			return cordova.exec(success, fail, "com.phonegap.LowLatencyAudio", "loop", [id]);
		},
	
		unload: function (id, success, fail) {
			return cordova.exec(success, fail, "com.phonegap.LowLatencyAudio", "unload", [id]);
		}
	};

	module.exports = new LowLatencyAudio();
});


