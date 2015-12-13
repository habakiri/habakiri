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

public class Interface {

	/** メンバーが存在しない時、メンバーを生成する
	 * create a member if not exists */
	static final public int MEMBERENSURE		= 0x00000200;

	/** メンバーは存在しなければならない member *must* exist ( for Dictionary/Array ) */
	static final public int MEMBERMUSTEXIST 	= 0x00000400;

	/** プロパティの呼び出しを行わない ignore property invoking */
	static final public int IGNOREPROP			= 0x00000800;

	/** 非表示メンバー member is hidden */
	static final public int HIDDENMEMBER		= 0x00001000;

	/** スタティックメンバー、オブジェクト生成時にコピーされない
	 * member is not registered to the object (internal use) */
	static final public int STATICMEMBER		= 0x00010000;

	/** EnumMembers コール時にメンバの実体取得を行わない
	 * values are not retrieved (for EnumMembers) */
	static final public int ENUM_NO_VALUE		= 0x00100000;

	static final public int
		NIS_REGISTER		= 0x00000001,	// set native pointer
		NIS_GETINSTANCE		= 0x00000002,	// get native pointer
		CII_ADD				= 0x00000001,	// register name
											// 'num' argument passed to CII is to be igonored.
		CII_GET				= 0x00000000,	// retrieve name
		CII_SET_FINALIZE	= 0x00000002,	// register "finalize" method name
											// (set empty string not to call the method)
											// 'num' argument passed to CII is to be igonored.
		CII_SET_MISSING		= 0x00000003;	// register "missing" method name.
											// the method is called when the member is not present.
											// (set empty string not to call the method)
											// 'num' argument passed to CII is to be igonored.
											// the method is to be called with three arguments;
											// get_or_set    : false for get, true for set
											// name          : member name
											// value         : value property; you must
											//               : dereference using unary '*' operator.
											// the method must return true for found, false for not-found.


	static final public int
		nitMethod	= 1,
		nitProperty = 2;
}
