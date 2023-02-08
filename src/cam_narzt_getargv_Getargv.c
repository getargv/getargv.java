#include <cam_narzt_getargv_Getargv.h>
#include <errno.h>
#include <libgetargv.h>
#include <stdlib.h>
#include <string.h>

void throw(JNIEnv * env, char *fqn, char *msg) {
  jclass IOException = (*env)->FindClass(env, fqn);
  if (IOException == NULL && (*env)->ExceptionCheck(env) == JNI_FALSE) {
    // error while looking up error type
    jclass TypeNotPresentException =
        (*env)->FindClass(env, "Ljava/lang/TypeNotPresentException");
    if (TypeNotPresentException == NULL &&
        (*env)->ExceptionCheck(env) == JNI_FALSE) {
      (*env)->FatalError(env,
                         "Error while looking up IOException type to throw.");
    } else if ((*env)->ThrowNew(env, TypeNotPresentException,
                                "Ljava/lang/TypeNotPresentException") < 0) {
      (*env)->FatalError(env,
                         "Error while throwing TypeNotPresentException that "
                         "occured while attempting to throw IOException.");
    }
  } else if ((*env)->ThrowNew(env, IOException, msg) < 0) {
    (*env)->FatalError(env, "Error while throwing Exception.");
  }
}

JNIEXPORT jobjectArray JNICALL
Java_cam_narzt_getargv_Getargv_get_1argv_1and_1argc_1of_1pid(JNIEnv *env,
                                                             jobject this,
                                                             jlong pid) {
  jclass ByteArray = (*env)->FindClass(env, "[B");
  if (ByteArray == NULL) {
    throw(env, "Ljava/lang/RuntimeException", "Failed to create byte[].");
    return NULL;
  }

  struct ArgvArgcResult result;
  if (!get_argv_and_argc_of_pid(pid, &result)) {
    errno_t err = errno;
    throw(env, "Ljava/io/IOException", strerror(err));
    return NULL;
  }

  jobjectArray array =
      (*env)->NewObjectArray(env, result.argc, ByteArray, NULL);
  if (array == NULL) {
    free(result.argv);
    free(result.buffer);
    throw(env, "Ljava/lang/RuntimeException", "Failed to create byte[][].");
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
      throw(env, "Ljava/lang/RuntimeException", "Failed to create byte[].");
      return NULL;
    }
    // len must be less than array length `GetArrayLength()`, might need to add
    // 1 to array length above...
    (*env)->SetByteArrayRegion(env, b, 0, len, result.argv[i]);
    (*env)->SetObjectArrayElement(env, array, i, b);
  }
  free(result.argv);
  free(result.buffer);

  return array;
}

JNIEXPORT jbyteArray JNICALL Java_cam_narzt_getargv_Getargv_get_1argv_1of_1pid(
    JNIEnv *env, jobject this, jlong pid, jlong skip, jboolean nuls) {

  struct ArgvResult result;
  struct GetArgvOptions options = {.pid = pid, .skip = skip, .nuls = nuls};

  if (!get_argv_of_pid(&options, &result)) {
    throw(env, "Ljava/io/IOException", strerror(errno));
    return NULL;
  } else {
    size_t len = result.start_pointer == result.end_pointer? 0 : result.end_pointer - result.start_pointer + 1;
    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes == NULL && (*env)->ExceptionCheck(env) == JNI_FALSE) {
      throw(env, "Ljava/lang/RuntimeException", "Failed to create byte[].");
    } else {
      (*env)->SetByteArrayRegion(env, bytes, 0, len, result.start_pointer);
    }

    free(result.buffer);

    return bytes;
  }
}
