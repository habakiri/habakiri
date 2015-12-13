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
package jp.kirikiri.tvp2.utils;

import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.StringStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;

/**
 *
 * KAG パーサー用
 *
 */
public class ScenarioCacheItem {

	private String[] mLines;

	static public class LabelCacheData {
		public int Line;
		public int Count;
		public LabelCacheData( int line, int count ) {
			Line = line;
			Count = count;
		}
	}

	/** Label cache  */
	private HashMap<String,LabelCacheData> mLabelCache;

	private ArrayList<String> mLabelAliases;

	/** whether the label is cached  */
	private boolean mLabelCached;


	public ScenarioCacheItem( final String name, boolean isstring) {
		//mLines = null;
		//mLineCount = 0;
		//mLabelCached = false;
		mLabelAliases = new ArrayList<String>();
		mLabelCache = new HashMap<String,LabelCacheData>();
		try {
			loadScenario(name, isstring);
		} catch( Exception e ) {
			mLines = null;
		}
	}

	/**
	 * load file or string to buffer
	 * @param name
	 * @param isstring
	 * @throws TJSException
	 */
	private final void loadScenario( final String name, boolean isstring ) throws TJSException {
		String buffer;
		if( isstring ) {
			// when onScenarioLoad returns string;
			// assumes the string is scenario
			buffer = name;
		} else {
			// else load from file
			buffer = Storage.readText(name, "");
		}

		StringStream stream = new StringStream( buffer );
		// pass1: count lines
		final int count = stream.getMaxLine();

		if(count == 0) Message.throwExceptionMessage(Message.KAGNoLine, name);
		mLines = new String[count];

		// pass2: split lines
		for( int i = 0; i < count; i++ ) {
			String line = stream.getLine(i);
			if( line.length() != 0 && line.charAt(0) == '\t' ) {
				int tab = 1;
				int length = line.length();
				for( ; tab < length; tab++ ) {
					if( line.charAt(tab) != '\t' ) break;
				}
				mLines[i] = line.substring(tab);
			} else {
				mLines[i] = line;
			}
		}
		stream = null;

		// オリジナルは次のようになっているけど、これはこうなってはいない
		// tab-only last line will not be counted in pass2, thus makes
		// pass2 counted lines are lesser than pass1 lines.
	}

	public final String getLabelAliasFromLine( int line ) {
		return mLabelAliases.get(line);
	}

	/**
	 * construct label cache
	 * @throws TJSException
	 */
	public final void ensureLabelCache() throws TJSException {
		if( !mLabelCached ) {
			// make label cache
			String prevlabel = null;
			final int count = mLines.length;
			mLabelAliases.ensureCapacity(count);
			for( int i = 0; i < count; i++ ) {
				mLabelAliases.add(null);
			}
			StringBuilder builder = new StringBuilder(256);
			for( int i = 0; i < count; i++  ) {
				if( mLines[i].length() >= 2 && mLines[i].charAt(0) == '*' ) {
					String p = mLines[i];
					int vl = p.indexOf('|');
					String label;
					if( vl != -1 ) {
						// page name found
						label = p.substring(0,vl);
					} else {
						label = p;
					}

					if( label.length() == 1) {
						if( prevlabel == null || prevlabel.length() == 0 )
							Message.throwExceptionMessage(Message.KAGCannotOmmitFirstLabelName);
						label = prevlabel;
					}

					prevlabel = label;

					LabelCacheData data = mLabelCache.get(label);
					if( data != null ) {
						// previous label name found (duplicated label)
						data.Count++;
						builder.append(label);
						builder.append(':');
						builder.append( data.Count );
						label = builder.toString();
						builder.delete(0, builder.length() );
					}
					mLabelCache.put(label, new LabelCacheData(i, 1) );
					mLabelAliases.add( i, label );
				}
			}
			mLabelCached = true;
		}
	}

	public final String[] getLines() { return mLines; }
	public final int getLineCount() { return mLines.length; }
	public final HashMap<String,LabelCacheData> getLabelCache() { return mLabelCache; }
}
