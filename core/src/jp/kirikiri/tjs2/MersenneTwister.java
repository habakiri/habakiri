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
/*
   A C-program for MT19937, with initialization improved 2002/2/10.
   Coded by Takuji Nishimura and Makoto Matsumoto.
   This is a faster version by taking Shawn Cokus's optimization,
   Matthe Bellew's simplification, Isaku Wada's real version.

   Before using, initialize the state by using init_genrand(seed)
   or init_by_array(init_key, key_length).

   Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji Nishimura,
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

	 1. Redistributions of source code must retain the above copyright
		notice, this list of conditions and the following disclaimer.

	 2. Redistributions in binary form must reproduce the above copyright
		notice, this list of conditions and the following disclaimer in the
		documentation and/or other materials provided with the distribution.

	 3. The names of its contributors may not be used to endorse or promote
		products derived from this software without specific prior written
		permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.	 IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


   Any feedback is very welcome.
   http://www.math.keio.ac.jp/matumoto/emt.html
   email: matumoto@math.keio.ac.jp

   ---------------------------------------------------------------------
   C++ wrapped version by W.Dee <dee@kikyou.info>

   Java rewrite version by T.Imoto
*/

package jp.kirikiri.tjs2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class MersenneTwister extends MersenneTwisterData {

	/* Period parameters */
	private static final int MT_N = 624;
	static private final int N = 624;
	static private final int M = 397;
	static private final long MATRIX_A = 0x9908b0dfL;	/* constant vector a */
	static private final long UMASK = 0x80000000L; /* most significant w-r bits */
	static private final long LMASK = 0x7fffffffL; /* least significant r bits */
	//#define MIXBITS(u,v) ( ((u) & UMASK) | ((v) & LMASK) )
	//#define TWIST(u,v) ((MIXBITS(u,v) >> 1) ^ ((v)&1UL ? MATRIX_A : 0UL))

	public MersenneTwister() {
		this(5489L);
	}
	/* initializes state[N] with a seed */
	public MersenneTwister( long s ) {
		super();
		left = 1;
		init_genrand(s);
	}
	/* initialize by an array with array-length */
	/* init_key is the array for initializing keys */
	/* key_length is its length */
	public MersenneTwister( int[] init_key ) {
		super();

		int i, j, k;
		init_genrand(19650218L);
		i=1; j=0;
		k = (N > init_key.length ? N : init_key.length);
		for( ; k > 0 ; k-- ) {
			state.put( i, ((state.get(i) ^ ((state.get(i-1) ^ (state.get(i-1) >> 30)) * 1664525) ) + init_key[j] + j) & 0xffffffffL );
			i++;
			j++;
			if( i >= N ) {
				state.put( 0, state.get(N-1));
				i=1;
			}
			if( j >= init_key.length) j=0;
		}
		for( k = N-1; k > 0; k-- ) {
			state.put( i, ((state.get(i) ^ ((state.get(i-1) ^ (state.get(i-1) >> 30)) * 1566083941)) - i) & 0xffffffffL );
			i++;
			if( i >= N ) {
				state.put( 0, state.get(N-1) );
				i=1;
			}
		}

		state.put( 0, 0x80000000L ); /* MSB is 1; assuring non-zero initial array */
		left = 1;
		next = 0;
	}
	/* construct tTJSMersenneTwisterData data */
	public MersenneTwister( final MersenneTwisterData data ) {
		super();
		setData( data );
	}

	//protected void finalize() {}

	/* initializes state[N] with a seed */
	private void init_genrand( long s) {
		state.put( 0, s & 0xffffffffL );
		for( int j = 1; j < N; j ++ ) {
			state.put( j, (1812433253L * (state.get(j-1) ^ (state.get(j-1) >> 30)) + j) & 0xffffffffL );
			/* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
			/* In the previous versions, MSBs of the seed affect   */
			/* only MSBs of the array state[].						  */
			/* 2002/01/09 modified by Makoto Matsumoto			   */
			/* for >32 bit machines */
		}
		left = 1;
		next = 0;
	}

	private void next_state() {
		int p = 0;
		int j;

		left = N;
		next = 0;

		for( j=N-M+1; (--j) > 0; p++ ) {
			long x = ( ( state.get(p) & UMASK) | ( state.get(p+1) & LMASK) );
			long y = state.get(p+M) ^ (x>>>1) ^ ((x&1) != 0 ? MATRIX_A : 0);
			state.put( p, y );
		}

		for( j=M; (--j) > 0; p++ ) {
			long x = ( ( state.get(p) & UMASK) | ( state.get(p+1) & LMASK) );
			long y = state.get(p+M-N) ^ (x>>>1) ^ ((x&1) != 0 ? MATRIX_A : 0);
			state.put( p, y );
		}

		{
			long x = ( ( state.get(p) & UMASK) | ( state.get(0) & LMASK) );
			long y = state.get(M-N) ^ (x>>>1) ^ ((x&1) != 0 ? MATRIX_A : 0);
			state.put( p, y );
		}
	}

	/* generates a random number on [0,0xffffffff]-interval */
	public int int32() {
		if( --left == 0 ) next_state();
		long num = state.get(next);
		int y = (int) (num > Integer.MAX_VALUE ? num - 0x100000000L : num);
		next++;

		/* Tempering */
		y ^= (y >>> 11);
		y ^= (y << 7) & 0x9d2c5680;
		y ^= (y << 15) & 0xefc60000;
		y ^= (y >>> 18);

		return y;
	}
	 /* generates a random number on [0,0x7fffffff]-interval */
	public int int31() {
		if( --left == 0 ) next_state();
		long num = state.get(left);
		int y = (int) (num > Integer.MAX_VALUE ? num - 0x100000000L : num);

		/* Tempering */
		y ^= (y >>> 11);
		y ^= (y << 7) & 0x9d2c5680;
		y ^= (y << 15) & 0xefc60000;
		y ^= (y >>> 18);

		return y>>>1;
	}
	/* generates a random number on [0,1]-real-interval */
	double real1() {
		if( --left == 0 ) next_state();
		long y = state.get(next);
		next++;

		/* Tempering */
		y ^= (y >>> 11);
		y ^= (y << 7) & 0x9d2c5680L;
		y ^= (y << 15) & 0xefc60000L;
		y ^= (y >>> 18);

		return (double)y * (1.0/4294967295.0);
		/* divided by 2^32-1 */
	}
	/* generates a random number on [0,1)-real-interval */
	public double real2() {
		if( --left == 0 ) next_state();
		long y = state.get(next);
		next++;

		/* Tempering */
		y ^= (y >>> 11);
		y ^= (y << 7) & 0x9d2c5680L;
		y ^= (y << 15) & 0xefc60000L;
		y ^= (y >>> 18);

		return (double)y * (1.0/4294967296.0);
		/* divided by 2^32 */
	}
	/* generates a random number on (0,1)-real-interval */
	public double real3() {
		if( --left == 0 ) next_state();
		long y = state.get(next);
		next++;

		/* Tempering */
		y ^= (y >>> 11);
		y ^= (y << 7) & 0x9d2c5680L;
		y ^= (y << 15) & 0xefc60000L;
		y ^= (y >>> 18);

		return ((double)y + 0.5) * (1.0/4294967296.0);
		/* divided by 2^32 */
	}
	/* generates a random number on [0,1) with 53-bit resolution*/
	public double res53() {
		if( --left == 0 ) next_state();
		long a = state.get(next);
		next++;
		/* Tempering */
		a ^= (a >>> 11);
		a ^= (a << 7) & 0x9d2c5680;
		a ^= (a << 15) & 0xefc60000;
		a ^= (a >>> 18);
		a >>>= 5;


		if( --left == 0 ) next_state();
		long b = state.get(next);
		next++;
		/* Tempering */
		b ^= (b >>> 11);
		b ^= (b << 7) & 0x9d2c5680;
		b ^= (b << 15) & 0xefc60000;
		b ^= (b >>> 18);
		b >>>= 6;

		return(a*67108864.0+b)*(1.0/9007199254740992.0);
	}

	/* generates a random number on [0,1) with IEEE 64-bit double precision */
	public double rand_double() {
		/* generates a random number on [0,1) with IEEE 64-bit double precision */
		long y;
		{
			if( --left == 0 ) next_state();
			y = state.get(next);
			next++;
			/* Tempering */
			y ^= (y >>> 11);
			y ^= (y << 7) & 0x9d2c5680L;
			y ^= (y << 15) & 0xefc60000L;
			y ^= (y >>> 18);
		}
		long v = (y & 0xffffffffL) << 32;
		{
			if( --left == 0 ) next_state();
			y = state.get(next);
			next++;
			/* Tempering */
			y ^= (y >>> 11);
			y ^= (y << 7) & 0x9d2c5680L;
			y ^= (y << 15) & 0xefc60000L;
			y ^= (y >>> 18);
		}
		v |= (y & 0xffffffffL);
		v &= 0x000fffffffffffffL;
		v = v | (1023L << 52);
		// at this point, v is : 1.0 <= v < 2.0

		return Double.longBitsToDouble(v) - 1.0; // returned value x is : 0.0 <= x < 1.0
	}

	/* retrieve data */
	public final MersenneTwisterData getData() { return this; }

	/* set data */
	public void setData( final MersenneTwisterData rhs) {
		// copy
		LongBuffer src = rhs.state.duplicate();
		src.position(0);
		src.limit(MT_N);

		ByteBuffer buff = ByteBuffer.allocateDirect(MT_N*8);
		buff.order( ByteOrder.nativeOrder() );
		state = buff.asLongBuffer();
		state.clear();
		state.put(src);

		next = rhs.next;
		left = rhs.left;
	}
}
