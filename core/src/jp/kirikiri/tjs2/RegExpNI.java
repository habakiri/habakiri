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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpNI extends NativeInstanceObject {

	public Pattern RegEx;
	public int mFlags;
	public int mStart;
	public Variant mArray;
	public int mIndex;
	public String mInput;
	public int mLastIndex;
	public String mLastMatch;
	public String mLastParen;
	public String mLeftContext;
	public String mRightContext;
	public Matcher mMatch;

	private static final int globalsearch = (1<<31);

	public static int regExpFlagToValue( char ch, int prev ) {
		// converts flag letter to internal flag value.
		// this returns modified prev.
		// when ch is '\0', returns default flag value and prev is ignored.

		if( ch == 0 ) {
			return 0;
		}

		switch(ch) {
		case 'g': // global search
			prev|=globalsearch; return prev;
		case 'i': // ignore case
			prev|=Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE; return prev;
		case 'l': // use localized collation
			//prev &= ~regbase::nocollate; return prev; 無視
			return prev;
		default:
			return prev;
		}
	}
	public static int getRegExpFlagsFromString( final String string ) {
		// returns a flag value represented by string
		int flag = regExpFlagToValue( (char)0, 0);
		final int count = string.length();
		int i = 0;
		while( i < count && string.charAt(i) != '/' ) {
			flag = regExpFlagToValue( string.charAt(i), flag);
			i++;
		}
		return flag;
	}
	public RegExpNI() {
		mFlags = regExpFlagToValue((char) 0, 0);
		//mStart = 0;
		//mIndex =0;
		//mLastIndex = 0;
		mArray = new Variant();
	}
	public void split( Holder<Dispatch2> array, String target, boolean purgeempty) throws VariantException, TJSException {
		if( array.mValue == null ) {
			array.mValue = TJS.createArrayObject();
		}
		if( RegEx != null ) {
			int limit = 0;
			if( purgeempty == false ) limit = -1;
			String strs[] = RegEx.split(target,limit);
			final int count = strs.length;
			for( int i = 0; i < count; i++ ) {
				Variant val = new Variant(strs[i]);
				array.mValue.propSetByNum( Interface.MEMBERENSURE, i, val, array.mValue );
			}
		}
	}
}
