/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.api;
/*
 * This is very simple interface that is specialised to have only the minimal set of crypto
 * operation that are needed for OpenVPN to authenticate with an external certificate
 */
public interface ExternalCertificateProvider extends android.os.IInterface
{
  /** Default implementation for ExternalCertificateProvider. */
  public static class Default implements de.blinkt.openvpn.api.ExternalCertificateProvider
  {
    /**
         * @deprecated use {@link #getSignedDataWithExtra} instead
         * Requests signing the data with RSA/ECB/PKCS1PADDING
         * for RSA certficate and with NONEwithECDSA for EC certificates
         * @param alias user certificate identifier
         */
    @Override public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException
    {
      return null;
    }
    /**
         * Requests the certificate chain for the selected alias
         * The first certifcate returned is assumed to be
         * the user certificate
         */
    @Override public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    /**
         * This function is called for the app to get additional meta information from the
         * external provider and will be called with the stored alias in the app
         *
         * For external app provider that do not provide an activity to configure them, this
         * is used to get the alias that should be used.
         * The format is the same as the activity should return, i.e.
         *
         * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
         * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
         *
         * as the keys for the bundle.
         *
         */
    @Override public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    /**
         * Requests signing the data with RSA/ECB/nopadding, RSA/ECB/PKCS1PADDING or PKCS1PSSPADDING
         * for RSA certficate and with NONEwithECDSA for EC certificates
         * @param alias user certificate identifier
         * @param data the data to be signed
         * @param extra additional information.
         * Should contain the following keys:
         * <ul>
           * <li>int key "de.blinkt.openvpn.api.RSA_PADDING_TYPE", may be set as:
           * <ul>
             * <li>0 - for RSA/ECB/nopadding
             * <li>1 - for RSA/ECB/PKCS1PADDING
             * <li>2 - for PKCS1PSSPADDING
           * </ul>
           * <li>string key "de.blinkt.openvpn.api.SALTLEN", may be set as:
           * <ul>
             * <li>"digest" - use the same salt size as the hash to sign
             * <li>"max" - use maximum possible saltlen which is '(nbits-1)/8 - hlen - 2'. Here
               * 'nbits' is the number of bits in the key modulus and 'hlen' is the size in octets of
               * the hash. See: RFC 8017 sec 8.1.1 and 9.1.1.
           * </ul>
           * <li>boolean key "de.blinkt.openvpn.api.NEEDS_DIGEST", indicating that the data should be
             * hashed before signing or not
           * <li>string key "de.blinkt.openvpn.api.DIGEST", the short common digest algorithm name to
             * use (such as SHA256, SHA224, etc.)
         * </ul>
         */
    @Override public byte[] getSignedDataWithExtra(java.lang.String alias, byte[] data, android.os.Bundle extra) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.api.ExternalCertificateProvider
  {
    private static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.api.ExternalCertificateProvider";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.api.ExternalCertificateProvider interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.api.ExternalCertificateProvider asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.api.ExternalCertificateProvider))) {
        return ((de.blinkt.openvpn.api.ExternalCertificateProvider)iin);
      }
      return new de.blinkt.openvpn.api.ExternalCertificateProvider.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_getSignedData:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          byte[] _arg1;
          _arg1 = data.createByteArray();
          byte[] _result = this.getSignedData(_arg0, _arg1);
          reply.writeNoException();
          reply.writeByteArray(_result);
          return true;
        }
        case TRANSACTION_getCertificateChain:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          byte[] _result = this.getCertificateChain(_arg0);
          reply.writeNoException();
          reply.writeByteArray(_result);
          return true;
        }
        case TRANSACTION_getCertificateMetaData:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.Bundle _result = this.getCertificateMetaData(_arg0);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_getSignedDataWithExtra:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          byte[] _arg1;
          _arg1 = data.createByteArray();
          android.os.Bundle _arg2;
          if ((0!=data.readInt())) {
            _arg2 = android.os.Bundle.CREATOR.createFromParcel(data);
          }
          else {
            _arg2 = null;
          }
          byte[] _result = this.getSignedDataWithExtra(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeByteArray(_result);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements de.blinkt.openvpn.api.ExternalCertificateProvider
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * @deprecated use {@link #getSignedDataWithExtra} instead
           * Requests signing the data with RSA/ECB/PKCS1PADDING
           * for RSA certficate and with NONEwithECDSA for EC certificates
           * @param alias user certificate identifier
           */
      @Override public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          _data.writeByteArray(data);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSignedData, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getSignedData(alias, data);
          }
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
           * Requests the certificate chain for the selected alias
           * The first certifcate returned is assumed to be
           * the user certificate
           */
      @Override public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCertificateChain, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getCertificateChain(alias);
          }
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
           * This function is called for the app to get additional meta information from the
           * external provider and will be called with the stored alias in the app
           *
           * For external app provider that do not provide an activity to configure them, this
           * is used to get the alias that should be used.
           * The format is the same as the activity should return, i.e.
           *
           * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
           * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
           *
           * as the keys for the bundle.
           *
           */
      @Override public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCertificateMetaData, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getCertificateMetaData(alias);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.os.Bundle.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
           * Requests signing the data with RSA/ECB/nopadding, RSA/ECB/PKCS1PADDING or PKCS1PSSPADDING
           * for RSA certficate and with NONEwithECDSA for EC certificates
           * @param alias user certificate identifier
           * @param data the data to be signed
           * @param extra additional information.
           * Should contain the following keys:
           * <ul>
             * <li>int key "de.blinkt.openvpn.api.RSA_PADDING_TYPE", may be set as:
             * <ul>
               * <li>0 - for RSA/ECB/nopadding
               * <li>1 - for RSA/ECB/PKCS1PADDING
               * <li>2 - for PKCS1PSSPADDING
             * </ul>
             * <li>string key "de.blinkt.openvpn.api.SALTLEN", may be set as:
             * <ul>
               * <li>"digest" - use the same salt size as the hash to sign
               * <li>"max" - use maximum possible saltlen which is '(nbits-1)/8 - hlen - 2'. Here
                 * 'nbits' is the number of bits in the key modulus and 'hlen' is the size in octets of
                 * the hash. See: RFC 8017 sec 8.1.1 and 9.1.1.
             * </ul>
             * <li>boolean key "de.blinkt.openvpn.api.NEEDS_DIGEST", indicating that the data should be
               * hashed before signing or not
             * <li>string key "de.blinkt.openvpn.api.DIGEST", the short common digest algorithm name to
               * use (such as SHA256, SHA224, etc.)
           * </ul>
           */
      @Override public byte[] getSignedDataWithExtra(java.lang.String alias, byte[] data, android.os.Bundle extra) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          _data.writeByteArray(data);
          if ((extra!=null)) {
            _data.writeInt(1);
            extra.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSignedDataWithExtra, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getSignedDataWithExtra(alias, data, extra);
          }
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static de.blinkt.openvpn.api.ExternalCertificateProvider sDefaultImpl;
    }
    static final int TRANSACTION_getSignedData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getCertificateChain = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getCertificateMetaData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getSignedDataWithExtra = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    public static boolean setDefaultImpl(de.blinkt.openvpn.api.ExternalCertificateProvider impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Stub.Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static de.blinkt.openvpn.api.ExternalCertificateProvider getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * @deprecated use {@link #getSignedDataWithExtra} instead
       * Requests signing the data with RSA/ECB/PKCS1PADDING
       * for RSA certficate and with NONEwithECDSA for EC certificates
       * @param alias user certificate identifier
       */
  public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException;
  /**
       * Requests the certificate chain for the selected alias
       * The first certifcate returned is assumed to be
       * the user certificate
       */
  public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException;
  /**
       * This function is called for the app to get additional meta information from the
       * external provider and will be called with the stored alias in the app
       *
       * For external app provider that do not provide an activity to configure them, this
       * is used to get the alias that should be used.
       * The format is the same as the activity should return, i.e.
       *
       * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
       * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
       *
       * as the keys for the bundle.
       *
       */
  public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException;
  /**
       * Requests signing the data with RSA/ECB/nopadding, RSA/ECB/PKCS1PADDING or PKCS1PSSPADDING
       * for RSA certficate and with NONEwithECDSA for EC certificates
       * @param alias user certificate identifier
       * @param data the data to be signed
       * @param extra additional information.
       * Should contain the following keys:
       * <ul>
         * <li>int key "de.blinkt.openvpn.api.RSA_PADDING_TYPE", may be set as:
         * <ul>
           * <li>0 - for RSA/ECB/nopadding
           * <li>1 - for RSA/ECB/PKCS1PADDING
           * <li>2 - for PKCS1PSSPADDING
         * </ul>
         * <li>string key "de.blinkt.openvpn.api.SALTLEN", may be set as:
         * <ul>
           * <li>"digest" - use the same salt size as the hash to sign
           * <li>"max" - use maximum possible saltlen which is '(nbits-1)/8 - hlen - 2'. Here
             * 'nbits' is the number of bits in the key modulus and 'hlen' is the size in octets of
             * the hash. See: RFC 8017 sec 8.1.1 and 9.1.1.
         * </ul>
         * <li>boolean key "de.blinkt.openvpn.api.NEEDS_DIGEST", indicating that the data should be
           * hashed before signing or not
         * <li>string key "de.blinkt.openvpn.api.DIGEST", the short common digest algorithm name to
           * use (such as SHA256, SHA224, etc.)
       * </ul>
       */
  public byte[] getSignedDataWithExtra(java.lang.String alias, byte[] data, android.os.Bundle extra) throws android.os.RemoteException;
}
