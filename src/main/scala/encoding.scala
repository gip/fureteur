// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.encoding

import java.io._
import org.apache.commons.codec.binary._
import org.apache.commons.codec.binary.Base64._

object Encoding {

  def byteToZippedString64(a:Array[Byte]):String = {
    toBin64(zip(a))
  }

  def string64ToUnzippedByte(s:String):Array[Byte] = {
    unzip(fromBin64(s))
  }

  // From Byte array to Binary 64
  def toBin64(a:Array[Byte]):String = {
    StringUtils.newStringUtf8(Base64.encodeBase64(a, false));
  }
  
  def fromBin64(s:String):Array[Byte] = {
    Base64.decodeBase64(s)
  }
  
  // Zip compression from Byte array to Byte array
  def zip(a:Array[Byte]):Array[Byte] = {
    import java.util.zip._
    val compressor = new Deflater();
    compressor.setInput(a);
    compressor.finish();
    val bos = new ByteArrayOutputStream(a.length);
    val buf= new Array[Byte](a.length);
    while(!compressor.finished()) {
      val c= compressor.deflate(buf);
      bos.write(buf, 0, c);
    }
    bos.close();
    bos.toByteArray();
  }
  
  // Zip depression from Byte array to Byte array
  def unzip(a:Array[Byte]):Array[Byte] = {
    import java.util.zip._
    val decompressor = new Inflater();
    decompressor.setInput(a);
    val bos = new ByteArrayOutputStream(a.length);
    val buf= new Array[Byte](a.length);
    while(!decompressor.finished()) {
      val c= decompressor.inflate(buf);
      bos.write(buf, 0, c);
    }
    bos.close();
    bos.toByteArray();
  }
  
}
