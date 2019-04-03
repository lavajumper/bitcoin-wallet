LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := native-scrypt
LOCAL_SRC_FILES := crypto_scrypt-nosse.c scrypt_jni.c sha256.c
include $(BUILD_SHARED_LIBRARY)
APP_ABI := all
