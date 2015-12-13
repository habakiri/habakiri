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

import java.util.Random;

public class MathClass extends NativeClass {

	static public int mClassID;
	static private Random mRandomGenerator;
	static final private String CLASS_NAME = "Math";

	static public void initialize() {
		mRandomGenerator = null;
	}
	public static void finalizeApplication() {
		mRandomGenerator = null;
	}

	public MathClass() throws VariantException, TJSException {
		super(CLASS_NAME);

		if( mRandomGenerator == null ) {
			mRandomGenerator = new Random( System.currentTimeMillis() );
		}

		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "abs", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.abs( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "acos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.acos( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "asin", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.asin( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "atan", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.atan( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "atan2", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.atan2( param[0].asDouble(), param[1].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "ceil", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.ceil( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "exp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
			if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
			if( result != null ) result.set( Math.exp( param[0].asDouble() ) );
			return Error.S_OK;
		}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "floor", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.floor( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "log", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.log( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "pow", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.pow( param[0].asDouble(), param[1].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "max", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) {
					double r = Double.NEGATIVE_INFINITY;
					final int count = param.length;
					for( int i = 0; i < count; i++ ) {
						double v = param[i].asDouble();
						if( Double.isNaN(v) ) {
							result.set( Double.NaN );
							return Error.S_OK;
						} else if( Double.compare( v, r ) > 0 ) {
							r = v;
						}
					}
					result.set( r );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "min", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) {
					double r = Double.POSITIVE_INFINITY;
					final int count = param.length;
					for( int i = 0; i < count; i++ ) {
						double v = param[i].asDouble();
						if( Double.isNaN(v) ) {
							result.set( Double.NaN );
							return Error.S_OK;
						} else if( Double.compare( v, r ) < 0 ) {
							r = v;
						}
					}
					result.set( r );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "random", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) result.set( mRandomGenerator.nextDouble() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "round", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.round( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "sin", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.sin( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "cos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.cos( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "sqrt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.sqrt( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "tan", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.tan( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 2.7182818284590452354 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LOG2E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 1.4426950408889634074 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LOG10E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.4342944819032518276 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LN10", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 2.30258509299404568402 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LN2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.69314718055994530942 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "PI", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 3.14159265358979323846 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "SQRT1_2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.70710678118654752440 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "SQRT2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 1.41421356237309504880 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );
	}
}
