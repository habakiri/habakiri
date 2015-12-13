/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
