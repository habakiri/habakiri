# 羽々斬
羽々斬は、吉里吉里2をJavaで実装した互換エンジンです。


## 構成
### core
共通部分です。

### android
Android 固有部分です。

### desktop
desktop (Windows/Linux/MacOSX)固有部分です。

### JOrbis
Ogg vorbis 再生のためのライブラリです。
扱いやすくするために VorbisFile2.java が追加されています。
jar 化して読み込ませます。
LGPL ですが、desktop での利用であれば、jar は動的に読み込まれるため、このライブラリのみ LGPL となります。
android では使用しません。

