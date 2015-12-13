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

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;

// a class for managing the script block
public class ScriptBlock implements SourceCodeAccessor {
	private static final boolean D_IS_DISASSEMBLE = false;

	private static final String NO_SCRIPT = "no script";

	private TJS mOwner;

	private InterCodeObject mTopLevelObject;
	private ArrayList<WeakReference<InterCodeObject>> mInterCodeObjectList;

	// 以下の4つは実行時にいるかな、名前以外はエラー発生時に必要になるだけだろうけど。
	private String mName;
	private int mLineOffset;
	private String mScript;
	private ScriptLineData mLineData;

	public ScriptBlock( TJS owner, final String name, int lineoffset, final String script, ScriptLineData linedata ) {
		mOwner = owner;
		mName = name;
		mLineOffset = lineoffset;
		mScript = script;
		mLineData = linedata;

		mOwner.addScriptBlock(this);
	}
	public void setObjects( InterCodeObject toplevel, ArrayList<InterCodeObject> objs ) {
		mTopLevelObject = toplevel;
		mInterCodeObjectList = new ArrayList<WeakReference<InterCodeObject>>(objs.size());
		final int count = objs.size();
		for( int i = 0; i < count; i++ ) {
			mInterCodeObjectList.add( new WeakReference<InterCodeObject>(objs.get(i)) );
		}
	}
	public void setObjects( InterCodeObject toplevel, InterCodeObject[] objs, int count ) {
		mTopLevelObject = toplevel;
		mInterCodeObjectList = new ArrayList<WeakReference<InterCodeObject>>(objs.length);
		for( int i = 0; i < count; i++ ) {
			mInterCodeObjectList.add( new WeakReference<InterCodeObject>(objs[i]) );
			objs[i] = null;
		}
	}

	public ScriptBlock( TJS owner ) {
		mOwner = owner;

		// Java で初期値となる初期化は省略
		//mScript = null;
		//mName = null;
		//mInterCodeContext = null;
		//mTopLevelContext = null;
		//mLexicalAnalyzer = null;
		//mUsingPreProcessor = false;
		//mLineOffset = 0;
		//mCompileErrorCount = 0;
		//mNode = null;

		mOwner.addScriptBlock(this);
	}
	protected void finalize() {
		if( mTopLevelObject != null ) mTopLevelObject = null;
		mOwner.removeScriptBlock(this);

		if( mScript != null ) mScript = null;
		if( mName != null ) mName = null;;

		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	public void compact() {
		if( TJS.IsLowMemory ) {
			mScript = null;
			mLineData = null;
			final int count = mInterCodeObjectList.size();
			for( int i = 0; i < count; i++ ) {
				InterCodeObject v = mInterCodeObjectList.get(i).get();
				if( v != null ) {
					v.compact();
				}
			}
		}
	}

	public int srcPosToLine( int pos ) {
		if( mLineData == null ) return 0;
		return mLineData.getSrcPosToLine(pos);
	}

	public TJS getTJS() {
		return mOwner;
	}

	public String getName() { return mName; }
	/*
	public void setName( String name, int lineofs ) {
		mName = null;
		if( name != null ) {
			mLineOffset = lineofs;
			mName = new String(name);
		}
	}
	*/
	public int getLineOffset() { return mLineOffset; }

	private void compactInterCodeObjectList() {
		// なくなっているオブジェクトを消す
		final int count = mInterCodeObjectList.size();
		for( int i = count-1; i >= 0; i-- ) {
			if( mInterCodeObjectList.get(i).get() == null ) {
				mInterCodeObjectList.remove(i);
			}
		}
	}
	public void executeTopLevel( Variant result, Dispatch2 context ) throws VariantException, TJSException, CompileException {
		// compiles text and executes its global level scripts.
		// the script will be compiled as an expression if isexpressn is true.


		// 逆アセンブル
		if( D_IS_DISASSEMBLE ) {
			compactInterCodeObjectList();
			final int count = mInterCodeObjectList.size();
			for( int i = 0; i < count; i++ ) {
				InterCodeObject v = mInterCodeObjectList.get(i).get();
				if( v != null ) {
					TJS.outputToConsole( v.getName() );
					v.disassemble( this, 0, 0 );
				}
			}
		}

		// execute global level script
		executeTopLevelScript(result, context);

		final int context_count = mInterCodeObjectList.size();
		if( context_count != 1 ) {
			// this is not a single-context script block
			// (may hook itself)
			// release all contexts and global at this time
			InterCodeObject toplevel = mTopLevelObject;
			if( mTopLevelObject != null ) mTopLevelObject = null;
			remove(toplevel);
		}
	}



	public void executeTopLevelScript(Variant result, Dispatch2 context) throws VariantException, TJSException {
		if( mTopLevelObject != null ) {
			mTopLevelObject.funcCall( 0, null, result, null, context );
		}
	}

	public String getLineDescriptionString(int pos) {
		// get short description, like "mainwindow.tjs(321)"
		// pos is in character count from the first of the script
		StringBuilder builer = new StringBuilder(512);
		int line = srcPosToLine(pos)+1;
		if( mName != null ) {
			builer.append( mName );
		} else {
			builer.append("anonymous@");
			builer.append( toString() );
		}
		builer.append('(');
		builer.append( String.valueOf(line) );
		builer.append(')');
		return builer.toString();
	}

	public int lineToSrcPos(int line) {
		if( mLineData == null ) return 0;
		// assumes line is added by LineOffset
		line -= mLineOffset;
		return mLineData.getLineToSrcPos(line);
	}

	public String getLine(int line ) {
		if( mLineData == null ) return NO_SCRIPT;
		// note that this function DOES matter LineOffset
		line -= mLineOffset;
		return mLineData.getLine(line);
	}

	/*
	public boolean isReusable() {
		return getContextCount() == 1 && mTopLevelObject != null && !mUsingPreProcessor;
	}
	*/
	public boolean isReusable() {
		return getContextCount() == 1 && mTopLevelObject != null;
	}

	public int getContextCount() {
		return mInterCodeObjectList.size();
	}

	public void add(InterCodeObject obj) {
		mInterCodeObjectList.add(new WeakReference<InterCodeObject>(obj));
	}
	public void remove(InterCodeObject obj) {
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			if( mInterCodeObjectList.get(i).get() == obj ) {
				mInterCodeObjectList.remove(i);
				break;
			}
		}
		compactInterCodeObjectList();
	}

