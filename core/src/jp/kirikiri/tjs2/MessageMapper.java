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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageMapper {
	static public class MessageHolder {
		final String mName;
		final String mDefaultMessage;
		String mAssignedMessage;

		public MessageHolder( final String name, final String defmsg ) {
			//mAssignedMessage = null;
			mDefaultMessage = defmsg;
			mName = name;
			TJS.registerMessageMap(mName, this);
		}

		public MessageHolder( final String name, final String defmsg, boolean regist ) {
			/* "name" and "defmsg" must point static area */
			//mAssignedMessage = null;
			mDefaultMessage = defmsg;
			if( regist ) {
				mName = name;
				TJS.registerMessageMap(mName, this);
			} else {
				mName = null;
			}
		}

		protected void finalize() {
			if(mName!=null) TJS.unregisterMessageMap(mName);
			if(mAssignedMessage!=null) mAssignedMessage = null;
		}

		public void assignMessage( final String msg ) {
			if(mAssignedMessage!=null) mAssignedMessage = null;
			mAssignedMessage = msg;
		}
		public String getMessage() {
			 return mAssignedMessage != null ? mAssignedMessage : mDefaultMessage;
		}
	}

	HashMap<String, MessageHolder> mHash;


	public MessageMapper() {
		mHash = new HashMap<String, MessageHolder>();
	}

	public void register( final String name, MessageHolder holder ) {
		mHash.put( name, holder);
	}

	public void unregister( final String name ) {
		mHash.remove(name);
	}

	public boolean assignMessage( final String name, final String newmsg ) {
		MessageHolder holder = mHash.get(name);
		if(holder!=null) {
			holder.assignMessage(newmsg);
			return true;
		}
		return false;
	}

	public String get( final String name ) {
		MessageHolder holder = mHash.get(name);
		if(holder!=null) {
			return holder.getMessage();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public String createMessageMapString() {
		StringBuilder script = new StringBuilder();
		Collection ite = mHash.entrySet();
		for( Iterator i = ite.iterator(); i.hasNext(); ) {
		    Map.Entry entry = (Map.Entry)i.next();
		    String name = (String)entry.getKey();

			MessageHolder h = (MessageHolder)entry.getValue();
			script.append("\tr(\"");
			script.append(  LexBase.escapeC(name) );
			script.append("\", \"");
			script.append( LexBase.escapeC(h.getMessage()) );
			script.append("\");\n");
		}
		return script.toString();
	}
}
