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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Random {
	// 512 is to avoid buffer over-run posibility in multi-threaded access
	static private byte[] RandomSeedPool;
	static private int RandomSeedPoolPos;
	static private byte RandomSeedAtom; // オリジナルでは初期化していない値を使うが、JavaだとゼロクリアされてしまうのでUUIDで初期化する

	static public void initialize() {
		RandomSeedPool = new byte[0x1000 + 512];
		RandomSeedPoolPos = 0;

		UUID uuid = UUID.randomUUID();
		long m = uuid.getMostSignificantBits();
		long l = uuid.getLeastSignificantBits();
		RandomSeedAtom ^= (byte) ((m >> 56) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 48) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 40) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 32) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 24) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 16) & 0xFF);
		RandomSeedAtom ^= (byte) ((m >> 8) & 0xFF);
		RandomSeedAtom ^= (byte) (m & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 56) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 48) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 40) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 32) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 24) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 16) & 0xFF);
		RandomSeedAtom ^= (byte) ((l >> 8) & 0xFF);
		RandomSeedAtom ^= (byte) (l & 0xFF);
	}
	public static void finalizeApplication() {
		RandomSeedPool = null;
		RandomSeedPoolPos = 0;
	}


	static public void pushEnvironNoise( final byte[] buf ) {
		final int bufsize = buf.length;
		for( int i = 0; i < bufsize; i++ ) {
			RandomSeedPool[RandomSeedPoolPos ++] ^= (RandomSeedAtom ^= buf[i]);
			RandomSeedPoolPos &= 0xfff;
		}
		RandomSeedPoolPos += (buf[0]&1);
		RandomSeedPoolPos &= 0xfff;
	}

	static public void updateEnvironNoiseForTick() {
		long tick = System.currentTimeMillis();
		pushEnvironNoise( tick );
	}
	static public void pushEnvironNoise( long val ) {
		byte[] buf = new byte[8];
		buf[0] = (byte) ((val >> 56) & 0xFF);
		buf[1] = (byte) ((val >> 48) & 0xFF);
		buf[2] = (byte) ((val >> 40) & 0xFF);
		buf[3] = (byte) ((val >> 32) & 0xFF);
		buf[4] = (byte) ((val >> 24) & 0xFF);
		buf[5] = (byte) ((val >> 16) & 0xFF);
		buf[6] = (byte) ((val >> 8) & 0xFF);
		buf[7] = (byte) (val & 0xFF);
		Random.pushEnvironNoise( buf );
		buf = null;
	}
	static public void pushEnvironNoise( int val ) {
		byte[] buf = new byte[4];
		buf[0] = (byte) ((val >> 24) & 0xFF);
		buf[1] = (byte) ((val >> 16) & 0xFF);
		buf[2] = (byte) ((val >> 8) & 0xFF);
		buf[3] = (byte) (val & 0xFF);
		Random.pushEnvironNoise( buf );
		buf = null;
	}
	static public void getRandomBits128( byte[] dest ) {
		// retrieve random bits
		pushEnvironNoise( RandomSeedPoolPos );
		UUID uuid = UUID.randomUUID();
		long m = uuid.getMostSignificantBits();
		long l = uuid.getLeastSignificantBits();
		if( dest.length >= 16 ) {
			dest[0] = (byte) ((m >> 56) & 0xFF);
			dest[1] = (byte) ((m >> 48) & 0xFF);
			dest[2] = (byte) ((m >> 40) & 0xFF);
			dest[3] = (byte) ((m >> 32) & 0xFF);
			dest[4] = (byte) ((m >> 24) & 0xFF);
			dest[5] = (byte) ((m >> 16) & 0xFF);
			dest[6] = (byte) ((m >> 8) & 0xFF);
			dest[7] = (byte) (m & 0xFF);
			dest[8] = (byte) ((l >> 56) & 0xFF);
			dest[9] = (byte) ((l >> 48) & 0xFF);
			dest[10] = (byte) ((l >> 40) & 0xFF);
			dest[11] = (byte) ((l >> 32) & 0xFF);
			dest[12] = (byte) ((l >> 24) & 0xFF);
			dest[13] = (byte) ((l >> 16) & 0xFF);
			dest[14] = (byte) ((l >> 8) & 0xFF);
			dest[15] = (byte) (l & 0xFF);
			pushEnvironNoise( dest );
		}

		// make 128bit hash of RandomSeedPool, using MD5 message digest
	    MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		    md.update(RandomSeedPool,0,0x1000);
		    byte[] digest = md.digest();
		    for( int i = 0; i < digest.length && i < dest.length; i++ ) {
		    	dest[i] ^= digest[i]; // UUID も混ぜる
		    }
			// push hash itself
		    pushEnvironNoise( digest );
		} catch (NoSuchAlgorithmException e) {
			// 見付からない時は、UUID で得たものをそのまま使う
		}
	}
}

