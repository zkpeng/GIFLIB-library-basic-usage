LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := giflib
LOCAL_SRC_FILES := MyClass.cpp\
dgif_lib.c\
egif_lib.c\
gif_hash.c\
gif_font.c\
gif_err.c\
gifalloc.c\
native-lib.cpp\
openbsd-reallocarray.c\
LOCAL_LDLIBS := -llog
LOCAL_LDLIBS+= -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)