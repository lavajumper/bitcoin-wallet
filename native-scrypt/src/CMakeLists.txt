cmake_minimum_required(VERSION 3.4.1)

add_library(native_scrypt SHARED
            scrypt_jni.c
            crypto_scrypt-nosse.c
            sha256.c)
install(TARGETS native_scrypt
         LIBRARY DESTINATION "../wallet/libs/")
