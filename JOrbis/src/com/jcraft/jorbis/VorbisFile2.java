// 2012/2/25 VorbisFile を書き換えて、自分の用途で期待通り動くようにしたもの
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *
 * Many thanks to
 *   Monty <monty@xiph.org> and
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;

import com.jcraft.jogg.*;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.JOrbisException;

import java.io.InputStream;
import java.io.IOException;

public class VorbisFile2 {
	static private final int CHUNKSIZE=8500;

	static private final int OV_FALSE=-1;
	static private final int OV_EOF=-2;

	static private final int OV_EREAD=-128;
	static private final int OV_EFAULT=-129;
	static private final int OV_ENOTVORBIS=-132;

	SeekableInputStream datasource;
	long offset;
	long end;

	SyncState oy;

	int links;
	long[] offsets;
	long[] dataoffsets;
	int[] serialnos;
	long[] pcmlengths;
	Info[] vi;
	Comment[] vc;

	// Decoding working state local storage
	long pcm_offset;
	boolean decode_ready;

	int current_serialno;
	int current_link;

	float bittrack;
	float samptrack;

	StreamState os; // take physical pages, weld into a logical
	// stream of packets
	DspState vd; // central working state for
	// the packet->PCM decoder
	Block vb; // local working space for packet->PCM decode

	public VorbisFile2(InputStream is, byte[] initial, int ibytes) throws JOrbisException, IOException {
		oy=new SyncState();
		os=new StreamState();
		vd=new DspState();
		vb=new Block(vd);
		try {
			SeekableInputStream sis = new SeekableInputStream(is);
			int ret = open( sis, initial, ibytes);
			if(ret==-1){
				throw new JOrbisException("VorbisFile: open return -1");
			}
		} catch( Exception e ) {
			throw new JOrbisException("VorbisFile: "+e.toString());
		}
	}

	private final int get_data(){
		int index=oy.buffer(CHUNKSIZE);
		byte[] buffer=oy.data;
		int bytes=0;
		try{
			bytes=datasource.read(buffer, index, CHUNKSIZE);
		}
		catch(Exception e){
			return OV_EREAD;
		}
		oy.wrote(bytes);
		if(bytes==-1){
			bytes=0;
		}
		return bytes;
	}

	private final void seek_helper(long offst){
		try {
			datasource.seek(offst);
		} catch (IOException e) {
		}
		this.offset=offst;
		oy.reset();
	}

	private final int get_next_page(Page page, long boundary){
		if(boundary>0)
			boundary+=offset;
		while(true){
			int more;
			if(boundary>0&&offset>=boundary)
				return OV_FALSE;
			more=oy.pageseek(page);
			if(more<0){
				offset-=more;
			}
			else{
				if(more==0){
					if(boundary==0)
						return OV_FALSE;
					int ret=get_data();
					if(ret==0)
						return OV_EOF;
					if(ret<0)
						return OV_EREAD;
				}
				else{
					int ret=(int)offset; //!!!
					offset+=more;
					return ret;
				}
			}
		}
	}

	private final int get_prev_page(Page page) throws JOrbisException{
		long begin=offset; //!!!
		int ret;
		int offst=-1;
		while(offst==-1){
			begin-=CHUNKSIZE;
			if(begin<0)
				begin=0;
			seek_helper(begin);
			while(offset<begin+CHUNKSIZE){
				ret=get_next_page(page, begin+CHUNKSIZE-offset);
				if(ret==OV_EREAD){
					return OV_EREAD;
				}
				if(ret<0){
					if(offst==-1)
						throw new JOrbisException();
					break;
				}
				else{
					offst=ret;
				}
			}
		}
		seek_helper(offst); //!!!
		ret=get_next_page(page, CHUNKSIZE);
		if(ret<0){
			return OV_EFAULT;
		}
		return offst;
	}

