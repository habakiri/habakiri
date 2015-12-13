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
package jp.kirikiri.tjs2;


public class Error {
	public static final String InternalError = "内部エラーが発生しました";
	public static final String Warning = "警告: ";
	public static final String WarnEvalOperator = "グローバルでない場所で後置 ! 演算子が使われています(この演算子の挙動はTJS2 version 2.4.1 で変わりましたのでご注意ください)";
	public static final String NarrowToWideConversionError = "ANSI 文字列を UNICODE 文字列に変換できません。現在のコードページで解釈できない文字が含まれてます。正しいデータが指定されているかを確認してください。データが破損している可能性もあります";
	public static final String VariantConvertError = "%1 から %2 へ型を変換できません";
	public static final String VariantConvertErrorToObject = "%1 から Object へ型を変換できません。Object 型が要求される文脈で Object 型以外の値が渡されるとこのエラーが発生します";
	public static final String IDExpected = "識別子を指定してください";
	public static final String SubstitutionInBooleanContext = "論理値が求められている場所で = 演算子が使用されています(== 演算子の間違いですか？代入した上でゼロと値を比較したい場合は、(A=B) != 0 の形式を使うことをお勧めします)";;
	public static final String CannotModifyLHS = "不正な代入か不正な式の操作です";
	public static final String InsufficientMem = "メモリが足りません";
	public static final String CannotGetResult = "この式からは値を得ることができません";
	public static final String NullAccess = "null オブジェクトにアクセスしようとしました";
	public static final String MemberNotFound = "メンバ \"%1\" が見つかりません";
	public static final String MemberNotFoundNoNameGiven = "メンバが見つかりません";
	public static final String NotImplemented = "呼び出そうとした機能は未実装です";
	public static final String InvalidParam = "不正な引数です";
	public static final String BadParamCount = "引数の数が不正です";
	public static final String InvalidType = "関数ではないかプロパティの種類が違います";
	public static final String SpecifyDicOrArray = "Dictionary または Array クラスのオブジェクトを指定してください";
	public static final String SpecifyArray = "Array クラスのオブジェクトを指定してください";
	public static final String StringDeallocError = "文字列メモリブロックを解放できません";
	public static final String StringAllocError = "文字列メモリブロックを確保できません";
	public static final String MisplacedBreakContinue = "\"break\" または \"continue\" はここに書くことはできません";
	public static final String MisplacedCase = "\"case\" はここに書くことはできません";
	public static final String MisplacedReturn = "\"return\" はここに書くことはできません";
	public static final String StringParseError = "文字列定数/正規表現/オクテット即値が終わらないままスクリプトの終端に達しました";
	public static final String NumberError = "数値として解釈できません";
	public static final String UnclosedComment = "コメントが終わらないままスクリプトの終端に達しました";
	public static final String InvalidChar = "不正な文字です : \'%1\'";
	public static final String Expected = "%1 がありません";
	public static final String SyntaxError = "文法エラーです(%1)";
	public static final String PPError = "条件コンパイル式にエラーがあります";
	public static final String CannotGetSuper = "スーパークラスが存在しないかスーパークラスを特定できません";
	public static final String InvalidOpecode = "不正な VM コードです";
	public static final String RangeError = "値が範囲外です";
	public static final String AccessDenyed = "読み込み専用あるいは書き込み専用プロパティに対して行えない操作をしようとしました";
	public static final String NativeClassCrash = "実行コンテキストが違います";
	public static final String InvalidObject = "オブジェクトはすでに無効化されています";
	public static final String CannotOmit = "\"...\" は関数外では使えません";
	public static final String CannotParseDate = "不正な日付文字列の形式です";
	public static final String InvalidValueForTimestamp = "不正な日付・時刻です";
	public static final String ExceptionNotFound = "\"Exception\" が存在しないため例外オブジェクトを作成できません";
	public static final String InvalidFormatString = "不正な書式文字列です";
	public static final String DivideByZero = "0 で除算をしようとしました";
	public static final String NotReconstructiveRandomizeData = "乱数系列を初期化できません(おそらく不正なデータが渡されました)";
	public static final String Symbol = "識別子";
	public static final String CallHistoryIsFromOutOfTJS2Script = "[TJSスクリプト管理外]";
	public static final String NObjectsWasNotFreed = "合計 %1 個のオブジェクトが解放されていません";
	public static final String ObjectCreationHistoryDelimiter = "\n                     ";
	public static final String ObjectWasNotFreed = "オブジェクト %1 [%2] が解放されていません。オブジェクト作成時の呼び出し履歴は以下の通りです:\n                     %3";
	public static final String GroupByObjectTypeAndHistory = "オブジェクトのタイプとオブジェクト作成時の履歴による分類";
	public static final String GroupByObjectType = "オブジェクトのタイプによる分類";
	public static final String ObjectCountingMessageGroupByObjectTypeAndHistory = "%1 個 : [%2]\n                     %3";
	public static final String ObjectCountingMessageTJSGroupByObjectType = "%1 個 : [%2]";
	public static final String WarnRunningCodeOnDeletingObject = "%4: 削除中のオブジェクト %1[%2] 上でコードが実行されています。このオブジェクトの作成時の呼び出し履歴は以下の通りです:\n                     %3";
	public static final String WriteError = "書き込みエラーが発生しました";
	public static final String ReadError = "読み込みエラーが発生しました。ファイルが破損している可能性や、デバイスからの読み込みに失敗した可能性があります";
	public static final String SeekError = "シークエラーが発生しました。ファイルが破損している可能性や、デバイスからの読み込みに失敗した可能性があります";

