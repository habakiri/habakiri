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


public class RandomGeneratorClass extends NativeClass {

	public static int mClassID;
	static private final String CLASS_NAME = "RandomGenerator";

	protected NativeInstance createNativeInstance() {
		return new RandomGeneratorNI();
	}
	/*
	public static RandomGeneratorNI getNativeInstance( Dispatch2 objthis ) {
		return (RandomGeneratorNI) objthis.getNativeInstance( mClassID );
	}
	*/
	public RandomGeneratorClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance( mClassID );
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct( param, objthis );
				if( hr < 0 ) return hr;

				_this.randomize( param );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "randomize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.randomize( param );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "random", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// returns 53-bit precision random value x, x is in 0 <= x < 1
				if( result != null ) {
					result.set( _this.random() );
				} else {
					_this.random();
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "random32", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// returns 32-bit precision integer value x, x is in 0 <= x <= 4294967295
				if( result != null ) {
					result.set( _this.random32() );
				} else {
					_this.random32();
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "random63", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// returns 63-bit precision integer value x, x is in 0 <= x <= 9223372036854775807
				if( result != null ) {
					result.set( _this.random63() );
				} else {
					_this.random63();
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "random64", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// returns 64-bit precision integer value x, x is in
				// -9223372036854775808 <= x <= 9223372036854775807
				// Java 実装では、int は32 bitまで
				if( result != null ) {
					result.set( _this.random64() );
				} else {
					_this.random64();
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "serialize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RandomGeneratorNI _this = (RandomGeneratorNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// returns 64-bit precision integer value x, x is in
				// -9223372036854775808 <= x <= 9223372036854775807
				if( result != null ) {
					Dispatch2 dsp = _this.serialize();
					result.set( dsp, dsp );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );
	}
}
