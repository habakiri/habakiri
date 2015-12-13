/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
/**
 * Message Strings ( these should be localized )
 */
package jp.kirikiri.tvp2.msg;

import jp.kirikiri.tjs2.TJSException;

public class Message {
	// Japanese localized messages
	static public final String ScriptExceptionRaised = "スクリプトで例外が発生しました";
	static public final String HardwareExceptionRaised = "ハードウェア例外が発生しました";
	static public final String MainCDPName = "スクリプトエディタ (メイン)";
	static public final String ExceptionCDPName = "スクリプトエディタ (例外通知)";
	//static public final String CannnotLocateUIDLLForFolderSelection = "フォルダ/アーカイブの選択画面を表示しようとしましたが krdevui.dll が見つからないので表示できません.\n実行するフォルダ/アーカイブはコマンドラインの引数として指定してください";
	static public final String CannnotLocateUIDLLForFolderSelection = "dataフォルダ/data.xp3 が見付かりません。\n実行するフォルダ/アーカイブはコマンドラインの引数として指定してください";
	static public final String InvalidUIDLL = "krdevui.dll が異常か、バージョンが一致しません";
	static public final String InvalidBPP = "無効な色深度です";
	static public final String CannotLoadPlugin = "プラグイン %1 を読み込めません";
	static public final String NotValidPlugin = "%1 は有効なプラグインではありません";
	static public final String PluginUninitFailed = "プラグインの解放に失敗しました";
	static public final String CannnotLinkPluginWhilePluginLinking = "プラグインの接続中に他のプラグインを接続することはできまません";
	static public final String NotSusiePlugin = "異常な Susie プラグインです";
	static public final String SusiePluginError = "Susie プラグインでエラーが発生しました/エラーコード %1";
	static public final String CannotReleasePlugin = "指定されたプラグインは使用中のため解放できません";
	static public final String NotLoadedPlugin = "%1 は読み込まれていません";
	static public final String CannotAllocateBitmapBits = "ビットマップ用メモリを確保できません/%1(size=%2)";
	static public final String ScanLineRangeOver = "スキャンライン %1 は範囲(0～%2)を超えています";
	static public final String PluginError = "プラグインでエラーが発生しました/%1";
	static public final String InvalidCDDADrive = "指定されたドライブでは CD-DA を再生できません";
	static public final String CDDADriveNotFound = "CD-DA を再生できるドライブが見つかりません";
	static public final String MCIError = "MCI でエラーが発生しました : %1";
	static public final String InvalidSMF = "有効な SMF ファイルではありません : %1";
	static public final String MalformedMIDIMessage = "指定されたメッセージは MIDI メッセージとして有効な形式ではありません";
	static public final String CannotInitDirectSound = "DirectSound を初期化できません : %1";
	static public final String CannotCreateDSSecondaryBuffer = "DirectSound セカンダリバッファを作成できません : %1/%2";
	static public final String InvalidLoopInformation = "ループ情報 %1 は異常です";
	static public final String NotChildMenuItem = "指定されたメニュー項目はこのメニュー項目の子ではありません";
	static public final String CannotInitDirectDraw = "DirectDraw を初期化できません : %1";
	static public final String CannotFindDisplayMode = "適合する画面モードが見つかりません : %1";
	static public final String CannotSwitchToFullScreen = "フルスクリーンに切り替えられません : %1";
	static public final String InvalidPropertyInFullScreen = "フルスクリーン中では操作できないプロパティを設定しようとしました";
	static public final String InvalidMethodInFullScreen = "フルスクリーン中では操作できないメソッドを呼び出そうとしました";
	static public final String CannotLoadCursor = "マウスカーソル %1 の読み込みに失敗しました";
	static public final String CannotLoadKrMovieDLL = "ビデオ/Shockwave Flash を再生するためには krmovie.dll / krflash.dll が必要ですが 読み込むことができません";
	static public final String InvalidKrMovieDLL = "krmovie.dll/krflash.dll が異常か 対応できないバージョンです";
	static public final String ErrorInKrMovieDLL = "krmovie.dll/krflash.dll 内でエラーが発生しました/%1";
	static public final String WindowAlreadyMissing = "ウィンドウはすでに存在していません";
	static public final String PrerenderedFontMappingFailed = "レンダリング済みフォントのマッピングに失敗しました : %1";
	static public final String ConfigFailOriginalFileCannotBeRewritten = "%1 に書き込みできません。ソフトウェアが実行中のままになっていないか、あるいは書き込み権限があるかどうかを確認してください";
	static public final String ConfigFailTempExeNotErased = "%1 の終了を確認できないため、これを削除できませんでした(このファイルは削除して結構です)";
	static public final String ExecutionFail = "%1 を実行できません";
	static public final String PluginUnboundFunctionError = "プラグインから関数 %1 を要求されましたが、その関数は本体内に存在しません。プラグインと本体のバージョンが正しく対応しているか確認してください";
	static public final String ExceptionHadBeenOccured = " = (例外発生)";
	static public final String ConsoleResult = "コンソール : ";