	public static final String TooManyErrors = "Too many errors";
	public static final String ConstDicDelimiterError = "定数辞書(const Dictionary)で要素名と値の区切りが不正です";
	public static final String ConstDicValueError = "定数辞書(const Dictionary)の要素値が不正です";
	public static final String ConstArrayValueError = "定数配列(const Array)の要素値が不正です";
	public static final String ConstDicArrayStringError = "定数辞書もしくは配列で(const)文字が不正です";
	public static final String ConstDicLBRACKETError = "定数辞書(const Dictionary)で(const)%の後に\"[\"がありません";
	public static final String ConstArrayLBRACKETError = "定数配列(const Array)で(const)の後に\"[\"がありません";
	public static final String DicDelimiterError = "辞書(Dictionary)で要素名と値の区切りが不正です";
	public static final String DicError = "辞書(Dictionary)が不正です";
	public static final String DicLBRACKETError = "辞書(Dictionary)で%の後に\"[\"がありません";
	public static final String DicRBRACKETError = "辞書(Dictionary)の終端に\"]\"がありません";
	public static final String ArrayRBRACKETError = "配列(Array)の終端に\"]\"がありません";
	public static final String NotFoundRegexError = "正規表現が要求される文脈で正規表現がありません";
	public static final String NotFoundSymbolAfterDotError = "\".\"の後にシンボルがありません";
	public static final String NotFoundDicOrArrayRBRACKETError = "配列もしくは辞書要素を指す変数の終端に\"]\"がありません";
	public static final String NotFoundRPARENTHESISError = "\")\"が要求される文脈で\")\"がありません";
	public static final String NotFoundSemicolonAfterThrowError = "throwの後の\";\"がありません";
	public static final String NotFoundRPARENTHESISAfterCatchError= "catchの後の\")\"がありません";
	public static final String NotFoundCaseOrDefaultError = "caseかdefaultが要求される文脈でcaseかdefaultがありません";
	public static final String NotFoundWithLPARENTHESISError = "withの後に\"(\"がありません";
	public static final String NotFoundWithRPARENTHESISError = "withの後に\")\"がありません";
	public static final String NotFoundSwitchLPARENTHESISError = "switchの後に\"(\"がありません";
	public static final String NotFoundSwitchRPARENTHESISError = "switchの後に\")\"がありません";
	public static final String NotFoundSemicolonAfterReturnError = "returnの後の\";\"がありません";
	public static final String NotFoundPropGetRPARENTHESISError = "property getterの後に\")\"がありません";
	public static final String NotFoundPropSetLPARENTHESISError = "property setterの後に\"(\"がありません";
	public static final String NotFoundPropSetRPARENTHESISError = "property setterの後に\")\"がありません";
	public static final String NotFoundPropError = "propertyの後に\"getter\"もしくは\"setter\"がありません";
	public static final String NotFoundSymbolAfterPropError = "propertyの後にシンボルがありません";
	public static final String NotFoundLBRACEAfterPropError = "propertyの後に\"{\"がありません";
	public static final String NotFoundRBRACEAfterPropError = "propertyの後に\"}\"がありません";
	public static final String NotFoundFuncDeclRPARENTHESISError = "関数定義の後に\")\"がありません";
	public static final String NotFoundFuncDeclSymbolError = "関数定義にシンボル名がありません";
	public static final String NotFoundSymbolAfterVarError = "変数宣言にシンボルがありません";
	public static final String NotFoundForLPARENTHESISError = "forの後に\"(\"がありません";
	public static final String NotFoundForRPARENTHESISError = "forの後に\")\"がありません";
	public static final String NotFoundForSemicolonError = "forの各節の区切りに\";\"がありません";
	public static final String NotFoundIfLPARENTHESISError = "ifの後に\"(\"がありません";
	public static final String NotFoundIfRPARENTHESISError = "ifの後に\")\"がありません";
	public static final String NotFoundDoWhileLPARENTHESISError = "do-whileの後に\"(\"がありません";
	public static final String NotFoundDoWhileRPARENTHESISError = "do-whileの後に\")\"がありません";
	public static final String NotFoundDoWhileError = "do-while文でwhileがありません";
	public static final String NotFoundDoWhileSemicolonError = "do-while文でwhileの後に\";\"がありません";
	public static final String NotFoundWhileLPARENTHESISError = "whileの後に\"(\"がありません";
	public static final String NotFoundWhileRPARENTHESISError = "whileの後に\")\"がありません";
	public static final String NotFoundLBRACEAfterBlockError = "ブロックが要求される文脈で\"{\"がありません";
	public static final String NotFoundRBRACEAfterBlockError = "ブロックが要求される文脈で\"}\"がありません";
	public static final String NotFoundSemicolonError = "文の終わりに\";\"がありません";
	public static final String NotFoundSemicolonOrTokenTypeError = "文の終わりに\";\"がないか、予約語のタイプミスです";
	public static final String NotFoundBlockRBRACEError = "ブロックの終わりに\"}\"がありません";
	public static final String NotFoundCatchError = "tryの後にcatchがありません";
	public static final String NotFoundFuncCallLPARENTHESISError = "関数呼び出しの後に\"(\"がありません";
	public static final String NotFoundFuncCallRPARENTHESISError = "関数呼び出しの後に\")\"がありません";
	public static final String NotFoundVarSemicolonError = "変数宣言の後に\";\"がありません";
	public static final String NotFound3ColonError = "条件演算子の\":\"がありません";
	public static final String NotFoundCaseColonError = "caseの後に\":\"がありません";
	public static final String NotFoundDefaultColonError = "defaultの後に\":\"がありません";
	public static final String NotFoundSymbolAfterClassError = "classの後にシンボルがありません";
	public static final String NotFoundPropSetSymbolError = "property setterの引数がありません";
	public static final String NotFoundBreakSemicolonError = "breakの後に\";\"がありません";
	public static final String NotFoundContinueSemicolonError = "continueの後に\";\"がありません";
	public static final String NotFoundBebuggerSemicolonError = "debuggerの後に\";\"がありません";
	public static final String NotFoundAsteriskAfterError = "関数呼び出し、関数定義の配列展開(*)が不正か、乗算が不正です";
	public static final String EndOfBlockError = "ブロックの対応が取れていません。\"}\"が多いです";

