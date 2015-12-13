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

public class Utils {

	public static String VariantToReadableString( final Variant val ) throws VariantException {
		return VariantToReadableString( val, 512 );
	}
	public static String VariantToReadableString( final Variant val, int maxlen ) throws VariantException {
		String ret = null;
		if( val == null || val.isVoid() ) {
			ret = new String( "(void)" );
		} else if( val.isInteger() ) {
			ret = new String( "(int)" + val.asString() );
		} else if( val.isReal() ) {
			ret = new String( "(real)" + val.asString() );
		} else if( val.isString() ) {
			ret = new String( "(string)\"" + LexBase.escapeC( val.asString() ) + "\"" );
		} else if( val.isOctet() ) {
			ret = new String( "(octet)<% " + Variant.OctetToListString( val.asOctet() ) + " %>" );
		} else if( val.isObject() ) {
			VariantClosure c = (VariantClosure)val.asObjectClosure();
			StringBuilder str = new StringBuilder(128);
			str.append( "(object)" );
			str.append( '(' );
			if( c.mObject != null ) {
				str.append( '[' );
				if( c.mObject instanceof NativeClass ) {
					str.append( ((NativeClass)c.mObject).getClassName() );
				} else if( c.mObject instanceof InterCodeObject ) {
					str.append( ((InterCodeObject)c.mObject).getName() );
				} else if( c.mObject instanceof CustomObject ) {
					String name = ((CustomObject)c.mObject).getClassNames();
					if( name != null ) str.append( name );
					else str.append( c.mObject.getClass().getName() );
				} else {
					str.append( c.mObject.getClass().getName() );
				}
				str.append( ']' );
			} else {
				str.append("0x00000000");
			}
			if( c.mObjThis != null ) {
				str.append( '[' );
				if( c.mObjThis instanceof NativeClass ) {
					str.append( ((NativeClass)c.mObjThis).getClassName() );
				} else if( c.mObjThis instanceof InterCodeObject ) {
					str.append( ((InterCodeObject)c.mObjThis).getName() );
				} else if( c.mObjThis instanceof CustomObject ) {
					String name = ((CustomObject)c.mObjThis).getClassNames();
					if( name != null ) str.append( name );
					else str.append( c.mObjThis.getClass().getName() );
				} else {
					str.append( c.mObjThis.getClass().getName() );
				}
				str.append( ']' );
			} else {
				str.append(":0x00000000");
			}
			str.append( ')' );
			ret = str.toString();
		} else { // native object ?
			ret = new String( "(octet) [" + val.getClass().getName() + "]" );
		}

		if( ret != null ) {
			if( ret.length() > maxlen ) {
				return ret.substring( 0, maxlen );
			} else {
				return ret;
			}
		}
		return new String("");
	}
	public static final String variantToExpressionString( final Variant val ) throws VariantException {
		// convert given variant to string which can be interpret as an expression.
		// this function does not convert objects ( returns empty string )
		if( val.isVoid() ) {
			return "void";
		} else if( val.isInteger() ) {
			return val.asString();
		} else if( val.isReal() ) {
			String s = Variant.realToHexString( val.asDouble() );
			return s + " /* " + val.asString() + " */";
		} else if( val.isString() ) {
			String s = LexBase.escapeC(val.asString());
			return "\"" + s + "\"";
		} else if( val.isOctet() ) {
			String s = Variant.octetToListString(val.asOctet());
			return "<%" + s + "%>";
		} else {
			return new String();
		}
	}
	public static String formatString(String format, Variant[] params) {
		final int count = params.length;
		Object[] args = new Object[count];
		for( int i = 0; i < count; i++ ) {
			args[i] = params[i].toJavaObject();
			if( args[i] instanceof String ) {
				final int length = ((String)args[i]).length();
				if( length == 1 ) {
					args[i] = Character.valueOf(((String)args[i]).charAt(0));
				}
			}
		}
		return String.format(format, args);
	}
}

