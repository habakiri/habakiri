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

/**
 * 追加順を持ったハッシュテーブル、配列の拡大は行われない
 * 古いものを削除できる
 * @param <Key> ハッシュのキー
 * @param <Value> 格納する値
 */
public class HashTable<Key,Value> {

	/** デフォルト配列サイズ */
	private static final int DEFAULT_HASH_SIZE = 64;
	/** 使用中フラグ */
	private static final int HASH_USING = 0x1;
	/** 配列に直に入っている要素フラグ */
	private static final int HASH_LV1 = 0x2;

	/**
	 * 各要素
	 * @param <Key> ハッシュのキー
	 * @param <Value> 格納する値
	 */
	static class Element<Key,Value> {
		/** ハッシュ値 */
		int mHash;
		/** 内部で使用するフラグ */
		int mFlags;
		/** キー */
		Key mKey;
		/** 格納する値 */
		Value mValue;
		/** アイテムチェーンで前のアイテム */
		Element<Key,Value> mPrev;
		/** アイテムチェーンで次のアイテム */
		Element<Key,Value> mNext;
		/** 追加順で直前に追加されたアイテム */
		Element<Key,Value> mNPrev;
		/** 追加順で直後に追加されたアイテム */
		Element<Key,Value> mNNext;
	}
	/** 要素配列 */
	private Element<Key,Value>[] mElms;
	/** 実要素数 */
	private int mCount;
	/** 追加順で最初に追加されたアイテム */
	private Element<Key,Value> mNFirst;
	/** 追加順で最後に追加されたアイテム */
	private Element<Key,Value> mNLast;

	/**
	 * デフォルトコンストラクタ
	 * 要素数は DEFAULT_HASH_SIZE となる。
	 */
	public HashTable() {
		this(DEFAULT_HASH_SIZE);
	}
	/**
	 * コンストラクタ
	 * @param initCapacity 初期サイズ
	 */
	@SuppressWarnings("unchecked")
	public HashTable( int initCapacity ) {
		// サイズが必ず2の累乗値になるようにする
		int capacity = 1;
		while( capacity < initCapacity )
			capacity <<= 1;

		mElms = new Element[capacity];
	}
	/**
	 * 全要素を削除する
	 */
	public void clear() {
		internalClear();
	}
	/**
	 * キーに対応した値を得る
	 * @param key キー
	 * @return キーに対応した値
	 */
	public Value get( Key key ) {
		if( key == null ) return null;
		Element<Key,Value> e = internalFindWithHash( key, key.hashCode() );
		if( e == null ) return null;
		return e.mValue;
	}
	/**
	 * キーに対応した値を得つつ、並び順で一番新しいものとする
	 * @param key キー
	 * @return キーに対応した値
	 */
	public Value getAndTouch( Key key ) {
		if( key == null ) return null;
		Element<Key,Value> e = internalFindWithHash( key, key.hashCode() );
		if( e == null ) return null;
		checkUpdateElementOrder( e );
		return e.mValue;
	}