	private final int bisect_forward_serialno(long begin, long searched, long end,
			int currentno, int m){
		long endsearched=end;
		long next=end;
		Page page=new Page();
		int ret;

		while(searched<endsearched){
			long bisect;
			if(endsearched-searched<CHUNKSIZE){
				bisect=searched;
			}
			else{
				bisect=(searched+endsearched)/2;
			}

			seek_helper(bisect);
			ret=get_next_page(page, -1);
			if(ret==OV_EREAD)
				return OV_EREAD;
			if(ret<0||page.serialno()!=currentno){
				endsearched=bisect;
				if(ret>=0)
					next=ret;
			}
			else{
				searched=ret+page.header_len+page.body_len;
			}
		}
		seek_helper(next);
		ret=get_next_page(page, -1);
		if(ret==OV_EREAD)
			return OV_EREAD;

		if(searched>=end||ret==-1){
			links=m+1;
			offsets=new long[m+2];
			offsets[m+1]=searched;
		}
		else{
			ret=bisect_forward_serialno(next, offset, end, page.serialno(), m+1);
			if(ret==OV_EREAD)
				return OV_EREAD;
		}
		offsets[m]=begin;
		return 0;
	}

	// uses the local ogg_stream storage in vf; this is important for
	// non-streaming input sources
	private final int fetch_headers(Info vi, Comment vc, int[] serialno, Page og_ptr){
		Page og=new Page();
		Packet op=new Packet();
		int ret;

		if(og_ptr==null){
			ret=get_next_page(og, CHUNKSIZE);
			if(ret==OV_EREAD)
				return OV_EREAD;
			if(ret<0)
				return OV_ENOTVORBIS;
			og_ptr=og;
		}

		if(serialno!=null)
			serialno[0]=og_ptr.serialno();

		os.init(og_ptr.serialno());

		// extract the initial header from the first page and verify that the
		// Ogg bitstream is in fact Vorbis data

		vi.init();
		vc.init();

		int i=0;
		while(i<3){
			os.pagein(og_ptr);
			while(i<3){
				int result=os.packetout(op);
				if(result==0)
					break;
				if(result==-1){
					vi.clear();
					vc.clear();
					os.clear();
					return -1;
				}
				if(vi.synthesis_headerin(vc, op)!=0){
					vi.clear();
					vc.clear();
					os.clear();
					return -1;
				}
				i++;
			}
			if(i<3)
				if(get_next_page(og_ptr, 1)<0){
					vi.clear();
					vc.clear();
					os.clear();
					return -1;
				}
		}
		return 0;
	}

	// last step of the OggVorbis_File initialization; get all the
	// vorbis_info structs and PCM positions.  Only called by the seekable
	// initialization (local stream storage is hacked slightly; pay
	// attention to how that's done)
	private final void prefetch_all_headers(Info first_i, Comment first_c, int dataoffset)
	throws JOrbisException{
		Page og=new Page();
		int ret;

		vi=new Info[links];
		vc=new Comment[links];
		dataoffsets=new long[links];
		pcmlengths=new long[links];
		serialnos=new int[links];

		for(int i=0; i<links; i++){
			if(first_i!=null&&first_c!=null&&i==0){
				// we already grabbed the initial header earlier.  This just
				// saves the waste of grabbing it again
				vi[i]=first_i;
				vc[i]=first_c;
				dataoffsets[i]=dataoffset;
			}
			else{
				// seek to the location of the initial header
				seek_helper(offsets[i]); //!!!
				vi[i]=new Info();
				vc[i]=new Comment();
				if(fetch_headers(vi[i], vc[i], null, null)==-1){
					dataoffsets[i]=-1;
				}
				else{
					dataoffsets[i]=offset;
					os.clear();
				}
			}

			// get the serial number and PCM length of this link. To do this,
			// get the last page of the stream
			{
				long end=offsets[i+1]; //!!!
				seek_helper(end);

				while(true){
					ret=get_prev_page(og);
					if(ret==-1){
						// this should not be possible
						vi[i].clear();
						vc[i].clear();
						break;
					}
					if(og.granulepos()!=-1){
						serialnos[i]=og.serialno();
						pcmlengths[i]=og.granulepos();
						break;
					}
				}
			}
		}
	}