	static public final String VersionInformation = "羽々斬(吉里吉里2互換エンジン) 実行コア/%1 TJS2J/%2 Copyright (C) 2011 T.Imoto and contributors All rights reserved.";
	static public final String VersionInformation2 = "羽々斬は吉里吉里2のソースコードを元に開発されています。";
	static public final String DownloadPageURL = "http://kirikiri.jp/";
	static public final String InternalError = "内部エラーが発生しました: at %1 line %2";
	static public final String InvalidParam = "不正なパラメータです";
	static public final String WarnDebugOptionEnabled = "-debug オプションが指定されているため、現在 吉里吉里はデバッグモードで動作しています。デバッグモードでは十分な実行速度が出ない場合があるので注意してください";
	static public final String CommandLineParamIgnoredAndDefaultUsed = 		"コマンドラインパラメータ %1 に指定された値 %2 は無効のためデフォルトの設定を用います";
	static public final String InvalidCommandLineParam = "コマンドラインパラメータ %1 に指定された値 %2 は無効です";
	static public final String NotImplemented = "未実装の機能を呼び出そうとしました";
	static public final String CannotOpenStorage = "ストレージ %1 を開くことができません";
	static public final String CannotFindStorage = "ストレージ %1 が見つかりません";
	static public final String CannotOpenStorageForWrite = "ストレージ %1 を書き込み用に開くことができません。ファイルが書き込み禁止になっていないか、あるいはファイルに書き込み権限があるかどうか、あるいはそもそもそれが書き込み可能なメディアやファイルなのかを確認してください";
	static public final String StorageInArchiveNotFound = "ストレージ %1 がアーカイブ %2 の中に見つかりません";
	static public final String InvalidPathName = "パス名 %1 は無効な形式です。形式が正しいかどうかを確認してください";
	static public final String UnsupportedMediaName = "\"%1\" は対応していないメディアタイプです";
	static public final String CannotUnbindXP3EXE = "%1 は実行可能ファイルに見えますが、これに結合されたアーカイブを発見できませんでした";
	static public final String CannotFindXP3Mark = "%1 は XP3 アーカイブではないか、対応できない形式です。アーカイブファイルを指定すべき場面で通常のファイルを指定した場合、あるいは対応できないアーカイブファイルを指定した場合などにこのエラーが発生しますので、確認してください";
	static public final String MissingPathDelimiterAtLast = "パス名の最後には '>' または '/' を指定してください (吉里吉里２ 2.19 beta 14 よりアーカイブの区切り記号が '#' から '>' に変わりました)";
	static public final String FilenameContainsSharpWarn = "(注意) '#' がファイル名 \"%1\" に含まれています。アーカイブの区切り文字は吉里吉里２ 2.19 beta 14 より'#' から '>' に変わりました。もしアーカイブの区切り文字のつもりで '#' を使用した場合は、お手数ですが '>' に変えてください";
	static public final String CannotGetLocalName = "ストレージ名 %1 をローカルファイル名に変換できません。アーカイブファイル内のファイルや、ローカルファイルでないファイルはローカルファイル名に変換できません。";
	static public final String ReadError = "読み込みエラーです。ファイルが破損している可能性や、デバイスからの読み込みに失敗した可能性があります";
	static public final String WriteError = "書き込みエラーです";
	static public final String SeekError = "シークに失敗しました。ファイルが破損している可能性や、デバイスからの読み込みに失敗した可能性があります";
	static public final String TruncateError = "ファイルの長さを切り詰めるのに失敗しました";
	static public final String InsufficientMemory = "メモリ確保に失敗しました。";
	static public final String UncompressionFailed = "ファイルの展開に失敗しました。未対応の圧縮形式が指定されたか、あるいはファイルが破損している可能性があります";
	static public final String CompressionFailed = "ファイルの圧縮に失敗しました";
	static public final String CannotWriteToArchive = "アーカイブにデータを書き込むことはできません";
	static public final String UnsupportedCipherMode = "%1 は未対応の暗号化形式か、データが破損しています";
	static public final String UnsupportedModeString = "認識できないモード文字列の指定です(%1)";
	static public final String UnknownGraphicFormat = "%1 は未知の画像形式です";
	static public final String CannotSuggestGraphicExtension = "%1 について適切な拡張子を持ったファイルを見つけられませんでした";
	static public final String MaskSizeMismatch = "マスク画像のサイズがメイン画像のサイズと違います";
	static public final String ProvinceSizeMismatch = "領域画像 %1 はメイン画像とサイズが違います";
	static public final String ImageLoadError = "画像読み込み中にエラーが発生しました/%1";
	static public final String JPEGLoadError = "JPEG 読み込み中にエラーが発生しました/%1";
	static public final String PNGLoadError = "PNG 読み込み中にエラーが発生しました/%1";
	static public final String ERILoadError = "ERI 読み込み中にエラーが発生しました/%1";
	static public final String TLGLoadError = "TLG 読み込み中にエラーが発生しました/%1";
	static public final String InvalidImageSaveType = "無効な保存画像形式です(%1)";
	static public final String InvalidOperationFor8BPP = "8bpp 画像に対しては行えない操作を行おうとしました";
	static public final String SpecifyWindow = "Window クラスのオブジェクトを指定してください";
	static public final String SpecifyLayer = "Layer クラスのオブジェクトを指定してください";
	static public final String CannotCreateEmptyLayerImage = "画像サイズの横幅あるいは縦幅を 0 以下の数に設定することはできません";
	static public final String CannotSetPrimaryInvisible = "プライマリレイヤは不可視にできません";
	static public final String CannotMovePrimary = "プライマリレイヤは移動できません";
	static public final String CannotSetParentSelf = "自分自身を親とすることはできません";
	static public final String CannotMoveNextToSelfOrNotSiblings = "自分自身の前後や親の異なるレイヤの前後に移動することはできません";
	static public final String CannotMovePrimaryOrSiblingless = "プライマリレイヤや兄弟の無いレイヤは前後に移動することはできません";
	static public final String CannotMoveToUnderOtherPrimaryLayer = "別のプライマリレイヤ下にレイヤを移動することはできません";
	static public final String InvalidImagePosition = "レイヤ領域に画像の無い領域が発生しました";
	static public final String CannotSetModeToDisabledOrModal = "すでにモーダルなレイヤの親レイヤ、あるいは不可視/無効なレイヤをモーダルにすることはできません";
	static public final String NotDrawableLayerType = "この type のレイヤでは描画や画像読み込みや画像サイズ/位置の変更/取得はできません";
	static public final String SourceLayerHasNoImage = "転送元レイヤは画像を持っていません";
	static public final String UnsupportedLayerType = "%1 はこの type のレイヤでは使用できません";
	static public final String NotDrawableFaceType = "%1 ではこの face に描画できません";
	static public final String CannotConvertLayerTypeUsingGivenDirection = "指定されたレイヤタイプ変換はできません";
	static public final String NegativeOpacityNotSupportedOnThisFace = "負の不透明度はこの face では指定できません";
	static public final String SrcRectOutOfBitmap = "転送元がビットマップ外の領域を含んでいます。正しい範囲に収まるように転送元を指定してください";
	static public final String BoxBlurAreaMustContainCenterPixel = "矩形ブラーの範囲は必ず(0,0)をその中に含む必要があります。leftとrightが両方とも正の数値、あるいは両方とも負の数値という指定はできません(topとbottomに対しても同様)";
	//static public final String BoxBlurAreaMustBeSmallerThan16Million = "矩形ブラーの範囲が大きすぎます。矩形ブラーの範囲は1677万以下である必要があります";
	static public final String BoxBlurAreaMustBeSmallerThan16Million = "矩形ブラーの範囲が大きすぎます。矩形ブラーの範囲は838万以下である必要があります";
	static public final String CannotChangeFocusInProcessingFocus = "フォーカス変更処理中はフォーカスを新たに変更することはできません";
	static public final String WindowHasNoLayer = "ウィンドウにレイヤがありません";
	static public final String WindowHasAlreadyPrimaryLayer = "ウィンドウにはすでにプライマリレイヤがあります";
	static public final String SpecifiedEventNeedsParameter = "イベント %1 にはパラメータが必要です";
	static public final String SpecifiedEventNeedsParameter2 = "イベント %1 にはパラメータ %2 が必要です";
	static public final String SpecifiedEventNameIsUnknown = "イベント名 %1 は未知のイベント名です";
	static public final String OutOfRectangle = "矩形外を指定されました";
	static public final String InvalidMethodInUpdating = "画面更新中はこの機能を実行できません";
	static public final String CannotCreateInstance = "このクラスはインスタンスを作成できません";
	static public final String UnknownWaveFormat = "%1 は対応できない Wave 形式です";
	static public final String SpecifyMenuItem = "MenuItem クラスのオブジェクトを指定してください";
	static public final String CurrentTransitionMustBeStopping = "現在のトランジションを停止させてから新しいトランジションを開始してください。同じレイヤに対して複数のトランジションを同時に実行しようとするとこのエラーが発生します";
	static public final String TransHandlerError = "トランジションハンドラでエラーが発生しました : %1";
	static public final String TransAlreadyRegistered = "トランジション %1 は既に登録されています";
	static public final String CannotFindTransHander = "トランジションハンドラ %1 が見つかりません";
	static public final String SpecifyTransitionSource = "トランジション元を指定してください";
	static public final String LayerCannotHaveImage = "このレイヤは画像を持つことはできません";
	static public final String TransitionSourceAndDestinationMustHaveImage = "トランジション元とトランジション先はともに画像を持っている必要があります";
	static public final String CannotLoadRuleGraphic = "ルール画像 %1 を読み込むことができません";
	static public final String SpecifyOption = "オプション %1 を指定してください";
	static public final String TransitionLayerSizeMismatch = "トランジション元(%1)とトランジション先(%2)のレイヤのサイズが一致しません";
	static public final String TransitionMutualSource = "トランジション元のトランジション元が自分自身です";
	static public final String HoldDestinationAlphaParameterIsNowDeprecated = "警告 : メソッド %1 の %2 番目に渡された hda パラメータは、吉里吉里２ 2.23 beta 2 より無視されるようになりました。代わりに Layer.holdAlpha プロパティを用いてください。";
	static public final String CannotConnectMultipleWaveSoundBufferAtOnce = "複数の WaveSoundBuffer を一つのフィルタで同時に使用することはできません";
	static public final String InvalidWindowSizeMustBeIn64to32768 = "window は 64～32768 の範囲の 2 の累乗で無ければなりません";
	static public final String InvalidOverlapCountMustBeIn2to32 = "overlap は 2～32 の範囲の 2 の累乗で無ければなりません";
	static public final String KAGNoLine = "読み込もうとしたシナリオファイル %1 は空です";
	static public final String KAGCannotOmmitFirstLabelName = "シナリオファイルの最初のラベル名は省略できません";
	static public final String KAGLabelNotFound = "シナリオファイル %1 内にラベル %2 が見つかりません";
	static public final String KAGInlineScriptNotEnd = "[endscript] または @endscript が見つかりません";
	static public final String KAGSyntaxError = "タグの文法エラーです。'[' や ']' の対応、\" と \" の対応、スペースの入れ忘れ、余分な改行、macro ～ endmacro の対応、必要な属性の不足などを確認してください";
	static public final String KAGMacroEntityNotAvailable = "マクロエンティティはマクロ外では使用できません";
	static public final String KAGCallStackUnderflow = "return タグが call タグと対応していません ( return タグが多い )";
	static public final String KAGReturnLostSync = "シナリオファイルに変更があったため return の戻り先位置を特定できません";
	static public final String KAGSpecifyKAGParser = "KAGParser クラスのオブジェクトを指定してください";
	static public final String KAGMalformedSaveData = "栞データが異常です。データが破損している可能性があります";
	static public final String LabelOrScriptInMacro = "ラベルや iscript はマクロ中に記述できません";
	static public final String UnknownMacroName = "マクロ \"%1\" は登録されていません";

	static public final String CannotLoadSound = "サウンドファイル %1 を読み込むことができません";

	//static public final String XP3Protected = "Specified storage had been protected!";
	static public final String XP3Protected = "プロテクトされたアーカイブです";

	static public final void throwExceptionMessage( final String msg ) throws TJSException {
		throw new TJSException(msg);
	}
	static public final void throwExceptionMessage( final String msg, final String p1, int num ) throws TJSException {
		String tmp = msg.replace( "%1", p1 );
		tmp = tmp.replace( "%2", String.valueOf(num) );
		throw new TJSException( tmp );
	}
	static public final void throwExceptionMessage( final String msg, final String p1 ) throws TJSException {
		String tmp = msg.replace( "%1", p1 );
		throw new TJSException( tmp );
	}
	static public final void throwExceptionMessage( final String msg, final String p1, final String p2 ) throws TJSException {
		String tmp = msg.replace( "%1", p1 );
		tmp = tmp.replace( "%2", p2 );
		throw new TJSException( tmp );
	}
	static public final String formatMessage( final String msg, final String p1, final String p2) {
		String tmp = msg.replace( "%1", p1 );
		return tmp.replace( "%2", p2 );
	}
};
