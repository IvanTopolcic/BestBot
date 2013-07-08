//-------------------------------------------------------------------------------
//
//huffman.java
//
//
//
//Version 5, released 5/13/2009. Compatible with Skulltag launchers and servers.
//
// > Retrieved from http://www.skulltag.com/download/huffman.zip on 07/07/13
//   from the Zandronum wiki
//-------------------------------------------------------------------------------

package org.bestever.serverquery;

import java.util.*;

public class Huffman
{
	private static HuffmanNode huffTree;
	private static HuffmanTab[] huffLookup;
	private static HuffmanNode[] myWork;
	private static HuffmanNode[] myTmp;

	private static final int[] masks = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80 };
	private static final double[] freq=
	{
		0.14473691, 0.01147017, 0.00167522, 0.03831121, 0.00356579,
		0.03811315, 0.00178254, 0.00199644, 0.00183511, 0.00225716,
		0.00211240, 0.00308829, 0.00172852, 0.00186608, 0.00215921,
		0.00168891, 0.00168603, 0.00218586, 0.00284414, 0.00161833,
		0.00196043, 0.00151029, 0.00173932, 0.00218370, 0.00934121,
		0.00220530, 0.00381211, 0.00185456, 0.00194675, 0.00161977,
		0.00186680, 0.00182071, 0.06421956, 0.00537786, 0.00514019,
		0.00487155, 0.00493925, 0.00503143, 0.00514019, 0.00453520,
		0.00454241, 0.00485642, 0.00422407, 0.00593387, 0.00458130,
		0.00343687, 0.00342823, 0.00531592, 0.00324890, 0.00333388,
		0.00308613, 0.00293776, 0.00258918, 0.00259278, 0.00377105,
		0.00267488, 0.00227516, 0.00415997, 0.00248763, 0.00301555,
		0.00220962, 0.00206990, 0.00270369, 0.00231694, 0.00273826,
		0.00450928, 0.00384380, 0.00504728, 0.00221251, 0.00376961,
		0.00232990, 0.00312574, 0.00291688, 0.00280236, 0.00252436,
		0.00229461, 0.00294353, 0.00241201, 0.00366590, 0.00199860,
		0.00257838, 0.00225860, 0.00260646, 0.00187256, 0.00266552,
		0.00242641, 0.00219450, 0.00192082, 0.00182071, 0.02185930,
		0.00157439, 0.00164353, 0.00161401, 0.00187544, 0.00186248,
		0.03338637, 0.00186968, 0.00172132, 0.00148509, 0.00177749,
		0.00144620, 0.00192442, 0.00169683, 0.00209439, 0.00209439,
		0.00259062, 0.00194531, 0.00182359, 0.00159096, 0.00145196,
		0.00128199, 0.00158376, 0.00171412, 0.00243433, 0.00345704,
		0.00156359, 0.00145700, 0.00157007, 0.00232342, 0.00154198,
		0.00140730, 0.00288807, 0.00152830, 0.00151246, 0.00250203,
		0.00224420, 0.00161761, 0.00714383, 0.08188576, 0.00802537,
		0.00119484, 0.00123805, 0.05632671, 0.00305156, 0.00105584,
		0.00105368, 0.00099246, 0.00090459, 0.00109473, 0.00115379,
		0.00261223, 0.00105656, 0.00124381, 0.00100326, 0.00127550,
		0.00089739, 0.00162481, 0.00100830, 0.00097229, 0.00078864,
		0.00107240, 0.00084409, 0.00265760, 0.00116891, 0.00073102,
		0.00075695, 0.00093916, 0.00106880, 0.00086786, 0.00185600,
		0.00608367, 0.00133600, 0.00075695, 0.00122077, 0.00566955,
		0.00108249, 0.00259638, 0.00077063, 0.00166586, 0.00090387,
		0.00087074, 0.00084914, 0.00130935, 0.00162409, 0.00085922,
		0.00093340, 0.00093844, 0.00087722, 0.00108249, 0.00098598,
		0.00095933, 0.00427593, 0.00496661, 0.00102775, 0.00159312,
		0.00118404, 0.00114947, 0.00104936, 0.00154342, 0.00140082,
		0.00115883, 0.00110769, 0.00161112, 0.00169107, 0.00107816,
		0.00142747, 0.00279804, 0.00085922, 0.00116315, 0.00119484,
		0.00128559, 0.00146204, 0.00130215, 0.00101551, 0.00091756,
		0.00161184, 0.00236375, 0.00131872, 0.00214120, 0.00088875,
		0.00138570, 0.00211960, 0.00094060, 0.00088083, 0.00094564,
		0.00090243, 0.00106160, 0.00088659, 0.00114514, 0.00095861,
		0.00108753, 0.00124165, 0.00427016, 0.00159384, 0.00170547,
		0.00104431, 0.00091395, 0.00095789, 0.00134681, 0.00095213,
		0.00105944, 0.00094132, 0.00141883, 0.00102127, 0.00101911,
		0.00082105, 0.00158448, 0.00102631, 0.00087938, 0.00139290,
		0.00114658, 0.00095501, 0.00161329, 0.00126542, 0.00113218,
		0.00123661, 0.00101695, 0.00112930, 0.00317976, 0.00085346,
		0.00101190, 0.00189849, 0.00105728, 0.00186824, 0.00092908,
		0.00160896
	};

	private static class HuffmanNode
	{
		public HuffmanNode zero;
		public HuffmanNode one;
		public char val;
		public double freq;
	}
	private static class HuffmanTab
	{
		long bits;
		int len;
	}

	static //init
	{
		huffTree = new HuffmanNode();
		huffLookup = new HuffmanTab[256];
		myWork = new HuffmanNode[256];
		myTmp = new HuffmanNode[256];
		for(int i = 0;i < 256;i++)
		{
			huffLookup[i] = new HuffmanTab();
			myWork[i] = new HuffmanNode();
			myTmp[i] = new HuffmanNode();
		}
		if(!buildTree())
			System.out.println("Error");
	}

	static boolean findTab(HuffmanNode tmp, int len, long bits)
	{
		if(tmp == null)
		{
			System.out.println("Huffman init error: no huff node");
			return false;
		}
		if(tmp.zero != null)
		{
			if(tmp.one == null)
			{
				System.out.println("Huffman init error: no one in node");
				return false;
			}
			if(len >= 32)
			{
				System.out.println("Huffman init error: compression screwd");
				return false;
			}
			if(!findTab(tmp.zero, len+1, bits<<1)) return false;
			if(!findTab(tmp.one, len+1, (bits<<1)|1)) return false;
			return true;
		}
		huffLookup[tmp.val].len = len;
		huffLookup[tmp.val].bits = bits;
		return true;
	}
	static void putBit(byte[] buf, int pos, long bit)
	{
		if(bit != 0)
			buf[pos/8] |= masks[pos%8];
		else
			buf[pos/8] &= ~masks[pos%8];
	}
	static boolean getBit(byte[] buf, int pos)
	{
		return ((unsigned(buf[pos/8]) & masks[pos%8]) != 0);
	}
	static boolean buildTree()
	{
		double min1, min2;
		int i, j, minat1, minat2;
		HuffmanNode[] work = new HuffmanNode[256];
		HuffmanNode tmp = new HuffmanNode();
		for(i = 0;i < 256;i++)
		{
			work[i] = myWork[i];
			work[i].val = (char) i;
			work[i].freq = freq[i];
			work[i].zero = null;
			work[i].one = null;
			huffLookup[i].len = 0;
		}
		for(i = 0;i < 255;i++)
		{
			minat1 = -1;
			minat2 = -1;
			min1 = 1E30;
			min2 = 1E30;
			for(j = 0;j < 256;j++)
			{
				if(work[j] == null)
					continue;
				if(work[j].freq < min1)
				{
					minat2 = minat1;
					min2 = min1;
					minat1 = j;
					min1 = work[j].freq;
				}
				else if(work[j].freq < min2)
				{
					minat2 = j;
					min2 = work[j].freq;
				}
			}
			if(minat1 < 0)
			{
				System.out.println("Huffman init error: minat1: " + minat1);
				return false;
			}
			if(minat2 < 0)
			{
				System.out.println("Huffman init error: minat2: " + minat2);
				return false;
			}
			tmp = myTmp[i];
			tmp.zero = work[minat2];
			tmp.one = work[minat1];
			tmp.freq = work[minat2].freq + work[minat1].freq;
			tmp.val = 0xFF;
			work[minat1] = tmp;
			work[minat2] = null;
		}
		huffTree = tmp;
		return findTab(huffTree, 0, 0);
	}
	static public byte[] decode(byte[] in)
	{
		String out = new String(""); //We'll use a string so we don't need any expanding arrays
		int bits, tbits;
		HuffmanNode tmp;
		if(unsigned(in[0]) == 0xFF)
		{
			return Arrays.copyOfRange(in, 1, in.length);
		}
		tbits = (in.length-1)*8 - unsigned(in[0]);
		bits = 0;
		in = Arrays.copyOfRange(in, 1, in.length);
		while(bits < tbits)
		{
			tmp = huffTree;
			do
			{
				if(getBit(in, bits))
					tmp = tmp.one;
				else
					tmp = tmp.zero;
				bits++;
			}
			while(tmp.zero != null);
			char[] outTmp = {tmp.val};
			out += new String(outTmp);
		}
		return out.getBytes();
	}
	static public byte[] encode(byte[] in)
	{
		byte[] out = new byte[20000];
		int i, j, bitat, outlen, firstByte;
		long t;
		bitat = 0;
		for(i = 0;i < in.length;i++)
		{
			t = huffLookup[unsigned(in[i])].bits;
			for(j = 0;j < huffLookup[unsigned(in[i])].len;j++)
			{
				putBit(out, bitat + huffLookup[unsigned(in[i])].len - j - 1, t&1);
				t >>= 1;
			}
			bitat += huffLookup[unsigned(in[i])].len;
		}
		outlen = 1 + (bitat+7)/8;
		firstByte = 8*(outlen-1) - bitat;
		if(outlen >= in.length+1)
		{
			byte[] returnVal = new byte[in.length+1];
			returnVal[0] = (byte) 0xFF;
			for(int k = 0;k < in.length;k++)
			{
				returnVal[k+1] = in[k];
			}
			return returnVal;
		}
		byte[] returnVal = new byte[outlen];
		returnVal[0] = (byte) firstByte;
		for(int k = 0;k < outlen-1;k++)
		{
			returnVal[k+1] = out[k];
		}
		return returnVal;
	}
	public static int unsigned(byte in) //Same as Main.unsigned but redefined here so this can be re-used.
	{
		if(in < 0)
			return 256 + in;
		else
			return in;
	}
}