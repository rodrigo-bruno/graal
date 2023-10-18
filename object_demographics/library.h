#ifndef OBJECT_INLINING_PROFILER_LIBRARY_H
#define OBJECT_INLINING_PROFILER_LIBRARY_H

#include <jvmti.h>
#include <map>
#include <vector>
#include <algorithm>

// Sampling interval in seconds
#define SAMPLING_INTERVAL 10

#define HASHMAP_SIGNATURE "Ljava/util/HashMap;"
#define CHASHMAP_SIGNATURE "Ljava/util/concurrent/ConcurrentHashMap;"
#define STRING_SIGNATURE "Ljava/lang/String;"
#define BOOLEAN_SIGNATURE "Ljava/lang/Boolean;"
#define BYTE_SIGNATURE "Ljava/lang/Byte;"
#define CHAR_SIGNATURE "Ljava/lang/Character;"
#define FLOAT_SIGNATURE "Ljava/lang/Float;"
#define INT_SIGNATURE "Ljava/lang/Integer;"
#define LONG_SIGNATURE "Ljava/lang/Long;"
#define SHORT_SIGNATURE "Ljava/lang/Short;"
#define DOUBLE_SIGNATURE "Ljava/lang/Double;"

#define UNTRACKED_CLASS_TAG 0
#define STRING_CLASS_TAG 1
#define BOOLEAN_CLASS_TAG 2
#define BYTE_CLASS_TAG 3
#define CHAR_CLASS_TAG 4
#define FLOAT_CLASS_TAG 5
#define INT_CLASS_TAG 6
#define LONG_CLASS_TAG 7
#define SHORT_CLASS_TAG 8
#define DOUBLE_CLASS_TAG 9
#define HASHMAP_CLASS_TAG 10
#define CHASHMAP_CLASS_TAG 11

typedef struct TraversalState {
    // counters of found references
    unsigned int found_roots;
    unsigned int found_stacks;
    unsigned int found_objects;
    unsigned long found_size;
    unsigned int found_HashMaps;
    unsigned int found_CHashMaps;
    unsigned int found_Strings;
    unsigned int found_Booleans;
    unsigned int found_Bytes;
    unsigned int found_Characters;
    unsigned int found_Floats;
    unsigned int found_Integers;
    unsigned int found_Longs;
    unsigned int found_Shorts;
    unsigned int found_Doubles;
} TraversalState;

#endif //OBJECT_DEMOGRAFICS_PROFILER_LIBRARY_H
