package com.example.chap17_mp3project

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.IOException

@Parcelize
class Music(var id: String, var title: String?, var artist: String?, var albumId: String?, var duration: Int?, var likes : Int? ) :
    Parcelable {
    //playmusicActivity 객체(Mutablelist)를 넘기기 위해 serializable 말고 Parcelable 사용

    //Parceler 를 쓰기 위한
    companion object : Parceler<Music> {

        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeInt(duration!!)
            parcel.writeInt(likes!!)
        }

        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }
    }

    //생성자
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    //컨텐트리졸버를 사용해 앨범 정보 Uri 정보 가져오기 위한 방법 함수
    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/" + albumId)
    }

    //음악 정보를 가져오기 위한 경로 uri 열기
    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //해당되는 음악 이미지를 내가 원하는 사이즈로 비트맵 만들기
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResolver: ContentResolver = context.getContentResolver()
        //앨범 경로
        val uri = getAlbumUri()
        //앨범에 대한 경로 저장
        val options = BitmapFactory.Options()

        if (uri != null) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {

                //외부파일에 있는 이미지 파일을 가져오기 위한 스트림
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                var bitmap = BitmapFactory.decodeFileDescriptor(
                    parcelFileDescriptor!!.fileDescriptor,
                    null,
                    options
                )

                //비트맵 가져와서 사이즈 결정 (원본 이미지 사이즈가 내가 원하는 사이즈와 맞지 않을 경우 원하는 사이즈로 가져옴)
                if (bitmap != null) {
                    if (options.outHeight !== albumImageSize || options.outWidth !== albumImageSize) {
                        val tempBitmap =
                            Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                        bitmap.recycle()
                        bitmap = tempBitmap
                    }
                }
                return bitmap

            } catch (e: Exception) {
                Log.d("Yun", "getAlbumImage() : ${e.toString()}")
            } finally {
                try {
                    parcelFileDescriptor?.close()
                } catch (e: IOException) {
                    Log.d("Yun", "parcelFileDescriptor() : ${e.toString()}")
                }
            }
        }
        return null
    }
}