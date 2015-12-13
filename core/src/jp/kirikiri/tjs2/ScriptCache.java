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

import java.util.HashMap;

public class ScriptCache {
	private static final int SCRIPT_CACHE_MAX = 64;

	static class ScriptCacheData {
		public String mScript;
		public boolean mExpressionMode;
		public boolean mMustReturnResult;

		public boolean equals( Object o ) {
			if( o instanceof ScriptCacheData && o != null ) {
				ScriptCacheData rhs = (ScriptCacheData)o;
				return( mScript.equals(rhs.mScript) && mExpressionMode == rhs.mExpressionMode &&
						mMustReturnResult == rhs.mMustReturnResult );
			} else {
				return false;
			}
		}
		public int hashCode() {
			int v = mScript.hashCode();
			v ^= mExpressionMode ? 1 : 0;
			v ^= mMustReturnResult ? 1 : 0;
			return v;
		}
	}

	private TJS mOwner;
	private HashMap<ScriptCacheData,ScriptBlock> mCache;

	public ScriptCache( TJS owner) {
		mOwner = owner;
		mCache = new HashMap<ScriptCacheData,ScriptBlock>(SCRIPT_CACHE_MAX);
	}

	public void execScript( final String script, Variant result, Dispatch2 context, final String name, int lineofs ) throws VariantException, TJSException, CompileException {

		Compiler compiler = new Compiler(mOwner);
		if( name != null ) compiler.setName(name, lineofs);
		ScriptBlock blk = compiler.doCompile(script, false, result!=null);
		if( blk == null ) return;
		compiler = null;
		blk.executeTopLevel(result, context);

		/*
		ScriptBlock blk = new ScriptBlock(mOwner);
		if( name != null ) blk.setName(name, lineofs);
		blk.setText(result, script, context, false);
		*/
		if( blk.getContextCount() == 0 ) {
			mOwner.removeScriptBlock(blk);
		}
		blk.compact();
		blk = null;
	}

	public void evalExpression(String expression, Variant result, Dispatch2 context, String name, int lineofs) throws VariantException, TJSException, CompileException {
		// currently this works only with anonymous script blocks.
		// note that this function is basically the same as function above.
		if( name != null && name.length() > 0 ) {
			Compiler compiler = new Compiler(mOwner);
			compiler.setName(name, lineofs);
			ScriptBlock blk = compiler.doCompile(expression, true, result!=null);
			compiler = null;
			if( blk != null ) {
				blk.executeTopLevel(result, context);
				/*
				ScriptBlock blk = new ScriptBlock(mOwner);
				blk.setName( name, lineofs);
				blk.setText( result, expression, context, true );
				 */
				if( blk.getContextCount() == 0 ) {
					mOwner.removeScriptBlock(blk);
				}
				blk.compact();
				blk = null;
			}
			return;
		}

		// search through script block cache
		ScriptCacheData data = new ScriptCacheData();
		data.mScript = expression;
		data.mExpressionMode = true;
		data.mMustReturnResult = result != null;

		ScriptBlock block = mCache.get(data);
		if( block != null ) {
			// found in cache
			// execute script block in cache
			block.executeTopLevelScript(result, context);
			return;
		}

		// not found in cache
		Compiler compiler = new Compiler(mOwner);
		compiler.setName(name, lineofs);
		ScriptBlock blk = compiler.doCompile(expression, true, result!=null);
		blk.executeTopLevel(result, context);
		boolean preprocess = compiler.isUsingPreProcessor();
		compiler = null;

		//ScriptBlock blk = new ScriptBlock(mOwner);
		//blk.setText( result, expression, context, true);
		// add to cache
		if( blk.isReusable() && !preprocess ) {
			// currently only single-context script block is cached
			mCache.put(data, blk);
		} else {
			if( blk.getContextCount() == 0 ) {
				mOwner.removeScriptBlock(blk);
			}
		}
		blk.compact();
		blk = null;
		return;
	}
}
