#ifndef OBJECT_INLINING_PROFILER_CALLBACKS_H
#define OBJECT_INLINING_PROFILER_CALLBACKS_H

#include <jvmti.h>

// ####################################################################################################################
// ####################################################################################################################
// Callbacks used to produce profiling information
// ####################################################################################################################
// ####################################################################################################################
jvmtiIterationControl JNICALL heapRootCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        void* user_data);

jvmtiIterationControl JNICALL stackReferenceCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong thread_tag,
        jint depth,
        jmethodID method,
        jint slot,
        void* user_data);

jvmtiIterationControl JNICALL objectReferenceCallback(
        jvmtiObjectReferenceKind reference_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong referrer_tag,
        jint referrer_index,
        void* user_data);

// ####################################################################################################################
// ####################################################################################################################
// Callbacks used to clean all tags
// ####################################################################################################################
// ####################################################################################################################
jvmtiIterationControl JNICALL cleanHeapRootCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        void* user_data);

jvmtiIterationControl JNICALL cleanStackReferenceCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong thread_tag,
        jint depth,
        jmethodID method,
        jint slot,
        void* user_data);

jvmtiIterationControl JNICALL cleanObjectReferenceCallback(
        jvmtiObjectReferenceKind reference_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong referrer_tag,
        jint referrer_index,
        void* user_data);

#endif //OBJECT_DEMOGRAPHICS_PROFILER_CALLBACKS_H
