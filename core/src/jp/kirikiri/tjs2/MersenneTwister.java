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