	private final int make_decode_ready(){
		if(decode_ready)
			System.exit(1);
		vd.synthesis_init(vi[0]);
		vb.init(vd);
		decode_ready=true;
		return (0);
	}

	private final int open_seekable() throws JOrbisException{
		Info initial_i=new Info();
		Comment initial_c=new Comment();
		int serialno;
		long end;
		int ret;
		int dataoffset;
		Page og=new Page();
		// is this even vorbis...?
		int[] foo=new int[1];
		ret=fetch_headers(initial_i, initial_c, foo, null);
		serialno=foo[0];
		dataoffset=(int)offset; //!!
		os.clear();
		if(ret==-1)
			return (-1);
		if(ret<0)
			return (ret);
		// we can seek, so set out learning all about this file
		end = datasource.getLength();
		// We get the offset for the last page of the physical bitstream.
		// Most OggVorbis files will contain a single logical bitstream
		end=get_prev_page(og);
		// moer than one logical bitstream?
		if(og.serialno()!=serialno){
			// Chained bitstream. Bisect-search each logical bitstream
			// section.  Do so based on serial number only
			if(bisect_forward_serialno(0, 0, end+1, serialno, 0)<0){
				clear();
				return OV_EREAD;
			}
		}
		else{
			// Only one logical bitstream
			if(bisect_forward_serialno(0, end, end+1, serialno, 0)<0){
				clear();
				return OV_EREAD;
			}
		}
		prefetch_all_headers(initial_i, initial_c, dataoffset);
		return 0;
	}

	// clear out the current logical bitstream decoder
	private final void decode_clear(){
		os.clear();
		vd.clear();
		vb.clear();
		decode_ready=false;
		bittrack=0.f;
		samptrack=0.f;
	}

	// fetch and process a packet.  Handles the case where we're at a
	// bitstream boundary and dumps the decoding machine.  If the decoding
	// machine is unloaded, it loads it.  It also keeps pcm_offset up to
	// date (seek and read both use this.  seek uses a special hack with
	// readp).
	//
	// return: -1) hole in the data (lost packet)
	//          0) need more date (only if readp==0)/eof
	//          1) got a packet

	private final int process_packet(int readp){
		Page og=new Page();

		// handle one packet.  Try to fetch it from current stream state
		// extract packets from page
		while(true){
			// process a packet if we can.  If the machine isn't loaded,
			// neither is a page
			if(decode_ready){
				Packet op=new Packet();
				int result=os.packetout(op);
				long granulepos;
				// if(result==-1)return(-1); // hole in the data. For now, swallow
				// and go. We'll need to add a real
				// error code in a bit.
				if(result>0){
					// got a packet.  process it
					granulepos=op.granulepos;
					if(vb.synthesis(op)==0){ // lazy check for lazy
						// header handling.  The
						// header packets aren't
						// audio, so if/when we
						// submit them,
						// vorbis_synthesis will
						// reject them
						// suck in the synthesis data and track bitrate
						{
							int oldsamples=vd.synthesis_pcmout(null, null);
							vd.synthesis_blockin(vb);
							samptrack+=vd.synthesis_pcmout(null, null)-oldsamples;
							bittrack+=op.bytes*8;
						}

						// update the pcm offset.
						if(granulepos!=-1&&op.e_o_s==0){
							//int link=(seekable ? current_link : 0);
							int link= current_link;
							int samples;
							// this packet has a pcm_offset on it (the last packet
							// completed on a page carries the offset) After processing
							// (above), we know the pcm position of the *last* sample
							// ready to be returned. Find the offset of the *first*
							//
							// As an aside, this trick is inaccurate if we begin
							// reading anew right at the last page; the end-of-stream
							// granulepos declares the last frame in the stream, and the
							// last packet of the last page may be a partial frame.
							// So, we need a previous granulepos from an in-sequence page
							// to have a reference point.  Thus the !op.e_o_s clause above

							samples=vd.synthesis_pcmout(null, null);
							granulepos-=samples;
							for(int i=0; i<link; i++){
								granulepos+=pcmlengths[i];
							}
							pcm_offset=granulepos;
						}
						return (1);
					}
				}
			}

			if(readp==0)
				return (0);
			if(get_next_page(og, -1)<0)
				return (0); // eof. leave unitialized

			// bitrate tracking; add the header's bytes here, the body bytes
			// are done by packet above
			bittrack+=og.header_len*8;

			// has our decoding just traversed a bitstream boundary?
			if(decode_ready){
				if(current_serialno!=og.serialno()){
					decode_clear();
				}
			}

			// Do we need to load a new machine before submitting the page?
			// This is different in the seekable and non-seekable cases.
			//
			// In the seekable case, we already have all the header
			// information loaded and cached; we just initialize the machine
			// with it and continue on our merry way.
			//
			// In the non-seekable (streaming) case, we'll only be at a
			// boundary if we just left the previous logical bitstream and
			// we're now nominally at the header of the next bitstream

			if(!decode_ready){
				int i;
				current_serialno=og.serialno();

				// match the serialno to bitstream section.  We use this rather than
				// offset positions to avoid problems near logical bitstream
				// boundaries
				for(i=0; i<links; i++){
					if(serialnos[i]==current_serialno)
						break;
				}
				if(i==links)
					return (-1); // sign of a bogus stream.  error out,
				// leave machine uninitialized
				current_link=i;

				os.init(current_serialno);
				os.reset();
				make_decode_ready();
			}
			os.pagein(og);
		}
	}