	public static final String NotFoundPreprocessorRPARENTHESISError = "プリプロセッサに\")\"がありません";
	public static final String PreprocessorZeroDiv = "プリプロセッサのゼロ除算エラー";

	public static final String ByteCodeBroken = "バイトコードファイル読み込みエラー。ファイルが壊れているかバイトコードとは異なるファイルです";

	public static final int
		S_OK		= 0,
		S_TRUE		= 1,
		S_FALSE		= 2,
		E_FAIL		= -1;

	static public final int
		E_MEMBERNOTFOUND	= -1001,
		E_NOTIMPL			= -1002,
		E_INVALIDPARAM		= -1003,
		E_BADPARAMCOUNT		= -1004,
		E_INVALIDTYPE		= -1005,
		E_INVALIDOBJECT		= -1006,
		E_ACCESSDENYED		= -1007,
		E_NATIVECLASSCRASH	= -1008;

	private static final String EXCEPTION_NAME = "Exception";
	/**
	 * TJSGetExceptionObject : retrieves TJS 'Exception' object
	 * @throws TJSException
	 * @throws VariantException
	 */
	static public final void getExceptionObject( TJS tjs, Variant res, Variant msg, Variant trace/* trace is optional */) throws VariantException, TJSException {
		if( res == null ) return; // not prcess

		// retrieve class "Exception" from global
		Dispatch2 global = tjs.getGlobal();
		Variant val = new Variant();
		int hr = global.propGet( 0, EXCEPTION_NAME, val, global );
		if( hr < 0 ) throw new TJSException( ExceptionNotFound );
		// create an Exception object
		Holder<Dispatch2> excpobj = new Holder<Dispatch2>(null);
		VariantClosure clo = val.asObjectClosure();
		Variant[] pmsg = new Variant[1];
		pmsg[0] = msg;
		hr = clo.createNew(0, null, excpobj, pmsg, clo.mObjThis );
		if( hr < 0 ) throw new TJSException( ExceptionNotFound );
		Dispatch2 disp = excpobj.mValue;
		if( trace != null ) {
			final String trace_name = "trace";
			disp.propSet( Interface.MEMBERENSURE, trace_name, trace, disp );
		}
		res.set( disp, disp );
		excpobj = null;
	}
	static public void reportExceptionSource( final String msg, InterCodeObject context, int codepos ) {
		if( TJS.EnableDebugMode) {
			TJS.outputExceptionToConsole( msg + " at " + context.getPositionDescriptionString(codepos) );
		}
	}
	static public void throwFrom_tjs_error(int hr, final String name) throws TJSException {
		// raise an exception descripted as tjs_error
		// name = variable name ( otherwide it can be NULL )
		switch( hr ) {
		case Error.E_MEMBERNOTFOUND: {
			if( name != null ) {
				String str = Error.MemberNotFound.replace( "%1", name );
				throw new TJSException(str);
			} else {
				throw new TJSException(Error.MemberNotFoundNoNameGiven);
			}
		}
		case Error.E_NOTIMPL:
			throw new TJSException(Error.NotImplemented);
		case Error.E_INVALIDPARAM:
			throw new TJSException(Error.InvalidParam);
		case Error.E_BADPARAMCOUNT:
			throw new TJSException(Error.BadParamCount);
		case Error.E_INVALIDTYPE:
			throw new TJSException(Error.InvalidType);
		case Error.E_ACCESSDENYED:
			throw new TJSException(Error.AccessDenyed);
		case Error.E_INVALIDOBJECT:
			throw new TJSException(Error.InvalidObject);
		case Error.E_NATIVECLASSCRASH:
			throw new TJSException(Error.NativeClassCrash);
		default:
			if( hr < 0 ) {
				String buf = String.format( "Unknown failure : %08X", hr );
				throw new TJSException(buf);
			}
		}
	}
	/*
	public static void reportExceptionSource( final String msg, InterCodeObject context, int codepos) {
		if( TJS.EnableDebugMode) {
			TJS.outputExceptionToConsole( msg + " at " + context.getPositionDescriptionString(codepos) );
		}
	}
	*/
}
