/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ur_rna.RNAstructureUI.drawing.proxy;

public class StructureBackendProxyJNI {
  public final static native long new_StructureBackend();
  public final static native boolean StructureBackend_addAnnotationProbability(long jarg1, StructureBackend jarg1_, String jarg2);
  public final static native boolean StructureBackend_addAnnotationSHAPE(long jarg1, StructureBackend jarg1_, String jarg2);
  public final static native void StructureBackend_flip(long jarg1, StructureBackend jarg1_);
  public final static native String StructureBackend_getStructureData(long jarg1, StructureBackend jarg1_, int jarg2);
  public final static native boolean StructureBackend_readStructureData(long jarg1, StructureBackend jarg1_, String jarg2);
  public final static native void StructureBackend_removeAnnotation(long jarg1, StructureBackend jarg1_);
  public final static native void StructureBackend_setNucleotidesCircled(long jarg1, StructureBackend jarg1_, boolean jarg2);
  public final static native String StructureBackend_writeDotBracketFile(long jarg1, StructureBackend jarg1_, String jarg2, String jarg3);
  public final static native String StructureBackend_writeHelixFile(long jarg1, StructureBackend jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void StructureBackend_writePostscriptFile(long jarg1, StructureBackend jarg1_, String jarg2, int jarg3);
  public final static native void StructureBackend_writeSVGFile(long jarg1, StructureBackend jarg1_, String jarg2, int jarg3);
  public final static native void delete_StructureBackend(long jarg1);
}