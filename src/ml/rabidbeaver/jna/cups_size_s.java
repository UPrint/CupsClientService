package ml.rabidbeaver.jna;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /home/adam/git/CupsClientService/jni/cups-2.0.2/cups/cups.h:83</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class cups_size_s extends Structure {
	/**
	 * Media name to use<br>
	 * C type : char[128]
	 */
	public byte[] media = new byte[128];
	/** Width in hundredths of millimeters */
	public int width;
	/** Width in hundredths of millimeters */
	public int length;
	/** Width in hundredths of millimeters */
	public int bottom;
	/** Width in hundredths of millimeters */
	public int left;
	/** Width in hundredths of millimeters */
	public int right;
	/** Width in hundredths of millimeters */
	public int top;
	public cups_size_s() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("media", "width", "length", "bottom", "left", "right", "top");
	}
	/**
	 * @param media Media name to use<br>
	 * C type : char[128]<br>
	 * @param width Width in hundredths of millimeters<br>
	 * @param length Width in hundredths of millimeters<br>
	 * @param bottom Width in hundredths of millimeters<br>
	 * @param left Width in hundredths of millimeters<br>
	 * @param right Width in hundredths of millimeters<br>
	 * @param top Width in hundredths of millimeters
	 */
	public cups_size_s(byte media[], int width, int length, int bottom, int left, int right, int top) {
		super();
		if ((media.length != this.media.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.media = media;
		this.width = width;
		this.length = length;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.top = top;
	}
	public cups_size_s(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends cups_size_s implements Structure.ByReference {
		
	};
	public static class ByValue extends cups_size_s implements Structure.ByValue {
		
	};
}
