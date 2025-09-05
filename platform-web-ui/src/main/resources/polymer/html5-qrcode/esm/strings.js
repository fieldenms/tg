var Html5QrcodeStrings = (function () {
    function Html5QrcodeStrings() {
    }
    Html5QrcodeStrings.codeParseError = function (exception) {
        return "QR code parse error, error = ".concat(exception);
    };
    Html5QrcodeStrings.errorGettingUserMedia = function (error) {
        return "Error getting userMedia, error = ".concat(error);
    };
    Html5QrcodeStrings.onlyDeviceSupportedError = function () {
        return "The device doesn't support navigator.mediaDevices , only "
            + "supported cameraIdOrConfig in this case is deviceId parameter "
            + "(string).";
    };
    Html5QrcodeStrings.cameraStreamingNotSupported = function () {
        return "Camera streaming not supported by the browser.";
    };
    Html5QrcodeStrings.unableToQuerySupportedDevices = function () {
        return "Unable to query supported devices, unknown error.";
    };
    Html5QrcodeStrings.insecureContextCameraQueryError = function () {
        return "Camera access is only supported in secure context like https "
            + "or localhost.";
    };
    Html5QrcodeStrings.scannerPaused = function () {
        return "Scanner paused";
    };
    return Html5QrcodeStrings;
}());

export { Html5QrcodeStrings };
