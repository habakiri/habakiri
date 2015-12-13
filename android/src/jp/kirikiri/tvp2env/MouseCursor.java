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
package jp.kirikiri.tvp2env;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.base.Storage;


/**
 * Android でマウスカーソル変更とか出来るのかな？
 * frameworks/base/core/res/res/drawable-xhdpi/pointer_arrow.png
 * frameworks/base/core/res/res/drawable-hdpi/pointer_arrow.png
 * frameworks/base/core/res/res/drawable-mdpi/pointer_arrow.png
 * をリフレクションで差し替えれば出来なくはないのかな？ いや、無理か
 */
public class MouseCursor {
	static public final int
	crDefault = 0x0,
	crNone = -1,
	crArrow = -2,
	crCross = -3,
	crIBeam = -4,
	crSize = -5,
	crSizeNESW = -6,
	crSizeNS = -7,
	crSizeNWSE = -8,
	crSizeWE = -9,
	crUpArrow = -10,
	crHourGlass = -11,
	crDrag = -12,
	crNoDrop = -13,
	crHSplit = -14,
	crVSplit = -15,
	crMultiDrag = -16,
	crSQLWait = -17,
	crNo = -18,
	crAppStart = -19,
	crHelp = -20,
	crHandPoint = -21,
	crSizeAll = -22,
	crHBeam = 1;

	private HashMap<String, Integer> mCursorTable;
	private int mCursorCount;
	//private ArrayList<Cursor> mCursors;

	//private HashMap<Integer, Cursor> mInternalCursor;
	public MouseCursor() {
		mCursorTable = new HashMap<String,Integer>();
		mCursorCount = 1;

		// 未定義のカーソル画像をどうするか……
		//Toolkit.getDefaultToolkit()
	}
	/*
	public Cursor getCursor( int v ) {
		return null;
	}
	*/
	public int getCursor( WindowForm win, final String name ) throws TJSException {
		// get placed path
		String place = Storage.searchPlacedPath(name);

		// search in cache
		Integer in_hash = mCursorTable.get(place);
		if(in_hash!=null) return in_hash.intValue();

		// not found
		return mCursorCount;
	}
}
