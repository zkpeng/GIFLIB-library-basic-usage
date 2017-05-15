//
// Created by Administrator on 2017/4/25.
//

#include <jni.h>
#include <malloc.h>
#include "gif_lib.h"
#include<string>
#include <android/log.h>
using namespace std;

extern "C"
{
jboolean JNIEXPORT Java_com_example_zkp_gifdemo_MyClass_LoadGIF(JNIEnv *jni, jobject self,
                                                                jstring jfile,
                                                                jboolean bHighColor) {
    const char* File = jni->GetStringUTFChars(jfile, 0);
    string s(File,15);
    int *ErrorCode;
    //根据GIF名称打开GIF文件，还有两种打开GIF文件的方式，请查看相关文档(http://giflib.sourceforge.net/gif_lib.html#idm46571690335920)
    //返回一个GifFileType类型变量，在以后的函数调用中基本上都需要传入这个变量；
    GifFileType *g = DGifOpenFileName(File, ErrorCode);
    jni->ReleaseStringUTFChars(jfile, File);
    if (!g)
        return JNI_FALSE;
    //将GIF文件的其他信息读入到GifFileType类型变量中，
    //该函数执行成功后，GIF文件的所有图像和扩展块信息都可以通过GifFileType的SavedImages成员进行访问了；
    if (DGifSlurp(g) != GIF_OK) {
        DGifCloseFile(g, ErrorCode);
        return JNI_FALSE;
    }

    jclass cl = jni->GetObjectClass(self);

    jni->CallVoidMethod(self, jni->GetMethodID(cl, "StartImage", "(I)V"), (jint) g->ImageCount);

    jmethodID afmid = jni->GetMethodID(cl, "AddFrame", "(IIIIII[B)V");
    if (!afmid) {
        DGifCloseFile(g, ErrorCode);
        return JNI_FALSE;
    }

    int i, Frame;
    unsigned int Colors[256];
    //TODO: don't rebuild colors if there's a global color table only
    for (Frame = 0; Frame < g->ImageCount; Frame++) {
        SavedImage &si = g->SavedImages[Frame];

        GraphicsControlBlock graphicsControlBlock;
        if (DGifSavedExtensionToGCB(g,Frame,&graphicsControlBlock) == GIF_OK) {
        //每个image加个10*0.01sec的延迟，GraphicsControlBlock还可以控制image的基本属性，请参照文档
            graphicsControlBlock.DelayTime = 10;
            EGifGCBToSavedExtension(&graphicsControlBlock,g,Frame);
        }
        //给每个image添加文字，除此之外还可以添加Box，Rectangle和BoxedText；
        const char* text = "hello world";
        GifDrawText8x8(&si,0,0,text,20);

        int Delay = 0;
        int TransColor = -1;
        jint Disp = 0;
        for (i = 0; i < si.ExtensionBlockCount; i++) {
            ExtensionBlock &eb = si.ExtensionBlocks[i];
            int byteCount = eb.ByteCount;
            //__android_log_print(ANDROID_LOG_INFO, "JNITag1","string From Java To C : %i", byteCount);
            if (eb.Function == 0xf9) //Animation!graphics control (GIF89)
            {
                char c = eb.Bytes[0];
                Delay = (int) (unsigned char) (eb.Bytes[1]) |
                        ((int) (unsigned char) (eb.Bytes[2]) << 8);
                        __android_log_print(ANDROID_LOG_INFO, "JNITag2","string From Java To C : %i", Delay);
                if (c & 1)
                    TransColor = (unsigned char) (eb.Bytes[3]);
                Disp = (c >> 2) & 7;
            }
        }

        //Now format the bits...
        GifImageDesc &id = si.ImageDesc;
        //id.Left = 10;
        //id.Width = id.Width / 3;
        ColorMapObject *cm = id.ColorMap ? id.ColorMap : g->SColorMap;
        int w = id.Width, h = id.Height;
        int wh = w * h;
        int Size = wh * (bHighColor ? 4
                                    : 2); //Size in bytes - in RGBA-4444 format, 2 bytes per pixel, in RGBA-8888, 4
        unsigned char *Buf = (unsigned char *) malloc(Size);
        if (!Buf) {
            DGifCloseFile(g, ErrorCode);
            return JNI_FALSE;
        }
        //把image的所有颜色信息存到Colors整型数组中
        //Translate the color map into RGBA-4444 or RGBA-8888 format, take alpha into account. 256 colors tops, static is OK
        for (i = 0; i < cm->ColorCount; i++) {
            GifColorType &c = cm->Colors[i];
            Colors[i] =
                    bHighColor ?
                    ((unsigned int) c.Red << 16) |
                    ((unsigned int) c.Green << 8) |
                    ((unsigned int) c.Blue) |
                    0xff000000
                               :
                    ((unsigned short) (c.Red & 0xf0) << 8) |
                    ((unsigned short) (c.Green & 0xf0) << 4) |
                    c.Blue | 0xf;
        }
        if (TransColor != -1)
            Colors[TransColor] = 0;

        //Convert pixel by pixel...
        //把Colors中的数据存入Buf数组中
        unsigned char *pSrc = si.RasterBits;
        if (bHighColor) {
            unsigned int *pDest = (unsigned int *) Buf;
            for (i = 0; i < wh; i++)
                *pDest++ = Colors[*pSrc++];
        }
        else {
            unsigned short *pDest = (unsigned short *) Buf;
            for (i = 0; i < wh; i++)
                *pDest++ = (unsigned short) (Colors[*pSrc++]);
        }

        jbyteArray ja = jni->NewByteArray(Size);
        if (!ja) {
            free(Buf);
            DGifCloseFile(g, ErrorCode);
            return JNI_FALSE;
        }
        jni->SetByteArrayRegion(ja, 0, Size, (jbyte *) Buf);
        free(Buf);

        jni->CallVoidMethod(self, afmid,
                            (jint) Delay * 10,
                            (jint) id.Left, (jint) id.Top,
                            (jint) w, (jint) h, Disp, ja);
        jni->DeleteLocalRef(ja);
    }
    DGifCloseFile(g, ErrorCode);
    return JNI_TRUE;
}
}