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
/*
 * TODO 未テスト
 */
package jp.kirikiri.tjs2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class NativeJavaClass extends NativeClass {

	private Class<?> mJavaClass;
	private int mClassID;

	public NativeJavaClass( String name, Class<?> c ) throws VariantException, TJSException {
		super(name);
		mJavaClass = c;

		String classname = name;
		mClassID = TJS.registerNativeClass(classname);
		try {
			HashSet<String> registProp = new HashSet<String>(); // set/getで重複しないようにチェック

			Method[] methods = c.getMethods();
			for( Method m : methods ) {
				final String methodName = m.getName();
				final int modif = m.getModifiers();
				int flag = 0;
				if( Modifier.isStatic( modif ) ) flag |= Interface.STATICMEMBER;

				if( "constructor" .equals( methodName ) ) {
					// コンストラクタ
					registerNCM( classname, new NativeJavaClassConstructor(m, mClassID), classname, Interface.nitMethod, flag );
				} else if( methodName.startsWith("prop_") ) {
					// プロパティ prop_ で始まるものはプロパティとみなす
					@SuppressWarnings("rawtypes")
					Class[] params = m.getParameterTypes();
					Method setMethod = null;
					Method getMethod = null;
					String propName = null;
					if( methodName.startsWith("prop_set_") ) {
						if( params.length == 1 ) {
							setMethod = m;
							propName = methodName.substring( "prop_set_".length() );
							if( registProp.contains(propName) == false ) {
								final String getMethodName = "prop_get_" + propName;
								for( Method getm : methods ) {
									if( getm.getName().equals( getMethodName ) ) {
										@SuppressWarnings("rawtypes")
										Class[] p = getm.getParameterTypes();
										if( p.length == 0 && getm.getReturnType().equals(void.class) != true ) {
											getMethod = getm;
											break;
										}
									}
								}
							}
						}
					} else if( methodName.startsWith("prop_get_") ) {
						if( params.length == 0 && m.getReturnType().equals(void.class) != true ) {
							getMethod = m;
							propName = methodName.substring( "prop_get_".length() );
							if( registProp.contains(propName) == false ) {
								final String setMethodName = "prop_set_" + propName;
								for( Method setm : methods ) {
									if( setm.getName().equals( setMethodName ) ) {
										@SuppressWarnings("rawtypes")
										Class[] p = setm.getParameterTypes();
										if( p.length == 1 ) {
											setMethod = setm;
											break;
										}
									}
								}
							}
						}
					}
					if( propName != null && registProp.contains(propName) == false ) {
						if( setMethod != null || getMethod != null ) {
							registerNCM( propName, new NativeJavaClassProperty(getMethod, setMethod, mClassID), classname, Interface.nitProperty, flag );
							registProp.add(propName);
						}
					}
				} else {
					// 通常メソッド
					registerNCM( methodName, new NativeJavaClassMethod(m, mClassID), classname, Interface.nitMethod, flag );
				}
			}
			registProp = null;
		} catch (SecurityException e) {
			throw new TJSException(Error.InternalError + e.toString());
		}
	}
	/**
	 * 引数があるコンストラクタには未対応
	 * TODO エラー時エラー表示するようにした方がいいかも
	 */
	protected NativeInstance createNativeInstance() {
		Object obj;
		try {
			obj = mJavaClass.newInstance();
		} catch (InstantiationException e) {
			TJS.outputExceptionToConsole( e.toString() );
			return null;
		} catch (IllegalAccessException e) {
			TJS.outputExceptionToConsole( e.toString() );
			return null;
		}
		if( obj != null ) {
			return new NativeJavaInstance(obj);
		}
		return null;
	}
	static public Object variantToJavaObject( Variant param, Class<?> type ) throws VariantException {
		if( type.isPrimitive() ) { // プリミティブタイプの場合
			if( type.equals( Integer.TYPE ) ) {
				return Integer.valueOf( param.asInteger() );
			} else if( type.equals( Double.TYPE ) ) {
				return Double.valueOf( param.asDouble() );
			} else if( type.equals( Boolean.TYPE ) ) {
				return Boolean.valueOf( param.asInteger() != 0 ? true : false );
			} else if( type.equals( Float.TYPE ) ) {
				return Float.valueOf( (float)param.asDouble() );
			} else if( type.equals( Long.TYPE ) ) {
				return Long.valueOf( param.asInteger() );
			} else if( type.equals( Character.TYPE ) ) {
				return Character.valueOf( (char)param.asInteger() );
			} else if( type.equals( Byte.TYPE ) ) {
				return Byte.valueOf( (byte)param.asInteger() );
			} else if( type.equals( Short.TYPE ) ) {
				return Short.valueOf( (short)param.asInteger() );
			} else { // may be Void.TYPE
				return null;
			}
		} else if( type.equals( String.class ) ) {
			return param.asString();
		} else if( type.equals( ByteBuffer.class ) ) {
			return param.asOctet();
		} else if( type.equals( Variant.class ) ) {
			return param;
		} else if( type.equals( VariantClosure.class ) ) {
			return param.asObjectClosure();
		} else if( type.equals( Dispatch2.class ) ) {
			return param.asObject();
		} else if( type.equals( param.toJavaObject().getClass() )) {
			return param.toJavaObject();
		} else {
			// その他 のクラス
			return null;
		}
	}
	static public void javaObjectToVariant( Variant result, Class<?> type, Object src ) {
		if( result == null ) return;
		if( type.isPrimitive() ) { // プリミティブタイプの場合
			if( type.equals( Integer.TYPE ) ) {
				result.set( ((Integer)src).intValue() );
			} else if( type.equals( Double.TYPE ) ) {
				result.set( ((Double)src).doubleValue() );
			} else if( type.equals( Boolean.TYPE ) ) {
				result.set( ((Boolean)src).booleanValue() ? 1 : 0 );
			} else if( type.equals( Float.TYPE ) ) {
				result.set( ((Float)src).doubleValue() );
			} else if( type.equals( Long.TYPE ) ) {
				result.set( ((Long)src).intValue() );
			} else if( type.equals( Character.TYPE ) ) {
				result.set( (int)((Character)src).charValue() );
			} else if( type.equals( Byte.TYPE ) ) {
				result.set( ((Byte)src).intValue() );
			} else if( type.equals( Short.TYPE ) ) {
				result.set( ((Short)src).intValue() );
			} else { // may be Void.TYPE
				result.clear();
			}
		} else if( type.equals( String.class ) ) {
			result.set( (String)src );
		} else if( type.equals( ByteBuffer.class ) ) {
			result.set( (ByteBuffer)src );
		} else if( type.equals( Variant.class ) ) {
			result.set( (Variant)src );
		} else if( type.equals( VariantClosure.class ) ) {
			result.set( ((VariantClosure)src).mObject, ((VariantClosure)src).mObjThis );
		} else if( type.equals( Dispatch2.class ) ) {
			result.set( (Dispatch2)src );
		} else {
			// その他 のクラス, 直接入れてしまう
			result.setJavaObject( src );
		}
	}
	static public Object[] variantArrayToJavaObjectArray( Variant[] params, Class<?>[] types ) throws VariantException {
		if( types.length == 0 ) return null; // 元々引数不要
		if( params.length < types.length ) return null; // パラメータが少ない

		final int count = types.length;
		Object[] ret = new Object[count];
		for( int i = 0; i < count; i++ ) {
			Class<?> type = types[i];
			Variant param = params[i];
			if( type.isPrimitive() ) { // プリミティブタイプの場合
				if( type.equals( Integer.TYPE ) ) {
					ret[i] = Integer.valueOf( param.asInteger() );
				} else if( type.equals( Double.TYPE ) ) {
					ret[i] = Double.valueOf( param.asDouble() );
				} else if( type.equals( Boolean.TYPE ) ) {
					ret[i] = Boolean.valueOf( param.asInteger() != 0 ? true : false );
				} else if( type.equals( Float.TYPE ) ) {
					ret[i] = Float.valueOf( (float)param.asDouble() );
				} else if( type.equals( Long.TYPE ) ) {
					ret[i] = Long.valueOf( param.asInteger() );
				} else if( type.equals( Character.TYPE ) ) {
					ret[i] = Character.valueOf( (char)param.asInteger() );
				} else if( type.equals( Byte.TYPE ) ) {
					ret[i] = Byte.valueOf( (byte)param.asInteger() );
				} else if( type.equals( Short.TYPE ) ) {
					ret[i] = Short.valueOf( (short)param.asInteger() );
				} else { // may be Void.TYPE
					ret[i] = null;
				}
			} else if( type.equals( String.class ) ) {
				ret[i] = param.asString();
			} else if( type.equals( ByteBuffer.class ) ) {
				ret[i] = param.asOctet();
			} else if( type.equals( Variant.class ) ) {
				ret[i] = param;
			} else if( type.equals( VariantClosure.class ) ) {
				ret[i] = param.asObjectClosure();
			} else if( type.equals( Dispatch2.class ) ) {
				ret[i] = param.asObject();
			} else if( type.equals( param.toJavaObject().getClass() )) {
				ret[i] = param.toJavaObject();
			} else {
				// その他 のクラス
				ret[i] = null;
			}
		}
		return ret;
	}
}

