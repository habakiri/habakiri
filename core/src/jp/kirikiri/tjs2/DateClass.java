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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateClass extends NativeClass {

	public static int mClassID;
	static private final String CLASS_NAME = "Date";

	protected NativeInstance createNativeInstance() {
		return new DateNI();
	}
	/*
	public static DateNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (DateNI) holder.mValue;
	}
	*/
	public DateClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		//Class<? extends DateClass> c = getClass();
		//registerMethods( c, CLASS_NAME );
		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				DateNI _this = (DateNI) objthis.getNativeInstance( mClassID );
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct( param, objthis );
				if( hr < 0 ) return hr;

				if( param.length == 0 ) {
					_this.mDateTime = Calendar.getInstance();
				} else if( param.length >= 1 ) {
					if( param[0].isString() ) {
						// formatted string -> date/time
						_this.mDateTime = parseDateString( param[0].getString() );
					} else {
						int y, mon=0, day=1, h=0, m=0, s=0;
						y = param[0].asInteger();
						if( param.length > 1 && param[1].isVoid() != true ) mon = param[1].asInteger();
						if( param.length > 2 && param[2].isVoid() != true ) day = param[2].asInteger();
						if( param.length > 3 && param[3].isVoid() != true ) h = param[3].asInteger();
						if( param.length > 4 && param[4].isVoid() != true ) m = param[4].asInteger();
						if( param.length > 5 && param[5].isVoid() != true ) s = param[5].asInteger();

						Calendar cal = Calendar.getInstance();
						cal.set( y, mon, day, h, m, s );
						_this.mDateTime = cal;
						// if( _this.mDateTime == -1) throw new TJSException(Error.InvalidValueForTimestamp);
					}
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setYear", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.YEAR, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMonth", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.MONTH, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setDate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.DAY_OF_MONTH, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setHours", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.HOUR_OF_DAY, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMinutes", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.MINUTE, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSeconds", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.mDateTime.set(Calendar.SECOND, param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setTime", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				//int num = param[0].asInteger();
				//long y = num < 0 ? (long)num + 0x100000000L : num;
				//_this.mDateTime.setTimeInMillis( y );
				double num = param[0].asDouble();
				_this.mDateTime.setTimeInMillis( (long)num );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getDate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.DAY_OF_MONTH) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getDay", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.DAY_OF_WEEK) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getHours", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.HOUR_OF_DAY) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getMinutes", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.MINUTE) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getMonth", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.MONTH) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getSeconds", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( _this.mDateTime.get(Calendar.SECOND) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTime", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					long num = _this.mDateTime.getTimeInMillis();
					//int y = (int) (num > Integer.MAX_VALUE ? num - 0x100000000L : num);
					//result.set( y );
					result.set( num );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTimezoneOffset", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) {
					result.set( TimeZone.getDefault().getRawOffset()/(60*1000) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getYear", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) {
					result.set( (int)_this.mDateTime.get(Calendar.YEAR) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "parse", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				DateNI _this = (DateNI)objthis.getNativeInstance(mClassID);	// インスタンス所得
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.mDateTime = parseDateString( param[0].getString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );
	}


	public static Calendar parseDateString( final String str ) throws TJSException {
		DateFormat format = DateFormat.getInstance();
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(format.parse(str));
			return cal;
		} catch (ParseException e) {
			// TODO "年/月/日 時:分:秒" には対応していないかも、以下でいいか確認する
			Pattern regex = Pattern.compile("([0-9]+)\\/([0-9]+)\\/([0-9]+)[ \t]+([0-9]+):([0-9]+):([0-9]+)");
			Matcher m = regex.matcher(str);
			if( m.groupCount() > 6 ) {
				int y = Integer.valueOf(m.group(1));
				int mon = Integer.valueOf(m.group(2));
				int day = Integer.valueOf(m.group(3));
				int h = Integer.valueOf(m.group(4));
				int min = Integer.valueOf(m.group(5));
				int s = Integer.valueOf(m.group(6));
				Calendar cal = Calendar.getInstance();
				cal.set( y, mon, day, h, min, s );
				return cal;
			} else {
				throw new TJSException(Error.CannotParseDate);
			}
		}
	}
}
