#include <jvmti.h>
#include <cstring>
#include <pthread.h>
#include <unistd.h>
#include <sys/time.h>
#include <fstream>

#include "library.h"
#include "callbacks.h"

#define SUCCESS 0
#define FAILURE 1

// cached pointer to the JVM structure
static JavaVM* vm;

// cached struct of the profiler thread
static pthread_t thread;

static volatile bool should_finish = false;
static volatile bool finished = false;
static volatile bool sleeping = false;

static int total_iterations = 0;

static std::ofstream out;

int setup_jni_jvmti(JNIEnv** jni, jvmtiEnv** jvmti)
{
    jint jni_result;
    jvmtiError jvmti_result;
    jvmtiCapabilities capabilities;

    // this is required to allow the JVM to initialize properly
    sleep(10);

    // attach thread to JVM (required to access JNI/JVMTI)
    jni_result = vm->AttachCurrentThread((void **) jni, nullptr);
    if (jni_result != JNI_OK) {
        out << "[setup_jni_jvmti] ERROR: failed to attach profiler thread to jvm" << std::endl;
        return FAILURE;
    }

    // get jvmti
    jni_result = vm->GetEnv((void **) jvmti, JVMTI_VERSION_1_1);
    if (jni_result != JNI_OK) {
        out << "[setup_jni_jvmti] ERROR: unable to access JVMTI\n " << jni_result << std::endl;
        return FAILURE;
    }

    // request object tagging feature
    (void)memset(&capabilities, 0, sizeof(jvmtiCapabilities));
    capabilities.can_tag_objects = 1;
    jvmti_result = (*jvmti)->AddCapabilities(&capabilities);
    if (jvmti_result != JVMTI_ERROR_NONE) {
        out << "[setup_jni_jvmti] ERROR: JVMTI does not support object tagging\n" << std::endl;
        return FAILURE;
    }

    return SUCCESS;
}

int tag_classes(jvmtiEnv* jvmti, TraversalState* state)
{
    jvmtiError jvmti_result;
    jint found_count;
    jclass* found_classes;
    struct timeval st, ft;

    gettimeofday(&st, nullptr);
    jvmti_result = jvmti->GetLoadedClasses(&found_count, &found_classes);
    if (jvmti_result != JVMTI_ERROR_NONE) {
        out << "[tag_classes] ERROR: failed to get loaded classes\n" << std::endl;
        return FAILURE;
    }

    for (int i = 0; i < found_count; i++) {
        char* signature_charptr;
        char* generic_charptr;

        jvmti_result = jvmti->GetClassSignature(found_classes[i], &signature_charptr, &generic_charptr);
        if (jvmti_result != JVMTI_ERROR_NONE) {
            out << "[tag_classes] ERROR: to get class signature\n" << std::endl;
            return FAILURE;
        }

        if(strcmp(signature_charptr, HASHMAP_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], HASHMAP_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, CHASHMAP_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], CHASHMAP_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, STRING_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], STRING_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, STRING_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], STRING_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, BOOLEAN_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], BOOLEAN_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, BYTE_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], BYTE_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, CHAR_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], CHAR_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, FLOAT_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], FLOAT_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, INT_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], INT_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, LONG_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], LONG_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, SHORT_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], SHORT_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else if(strcmp(signature_charptr, DOUBLE_SIGNATURE) == 0) {
            jvmti_result = jvmti->SetTag(found_classes[i], DOUBLE_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }
        else {
            jvmti_result = jvmti->SetTag(found_classes[i], UNTRACKED_CLASS_TAG);
            if (jvmti_result != JVMTI_ERROR_NONE) {
                out << "[tag_classes] ERROR: failed to tag class\n" << std::endl;
                return FAILURE;
            }
        }

        jvmti->Deallocate((unsigned char*) signature_charptr);
        jvmti->Deallocate((unsigned char*) generic_charptr);
    }
    gettimeofday(&ft, nullptr);

    out << "[tag_classes] found ";
    out << found_count << " classes; ";
    out << "took " << ((ft.tv_sec - st.tv_sec) * 1000000) + (ft.tv_usec - st.tv_usec) << " us" << std::endl;
    return SUCCESS;
}

