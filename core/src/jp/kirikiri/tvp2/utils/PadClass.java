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
package jp.kirikiri.tvp2.utils;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeClassProperty;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;


public class PadClass extends NativeClass {

	static public int mClassID = -1;
	static private final String CLASS_NAME = "Pad";

	protected NativeInstance createNativeInstance() {
		return new PadNI();
	}
	/*
	private static PadNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (PadNI) holder.mValue;
	}
	*/

	public PadClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadScenario", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				return Error.E_NOTIMPL;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );



		registerNCM( "text", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getText() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setText( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fileName", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFileName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFileName( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "color", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getColor() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setColor( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "visible", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setVisible( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "title", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getTitle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setTitle( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontColor", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontColor() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontColor( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontSize", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontSize() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontSize( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontBold", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontBold() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontBold( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontItalic", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontItalic() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontItalic( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontUnderline", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontUnderline() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontUnderline( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontStrikeOut", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontStrikeOut() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontStrikeOut( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fontFace", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFontName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFontName( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "readOnly", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.isReadOnly() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setReadOnly( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "wordWrap", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getWordWrap() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setWordWrap( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "opacity", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getOpacity() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setOpacity( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "showStatusBar", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getStatusBarVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setStatusBarVisible( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "showScrollBars", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getScrollBarsVisible() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setScrollBarsVisible( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "statusText", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getStatusText() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setStatusText( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "borderStyle", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getBorderStyle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setBorderStyle( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "width", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFormWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFormWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFormHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFormHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "top", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFormTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFormTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "left", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFormLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				PadNI _this = (PadNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFormLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

}
