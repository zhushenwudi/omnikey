1.在主工程的清单文件中添加

    <uses-feature android:name="android.hardware.usb.host" />

2.在主工程的 res/xml 目录下新建 device_filter.xml，并填入

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <usb-device
            vendor-id="1659"
            product-id="8963" />
        <usb-device
            vendor-id="1659"
            product-id="8964" />
        <usb-device
            vendor-id="1659"
            product-id="41216" />
    </resources>