	// The helpers are over; it's all toplevel interface from here on out
	// clear out the OggVorbis_File struct
	private final int clear(){
		vb.clear();
		vd.clear();
		os.clear();

		if(vi!=null&&links!=0){
			for(int i=0; i<links; i++){
				vi[i].clear();
				vc[i].clear();
			}
			vi=null;
			vc=null;
		}
		if(dataoffsets!=null)
			dataoffsets=null;
		if(pcmlengths!=null)
			pcmlengths=null;
		if(serialnos!=null)
			serialnos=null;
		if(offsets!=null)
			offsets=null;
		oy.clear();

		return (0);
	}

	// inspects the OggVorbis file and finds/documents all the logical
	// bitstreams contained in it.  Tries to be tolerant of logical
	// bitstream sections that are truncated/woogie.
	//
	// return: -1) error
	//          0) OK
	private final int open(SeekableInputStream is, byte[] initial, int ibytes) throws JOrbisException{
		int ret;
		datasource=is;

		oy.init();

		// perhaps some data was previously read into a buffer for testing
		// against other stream types.  Allow initialization from this
		// previously read data (as we may be reading from a non-seekable
		// stream)
		if(initial!=null){
			int index=oy.buffer(ibytes);
			System.arraycopy(initial, 0, oy.data, index, ibytes);
			oy.wrote(ibytes);
		}
		// can we seek? Stevens suggests the seek test was portable
		ret = open_seekable();
		if(ret!=0){
			datasource=null;
			clear();
		}
		return ret;
	}

	// How many logical bitstreams in this physical bitstream?
	public final int streams(){
		return links;
	}

	// returns the bitrate for a given logical bitstream or the entire
	// physical bitstream.  If the file is open for random access, it will
	// find the *actual* average bitrate.  If the file is streaming, it
	// returns the nominal bitrate (if set) else the average of the
	// upper/lower bounds (if set) else -1 (unset).
	//
	// If you want the actual bitrate field settings, get them from the
	// vorbis_info structs

	public final int bitrate(int i){
		if(i>=links)
			return (-1);
		if(i<0){
			long bits=0;
			for(int j=0; j<links; j++){
				bits+=(offsets[j+1]-dataoffsets[j])*8;
			}
			return ((int)Math.rint(bits/time_total(-1)));
		}
		else{
			// return the actual bitrate
			return ((int)Math.rint((offsets[i+1]-dataoffsets[i])*8/time_total(i)));
		}
	}

