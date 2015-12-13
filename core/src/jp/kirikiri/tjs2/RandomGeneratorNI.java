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

import java.nio.ByteBuffer;

public class RandomGeneratorNI extends NativeInstanceObject {
	static private RandomBits128 mRandomBits128 = null;
	static public void setRandomBits128( RandomBits128 rbit128 ) {
		mRandomBits128 = rbit128;
	}


	private static final int MT_N = 624;

	private MersenneTwister mGenerator;

	public RandomGeneratorNI() {
		super();
		//mGenerator = null;
	}
	/*
	protected void finalize() {
		mGenerator = null;
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	*/

	public Dispatch2 serialize() throws VariantException, TJSException {
		// create dictionary object which has reconstructible information
		// which can be passed into constructor or randomize method.
		if( mGenerator == null ) return null;

		Dispatch2 dic = null;
		Variant val = new Variant();

		// retrive tTJSMersenneTwisterData
		final MersenneTwisterData data = mGenerator.getData();
		// create 'state' string
		String state;
		StringBuilder p = new StringBuilder(MT_N * 8);
		for( int i = 0; i < MT_N; i++ ) {
			final String hex = "0123456789abcdef";
			p.append( hex.charAt( (int) ((data.state.get(i)  >> 28) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >> 24) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >> 20) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >> 16) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >> 12) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >>  8) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >>  4) & 0x000f) ) );
			p.append( hex.charAt( (int) ((data.state.get(i)  >>  0) & 0x000f) ) );
		}
		state = p.toString();

		// create dictionary and store information
		dic = TJS.createDictionaryObject();

		val.set( state );
		dic.propSet(Interface.MEMBERENSURE, "state", val, dic);

		val.set( data.left );
		dic.propSet(Interface.MEMBERENSURE, "left", val, dic);

		val.set( data.next );
		dic.propSet(Interface.MEMBERENSURE, "next", val, dic);
		return dic;
	}

	public void randomize( Variant[] param ) throws TJSException, VariantException {
		if( param.length == 0 ) {
			// parametor not given
			if( mRandomBits128 != null ) {
				// another random generator is given
				//tjs_uint8 buf[32];
				//unsigned long tmp[32];
				ByteBuffer buf = ByteBuffer.allocateDirect(32);
				mRandomBits128.getRandomBits128( buf, 0 );
				mRandomBits128.getRandomBits128( buf, 16 );
				int[] tmp = new int[32];

				for( int i = 0; i < 32; i++) {
					long num = (long)buf.get(i) + ((long)buf.get(i) << 8) + ((long)buf.get(1) << 16) + ((long)buf.get(i) << 24);
					tmp[i] = (int) (num > Integer.MAX_VALUE ? num - 0x100000000L : num);
				}
				if( mGenerator != null ) mGenerator = null;
				mGenerator = new MersenneTwister(tmp);
			} else {
				if( mGenerator != null ) mGenerator = null;
				mGenerator = new MersenneTwister( System.currentTimeMillis() );
			}
		} else if( param.length >= 1 ) {
			if( param[0].isObject() ) {
				MersenneTwisterData data = null;
				try {
					// may be a reconstructible information
					VariantClosure clo = param[0].asObjectClosure();
					if( clo.mObject == null ) throw new TJSException( Error.NullAccess );

					String state;
					Variant val = new Variant();
					data = new MersenneTwisterData();

					// get state array
					//TJSThrowFrom_tjs_error
					int hr = clo.propGet(Interface.MEMBERMUSTEXIST, "state", val, null );
					if( hr < 0 ) Error.throwFrom_tjs_error( hr, null );

					state = val.asString();
					if( state.length() != MT_N * 8) {
						throw new TJSException( Error.NotReconstructiveRandomizeData );
					}

					int p = 0;
					for( int i = 0; i < MT_N; i++) {
						long n = 0;
						int tmp;
						for( int j = 0; j < 8; j++ ) {
							int c = state.charAt(p+j);
							tmp = -1;
							if(c >= '0' && c <= '9') n = c - '0';
							else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
							else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;

							if(tmp == -1) {
								throw new TJSException( Error.NotReconstructiveRandomizeData );
							} else {
								n <<= 4;
								n += tmp;
							}
						}
						p += 8;
						data.state.put( i, n&0xffffffffL );
					}

					// get other members
					hr = clo.propGet(Interface.MEMBERMUSTEXIST, "left", val, null );
					if( hr < 0 ) Error.throwFrom_tjs_error( hr, null );

					data.left = val.asInteger();

					hr = clo.propGet( Interface.MEMBERMUSTEXIST, "next", val, null  );
					data.next = val.asInteger();

					if( mGenerator != null ) mGenerator = null;
					mGenerator = new MersenneTwister(data);
				} catch( VariantException e ) {
					data = null;
					throw new TJSException( Error.NotReconstructiveRandomizeData );
				} catch( TJSException e ) {
					data = null;
					throw new TJSException( Error.NotReconstructiveRandomizeData );
				}
				data = null;
			} else {
				// 64bitじゃなくて、32bit にしてしまっている。実用上問題あれば修正。
				int n = param[0].asInteger();
				int[] tmp = new int[1];
				tmp[0] = n;
	  			if( mGenerator != null ) mGenerator = null;
	  			mGenerator = new MersenneTwister(tmp);
			}
		}
	}
	public double random() {
		// returns double precision random value x, x is in 0 <= x < 1
		if( mGenerator == null) return 0;
		return mGenerator.rand_double();

	}
	public int random32() {
		// returns 63 bit integer random value
		if( mGenerator == null ) return 0;
		return mGenerator.int32();
	}
	public long random63() {
		// returns 63 bit integer random value
		if( mGenerator == null ) return 0;

		long v;
		v = (long)mGenerator.int32() << 32;
		v |= mGenerator.int32();

		return v & 0x7fffffffffffffffL;
	}
	public long random64() {
		// returns 64 bit integer random value
		if( mGenerator == null ) return 0;

		long v;
		v = (long)mGenerator.int32() << 32;
		v |= mGenerator.int32();

		return v;
	}
}