	public int getObjectIndex( InterCodeObject obj ) {
		return mInterCodeObjectList.indexOf(obj);
	}
	public InterCodeObject getCodeObject( int index ) {
		if( index >= 0 && index < mInterCodeObjectList.size() ) {
			return mInterCodeObjectList.get(index).get();
		} else {
			return null;
		}
	}

	/*
	private void exportByteCode( BinaryStream output ) throws TJSException {
		//BinaryStream output = TJS.mStorage.createBinaryWriteStream(filename);
		byte[] filetag = { 'T', 'J', 'S', '2' };
		byte[] objtag = { 'O', 'B', 'J', 'S' };
		byte[] datatag = { 'D', 'A', 'T', 'A' };

		final int count = mInterCodeObjectList.size();
		ArrayList<ByteBuffer> objarray = new ArrayList<ByteBuffer>(count*2);
		ConstArrayData constarray = new ConstArrayData();
		int objsize = 0;
		for( int i = 0; i < count; i++ ) {
			InterCodeObject obj = mInterCodeObjectList.get(i).get();
			ByteBuffer buf = obj.exportByteCode(this,constarray);
			objarray.add(buf);
			objsize += buf.capacity() + 4 + 4; // tag + size
		}
		objsize += 4 + 4 + 4; // OBJS tag + size + count
		ByteBuffer dataarea = constarray.exportBuffer();
		int datasize = dataarea.capacity() + 4 + 4; // DATA tag + size
		int filesize = objsize + datasize + 4 + 4; // TJS2 tag + file size
		byte[] filesizearray = { (byte) (filesize&0xff), (byte) ((filesize>>>8)&0xff), (byte) ((filesize>>>16)&0xff), (byte) ((filesize>>>24)&0xff) };
		byte[] datasizearray = { (byte) (datasize&0xff), (byte) ((datasize>>>8)&0xff), (byte) ((datasize>>>16)&0xff), (byte) ((datasize>>>24)&0xff) };
		byte[] objsizearray = { (byte) (objsize&0xff), (byte) ((objsize>>>8)&0xff), (byte) ((objsize>>>16)&0xff), (byte) ((objsize>>>24)&0xff) };
		byte[] objcountarray = { (byte) (count&0xff), (byte) ((count>>>8)&0xff), (byte) ((count>>>16)&0xff), (byte) ((count>>>24)&0xff) };

		output.write(filetag);
		output.write(filesizearray);
		output.write(datatag);
		output.write(datasizearray);
		output.write(dataarea);
		output.write(objtag);
		output.write(objsizearray);
		output.write(objcountarray);
		for( int i = 0; i < count; i++ ) {
			ByteBuffer buf = objarray.get(i);
			int size = buf.capacity();
			byte[] bufsizearray = { (byte) (size&0xff), (byte) ((size>>>8)&0xff), (byte) ((size>>>16)&0xff), (byte) ((size>>>24)&0xff) };
			output.write(filetag);
			output.write(bufsizearray);
			output.write(buf);
		}
		output.close();
		output = null;
		objarray.clear();
		objarray = null;
		constarray = null;
		dataarea = null;
	}
	*/


	public String getNameInfo() {
		if( mLineOffset == 0 ) {
			return new String(mName);
		} else {
			return mName + "(line +" + String.valueOf(mLineOffset) + ")";
		}
	}

	public int getTotalVMCodeSize() {
		compactInterCodeObjectList();
		int size = 0;
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeObject obj = mInterCodeObjectList.get(i).get();
			if( obj != null ) size += obj.getCodeSize();
		}
		return size;
	}

	public int getTotalVMDataSize() {
		compactInterCodeObjectList();
		int size = 0;
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeObject obj = mInterCodeObjectList.get(i).get();
			if( obj != null ) size += obj.getDataSize();
		}
		return size;
	}

	static public void consoleOutput( final String msg, ScriptBlock blk  ) {
		TJS.outputToConsole(msg);
	}
	public void dump() throws VariantException {
		compactInterCodeObjectList();
		final int count = mInterCodeObjectList.size();
		for( int i = 0; i < count; i++ ) {
			InterCodeObject v = mInterCodeObjectList.get(i).get();
			if( v != null ) {
				consoleOutput( "", this );
				String ptr = String.format( " 0x%08X", v.hashCode() );
				consoleOutput( "(" + v.getContextTypeName() + ") " + v.getName() + ptr, this );
				v.disassemble( this, 0, 0 );
			}
		}
	}

	public String getScript() {
		if( mScript != null ) return mScript;
		else return NO_SCRIPT;
	}
	@Override
	public int codePosToSrcPos(int codepos) {
		return 0; // allways 0, 基本的に使われない
	}
}