	// returns the actual bitrate since last call.  returns -1 if no
	// additional data to offer since last call (or at beginning of stream)
	public final int bitrate_instant(){
		int _link = current_link;
		if(samptrack==0)
			return (-1);
		int ret=(int)(bittrack/samptrack*vi[_link].rate+.5);
		bittrack=0.f;
		samptrack=0.f;
		return (ret);
	}

	public final int serialnumber(int i){
		if(i>=links)
			return (-1);
		if(i<0){
			return (current_serialno);
		}
		else{
			return (serialnos[i]);
		}
	}

	// returns: total raw (compressed) length of content if i==-1
	//          raw (compressed) length of that logical bitstream for i==0 to n
	//          -1 if the stream is not seekable (we can't know the length)
	public final long raw_total(int i){
		if(i<0){
			long acc=0; // bug?
			for(int j=0; j<links; j++){
				acc+=raw_total(j);
			}
			return (acc);
		}
		else{
			return (offsets[i+1]-offsets[i]);
		}
	}

	// returns: total PCM length (samples) of content if i==-1
	//          PCM length (samples) of that logical bitstream for i==0 to n
	//          -1 if the stream is not seekable (we can't know the length)
	public final long pcm_total(int i){
		if(i<0){
			long acc=0;
			for(int j=0; j<links; j++){
				acc+=pcm_total(j);
			}
			return (acc);
		}
		else{
			return (pcmlengths[i]);
		}
	}

	// returns: total seconds of content if i==-1
	//          seconds in that logical bitstream for i==0 to n
	//          -1 if the stream is not seekable (we can't know the length)
	public final float time_total(int i){
		if(i<0){
			float acc=0;
			for(int j=0; j<links; j++){
				acc+=time_total(j);
			}
			return (acc);
		}
		else{
			return ((float)(pcmlengths[i])/vi[i].rate);
		}
	}

	// seek to an offset relative to the *compressed* data. This also
	// immediately sucks in and decodes pages to update the PCM cursor. It
	// will cross a logical bitstream boundary, but only if it can't get
	// any packets out of the tail of the bitstream we seek to (so no
	// surprises).
	//
	// returns zero on success, nonzero on failure
	public final int raw_seek(int pos){
		if(pos<0||pos>offsets[links]){
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		}

		// clear out decoding machine state
		pcm_offset=-1;
		decode_clear();

		// seek
		seek_helper(pos);

		// we need to make sure the pcm_offset is set.  We use the
		// _fetch_packet helper to process one packet with readp set, then
		// call it until it returns '0' with readp not set (the last packet
		// from a page has the 'granulepos' field set, and that's how the
		// helper updates the offset

		switch(process_packet(1)){
		case 0:
			// oh, eof. There are no packets remaining.  Set the pcm offset to
			// the end of file
			pcm_offset=pcm_total(-1);
			return (0);
		case -1:
			// error! missing data or invalid bitstream structure
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		default:
			// all OK
			break;
		}
		while(true){
			switch(process_packet(0)){
			case 0:
				// the offset is set.  If it's a bogus bitstream with no offset
				// information, it's not but that's not our fault.  We still run
				// gracefully, we're just missing the offset
				return (0);
			case -1:
				// error! missing data or invalid bitstream structure
				//goto seek_error;
				pcm_offset=-1;
				decode_clear();
				return -1;
			default:
				// continue processing packets
				break;
			}
		}

		// seek_error:
		// dump the machine so we're in a known state
		//pcm_offset=-1;
		//decode_clear();
		//return -1;
	}

	// seek to a sample offset relative to the decompressed pcm stream
	// returns zero on success, nonzero on failure

