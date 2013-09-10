package org.achartengine.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Represents a serializable bitmap
 * @source http://stackoverflow.com/questions/6002800/android-serializable-problem
 */
public class SerializableBitmap implements Serializable {

  private Bitmap mBitmap;

  public SerializableBitmap(Context context, int resource) {
    mBitmap = BitmapFactory.decodeResource(context.getResources(), resource);
  }
  public SerializableBitmap(Context context, int resource, int width, int height) {
    mBitmap = BitmapFactory.decodeResource(context.getResources(), resource);
    mBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true);
  }

  // Converts the Bitmap into a byte array for serialization
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    mBitmap.compress(Bitmap.CompressFormat.PNG, 0, byteStream);
    byte bitmapBytes[] = byteStream.toByteArray();
    out.write(bitmapBytes, 0, bitmapBytes.length);
  }

  // Deserializes a byte array representing the Bitmap and decodes it
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    int b;
    while((b = in.read()) != -1)
      byteStream.write(b);
    byte bitmapBytes[] = byteStream.toByteArray();
    mBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
  }
  
  public Bitmap getBitmap(){
    return mBitmap;
  }
}