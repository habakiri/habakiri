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
package jp.kirikiri.tvp2.visual;

import java.util.ArrayList;

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
import jp.kirikiri.tvp2.TVP;

public class FontClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "Font";

	protected NativeInstance createNativeInstance() {
		return new FontNI();
	}

	/*
	private static FontNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (FontNI) holder.mValue;
	}
	*/

	public FontClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTextWidth", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getTextWidth(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTextHeight", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getTextHeight(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscWidthX", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscWidthX(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscWidthY", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscWidthY(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscHeightX", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscHeightX(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscHeightY", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscHeightY(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "doUserSelect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 4) return Error.E_BADPARAMCOUNT;
				int flags = param[0].asInteger();
				String caption = param[1].asString();
				String prompt = param[2].asString();
				String samplestring = param[3].asString();
				int ret = _this.getLayer().doUserFontSelect( flags, caption, prompt, samplestring ) ? 1 : 0;
				if( result != null ) result.set( ret );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getList", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				int flags = param[0].asInteger();
				ArrayList<String> list = new ArrayList<String>();
				_this.getLayer().getFontList(flags,list);
				if( result != null ) {
					Dispatch2 dsp = TJS.createArrayObject();
					result.set( dsp, dsp );
					final int count = list.size();
					Variant tmp = new Variant();
					for( int i = 0; i < count; i++) {
						tmp.set( list.get(i) );
						dsp.propSetByNum( Interface.MEMBERENSURE, i, tmp, dsp );
					}
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "mapPrerenderedFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.getLayer().mapPrerenderedFont(param[0].asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "unmapPrerenderedFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.getLayer().unmapPrerenderedFont();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				String facename = null;
				if( param.length >= 2 && param[1].isVoid() != true ) facename = param[1].asString();
				_this.getLayer().loadFont(param[0].asString(),facename);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );



		registerNCM( "face", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontFace() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontFace(param.asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontHeight(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "bold", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontBold() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontBold(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "italic", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontItalic() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontItalic(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "strikeout", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontStrikeout() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontStrikeout(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "underline", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontUnderline() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontUnderline(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "angle", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontAngle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontAngle(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

}