	public final int pcm_seek(long pos){
		int link=-1;
		long total=pcm_total(-1);

		if(pos<0||pos>total){
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		}

		// which bitstream section does this pcm offset occur in?
		for(link=links-1; link>=0; link--){
			total-=pcmlengths[link];
			if(pos>=total)
				break;
		}

		// search within the logical bitstream for the page with the highest
		// pcm_pos preceeding (or equal to) pos.  There is a danger here;
		// missing pages or incorrect frame number information in the
		// bitstream could make our task impossible.  Account for that (it
		// would be an error condition)
		{
			long target=pos-total;
			long end=offsets[link+1];
			long begin=offsets[link];
			int best=(int)begin;

			Page og=new Page();
			while(begin<end){
				long bisect;
				int ret;

				if(end-begin<CHUNKSIZE){
					bisect=begin;
				}
				else{
					bisect=(end+begin)/2;
				}

				seek_helper(bisect);
				ret=get_next_page(og, end-bisect);

				if(ret==-1){
					end=bisect;
				}
				else{
					long granulepos=og.granulepos();
					if(granulepos<target){
						best=ret; // raw offset of packet with granulepos
						begin=offset; // raw offset of next packet
					}
					else{
						end=bisect;
					}
				}
			}
			// found our page. seek to it (call raw_seek).
			if(raw_seek(best)!=0){
				//goto seek_error;
				pcm_offset=-1;
				decode_clear();
				return -1;
			}
		}

		// verify result
		//if(pcm_offset>=pos){ may be original bug.
		if(pcm_offset > pos ) {
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		}
		if(pos>pcm_total(-1)){
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		}

		// discard samples until we reach the desired position. Crossing a
		// logical bitstream boundary with abandon is OK.
		while(pcm_offset<pos){
			int target=(int)(pos-pcm_offset);
			float[][][] _pcm=new float[1][][];
			int[] _index=new int[getInfo(-1).channels];
			int samples=vd.synthesis_pcmout(_pcm, _index);

			if(samples>target)
				samples=target;
			vd.synthesis_read(samples);
			pcm_offset+=samples;

			if(samples<target)
				if(process_packet(1)==0){
					pcm_offset=pcm_total(-1); // eof
				}
		}
		return 0;

		// seek_error:
		// dump machine so we're in a known state
		//pcm_offset=-1;
		//decode_clear();
		//return -1;
	}

	// seek to a playback time relative to the decompressed pcm stream
	// returns zero on success, nonzero on failure
	public final int time_seek(float seconds){
		// translate time to PCM position and call pcm_seek

		int link=-1;
		long pcm_total=pcm_total(-1);
		float time_total=time_total(-1);

		if(seconds<0||seconds>time_total){
			//goto seek_error;
			pcm_offset=-1;
			decode_clear();
			return -1;
		}

		// which bitstream section does this time offset occur in?
		for(link=links-1; link>=0; link--){
			pcm_total-=pcmlengths[link];
			time_total-=time_total(link);
			if(seconds>=time_total)
				break;
		}

		// enough information to convert time offset to pcm offset
		{
			long target=(long)(pcm_total+(seconds-time_total)*vi[link].rate);
			return (pcm_seek(target));
		}

		//seek_error:
		// dump machine so we're in a known state
		//pcm_offset=-1;
		//decode_clear();
		//return -1;
	}

	// tell the current stream offset cursor.  Note that seek followed by
	// tell will likely not give the set offset due to caching
	public final long raw_tell(){
		return (offset);
	}

	// return PCM offset (sample) of next PCM sample to be read
	public final long pcm_tell(){
		return (pcm_offset);
	}

	// return time offset (seconds) of next PCM sample to be read
	public final float time_tell(){
		// translate time to PCM position and call pcm_seek

		int link=-1;
		long pcm_total=0;
		float time_total=0.f;

		pcm_total=pcm_total(-1);
		time_total=time_total(-1);

		// which bitstream section does this time offset occur in?
		for(link=links-1; link>=0; link--){
			pcm_total-=pcmlengths[link];
			time_total-=time_total(link);
			if(pcm_offset>=pcm_total)
				break;
		}

		return ((float)time_total+(float)(pcm_offset-pcm_total)/vi[link].rate);
	}

