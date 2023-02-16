#include <cam_narzt_getargv_Getargv.h>
#include <errno.h>
#include <libgetargv.h>
#include <stdlib.h>
#include <string.h>

/*
Throwing can itself fail in many annoying ways so I extracted the logic to
here and fatal if the madness gets too bad
 */
void throw(JNIEnv * env, char *fqn, const char *msg) {
  jclass ExceptionClass = (*env)->FindClass(env, fqn);
  if (ExceptionClass == NULL && (*env)->ExceptionCheck(env) == JNI_FALSE) {
    // error while looking up error type
    jclass TypeNotPresentException =
        (*env)->FindClass(env, "java/lang/TypeNotPresentException");
    if (TypeNotPresentException == NULL &&
        (*env)->ExceptionCheck(env) == JNI_FALSE) {
      (*env)->FatalError(env,
                         "Error while looking up exception class type to throw.");
    } else if ((*env)->ThrowNew(env, TypeNotPresentException,
                                "java/lang/TypeNotPresentException") < 0) {
      (*env)->FatalError(env,
                         "Error while throwing TypeNotPresentException that "
                         "occured while attempting to throw exception.");
    }
  } else if ((*env)->ThrowNew(env, ExceptionClass, msg) < 0) {
    (*env)->FatalError(env, "Error while throwing Exception.");
  }
}

JNIEXPORT jobjectArray JNICALL
Java_cam_narzt_getargv_Getargv_get_1argv_1and_1argc_1of_1pid(JNIEnv *env,
                                                             jobject this,
                                                             jlong pid) {
  jclass ByteArray = (*env)->FindClass(env, "[B");
  if (ByteArray == NULL) {
    throw(env, "java/lang/RuntimeException", "Failed to create byte[].");
    return NULL;
  }

  struct ArgvArgcResult result;
  if (!get_argv_and_argc_of_pid(pid, &result)) {
    errno_t err = errno;
    throw(env, "java/io/IOException", strerror(err));
    return NULL;
  }

  jobjectArray array =
      (*env)->NewObjectArray(env, result.argc, ByteArray, NULL);
  if (array == NULL) {
    free(result.argv);
    free(result.buffer);
    throw(env, "java/lang/RuntimeException", "Failed to create byte[][].");
    return NULL;
  }
  for (size_t i = 0; i < result.argc; i++) {
    size_t len =
        strlen(result.argv[i]); // number of chars preceeding trailing NUL, so
                                // might need to add 1 for trailing NUL...
    jbyteArray b = (*env)->NewByteArray(env, len);
    if (b == NULL) {
      free(result.argv);
      free(result.buffer);
      throw(env, "java/lang/RuntimeException", "Failed to create byte[].");
      return NULL;
    }
    // len must be less than array length `GetArrayLength()`, might need to add
    // 1 to array length above...
    (*env)->SetByteArrayRegion(env, b, 0, len, result.argv[i]);
    (*env)->SetObjectArrayElement(env, array, i, b);
  }
  free_ArgvArgcResult(&result);

  return array;
}

JNIEXPORT jbyteArray JNICALL Java_cam_narzt_getargv_Getargv_get_1argv_1of_1pid(
    JNIEnv *env, jobject this, jlong pid, jlong skip, jboolean nuls) {

  struct ArgvResult result;
  struct GetArgvOptions options = {.pid = pid, .skip = skip, .nuls = nuls};

  if (!get_argv_of_pid(&options, &result)) {
    throw(env, "java/io/IOException", strerror(errno));
    return NULL;
  } else {
    size_t len = result.start_pointer == result.end_pointer
                     ? 0
                     : result.end_pointer - result.start_pointer + 1;
    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes == NULL && (*env)->ExceptionCheck(env) == JNI_FALSE) {
      throw(env, "java/lang/RuntimeException", "Failed to create byte[].");
    } else {
      (*env)->SetByteArrayRegion(env, bytes, 0, len, result.start_pointer);
    }

    free_ArgvResult(&result);

    return bytes;
  }
}