	/**
	 * キーに対応した要素を探す
	 * @param key キー
	 * @param hash ハッシュ値
	 * @return キーに対応した要素
	 */
	private final Element<Key,Value> internalFindWithHash( Key key, int hash) {
		// find key ( hash )
		final int mask = mElms.length - 1;
		Element<Key,Value> lv1 = mElms[hash&mask];
		if( lv1 == null ) return null;
		if( hash == lv1.mHash && (lv1.mFlags & HASH_USING) != 0 ) {
			if( key.equals( lv1.mKey ) ) return lv1;
		}

		Element<Key,Value> elm = lv1.mNext;
		while( elm != null ) {
			if( hash == elm.mHash ) {
				if( key.equals( elm.mKey) ) return elm;
			}
			elm = elm.mNext;
		}
		return null; // not found
	}
	/**
	 * キーと値のペアを格納する
	 * @param key キー
	 * @param value 格納する値
	 */
	public void put( Key key, Value value ) {
		if( key == null ) return;
		addWithHash( key, key.hashCode(), value );
	}
	/**
	 * キーと値のペアを格納する
	 * @param key キー
	 * @param hash ハッシュ値
	 * @param value 値
	 */
	private void addWithHash( Key key, int hash, Value value ) {
		final int mask = mElms.length-1;
		final int index = hash & mask;
		Element<Key, Value> lv1 = mElms[index];
		if( lv1 == null ) {
			lv1 = new Element<Key, Value>();
			mElms[index] = lv1;
			lv1.mFlags = HASH_LV1;
		}
		Element<Key, Value> elm = lv1.mNext;
		while( elm != null ) {
			if( hash == elm.mHash ) {
				if( key.equals(elm.mKey) ) {
					checkUpdateElementOrder(elm);
					elm.mValue = value;
					return;
				}
			}
			elm = elm.mNext;
		}
		if( (lv1.mFlags&HASH_USING) == 0 ) {
			lv1.mKey = key;
			lv1.mValue = value;
			lv1.mFlags |= HASH_USING;
			lv1.mHash = hash;
			lv1.mPrev = null;
			checkAddingElementOrder(lv1);
			return;
		}
		if( hash == lv1.mHash ) {
			if( key.equals( lv1.mHash ) ) {
				checkUpdateElementOrder(lv1);
				lv1.mValue = value;
				return;
			}
		}
		// insert after lv1
		Element<Key, Value> newelm = new Element<Key, Value>();
		//newelm.mFlags = 0;
		newelm.mKey = key;
		newelm.mValue = value;
		newelm.mFlags |= HASH_USING;
		newelm.mHash = hash;
		if(lv1.mNext!=null) lv1.mNext.mPrev = newelm;
		newelm.mNext = lv1.mNext;
		newelm.mPrev = lv1;
		lv1.mNext = newelm;
		checkAddingElementOrder(newelm);
	}
	/**
	 * キーに対応した要素を削除する
	 * @param key キー
	 * @return 実際に削除したかどうか
	 */
	boolean remove( Key key ) {
		if( key == null ) return false;
		return deleteWithHash( key, key.hashCode() );
	}
	/**
	 * キーに対応した要素を削除する
	 * @param key キー
	 * @param hash ハッシュ値
	 * @return 実際に削除したかどうか
	 */
	private boolean deleteWithHash(Key key, int hash) {
		// delete key ( hash ) and return true if succeeded
		final int mask = mElms.length - 1;
		Element<Key, Value> lv1 = mElms[hash&mask];
		if( lv1 == null ) return false;
		if( (lv1.mFlags & HASH_USING) != 0 && hash == lv1.mHash ) {
			if( key.equals( lv1.mKey) ) {
				// delete lv1
				checkDeletingElementOrder(lv1);
				lv1.mKey = null;
				lv1.mValue = null;
				lv1.mFlags &= ~HASH_USING;
				return true;
			}
		}

		Element<Key, Value> prev = lv1;
		Element<Key, Value> elm = lv1.mNext;
		while( elm != null ) {
			if( hash == elm.mHash ) {
				if( key.equals(elm.mKey) ) {
					checkDeletingElementOrder(elm);
					elm.mKey = null;
					elm.mValue = null;
					elm.mFlags &= ~HASH_USING;
					prev.mNext = elm.mNext; // sever from the chain
					if(elm.mNext != null) elm.mNext.mPrev = prev;
					elm.mNext = null;
					elm.mPrev = null;
					return true;
				}
			}
			prev = elm;
			elm = elm.mNext;
		}
		return false; // not found
	}
	public int getCount() { return mCount; }
	/**
	 * 古いものから指定個数削除する
	 * @param count 削除する数
	 * @return 実際に削除した数
	 */
	public int chopLast( int count ) {
		int ret = 0;
		while( count > 0 ) {
			count--;
			if( mNLast == null ) break;
			deleteBytElement(mNLast);
			ret++;
		}
		return ret;
	}
	/**
	 * 指定要素を削除する
	 * @param elm 削除する要素
	 * @return 配列要素かどうか
	 */
	private boolean deleteBytElement(Element<Key, Value> elm) {
		checkDeletingElementOrder(elm);
		elm.mKey = null;
		elm.mValue = null;
		elm.mFlags &= ~HASH_USING;
		if( (elm.mFlags & HASH_LV1) != 0 ) {
			// lv1 element
			// nothing to do
			return false;
		} else {
			// other elements
			if( elm.mPrev != null ) elm.mPrev.mNext = elm.mNext;
			if( elm.mNext != null ) elm.mNext.mPrev = elm.mPrev;
			return true;
		}
	}
	/**
	 * 要素削除に伴い並び順を更新する
	 * @param elm 削除対象要素
	 */
	private void checkDeletingElementOrder(Element<Key, Value> elm) {
		mCount--;
		if( mCount > 0 ) {
			if( elm == mNFirst ) {
				// deletion of first item
				mNFirst = elm.mNNext;
				mNFirst.mNPrev = null;
			} else if( elm == mNLast ) {
				// deletion of last item
				mNLast = elm.mNPrev;
				mNLast.mNNext = null;
			} else {
				// deletion of intermediate item
				elm.mNPrev.mNNext = elm.mNNext;
				elm.mNNext.mNPrev = elm.mNPrev;
			}
		} else {
			// when the count becomes zero...
			mNFirst = mNLast = null;
		}
	}
	/**
	 * 指定要素の並び順を更新する
	 * @param elm 先頭に持ってくる要素
	 */
	private void checkUpdateElementOrder(Element<Key, Value> elm) {
		// move elm to the front of addtional order
		if( elm != mNFirst ) {
			if( mNLast == elm ) mNLast = elm.mNPrev;
			elm.mNPrev.mNNext = elm.mNNext;
			if(elm.mNNext!=null) elm.mNNext.mNPrev = elm.mNPrev;
			elm.mNNext = mNFirst;
			elm.mNPrev = null;
			mNFirst.mNPrev = elm;
			mNFirst = elm;
		}
	}
	/**
	 * 要素追加に伴い並び順を更新する
	 * @param elm 追加する要素
	 */
	private void checkAddingElementOrder(Element<Key, Value> elm) {
		if( mCount == 0 ) {
			mNLast = elm; // first addition
			elm.mNNext = null;
		} else {
			mNFirst.mNPrev = elm;
			elm.mNNext = mNFirst;
		}
		mNFirst = elm;
		elm.mNPrev = null;
		mCount++;
	}

	/**
	 * 初期化
	 */
	private void internalInit() {
		mCount = 0;
		mNFirst = null;
		mNLast = null;
	}
	/**
	 * 全要素クリア
	 */
	private void internalClear() {
		final int count = mElms.length;
		for( int i = 0; i < count; i++ ) {
			if( mElms[i] != null ) {
				Element<Key, Value> e = mElms[i].mNext;
				while( e != null ) {
					e.mKey = null;
					e.mValue = null;
					e.mFlags &= ~HASH_USING;
					Element<Key, Value> next = e.mNext;
					e.mPrev = null;
					e.mNext = null;
					e.mNPrev = null;
					e.mNNext = null;
					e = next;
				}
				e = mElms[i];
				if( (e.mFlags & HASH_USING ) != 0 ) {
					e.mKey = null;
					e.mValue = null;
					e.mFlags &= ~HASH_USING;
					e.mPrev = null;
					e.mNext = null;
					e.mNPrev = null;
					e.mNNext = null;
				}
			}
		}
		internalInit();
	}
	public Value getLastValue() {
		if( mNLast != null ) {
			return mNLast.mValue;
		}
		return null;
	}
}
