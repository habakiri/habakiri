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

public interface Dispatch2 {

	// function invocation
	public int funcCall( int flag, final String memberName, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// function invocation by index number
	public int funcCallByNum( int flag, int num, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// property get
	public int propGet( int flag, final String memberName, Variant result, Dispatch2 objThis ) throws VariantException, TJSException;

	// property get by index number
	public int propGetByNum( int flag, int num, Variant result, Dispatch2 objThis ) throws VariantException, TJSException;

	// property set
	public int propSet( int flag, String memberName, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// property set by index number
	public int propSetByNum( int flag, int num, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// get member count
	public int getCount( IntWrapper result, final String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// get member count by index number ( result is Integer )
	public int getCountByNum( IntWrapper result, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// enumerate members
	//public int enumMembers( int flag, VariantClosure callback, Dispatch2 objThis ) throws VariantException, TJSException;
	public int enumMembers( int flags, EnumMembersCallback callback, Dispatch2 objthis ) throws VariantException, TJSException;

	// delete member
	public int deleteMember( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// delete member by index number
	public int deleteMemberByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// invalidation
	public int invalidate( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// invalidation by index number
	public int invalidateByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// get validation, returns true or false
	public int isValid( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException;

	// get validation by index number, returns true or false
	public int isValidByNum( int flag, int num, Dispatch2 objThis ) throws VariantException, TJSException;

	// create new object
	public int createNew( int flag, String memberName, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// create new object by index number
	public int createNewByNum( int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException;

	// reserved1 not use

	// class instance matching returns false or true
	public int isInstanceOf( int flag, String memberName, String className, Dispatch2 objThis ) throws VariantException, TJSException;

	// class instance matching by index number
	public int isInstanceOfByNum( int flag, int num, String className, Dispatch2 objThis ) throws VariantException, TJSException;

	// operation with member
	public int operation( int flag, String memberName, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// operation with member by index number
	public int operationByNum( int flag, int num, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException;

	// support for native instance
	public int nativeInstanceSupport( int flag, int classid, Holder<NativeInstance> pointer );

	// support for class instance infomation
	public int classInstanceInfo( int flag, int num, Variant value ) throws VariantException;

	// special funcsion
	public int addClassInstanveInfo( final String name );

	// special funcsion
	public NativeInstance getNativeInstance( int classid );

	// special funcsion
	public int setNativeInstance(int classid, NativeInstance ni);
	// reserved2
	// reserved3

}
