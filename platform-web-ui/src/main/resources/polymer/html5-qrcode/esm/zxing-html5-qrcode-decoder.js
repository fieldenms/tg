import * as zxingJs_umd from '../../../_virtual/zxing-js.umd.js';
import { z as zxingJs_umdExports } from '../../../_virtual/zxing-js.umd.js';
import { Html5QrcodeSupportedFormats, QrcodeResultFormat } from './core.js';

var ZXingHtml5QrcodeDecoder = (function () {
    function ZXingHtml5QrcodeDecoder(requestedFormats, verbose, logger) {
        this.formatMap = new Map([
            [Html5QrcodeSupportedFormats.QR_CODE, zxingJs_umdExports.BarcodeFormat.QR_CODE],
            [Html5QrcodeSupportedFormats.AZTEC, zxingJs_umdExports.BarcodeFormat.AZTEC],
            [Html5QrcodeSupportedFormats.CODABAR, zxingJs_umdExports.BarcodeFormat.CODABAR],
            [Html5QrcodeSupportedFormats.CODE_39, zxingJs_umdExports.BarcodeFormat.CODE_39],
            [Html5QrcodeSupportedFormats.CODE_93, zxingJs_umdExports.BarcodeFormat.CODE_93],
            [
                Html5QrcodeSupportedFormats.CODE_128,
                zxingJs_umdExports.BarcodeFormat.CODE_128
            ],
            [
                Html5QrcodeSupportedFormats.DATA_MATRIX,
                zxingJs_umdExports.BarcodeFormat.DATA_MATRIX
            ],
            [
                Html5QrcodeSupportedFormats.MAXICODE,
                zxingJs_umdExports.BarcodeFormat.MAXICODE
            ],
            [Html5QrcodeSupportedFormats.ITF, zxingJs_umdExports.BarcodeFormat.ITF],
            [Html5QrcodeSupportedFormats.EAN_13, zxingJs_umdExports.BarcodeFormat.EAN_13],
            [Html5QrcodeSupportedFormats.EAN_8, zxingJs_umdExports.BarcodeFormat.EAN_8],
            [Html5QrcodeSupportedFormats.PDF_417, zxingJs_umdExports.BarcodeFormat.PDF_417],
            [Html5QrcodeSupportedFormats.RSS_14, zxingJs_umdExports.BarcodeFormat.RSS_14],
            [
                Html5QrcodeSupportedFormats.RSS_EXPANDED,
                zxingJs_umdExports.BarcodeFormat.RSS_EXPANDED
            ],
            [Html5QrcodeSupportedFormats.UPC_A, zxingJs_umdExports.BarcodeFormat.UPC_A],
            [Html5QrcodeSupportedFormats.UPC_E, zxingJs_umdExports.BarcodeFormat.UPC_E],
            [
                Html5QrcodeSupportedFormats.UPC_EAN_EXTENSION,
                zxingJs_umdExports.BarcodeFormat.UPC_EAN_EXTENSION
            ]
        ]);
        this.reverseFormatMap = this.createReverseFormatMap();
        if (!zxingJs_umd) {
            throw "Use html5qrcode.min.js without edit, ZXing not found.";
        }
        this.verbose = verbose;
        this.logger = logger;
        var formats = this.createZXingFormats(requestedFormats);
        var hints = new Map();
        hints.set(zxingJs_umdExports.DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.set(zxingJs_umdExports.DecodeHintType.TRY_HARDER, false);
        this.hints = hints;
    }
    ZXingHtml5QrcodeDecoder.prototype.decodeAsync = function (canvas) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            try {
                resolve(_this.decode(canvas));
            }
            catch (error) {
                reject(error);
            }
        });
    };
    ZXingHtml5QrcodeDecoder.prototype.decode = function (canvas) {
        var zxingDecoder = new zxingJs_umdExports.MultiFormatReader(this.verbose, this.hints);
        var luminanceSource = new zxingJs_umdExports.HTMLCanvasElementLuminanceSource(canvas);
        var binaryBitmap = new zxingJs_umdExports.BinaryBitmap(new zxingJs_umdExports.HybridBinarizer(luminanceSource));
        var result = zxingDecoder.decode(binaryBitmap);
        return {
            text: result.text,
            format: QrcodeResultFormat.create(this.toHtml5QrcodeSupportedFormats(result.format)),
            debugData: this.createDebugData()
        };
    };
    ZXingHtml5QrcodeDecoder.prototype.createReverseFormatMap = function () {
        var result = new Map();
        this.formatMap.forEach(function (value, key, _) {
            result.set(value, key);
        });
        return result;
    };
    ZXingHtml5QrcodeDecoder.prototype.toHtml5QrcodeSupportedFormats = function (zxingFormat) {
        if (!this.reverseFormatMap.has(zxingFormat)) {
            throw "reverseFormatMap doesn't have ".concat(zxingFormat);
        }
        return this.reverseFormatMap.get(zxingFormat);
    };
    ZXingHtml5QrcodeDecoder.prototype.createZXingFormats = function (requestedFormats) {
        var zxingFormats = [];
        for (var _i = 0, requestedFormats_1 = requestedFormats; _i < requestedFormats_1.length; _i++) {
            var requestedFormat = requestedFormats_1[_i];
            if (this.formatMap.has(requestedFormat)) {
                zxingFormats.push(this.formatMap.get(requestedFormat));
            }
            else {
                this.logger.logError("".concat(requestedFormat, " is not supported by")
                    + "ZXingHtml5QrcodeShim");
            }
        }
        return zxingFormats;
    };
    ZXingHtml5QrcodeDecoder.prototype.createDebugData = function () {
        return { decoderName: "zxing-js" };
    };
    return ZXingHtml5QrcodeDecoder;
}());

export { ZXingHtml5QrcodeDecoder };
