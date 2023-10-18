#include "callbacks.h"
#include "library.h"

void update_counters(TraversalState* state, jlong class_tag, jlong* tag_ptr, jlong size)
{
    state->found_objects += 1;
    state->found_size += size;

    if (class_tag == HASHMAP_CLASS_TAG) {
        state->found_HashMaps++;
    }
    else if (class_tag == CHASHMAP_CLASS_TAG) {
        state->found_CHashMaps++;
    }
    else if (class_tag == STRING_CLASS_TAG) {
        state->found_Strings++;
    }
    else if (class_tag == BOOLEAN_CLASS_TAG) {
        state->found_Booleans++;
    }
    else if (class_tag == BYTE_CLASS_TAG) {
        state->found_Bytes++;
    }
    else if (class_tag == CHAR_CLASS_TAG) {
        state->found_Characters++;
    }
    else if (class_tag == FLOAT_CLASS_TAG) {
        state->found_Floats++;
    }
    else if (class_tag == INT_CLASS_TAG) {
        state->found_Integers++;
    }
    else if (class_tag == LONG_CLASS_TAG) {
        state->found_Longs++;
    }
    else if (class_tag == SHORT_CLASS_TAG) {
        state->found_Shorts++;
    }
    else if (class_tag == DOUBLE_CLASS_TAG) {
        state->found_Doubles++;
    }
}

jvmtiIterationControl JNICALL heapRootCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        void* user_data)
{
    TraversalState* state = (TraversalState*) user_data;

    if (*tag_ptr == 0) {
        state->found_roots += 1;
        update_counters(state, class_tag, tag_ptr, size);
    }

    return JVMTI_ITERATION_CONTINUE;
}

jvmtiIterationControl JNICALL stackReferenceCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong thread_tag,
        jint depth,
        jmethodID method,
        jint slot,
        void* user_data)
{
    TraversalState* state = (TraversalState*) user_data;

    // Ignore JNI locals for now...
    if (root_kind == JVMTI_HEAP_ROOT_JNI_LOCAL || root_kind == JVMTI_HEAP_ROOT_JNI_GLOBAL) {
        return JVMTI_ITERATION_CONTINUE;
    }

    if (*tag_ptr == 0) {
        state->found_stacks += 1;
        update_counters(state, class_tag, tag_ptr, size);
    }

    return JVMTI_ITERATION_CONTINUE;
}

jvmtiIterationControl JNICALL objectReferenceCallback(
        jvmtiObjectReferenceKind reference_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong referrer_tag,
        jint referrer_index,
        void* user_data)
{
    TraversalState* state = (TraversalState*) user_data;

    if (reference_kind != JVMTI_REFERENCE_ARRAY_ELEMENT && reference_kind != JVMTI_REFERENCE_FIELD && reference_kind != JVMTI_REFERENCE_STATIC_FIELD) {
        return JVMTI_ITERATION_CONTINUE;
    }

    // install tag
    if (*tag_ptr == 0) {
        update_counters(state, class_tag, tag_ptr, size);
    }

    return JVMTI_ITERATION_CONTINUE;
}

jvmtiIterationControl JNICALL cleanHeapRootCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        void* user_data)
{
    *tag_ptr = 0;
    return JVMTI_ITERATION_CONTINUE;
}

jvmtiIterationControl JNICALL cleanStackReferenceCallback(
        jvmtiHeapRootKind root_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong thread_tag,
        jint depth,
        jmethodID method,
        jint slot,
        void* user_data)
{
    *tag_ptr = 0;
    return JVMTI_ITERATION_CONTINUE;
}

jvmtiIterationControl JNICALL cleanObjectReferenceCallback(
        jvmtiObjectReferenceKind reference_kind,
        jlong class_tag,
        jlong size,
        jlong* tag_ptr,
        jlong referrer_tag,
        jint referrer_index,
        void* user_data)
{
    *tag_ptr = 0;
    return JVMTI_ITERATION_CONTINUE;
}