int tag_objects(jvmtiEnv* jvmti, TraversalState* state)
{
    jvmtiError jvmti_result;
    struct timeval st, ft;

    gettimeofday(&st,nullptr);
    jvmti_result = jvmti->IterateOverReachableObjects(heapRootCallback, stackReferenceCallback, objectReferenceCallback, state);
    gettimeofday(&ft,nullptr);

    if (jvmti_result != JVMTI_ERROR_NONE) {
        out << "[tag_objects] ERROR: JVMTI IterateOverReachableObjects failed (jvmti_result " << jvmti_result << ")" << std::endl;
        return FAILURE;
    }

    out << "[tag_objects] found ";
    out << state->found_roots << " roots ";
    out << state->found_stacks << " stacks ";
    out << state->found_objects << " objects ";
    out << state->found_size << " bytes ";
    out << state->found_HashMaps << " HashMaps ";
    out << state->found_CHashMaps << " ConcurrentHashMaps ";
    out << state->found_Strings << " Strings ";
    out << state->found_Booleans << " Booleans ";
    out << state->found_Bytes << " Bytes ";
    out << state->found_Characters << " Characters ";
    out << state->found_Floats << " Floats ";
    out << state->found_Integers << " Integers ";
    out << state->found_Longs << " Longs ";
    out << state->found_Shorts << " Shorts ";
    out << state->found_Doubles << " Doubles ";
    out << "took " << ((ft.tv_sec - st.tv_sec) * 1000000) + (ft.tv_usec - st.tv_usec) << " us" << std::endl;

    return SUCCESS;
}


int clean_tags(jvmtiEnv* jvmti)
{
    jvmtiError jvmti_result;
    struct timeval st, ft;

    gettimeofday(&st, nullptr);
    jvmti_result = jvmti->IterateOverReachableObjects(cleanHeapRootCallback, cleanStackReferenceCallback, cleanObjectReferenceCallback, nullptr);
    gettimeofday(&ft,nullptr);

    if (jvmti_result != JVMTI_ERROR_NONE) {
        out << "[clean_tags] ERROR: JVMTI IterateOverReachableObjects failed (jvmti_result " << jvmti_result << ")" << std::endl;
        return FAILURE;
    }

    out << "[clean_tags] took " << ((ft.tv_sec - st.tv_sec) * 1000000) + (ft.tv_usec - st.tv_usec) << " us" << std::endl;
    return SUCCESS;
}


void* object_profiler_loop(void* ptr)
{
    JNIEnv* env = nullptr;
    jvmtiEnv* jvmti = nullptr;

    if (setup_jni_jvmti(&env, &jvmti) != SUCCESS) {
        out << "[object_profiler_loop] ERROR: failed to setup jni and/or jvmti" << std::endl;
        goto end;
    }

    while(!should_finish) {
        TraversalState state = {
                .found_roots = 0,
                .found_stacks = 0,
                .found_objects = 0,
                .found_size = 0,
                .found_HashMaps = 0,
                .found_CHashMaps = 0,
                .found_Strings = 0,
                .found_Booleans = 0,
                .found_Bytes = 0,
                .found_Characters = 0,
                .found_Floats = 0,
                .found_Integers = 0,
                .found_Longs = 0,
                .found_Shorts = 0,
                .found_Doubles=0 };
        struct timeval st, ft;

        out << "[object_profiler_loop] starting iteration " << total_iterations + 1 << std::endl;

        gettimeofday(&st,nullptr);

        if (tag_classes(jvmti, &state) != SUCCESS) {
            out << "[object_profiler_loop] ERROR: failed to tag classes" << std::endl;
            break;
        }

        if (tag_objects(jvmti, &state) != SUCCESS) {
            out << "[object_profiler_loop] ERROR: failed to tag objects" << std::endl;
            break;
        }

        if (clean_tags(jvmti) != SUCCESS) {
            out <<  "[object_profiler_loop] ERROR: failed to clean nonstring tags" << std::endl;
            break;
        }

        gettimeofday(&ft,nullptr);

        // update counters
        total_iterations++;
        out << "[object_profiler_loop] finished iteration " << total_iterations << "; took " << ((ft.tv_sec - st.tv_sec) * 1000000) + (ft.tv_usec - st.tv_usec) << " us" << std::endl;

        sleeping = true;
        sleep(SAMPLING_INTERVAL);
        sleeping = false;
    }
    end:
    finished = true;
    return nullptr;
}

extern "C"
JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved)
{
    vm = jvm;
    out = std::ofstream ("profile_output.txt");

    int c_result = pthread_create(&thread, nullptr, object_profiler_loop, nullptr);
    if (c_result != 0) {
        out << "ERROR: could not create profiler thread" << std::endl;
    }

    return JNI_OK;
}

extern "C"
JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *jvm)
{
    should_finish = true;
    while (!sleeping && !finished) {
        sleep(1);
    }

    out << "Profiler iterations = " << total_iterations << std::endl;
    out.close();

    // make sure the thread knows it must die
    //pthread_join(thread, NULL);
}