	//  link:   -1) return the vorbis_info struct for the bitstream section
	//              currently being decoded
	//         0-n) to request information for a specific bitstream section
	//
	// In the case of a non-seekable bitstream, any call returns the
	// current bitstream.  NULL in the case that the machine is not
	// initialized

	public final Info getInfo(int link){
		if(link<0){
			if(decode_ready){
				return vi[current_link];
			}
			else{
				return null;
			}
		}
		else{
			if(link>=links){
				return null;
			}
			else{
				return vi[link];
			}
		}
	}

	public final Comment getComment(int link){
		if(link<0){
			if(decode_ready){
				return vc[current_link];
			}
			else{
				return null;
			}
		}
		else{
			if(link>=links){
				return null;
			}
			else{
				return vc[link];
			}
		}
	}

	/**
	 * 16bit,signed,little-endianで読む
	 * @param buffer
	 * @return
	 */
	public final int read( byte[] buffer, int length ) {
		while( true ) {
			if( decode_ready ) {
				float[][] pcm;
				float[][][] _pcm=new float[1][][];
				int[] _index = new int[getInfo(-1).channels];
				int samples = vd.synthesis_pcmout(_pcm, _index);
				pcm=_pcm[0];
				if( samples != 0 ) {
					// yay! proceed to pack data into the byte buffer
					int channels = getInfo(-1).channels;
					int bytespersample = 2 * channels;
					if( samples > (length/bytespersample) ) samples = length/bytespersample;
					// a tight loop to pack each size

					final int stride = channels*2;
					int val;
					for( int i = 0; i < channels; i++ ) { // It's faster in this order
						int src = _index[i];
						int dest = i*2;
						for( int j = 0; j < samples; j++ ) {
							val = (int)(pcm[i][src+j]*32768.f+0.5f);
							if( val > 32767 ) val = 32767;
							else if( val < -32768 ) val = -32768;
							buffer[dest]=(byte)(val);
							buffer[dest+1]=(byte)(val>>>8);
							dest += stride;
						}
					}

					vd.synthesis_read(samples);
					pcm_offset += samples;
					return (samples * bytespersample);
				}
			}

			// suck in another packet
			switch(process_packet(1)){
			case 0:
				return (0);
			case -1:
				return -1;
			default:
				break;
			}
		}
	}

	public final Info[] getInfo(){
		return vi;
	}

	public final Comment[] getComment(){
		return vc;
	}

	public final void close() throws java.io.IOException{
		datasource.close();
	}

	static class SeekableInputStream extends InputStream {
		private InputStream mStream;
		private long mLength;
		private long mOffset;

		SeekableInputStream( InputStream is ) throws IOException {
			mStream = is;
			mLength = is.available();
			mStream.mark(0);
		}

		public int read() throws java.io.IOException{
			int ret = mStream.read();
			if( ret >= 0 ) mOffset++;
			return ret;
		}

		public int read(byte[] buf) throws java.io.IOException{
			int ret = mStream.read(buf);
			if( ret > 0 ) {
				mOffset += ret;
			}
			return ret;
		}

		public int read(byte[] buf, int s, int len) throws java.io.IOException{
			int ret = mStream.read(buf, s, len);
			if( ret > 0 ) {
				mOffset += ret;
			}
			return ret;
		}

		public long skip(long n) throws java.io.IOException {
			long ret = mStream.skip(n);
			if( ret > 0 ) {
				mOffset += ret;
			}
			return ret;
		}

		public long getLength() { return mLength; }
		public long tell() { return mOffset; }
		public int available() { return (int) mLength; }
		public void close() throws IOException { mStream.close(); }
		public synchronized void mark(int m){}
		public synchronized void reset() throws IOException {}
		public boolean markSupported(){return false;}

		public void seek(long pos) throws IOException{
			mStream.reset();
			mStream.skip(pos);
			mOffset = pos;
		}
	}
}
